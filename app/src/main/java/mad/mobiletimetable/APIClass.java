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
public class APIClass extends APIClassBase {
    private Activity activity;

    public APIClass(Activity activity, OnTaskCompleted listener) {
        super(activity.getApplicationContext(), listener);
        this.activity = activity;
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

    @Override
    protected void localHandler(HashMap<String,String> requestMap){
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
    protected void handlePermissionError() {
        Intent intent = new Intent(context, ActivityLogin.class);
        activity.finish();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("authentication_error", true);
        context.startActivity(intent);
    }

    @Override
    protected void handleError(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

}

