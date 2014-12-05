package mad.mobiletimetable;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
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
import android.util.Log;
import android.widget.Toast;

/**
 * Created by M on 28/11/2014.
 */
public class APIClass extends AsyncTask<HashMap<String,String>, Integer, JSONObject> {
    private Context context;
    private Activity activity;
    private OnTaskCompleted listener;
    public APIClass(Activity activity, OnTaskCompleted listener) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
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

    private int getFileID(HashMap<String,String> requestMap) {
        Log.d("API Class","Getting File ID for "+requestMap.toString());
        int id = 1;
        String dirPath = context.getFilesDir()+"/"+requestMap.get("method")+"/"+requestMap.get("action");
        File dir = new File(dirPath);
        File dirFiles[] = dir.listFiles();
        if(dirFiles!=null) {
            Log.d("API Class","We found "+dirFiles.length+" files for "+requestMap.get("method")+"|"+requestMap.get("action"));
            for (int i = 0; i < dirFiles.length; i++) {
                int tempid = Integer.parseInt(dirFiles[i].getName().split("-")[0].trim());
                if (tempid > id) {
                    id = tempid + 1;
                }
            }
        }
        Log.d("API Class","ID for "+requestMap.toString()+" is "+Integer.toString(id));
        return id;
    }

    // Check local storage for result of request if connection unavailable
    // use it as the result if it is present
    private String fetchFromStorage(HashMap<String,String> requestMap){
        Log.d("API Class","Fetch from Storage");
        String result = "";
        String mapCode = Integer.toString(requestMap.hashCode());
        String dirPath = context.getFilesDir()+"/"+requestMap.get("method")+"/"+requestMap.get("action");
        String fileName = getFileID(requestMap)+"-"+mapCode;
        File resultFile = new File(dirPath, fileName);
        if(resultFile.exists()){
            FileInputStream fis = null;
            try {
                fis = context.openFileInput(mapCode);
                char current;
                while(fis.available() > 0){
                    current = (char) fis.read();
                    result += String.valueOf(current);
                }
            } catch  (Exception e){
                e.printStackTrace();
            }
        }
        Log.d("API Class","Fetched from Storage");
        return result;
    }

    // Save successful request result to local storage
    private void saveToStorage(HashMap<String,String> requestMap, String result){
        Log.d("API Class","Saving to Storage");
        String mapCode = Integer.toString(requestMap.hashCode());
        String dirPath = context.getFilesDir()+"/"+requestMap.get("method")+"/"+requestMap.get("action");
        String fileName = getFileID(requestMap)+"-"+mapCode;
        File resultFile = new File(dirPath, fileName);
        FileOutputStream fos = null;
        try {
            // Save cached result
            fos = context.openFileOutput(mapCode, Context.MODE_PRIVATE);
            fos.write(result.getBytes());
            fos.close();
        } catch (Exception e){
            // We have a permissions problem or the app directory doesn't exist
            e.printStackTrace();
        }
        Log.d("API Class","Save Finished");
    }

