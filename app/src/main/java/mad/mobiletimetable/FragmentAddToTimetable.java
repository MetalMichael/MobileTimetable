/*
*   Created By: Max Pearson
*   Student ID: B123103
*
 */

//Load Project package
package mad.mobiletimetable;

//Context and Resources for finding predefined values
import android.content.Intent;
import android.content.res.Resources;
import android.content.Context;

//Bundle of Studio
import android.os.Bundle;

//Android Logging to terminal
import android.util.Log;

//Widgets for View
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.NumberPicker;

//Application
import android.app.Activity;
import android.app.Fragment;

//Requests
import android.net.Uri;

//View for containment of widgets
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

//HTTP JSON packages
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//Java Standard Library
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FragmentAddToTimetable extends Fragment{
    // TODO: Rename parameter arguments, choose names that match

    // TODO: Rename and change types of parameters
    private boolean edit, add;
    private View root;
    private String[] roomTypes,dates,times,durations;
    private APIClass api, apiEdit;
    private ModelEvent event;
    private String ModId;

    private String day,time ;

    private boolean active = true;
    private AdapterModules mAdapter;
    private ArrayList<String> moduleNameArray =new ArrayList<String>();
    private ArrayList<String> moduleIdArray =new ArrayList<String>();
    private ArrayAdapter<String> adapter1,adapter2,adapter3;

    private ArrayList<String> clashEventsDuration =new ArrayList<String>();
    private ArrayList<String> clashEventsTime =new ArrayList<String>();

    private boolean noClash=true;


    private NumberPicker timePicker,dayPicker,durationPicker;
    private OnFragmentInteractionListener mListener;

    private Spinner roomTypeSpinner;
    private Spinner ModuleChoiceView;
    private EditText roomView;

    public void idArray(ArrayList<String> id){
        this.moduleIdArray = id;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /////////////////////////////////////////////////////////////////////////////////////////////move to top
        //Request code to create event
        HashMap<String, String> request = new HashMap<String, String>();
        request.put("method", "module");
        request.put("action", "getall");
        api = new APIClass(getActivity(), new ModuleCallback());
        api.execute(request);

        Intent intent = getActivity().getIntent();

        if(intent.hasExtra("edit")){
            edit = true;
            add = false;
            //load the timetable ID
            String ID = intent.getStringExtra("edit");
            HashMap<String, String> request2 = new HashMap<String, String>();
            request2.put("method", "timetable");
            request2.put("action", "get");
            request2.put("eventid", ID);
            apiEdit = new APIClass(getActivity(), new Callback());
            apiEdit.execute(request2);
            Toast.makeText(getActivity(), "Edit ID: "+ID, Toast.LENGTH_LONG).show();
        }
        else if(intent.hasExtra("add")){
            add = true;
            edit = false;
            String temp = intent.getStringExtra("add");
            String[] parts = temp.split("-");
            time = parts[0];
            day = parts[1];
            //ensure the day and time are stored and can be used

            Toast.makeText(getActivity(), "Add, Time: "+time+" Day: "+day, Toast.LENGTH_LONG).show();

        }else{
            add = false;
            edit = false;
            //without loading
            Toast.makeText(getActivity(), "Standard", Toast.LENGTH_LONG).show();
        }

    }
    /*
    *   makeRequest(View v)
    *
    *   Find Inputs and using APIClass create values needed for JSON query
    *
     */
    private class Callback implements OnTaskCompleted {
        @Override
        public void onTaskCompleted(JSONObject result) {
            if (!active) return;
            if(!result.has("event")) {
                getActivity().finish();
                return;
            }
            try {
                JSONObject eventInfo = result.getJSONObject("event");
                ModelEvent eventEdit = new ModelEvent(eventInfo);
                        //ModelEvent eventEdit = (ModelEvent)(result.get("event"));
                loadEdit(eventEdit);
            } catch(JSONException e) {
                e.printStackTrace();
            }


        }
    }

    public View MakeRequest(View v) {

        //Number Picker set
        NumberPicker day_selector =(NumberPicker) root.findViewById(R.id.DAY);
        NumberPicker duration_selector =(NumberPicker) root.findViewById(R.id.Duration);
        NumberPicker time_selector =(NumberPicker) root.findViewById(R.id.Time);

        //Edit and Spinner set
        EditText room =(EditText) root.findViewById(R.id.completeRoom);
        Spinner moduleSpinner =(Spinner) root.findViewById(R.id.completeModule);
        Spinner classType =(Spinner) root.findViewById(R.id.completeType);

        //Day
        String day = Integer.toString(day_selector.getValue()+1);
        //Duration of event
        String duration  = Integer.toString(duration_selector.getValue()+1);
        //Time of event
        String time = times[time_selector.getValue()]+":00";
        //Room of event
        String selectedRoom=room.getText().toString();
        //Module String
        String selectedModule=moduleSpinner.getSelectedItem().toString();
        //Event Type
        String selectedType=classType.getSelectedItem().toString();


        //Check is all Inputs are filled in
        if( !day.isEmpty() && !duration.isEmpty() && !time.isEmpty() && !selectedRoom.isEmpty()
                && getIndex(selectedModule)!=-1 && !selectedType.isEmpty()  && clashEventsDuration.size()==0 ) {

            HashMap<String, String> request = new HashMap<String, String>();

            //Create Request and fill
            request.put("method", "timetable");
            request.put("action", "add");
            request.put("moduleid", Integer.toString(mAdapter.getItem(getIndex(selectedModule)).getId()));
            request.put("day", day);
            request.put("time", time);
            request.put("duration", duration);
            request.put("type",selectedType);
            request.put("room", selectedRoom);


            api = new APIClass(getActivity(), new CreateEventCallback());
            api.execute(request);
        }

        else{
            Toast.makeText(getActivity(), "Please fill in all inputs", Toast.LENGTH_LONG).show();
        }

        return v;
    }

    private class CreateEventCallback implements OnTaskCompleted {
        @Override
        public void onTaskCompleted(JSONObject result) {
            if(!active) return;

            if(!result.has("module")) {
                Toast.makeText(getActivity(), "Added to Timetable", Toast.LENGTH_LONG).show();
            }
            try {
                JSONObject eventID = result.getJSONObject("module");
                event = new ModelEvent(eventID);
                Toast.makeText(getActivity(), "Added to Timetable: " +event.getModule().toString(), Toast.LENGTH_LONG).show();

            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ModuleCallback implements OnTaskCompleted {

        @Override
        public void onTaskCompleted(JSONObject result) {


            if(result.has("modules")) {
                ArrayList<ModelModule> modules = new ArrayList<ModelModule>();

                mAdapter = new AdapterModules(getActivity(), new ArrayList<ModelModule>());

                try{
                    JSONArray jsonModules = result.getJSONArray("modules");

                    /*
                    *   Loop adding json query results into moduleNameArray and mAdapter
                    *   outside of loop
                     */
                    for(int i = 0; i < jsonModules.length(); i++) {
                        ModelModule mod=new ModelModule((JSONObject)jsonModules.get(i));
                        modules.add(mod);
                        moduleNameArray.add(mod.getTitle());
                        moduleIdArray.add(Integer.toString(mod.getId()));
                        Log.d("ModIdSize", Integer.toString(moduleIdArray.size()));
                    }
                    idArray(moduleIdArray);
                    Log.d("FragmentModules", "Modules Found");


                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
                //reset json storage container
                mAdapter.clear();

                //add all modules to container
                mAdapter.addAll(modules);

                //Update adapters
                ModuleChoiceView.setAdapter(adapter1);
                roomTypeSpinner.setAdapter(adapter2);

            }

        }
    }

    //return index of Module Name
    private int getIndex(String name){
        ArrayList<ModelModule> arrayList=mAdapter.getModulesArrayList();
        for (int i=0;i<arrayList.size();i++){
            if(arrayList.get(i).getTitle().equals(name)){
                return i;
            }
        }
        //if nothing return -1
        return -1;
    }

    //Get int time as String and return
    private String format(int hour){
        String output;
        if(hour<10){
            output="0"+Integer.toString(hour);
        }
        else{
            output=Integer.toString(hour);
        }
        return output;
    }
/*
    public void loadModules(){
        //Request code to create event
        HashMap<String, String> request = new HashMap<String, String>();
        request.put("method", "module");
        request.put("action", "getall");
        api = new APIClass(getActivity(), new ModuleCallback());
        api.execute(request);

    }
    */
    public void checkEvent(String day){
        HashMap<String, String> request = new HashMap<String, String>();
        request.put("method", "timetable");
        request.put("action", "getall");
        request.put("day" , day);

        api = new APIClass(getActivity(), new CheckEvent());
        api.execute(request);

    }
    private class CheckEvent implements OnTaskCompleted {
        @Override
        public void onTaskCompleted(JSONObject result) {

            if(result.length()!=0) {

                try{
                    JSONArray jsonEvents = result.getJSONArray("events");
                    clashEventsDuration =new ArrayList<String>();
                    clashEventsTime =new ArrayList<String>();
                    /*
                    *   Loop adding json query results into moduleNameArray and mAdapter
                    *   outside of loop
                     */

                    for (int i = 0; i < jsonEvents.length(); i++) {
                        ModelEvent mod = new ModelEvent((JSONObject) jsonEvents.get(i));
                        clashEventsDuration.add(Integer.toString(mod.getDuration()));
                        clashEventsTime.add(mod.getDate());
                    }

                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public boolean checkAll(String t1,String t2,ArrayList<String> t3,ArrayList<String> t4){
        String actualTime=t1;
        String actualDuration=t2;
        for (int i=0;i<t3.size();i++){

            String timeCheck=t3.get(i);
            String durationCheck=t4.get(i);

            boolean check=clash(actualTime,timeCheck,actualDuration,durationCheck);

            if(check){
                return true;
            }


        }
        return false;
    }
    public boolean clash(String actualTime,String timeCheck,String actualDur,String durCheck){

        int acTime=Integer.parseInt(actualTime.substring(0,2));
        int acDur=Integer.parseInt(actualDur);

        int chkTime=Integer.parseInt(timeCheck.substring(0,2));
        int chkDur=Integer.parseInt(durCheck);


        //      First Side
        if(acTime >= chkTime && (acTime+acDur) < chkTime ){
            if(acTime<=chkTime+chkDur){
                return true; //time falls inbetween
            }
            else if ( (acTime+acDur)<=chkTime+chkDur ){
                return true; //time falls inbetween
            }
            else{
                return false; //not sure
            }
        }
        //Time being injected starts after or at same time
        else if( (acTime + acDur) >= chkTime) {

            if ( ( acTime + acDur ) <= chkTime+chkDur ){
                return true;
            }

        }

        //      Second Side
        if(chkTime >= acTime && (chkTime+chkDur) < acTime ){

            if(chkTime<=acTime+acDur){
                return true; //time falls inbetween
            }
            else if ( (chkTime+chkDur)<=acDur+acTime ){
                return true; //time falls inbetween
            }
            else{
                return false; //not sure
            }
        }
        //Time being injected starts after or at same time
        else if( (chkTime + chkDur) >= acTime) {

            if ( ( chkTime+ chkDur ) <= acTime+acDur ){
                return true;
            }

        }
        return false;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        root= inflater.inflate( R.layout.fragment_add_to_timetable, container, false);


        //Button to make new Event
        Button mButton = (Button) root.findViewById(R.id.add_new);
        mButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                NumberPicker day_selector = (NumberPicker) root.findViewById(R.id.DAY);
                NumberPicker time_selector = (NumberPicker) root.findViewById(R.id.Time);
                NumberPicker duration_selector = (NumberPicker) root.findViewById(R.id.Duration);


                String day = Integer.toString(day_selector.getValue() + 1);
                String time = times[time_selector.getValue()] + ":00";
                String duration = Integer.toString(duration_selector.getValue() + 1);

                checkEvent(day);

                boolean checkIsClash = checkAll(time, duration, clashEventsTime, clashEventsDuration);

                if (clashEventsDuration.size() > 0 && checkIsClash) {
                    Toast.makeText(getActivity(), "Clash Select different time", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    //Make request if no clashes
                    MakeRequest(v);
                }
            }

            });


        //Fragment Title
        //loadModules()
        if(add || !(add &&edit)) {
            getActivity().setTitle("Add Event");
        }else{
            getActivity().setTitle("Edit Event");
        }

        //set context for Actiity Fragment
        Context c = getActivity().getApplicationContext();

        //Get Resources from strings
        Resources resources=getActivity().getResources();


        //Resources
        roomTypes=resources.getStringArray(R.array.roomTypes);
        dates=resources.getStringArray(R.array.Date);

        //Duration and Time String arrays
        durations= new String[14];
        times = new String[14];

        int test=1;

        for (int i=0;i<14;i++){
            if(i==0){
                durations[i]=Integer.toString(i+1)+" period";
            }
            else {
                durations[i] = Integer.toString(i + 1) + " periods";
            }
        }
        int count=0;
        //time
        for (int i=8;i<=21;i++){
            times[count]=format(i)+":00";

            count++;
        }

        //Find Number Picker elements
        timePicker = (NumberPicker) root.findViewById(R.id.Time);
        durationPicker = (NumberPicker) root.findViewById(R.id.Duration);
        dayPicker = (NumberPicker) root.findViewById(R.id.DAY);

        //Find Spinner elements
        ModuleChoiceView = (Spinner) root.findViewById(R.id.completeModule);
        roomTypeSpinner = (Spinner) root.findViewById(R.id.completeType);

        //Set timePicker attributes
        timePicker.setMinValue(0);
        timePicker.setMaxValue(times.length-1);
        timePicker.setWrapSelectorWheel(false);
        timePicker.setDisplayedValues(times);
        timePicker.setValue(0);
        timePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                 @Override
                 //needs a final look at
                 public void onValueChange(NumberPicker timePicker, int i, int i2) {
                     Log.d("times","Value was: " + Integer.toString(i) + " is now: " + Integer.toString(i2));
                     durationPicker.setMaxValue(durations.length-1-i2);
                     durationPicker.setMinValue(0);
                     durationPicker.setWrapSelectorWheel(false);
                     durationPicker.setDisplayedValues(durations);
                     durationPicker.setValue(0);
                 }
             }
        );

        //Set durationPicker attributes
        durationPicker.setMinValue(0);
        durationPicker.setMaxValue(durations.length-1);
        durationPicker.setWrapSelectorWheel(false);
        durationPicker.setDisplayedValues(durations);
        durationPicker.setValue(0);

        //Set dayPicker attributes
        dayPicker.setMinValue(0);
        dayPicker.setMaxValue(dates.length-1);
        dayPicker.setWrapSelectorWheel(false);
        dayPicker.setDisplayedValues(dates);
        dayPicker.setValue(0);

        //Populate adapters with modules and rooms
        adapter1 = new ArrayAdapter<String> (c, R.layout.spinner_item, moduleNameArray);
        adapter2 = new ArrayAdapter<String> (c, R.layout.spinner_item,roomTypes);

        ModuleChoiceView.setAdapter(adapter1);
        roomTypeSpinner.setAdapter(adapter2);
        if(edit) {
            //module choice
            Log.d("ag",Integer.toString(moduleIdArray.size()));
            int moduleIndex = -1;
            for (int i=0; i<moduleIdArray.size(); i++ ){
                Log.d("aogfosautgfhpisautgpiwsaug",(String)moduleIdArray.get(i));
                if (((String)moduleIdArray.get(i)).equals(ModId)){
                    moduleIndex = i;
                }
            }
            ModuleChoiceView.setSelection(moduleIndex);
        }
        if (add ==true){
            loadAdd(times);
        }


        //return View
        return root;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
    private void loadEdit(ModelEvent eventEdit){
        if(!active) return;
        Toast.makeText(getActivity(), "loadEdit" , Toast.LENGTH_LONG).show();
        Toast.makeText(getActivity(), eventEdit.getDate(), Toast.LENGTH_LONG).show();
        //apply changes here
        day = Integer.toString(eventEdit.getDay());
        time = eventEdit.getDate();
        //String moduleTitle = storedModule.getTitle();
        String room = eventEdit.getLocation();
        String lessonType = eventEdit.getLessonType();
        int dur = eventEdit.getDuration();
        ModId = Integer.toString(eventEdit.getModuleId());
        int lessonIndex = findIndex(roomTypes, lessonType);
        int durationIndex = dur -1;

        //room
        ((TextView) root.findViewById(R.id.completeRoom)).setText(room);
        //type
        roomTypeSpinner.setSelection(lessonIndex);
        //day and time
        loadAdd(times);
        //duration
        durationPicker.setValue(durationIndex);
    }
    private void loadAdd(String[] times){
        //apply changes
        int index = -1;
        //gets index of time
        for (int i=0; i<times.length; i++ ){
            if (times[i].equals(time)){
                index = i;
            }
        }
        dayPicker.setValue(Integer.parseInt(day));
        timePicker.setValue(index);
        durationPicker.setMaxValue(durationPicker.getMaxValue()-index);
        durationPicker.setWrapSelectorWheel(false);
    }
    private int findIndex(String[] a, String b){
        for (int i=0; i<a.length; i++ ){
            if (a[i].equals(b)){
                return i;
            }
        }
        return -1;
    }
    private int findIndex(ArrayList<String> a, String b){
        Log.d("length", Integer.toString(a.size()));
        for (int i=0; i<a.size(); i++ ){
            Log.d("aogfosautgfhpisautgpiwsaug",(String)a.get(i));
            if (((String)a.get(i)).equals(b)){

                return i;
            }
        }
        return -1;
    }
}
