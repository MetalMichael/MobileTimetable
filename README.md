MobileTimetable
===============

Mobile Application Development Coursework

## APIClass Example implementation
**Note:** ```method``` and ```action``` are required key-value pairs in the request HashMap.
```Java
class Callback implements OnTaskCompleted{
    @Override
    public void onTaskCompleted(JSONObject result) {
        Log.d("Resulting Request",result.toString());
    }
}
HashMap<String,String> request = new HashMap<String, String>();
request.put("method","module");
request.put("action","getall");
request.put("auth","debug");
new APIClass(getActivity(), new Callback()).execute(request);
```