    private JSONObject appendToArray(String arrayName, JSONObject newElement, HashMap<String,String> requestMap){
        JSONObject response = new JSONObject();
        String previousGetAll = fetchFromStorage(requestMap);
        try {
            JSONArray elements = new JSONArray();
            if(previousGetAll != ""){
                JSONObject previous = new JSONObject(previousGetAll);
                elements = previous.getJSONArray(arrayName);
            }
            elements.put(newElement);
            response.put(arrayName,elements);
            response.put("status","OK");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

    // Format a module add string to a module get string response
    private JSONObject formatModuleGet(HashMap<String,String> requestMap){
        JSONObject response = new JSONObject();
        JSONObject module = new JSONObject();
        try {
            module.put("ID",getFileID(requestMap));
            module.put("ModuleCode",requestMap.get("modulecode"));
            module.put("ModuleTitle",requestMap.get("moduletitle"));
            module.put("Lecturer",requestMap.get("lecturer"));
            response.put("module",module);
            response.put("status","OK");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }
    private JSONObject formatModuleGetAll(HashMap<String,String> requestMap){
        JSONObject response = new JSONObject();
        JSONObject newModule = formatModuleGet(requestMap);
        response = appendToArray("modules",newModule,requestMap);
        return response;
    }

    // Format a timetable add string to a timetable get string response
    private JSONObject formatTimetableGet(HashMap<String,String> requestMap){
        JSONObject response = new JSONObject();
        JSONObject event = new JSONObject();
        try {
            event.put("ID",getFileID(requestMap));
            event.put("ModuleID",requestMap.get("moduleid"));
            event.put("Day",requestMap.get("day"));
            event.put("Time",requestMap.get("time"));
            event.put("Duration",requestMap.get("duration"));
            response.put("event",event);
            response.put("status","OK");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

    private JSONObject formatTimetableGetAll(HashMap<String,String> requestMap){
        JSONObject response = new JSONObject();
        JSONObject newEvent = formatTimetableGet(requestMap);
        response = appendToArray("events",newEvent,requestMap);
        return response;
    }

    private void localHandler(HashMap<String,String> requestMap){
        Log.d("API Class","localHandler Called");
        String method = requestMap.get("method");
        String action = requestMap.get("action");
        if(method.equals("module")||method.equals("timetable")){
            String id = "eventid";
            if(method.equals("module")){
                id = "moduleid";
            }
            if(action.equals("add")){
                Log.d("API Class","Adding a "+method);
                HashMap<String,String> getRequest = new HashMap<String, String>();
                getRequest.put("method",method);
                getRequest.put("action", "getall");
                if(method.equals("module")) {
                    saveToStorage(getRequest, formatModuleGetAll(requestMap).toString());
                } else {
                    saveToStorage(getRequest,formatTimetableGetAll(requestMap).toString());
                }
                getRequest.put("action","get");
                getRequest.put(id, Integer.toString(getFileID(requestMap)));
                if(method.equals("module")) {
                    saveToStorage(getRequest,formatModuleGet(requestMap).toString());
                } else {
                    saveToStorage(getRequest,formatTimetableGet(requestMap).toString());
                }
            } else if(action.equals("get")){
                Log.d("API Class","Getting a "+method);
                Log.d("API Class","Fetched request "+fetchFromStorage(requestMap).toString());
            } else if(action.equals("getall")){
                Log.d("API Class","Getting all "+method+"s");
                Log.d("API Class","Fetched request "+fetchFromStorage(requestMap).toString());
            }
        }
        Log.d("API Class","localHandler Finished");
    }

    @Override
    protected JSONObject doInBackground(HashMap<String,String>... params) {
        JSONObject result = null;
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
                Log.d("APIClass", "Making a Request to: " + url);
                String requestResult = GET(url);
                Log.d("APIClass", "Response: " + requestResult);
                try {
                    result = new JSONObject(requestResult);
                } catch (JSONException e) {
                    //It's probably a bad idea to throw runtime errors for the sake of it
                    //Question is - do we want to show the user an internal (API) error?
                    e.printStackTrace();
                    Toast.makeText(context, "Invalid API Response", Toast.LENGTH_LONG).show();
                    return new JSONObject();
                }

                //Handle errors
                if (!result.has("status")) {
                    Toast.makeText(context, "Invalid API Response", Toast.LENGTH_LONG).show();
                    return new JSONObject();
                }
            } else {
                // Not connected, will look at local storage here
                localHandler(requestMap);
                //publishProgress(0);
            }
        } else {
            throw new Error("Set method and action for API call");
        }
        return result;
    }

    private void showLogin() {
        Intent intent = new Intent(context, ActivityLogin.class);
        activity.finish();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("authentication_error", true);
        context.startActivity(intent);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Toast.makeText(context, Integer.toString(values[0]), Toast.LENGTH_SHORT).show();
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
                        showLogin();
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

