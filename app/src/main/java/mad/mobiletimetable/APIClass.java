package mad.mobiletimetable;

import android.os.AsyncTask;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.lang.StringBuilder;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by M on 28/11/2014.
 */
public class APIClass {
    private static String APIurl = "http://timetable.michaelfiford.me/";
    private class PostTask extends AsyncTask<HashMap<String,String>, Integer, JSONObject> {
        @Override
        protected JSONObject doInBackground(HashMap<String,String>... params) {
            HashMap<String,String> requestMap = params[0];
            // Make sure we have the method and action, throw error if we don't
            if(requestMap.containsKey("method") && requestMap.containsKey("action")){
                StringBuilder urlBuilder = new StringBuilder(2083);
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
                String url = urlBuilder.toString();
            } else {
                throw new Error("Set method and action for API call");
            }
            JSONObject result = null;
            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
        }
    }
}
