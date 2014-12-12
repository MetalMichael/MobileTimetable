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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Michael on 07/12/2014.
 */
public class APIClassBase extends AsyncTask<HashMap<String,String>, Integer, JSONObject> {
    protected Context context;
    private OnTaskCompleted listener;

    // Set context and callback (listener)
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
        while((line = bufferedReader.readLine()) != null) {
            result += line;
        }
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

    // Gets arrayList of requests that are yet to be pushed
    // Deletes the stored requests once they're retrieved
    private ArrayList<HashMap<String,String>> checkStoredRequests(String userAuth){
        String dirPath = context.getFilesDir()+"/"+userAuth+"/localRequests";
        ArrayList<HashMap<String,String>> storedRequests = new ArrayList<HashMap<String,String>>();
        File resultsDir = new File(dirPath);
        if(resultsDir.exists()) {
            File[] requestFiles = resultsDir.listFiles();
            if(requestFiles != null){
                for(File request : requestFiles){
                    try {
                        FileInputStream fis = new FileInputStream(request);
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        storedRequests.add((HashMap<String, String>) ois.readObject());
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    request.delete();
                }
            }
        }
        return storedRequests;
    }

    // Looks for stored requests and pushes them to the server
    private void handleStoredRequests(String userAuth){
        ArrayList<HashMap<String,String>> storedRequests = checkStoredRequests(userAuth);
        if(storedRequests.size()>0){
            class Callback implements OnTaskCompleted{
                @Override
                public void onTaskCompleted(JSONObject result) {
                    Log.d("APIBaseClass","Stored request pushed | "+result.toString());
                }
            }
            for(HashMap<String,String> request : storedRequests){
                new APIClassBase(context,new Callback()).execute(request);
            }
        }
    }

    // Makes a file ID based on local storage for the method/action combination in the request
    protected int getFileID(HashMap<String,String> requestMap) {
        Log.d("API Class","Getting File ID for "+requestMap.toString());
        if(requestMap.containsKey("moduleid")){
            return Integer.parseInt(requestMap.get("moduleid"));
        } else if(requestMap.containsKey("eventid")){
            return Integer.parseInt(requestMap.get("eventid"));
        }
        Log.d("API Class","*----File ID----*");
        int id = 1;
        String dirPath = getDirPath(requestMap);
        Log.d("API Class","Looking in "+dirPath);
        File dir = new File(dirPath);
        File dirFiles[] = dir.listFiles();
        if(dirFiles!=null) {
            Log.d("API Class","Found "+dirFiles.length+" files for "+requestMap.get("method"));
            Log.d("API Class","----------");
            for (int i = 0; i < dirFiles.length; i++) {
                Log.d("API Class",dirFiles[i].getName());
                int tempid = Integer.parseInt(dirFiles[i].getName().trim());
                if (tempid >= id) {
                    id = tempid + 1;
                }
            }
            Log.d("API Class","----------");
        }
        Log.d("API Class","ID is "+Integer.toString(id));
        Log.d("API Class","*-----------------*");
        return id;
    }

    // Decides on filename for the request
    protected String makeFileName(HashMap<String,String> requestMap){
        String fileName = "";
        Log.d("APIBaseClass","Making name for "+requestMap.toString());
        // If the request is a getall, the file will be called all
        // If the request already has an ID, use that as the filename
        // Else make a new ID for it from the local files for the method/action combination
        if(requestMap.get("action").equals("getall")) {
            fileName = "all";
        } else if(requestMap.containsKey("moduleid")){
            fileName = requestMap.get("moduleid");
        } else if(requestMap.containsKey("eventid")){
            fileName = requestMap.get("eventid");
        } else {
            fileName = Integer.toString(getFileID(requestMap));
        }
        return fileName;
    }

    // Handles local directory routing
    protected String getDirPath(HashMap<String,String> requestMap){
        String action = requestMap.get("action");
        if(action.equals("edit")){
            action = "get";
        }
        String userFolder = requestMap.get("auth");
        if(userFolder==null){
            userFolder = "local";
        }
        return context.getFilesDir()+"/"+userFolder+"/"+requestMap.get("method")+"/"+action;
    }

    // Save successful request result to local storage
    protected String saveToStorage(HashMap<String,String> requestMap, String result){
        String dirPath = getDirPath(requestMap);
        String fileName = makeFileName(requestMap);
        Log.d("API Class","Saving result "+result);
        try {
            File resultFile = new File(dirPath, fileName);
            // Make the directory structure for the file
            resultFile.getParentFile().mkdirs();
            // Check the directory structure was made successfully
            if(resultFile.getParentFile().exists()) {
                resultFile.createNewFile();
                // Save cached result
                FileOutputStream fos = new FileOutputStream(resultFile);
                fos.write(result.getBytes());
                fos.close();
            }
        } catch (Exception e){
            // We have a permissions problem or the app directory doesn't exist
            e.printStackTrace();
        }
        Log.d("API Class","Save Finished "+result);
        return fileName;
    }

    @Override
    protected JSONObject doInBackground(HashMap<String,String>... params) {
        JSONObject result = new JSONObject();
        HashMap<String,String> requestMap = params[0];
        String auth = context.getSharedPreferences("MyAuthFile", 0).getString("Auth","");
        // Make sure we have the method and action, throw error if we don't
        if(requestMap.containsKey("method") && requestMap.containsKey("action")) {
            // Get auth if authenticated user
            if (!auth.equals("")) {
                requestMap.put("auth", auth);
            }
            // Check the network status
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean networked = (networkInfo != null && networkInfo.isConnected());
            // Check authorisation
            Boolean authedOrAuthing = (!auth.equals("")||requestMap.get("method").equals("user"));
            // If we have network and we're authorised, use network request/response
            if (networked && authedOrAuthing) {
                // Connected, check for stored requests to push, and push if found
                handleStoredRequests(auth);
                // Connected, make request
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

                // Handle errors
                if (!result.has("status")) {
                    Toast.makeText(context, "Invalid API Response", Toast.LENGTH_LONG).show();
                    return new JSONObject();
                }

                // Save the response locally
                saveToStorage(requestMap,requestResult);
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

    protected void handlePermissionError() {

    }

    protected String localHandler(HashMap<String, String> map) {
        return "";
    }


    @Override
    protected void onPostExecute(JSONObject result) {
        // Get and check authorisation
        String auth = context.getSharedPreferences("MyAuthFile", 0).getString("Auth","");
        // If authorised, check for errors
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
                        handleError(error);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // If we have a result, handler it with the provided callback
        if(result!=null) {
            Log.d("API Result", result.toString());
            listener.onTaskCompleted(result);
        }
    }
}
