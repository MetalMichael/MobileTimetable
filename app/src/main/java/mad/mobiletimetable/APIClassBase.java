package mad.mobiletimetable;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by Michael on 07/12/2014.
 */
public class APIClassBase extends AsyncTask<HashMap<String,String>, Integer, JSONObject> {
    protected Context context;
    private OnTaskCompleted listener;

    public APIClassBase(Context context, OnTaskCompleted listener) {
        this.context = context;
        this.listener = listener;
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

    @Override
    protected JSONObject doInBackground(HashMap<String,String>... params) {
        JSONObject result = new JSONObject();
        HashMap<String,String> requestMap = params[0];
        String auth = context.getSharedPreferences("MyAuthFile", 0).getString("Auth","");
        // Make sure we have the method and action, throw error if we don't
        if(requestMap.containsKey("method") && requestMap.containsKey("action")) {
        /*
            // if unauthenticated and a non-user method, handle actions locally
            if(auth.equals("") && !requestMap.get("method").equals("user")) {
                if(requestMap.get("action").equals("add")) {
                    // swap map action to get, ready for saving
                    requestMap.put("action", "get");
                    if (requestMap.get("method").equals("module")) {
                        // format module response string to save
                        formatModuleAdd(requestMap);
                    } else if (requestMap.get("method").equals("timetable")) {
                        // format timetable response string to save
                        formatTimetableAdd(requestMap);
                    }
                }
            }
        */
            // check the network status
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean networked = (networkInfo != null && networkInfo.isConnected());
            Boolean authedOrAuthing = (!auth.equals("")||requestMap.get("method").equals("user"));
            if (networked && authedOrAuthing) {
                // Connected, make request
                // Get auth if authenticated user
                if (!auth.equals("")) {
                    requestMap.put("auth", auth);
                }
                String url = buildURL(requestMap);
                Log.d("APIClassBase", "Making a Request to: " + url);
                String requestResult = GET(url);
                Log.d("APIClassBase", "Response: " + requestResult);
                try {
                    result = new JSONObject(requestResult);
                } catch (JSONException e) {
                    e.printStackTrace();
                    handleError("Invalid API Response");
                    return new JSONObject();
                }

                //Handle errors
                if (!result.has("status")) {
                    Toast.makeText(context, "Invalid API Response", Toast.LENGTH_LONG).show();
                    return new JSONObject();
                }
            } else {
                String localRequest = localHandler(requestMap);
                if(!localRequest.equals("")) {
                    try {
                        result = new JSONObject(localRequest);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        handleError("Invalid API Response");
                        return new JSONObject();
                    }
                }
            }
        } else {
            throw new Error("Set method and action for API call");
        }
        return result;
    }

    protected void handleError(String message) {
        Log.d("APIClassBase", message);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Toast.makeText(context, Integer.toString(values[0]), Toast.LENGTH_SHORT).show();
    }

    protected void handlePermissionError() {

    }

    protected String localHandler(HashMap<String, String> map) {
        return "";
    }


    @Override
    protected void onPostExecute(JSONObject result) {
        String auth = context.getSharedPreferences("MyAuthFile", 0).getString("Auth","");
        if(!auth.equals("")) {
            try {
                String status = result.getString("status");
                Log.d("APIClass", "Status: " + status);
                if (status.equals("error")) {
                    String error = result.getString("error");
                    if (error.equals("")) {
                        error = "An Unknown Error Occurred";
                    }
                    Log.d("APIClass", "Error: " + error);
                    if (error.equals("403")) {
                        handlePermissionError();
                    } else {
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        if(result!=null) {
            Log.d("API Result", result.toString());
            listener.onTaskCompleted(result);
        }
    }
}
