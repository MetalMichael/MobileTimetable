MobileTimetable
===============

Mobile Application Development Coursework

## APIClass Example implementation
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
new APIClass(getActivity().getApplicationContext(),new Callback()).execute(request);
```