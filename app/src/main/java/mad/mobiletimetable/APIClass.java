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

    private String getFileName (HashMap<String,String> requestMap){
        String fileName = "";
        if(requestMap.get("action").equals("getall")) {
            fileName = "all";
        } else {
            if (requestMap.get("method").equals("module")) {
                fileName = requestMap.get("moduleid");
            } else {
                fileName = requestMap.get("eventid");
            }
        }
        return fileName;
    }

    // Check local storage for result of request if connection unavailable
    // use it as the result if it is present
    private String fetchFromStorage(HashMap<String,String> requestMap){
        String result = "";
        String dirPath = getDirPath(requestMap);
        String fileName = getFileName(requestMap);
        Log.d("API Class","Fetching "+dirPath+"/"+fileName);
        File resultFile = new File(dirPath, fileName);
        if(resultFile.exists()){
            Log.d("API Class","Found file");
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(resultFile);
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

    private JSONObject appendToArray(String arrayName, JSONObject newElement, HashMap<String,String> requestMap){
        JSONObject response = new JSONObject();
        requestMap.put("action","getall");
        Log.d("API Class","*----Appending----*");
        String previousGetAll = fetchFromStorage(requestMap);
        Log.d("API Class","Append Previous "+previousGetAll);
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
        Log.d("API Class","Append Response "+response.toString());
        Log.d("API Class","*--Appending End--*");
        return response;
    }

    // Format a module add string to a module get string response
    private JSONObject formatModuleGet(HashMap<String,String> requestMap,Boolean singleResponse){
        JSONObject response = new JSONObject();
        JSONObject module = new JSONObject();
        requestMap.put("action","get");
        int id = 0;
        if(requestMap.containsKey("eventid")){
            id = Integer.getInteger(requestMap.get("eventid"));
        } else {
            id = getFileID(requestMap);
        }
        try {
            module.put("ID",id);
            module.put("ModuleCode",requestMap.get("modulecode"));
            module.put("ModuleTitle",requestMap.get("moduletitle"));
            module.put("Lecturer",requestMap.get("lecturer"));
            if(singleResponse) {
                response.put("module",module);
                response.put("status", "OK");
            } else {
                response = module;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }
    private JSONObject formatModuleGetAll(HashMap<String,String> requestMap){
        JSONObject newModule = formatModuleGet(requestMap,false);
        return appendToArray("modules",newModule,requestMap);
    }

    // Format a timetable add string to a timetable get string response
    private JSONObject formatTimetableGet(HashMap<String,String> requestMap,Boolean singleResponse){
        JSONObject response = new JSONObject();
        JSONObject event = new JSONObject();
        requestMap.put("action","get");
        int id = 0;
        if(requestMap.containsKey("eventid")){
            id = Integer.getInteger(requestMap.get("eventid"));
        } else {
            id = getFileID(requestMap);
        }
        try {
            event.put("ID",id);
            event.put("ModuleID",requestMap.get("moduleid"));
            event.put("Day",requestMap.get("day"));
            event.put("Time",requestMap.get("time"));
            event.put("Duration",requestMap.get("duration"));
            event.put("Room",requestMap.get("room"));
            event.put("Type",requestMap.get("type"));
            requestMap.put("method","module");
            requestMap.put("action","get");
            event.put("Module",formatModuleGet(requestMap,false));
            if(singleResponse) {
                response.put("event",event);
                response.put("status", "OK");
            } else {
                response = event;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

    private JSONObject formatTimetableGetAll(HashMap<String,String> requestMap){
        JSONObject newEvent = formatTimetableGet(requestMap,false);
        return appendToArray("events",newEvent,requestMap);
    }

    private String performGenericAdd(HashMap<String,String> requestMap){
        String method = requestMap.get("method");
        String id = "eventid";
        if(method.equals("module")){
            id = "moduleid";
        }
        Log.d("API Class","Adding "+requestMap.toString());
        HashMap<String,String> getRequest = new HashMap<String, String>(requestMap);
        getRequest.put("action", "getall");
        String formattedResponse = null;
        Log.d("API Class",">Getall updating<");
        if(method.equals("module")) {
            formattedResponse = formatModuleGetAll(requestMap).toString();
        } else {
            formattedResponse = formatTimetableGetAll(requestMap).toString();
        }
        saveToStorage(getRequest,formattedResponse);
        Log.d("API Class",">Get creation<");
        getRequest.put("action", "get");
        if(method.equals("module")) {
            formattedResponse = formatModuleGet(requestMap,true).toString();
        } else {
            formattedResponse = formatTimetableGet(requestMap,true).toString();
        }
        String fileID = saveToStorage(getRequest,formattedResponse);
        JSONObject response = new JSONObject();
        try {
            response.put(id, fileID);
            response.put("status","OK");
        } catch (JSONException e){
            e.printStackTrace();
        }
        return response.toString();
    }

    private JSONArray removeElementFromJSONArray(JSONArray jsonArray, int position){
        Log.d("API Class","removing element from "+jsonArray.toString());
        JSONArray list = new JSONArray();
        int arrayLength = jsonArray.length();
        if (jsonArray != null) {
            for (int i = 0;i < arrayLength;i++) {
                //Excluding the item at position
                if (i != position) {
                    try {
                        list.put(jsonArray.get(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return list;
    }

    private JSONObject removeElementGenericGetAll(HashMap<String,String> requestMap, String id, String name){
        requestMap.put("action","getall");
        String originalString = fetchFromStorage(requestMap);
        Log.d("API Class","<>Getall Pre-update | "+originalString);
        JSONObject original = new JSONObject();
        JSONObject response = new JSONObject();
        JSONArray newArray = new JSONArray();
        int position = -1;
        try {
            original = new JSONObject(originalString);
            JSONArray originalArray = original.getJSONArray(name);
            for(int i = 0; i < originalArray.length();i++){
                JSONObject arrayElement = (JSONObject) originalArray.get(i);
                if(arrayElement.get("ID").toString()==id){
                    position = i;
                    Log.d("API Class",arrayElement.get("ID").toString()+"="+id+"@"+position);
                    break;
                }
            }
            if(position>=0) {
                newArray = removeElementFromJSONArray(originalArray, position);
            }
            response.put(name,newArray);
            response.put("status","OK");
        } catch (Exception e){
            e.printStackTrace();
        }
        Log.d("API Class","<>Getall Post-update | "+response.toString());
        return response;
    }

    private void performGenericDelete(HashMap<String,String> requestMap){
        String method = requestMap.get("method");
        JSONObject updatedGetAll = new JSONObject();
        Log.d("API Class","Deleting a "+method);
        // Get the id to delete
        String entryID = null;
        String arrayName = null;
        if(method.equals("module")){
            entryID = requestMap.get("moduleid");
            arrayName = "modules";
        } else if (method.equals("timetable")){
            entryID = requestMap.get("eventid");
            arrayName = "events";
        }
        updatedGetAll = removeElementGenericGetAll(requestMap,entryID,arrayName);
        // Delete the id's get responses
        String dirPath = context.getFilesDir()+"/"+method+"/get";
        File getFile = new File(dirPath,entryID);
        Log.d("API Class","DELETING GET FILE "+getFile.toString());
        getFile.delete();
        // Delete the id from the methods getall response
        HashMap<String,String> getRequest = new HashMap<String, String>();
        getRequest.put("method",method);
        getRequest.put("action", "getall");
        saveToStorage(getRequest,updatedGetAll.toString());
    }

    private String performGenericEdit(HashMap<String,String> requestMap){
        Log.d("API Class","Editing a "+requestMap.get("method"));
        Log.d("API Class","Edit Request "+requestMap.toString());
        // Delete original entry
        performGenericDelete(requestMap);
        // Add updated entry
        String response = performGenericAdd(requestMap);
        return response;
    }

    @Override
    protected String localHandler(HashMap<String,String> requestMap){
        String result = "";
        String method = requestMap.get("method");
        String action = requestMap.get("action");
        Log.d("API Class","localHandler Called for "+method+" "+action);
        if(method.equals("module")||method.equals("timetable")){
            if(action.equals("add")){
                result = performGenericAdd(requestMap);
            } else if(action.equals("get")){
                result = fetchFromStorage(requestMap).toString();
            } else if(action.equals("getall")){
                result = fetchFromStorage(requestMap).toString();
            } else if(action.equals("edit")){
                result = performGenericEdit(requestMap);
            } else if(action.equals("delete")){
                performGenericDelete(requestMap);
            }
        }
        Log.d("API Class","localHandler Finished");
        return result;
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

