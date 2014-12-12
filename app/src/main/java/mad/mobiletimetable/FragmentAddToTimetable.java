/*
*   Created By: Max Pearson
*   Student ID: B123103
*
 */

//Load Project package
package mad.mobiletimetable;

//Context and Resources for finding predefined values
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.Context;

//Bundle of Studio
import android.os.Bundle;

//Android Logging to terminal
import android.support.v4.app.DialogFragment;
import android.util.Log;

//Widgets for View
import android.widget.AdapterView;
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
import java.util.Date;
import java.text.SimpleDateFormat;





public class FragmentAddToTimetable extends Fragment{
    // TODO: Rename parameter arguments, choose names that match

    // TODO: Rename and change types of parameters
    private boolean edit, add;

    private View root;
    private String[] roomTypes,dates,times,durations;
    private APIClass api, apiEdit;
    private ModelEvent event;

    private String day,time,eventID ;

    private boolean active = true;
    private boolean changedItem = false;
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

    public void idArray(ArrayList<String> id){
        this.moduleIdArray = id;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            eventID=ID;
            changedItem = false;
        }
        else if(intent.hasExtra("add")){
            add = true;
            edit = false;
            String temp = intent.getStringExtra("add");
            String[] parts = temp.split("-");
            time = parts[0];
            day = parts[1];
            //ensure the day and time are stored and can be used

        }else{
            add = false;
            edit = false;
            //without loading
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
                final ModelEvent eventEdit = new ModelEvent(eventInfo);
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
                && getIndex(selectedModule)!=-1 && !selectedType.isEmpty() ) {

            HashMap<String, String> request = new HashMap<String, String>();

            //Create Request and fill
            request.put("method", "timetable");
            if(!edit){
                request.put("action", "add");
            }else{
                request.put("action", "edit");
                request.put("eventid", eventID);
            }
            request.put("moduleid", Integer.toString(mAdapter.getItem(getIndex(selectedModule)).getId()));
            request.put("day", day);
            request.put("time", time);
            request.put("duration", duration);
            request.put("type",selectedType);
            request.put("room", selectedRoom);


            api = new APIClass(getActivity(), new CreateEventCallback());
            api.execute(request);
            getActivity().finish(); //closes
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
            String status;
            try {
                status = result.getString("status");
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ModuleCallback implements OnTaskCompleted {
        public ModuleCallback() {

        }

        private int moduleId = 0;
        public ModuleCallback(int moduleId) {
            this.moduleId = moduleId;
        }

        @Override
        public void onTaskCompleted(JSONObject result) {


            if(result.has("modules")) {
                ArrayList<ModelModule> modules = new ArrayList<ModelModule>();

                mAdapter = new AdapterModules(getActivity(), new ArrayList<ModelModule>());
                moduleNameArray = new ArrayList<String>();
                moduleIdArray = new ArrayList<String>();
                try{
                    JSONArray jsonModules = result.getJSONArray("modules");

                    /*
                    *   Loop adding json query results into moduleNameArray and mAdapter
                    *   outside of loop
                     */
                    if(jsonModules.length() > 0) {
                        for (int i = 0; i < jsonModules.length(); i++) {
                            ModelModule mod = new ModelModule((JSONObject) jsonModules.get(i));
                            modules.add(mod);
                            moduleNameArray.add(mod.getTitle());
                            moduleIdArray.add(Integer.toString(mod.getId()));
                            Log.d("ModIdSize", Integer.toString(moduleIdArray.size()));
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("There are currently no Modules.\nYou cannot create an event without an associated Module.\nWould you like to create one now?")
                            .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // go to edit activity
                                    Intent intent = new Intent(getActivity(), ActivityEditModule.class);
                                    startActivityForResult(intent, 1);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                    moduleNameArray.add("Create Module...");
                    moduleIdArray.add("-1");
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
                ModuleChoiceView = (Spinner) root.findViewById(R.id.completeModule);
                adapter1 = new ArrayAdapter<String> (getActivity(), R.layout.spinner_item, moduleNameArray);
                ModuleChoiceView.setAdapter(adapter1);
                ModuleChoiceView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                               int position, long id) {
                        if (position == moduleNameArray.size() - 1 && moduleNameArray.size() > 1) {
                            Intent intent = new Intent(getActivity(), ActivityEditModule.class);
                            getActivity().startActivityForResult(intent, 1);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {

                    }
                });

                if(moduleId != 0) {
                    int id = getIndex(moduleId);
                    ModuleChoiceView.setSelection(id);
                }

                //loadEditModules(eventModule);

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

    //return index of Module Name
    private int getIndex(int id){
        ArrayList<ModelModule> arrayList=mAdapter.getModulesArrayList();
        for (int i=0;i<arrayList.size();i++){
            if(arrayList.get(i).getId() == id){
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

    public void checkEvent(final String day, View v){

                Log.d("checkEvent", "here 1");
                HashMap<String, String> request = new HashMap<String, String>();
                request.put("method", "timetable");
                request.put("action", "getall");
                request.put("day" , day);
        Log.d("checkEvent", "here 2");
                api = new APIClass(getActivity(), new CheckEvent(v));
                api.execute(request);

    }

    private class CheckEvent implements OnTaskCompleted {
        public View v;
        private  CheckEvent(View v){
            this.v = v;
        }
        @Override
        public void onTaskCompleted(JSONObject result) {
            Log.d("checkEvent", "here 3");
            clashEventsDuration =new ArrayList<String>();
            clashEventsTime =new ArrayList<String>();

            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

            if(result.length()!=0) {

                try{
                    JSONArray jsonEvents = result.getJSONArray("events");

                    /*
                    *   Loop adding json query results into moduleNameArray and mAdapter
                    *   outside of loop
                     */

                    for (int i = 0; i < jsonEvents.length(); i++) {
                        ModelEvent mod = new ModelEvent((JSONObject) jsonEvents.get(i));
                        clashEventsDuration.add(Integer.toString(mod.getDuration()));
                        String time=df.format(mod.getTime());
                        clashEventsTime.add(time);

                    }

                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
                Log.d("checkEvent", "here 4");
                buttonclick(v);
            }
        }
    }
    public boolean checkAll(String t1,String t2,ArrayList<String> t3,ArrayList<String> t4){
        Log.d("checkEvent", "here 5");
        String actualTime=t1;
        String actualDuration=t2;
        for (int i=0;i<t3.size();i++){

            String timeCheck=t3.get(i);
            String durationCheck=t4.get(i);

            if(clash(actualTime,timeCheck,actualDuration,durationCheck)){
                return true;
            }
        }
        return false;
    }
    public boolean clash(String actualTime,String timeCheck,String actualDur,String durCheck){
        Log.d("checkEvent", "here 6");
        //OToast.makeText(getActivity(), "Checking...", Toast.LENGTH_LONG).show();

        int acTime=Integer.parseInt(actualTime.substring(0,2));
        int acDur=Integer.parseInt(actualDur);


        int chkTime=Integer.parseInt(timeCheck.substring(0,2));
        int chkDur=Integer.parseInt(durCheck);

        int startAc = acTime;
        int endAc = acTime+acDur;
        int startCh = chkTime;
        int endCh = chkTime+chkDur;
        //checks all posibilites of conflicts
        boolean check1 = ((endAc > startCh)&&(endAc <=endCh));
        boolean check2 = ((startAc >= startCh)&&(startAc < endCh));
        boolean check3 = ((startAc <= startCh)&&(endAc >= endCh));

        if(check1 || check2 || check3){
            return true;
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
        if(edit){
            mButton.setText(R.string.Update_Event);
        }else if(add){
            mButton.setText(R.string.Add_Event);
        }else{
            mButton.setText(R.string.Add_Event);
        }
        mButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                NumberPicker day_selector = (NumberPicker) root.findViewById(R.id.DAY);
                String day = Integer.toString(day_selector.getValue() + 1);
                checkEvent(day,v);  //loads events for this day - passes to functions to handle.
            }
        });


        //Fragment Title
        if(add) {
            getActivity().setTitle(R.string.Add_Event);
        }else if (edit){
            getActivity().setTitle(R.string.Edit_Event);
        }else{
            getActivity().setTitle(R.string.Add_Event);
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
                     changedItem = true;
                 }

             }
        );
        //currently redundant.  for clashes with edited events
        durationPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
             @Override
             //needs a final look at
             public void onValueChange(NumberPicker durationPicker, int i, int i2) {
                 changedItem = true;
             }
         }
        );
        //currently redundant.  for clashes with edited events
        dayPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
             @Override
             //needs a final look at
             public void onValueChange(NumberPicker dayPicker, int i, int i2) {
                 changedItem = true;
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
        adapter2 = new ArrayAdapter<String> (c, R.layout.spinner_item,roomTypes);

        roomTypeSpinner.setAdapter(adapter2);
        if(edit) {
            //module choice


        }
        if (add ==true){
            loadAdd(times);
        }
                //return View
        return root;
    }
    public void buttonclick(View v) {//function to handle the clashes
        NumberPicker time_selector = (NumberPicker) root.findViewById(R.id.Time);
        NumberPicker duration_selector = (NumberPicker) root.findViewById(R.id.Duration);
        String time = times[time_selector.getValue()] + ":00";
        String duration = Integer.toString(duration_selector.getValue() + 1);
        if (!edit) {//if edit then we ignore clashes - current Limitation
            //pulls info on the selected day
            if (checkAll(time, duration, clashEventsTime, clashEventsDuration)) {
                Toast.makeText(getActivity(), "Clash Select different time", Toast.LENGTH_LONG).show();
            } else {
                MakeRequest(v);
            }
        } else {
            MakeRequest(v);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int moduleId;
        if(data != null && data.hasExtra("moduleid")) {
             moduleId = data.getIntExtra("moduleid", 0);

            if (moduleId == 0) return;
        } else {
            return;
        }

        HashMap<String, String> request = new HashMap<String, String>();
        request.put("method", "module");
        request.put("action", "getall");
        api = new APIClass(getActivity(), new ModuleCallback(moduleId));
        api.execute(request);
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
    private void loadEdit(ModelEvent eventEdit){//inserts data into correct locations when Edit is passed to here.
        if(!active) return;
        //apply changes here
        day = Integer.toString(eventEdit.getDay()-2);
        time = eventEdit.getDate();
        //String moduleTitle = storedModule.getTitle();
        String room = eventEdit.getLocation();
        String lessonType = eventEdit.getLessonType();
        int dur = eventEdit.getDuration();
        int lessonIndex = findIndex(roomTypes, lessonType);
        int durationIndex = dur -1;
        ((TextView) root.findViewById(R.id.completeRoom)).setText(room);
        roomTypeSpinner.setSelection(lessonIndex);
        loadAdd(times);
        durationPicker.setValue(durationIndex);
        loadEditModules(eventEdit);
    }
    private void loadEditModules(ModelEvent eventEdit){
        String ModId = Integer.toString(eventEdit.getModuleId());
        Log.d("ag",Integer.toString(moduleIdArray.size()));
        int moduleIndex = -1;
        for (int i=0; i<moduleIdArray.size(); i++ ){
            if (((String)moduleIdArray.get(i)).equals(ModId)){
                moduleIndex = i;
            }
        }
        Log.d("Mod ID",ModId);
        Log.d("Mod index",Integer.toString(moduleIndex));
        ModuleChoiceView.setSelection(moduleIndex);
    }
    private void loadAdd(String[] times){   //inserts time and day appropriately on add/edit
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
            if (((String)a.get(i)).equals(b)){

                return i;
            }
        }
        return -1;
    }
}
