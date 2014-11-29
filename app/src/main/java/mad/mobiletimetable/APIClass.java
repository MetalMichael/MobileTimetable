package mad.mobiletimetable;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.lang.StringBuilder;
import android.content.Context;
import android.widget.Toast;

/**
 * Created by M on 28/11/2014.
 */
public class APIClass extends AsyncTask<HashMap<String,String>, Integer, JSONObject> {
    private Context c;
    public void APICall(Context context) {
        c = context;
    }
    private String buildURL(HashMap<String,String> requestMap){
        StringBuilder urlBuilder = new StringBuilder(2083);
        String APIurl = "http://timetable.michaelfiford.me/";
        //Build request URL
        urlBuilder.append(APIurl);
        urlBuilder.append(requestMap.get("method")).append("/");
        urlBuilder.append(requestMap.get("action")).append("?");
        for(HashMap.Entry<String,String> entry : requestMap.entrySet()){
            String key = entry.getKey();
            // Skip to next iteration if method or action
            if(key=="method"||key=="action"){
                continue;
            } else {
                String value = entry.getValue();
                // URLencode values as UTF-8
                try {
                    value = URLEncoder.encode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError("UTF-8 is unknown");
                }
                urlBuilder.append(key).append("=").append(value).append("&");
            }
        }
        // Chop off last &
        urlBuilder.setLength(urlBuilder.length()-1);
        return urlBuilder.toString();
    }

    @Override
    protected JSONObject doInBackground(HashMap<String,String>... params) {
        HashMap<String,String> requestMap = params[0];
        // Make sure we have the method and action, throw error if we don't
        if(requestMap.containsKey("method") && requestMap.containsKey("action")){
            // check the network status
            ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                // Connected, make request
                String url = buildURL(requestMap);
            } else {
                // Not connected, will look at local storage here
                publishProgress(0);
            }
        } else {
            throw new Error("Set method and action for API call");
        }
        JSONObject result = null;
        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Toast.makeText(c.getApplicationContext(),values[0], Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        super.onPostExecute(result);
    }
}

