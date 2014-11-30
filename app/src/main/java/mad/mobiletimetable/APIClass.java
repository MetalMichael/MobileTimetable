package mad.mobiletimetable;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private OnTaskCompleted listener;
    public APIClass(Context c, OnTaskCompleted listener) {
        this.c = c;
        this.listener = listener;
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
                // URL encode values as UTF-8
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

    // The same helper function that make the HTTP call
    public String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
            // convert inputstream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = "Did not work!";
            }

        } catch (Exception e) {
            //Log.i("week06inClass", e.toString());
            result = e.toString();
        }
        return result;
    }

    // The same helper function that convert the response buffer into string
    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    // TODO Use in doInBackground
    // Check local storage for result of request if connection unavailable
    // use it as the result if it is present
    private String fetchFromStorage(HashMap<String,String> requestMap){
        String result = "";
        String mapCode = Integer.toString(requestMap.hashCode());
        File resultFile = new File(c.getFilesDir(), mapCode);
        if(resultFile.exists()){
            FileInputStream fis = null;
            try {
                fis = c.openFileInput(mapCode);
                char current;
                while(fis.available() > 0){
                    current = (char) fis.read();
                    result += String.valueOf(current);
                }
            } catch  (Exception e){
                e.printStackTrace();
            }
        }
        return result;
    }

    // TODO Use in doInBackground
    // Save successful request result to local storage
    private void saveToStorage(HashMap<String,String> requestMap,String result){
        String mapCode = Integer.toString(requestMap.hashCode());
        File resultFile = new File(c.getFilesDir(), mapCode);
        FileOutputStream fos = null;
        try {
            // Save cached result
            fos = c.openFileOutput(mapCode, Context.MODE_PRIVATE);
            fos.write(result.getBytes());
            fos.close();
        } catch (Exception e){
            // We have a permissions problem or the app directory doesn't exist
            e.printStackTrace();
        }
    }

    @Override
    protected JSONObject doInBackground(HashMap<String,String>... params) {
        JSONObject result = null;
        HashMap<String,String> requestMap = params[0];
        // Make sure we have the method and action, throw error if we don't
        if(requestMap.containsKey("method") && requestMap.containsKey("action")){
            // check the network status
            ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                // Connected, make request
                String url = buildURL(requestMap);
                String requestResult = GET(url);
                try {
                    result = new JSONObject(requestResult);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // Not connected, will look at local storage here
                publishProgress(0);
            }
        } else {
            throw new Error("Set method and action for API call");
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Toast.makeText(c.getApplicationContext(),values[0], Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        listener.onTaskCompleted(result);
    }
}

