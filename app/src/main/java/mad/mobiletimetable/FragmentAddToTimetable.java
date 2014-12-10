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
    private ModelEvent event, eventEdit;

    private String day,time ;

    private boolean active = true;
    private AdapterModules mAdapter;
    private ArrayList<String> moduleNameArray =new ArrayList<String>();
    private ArrayAdapter<String> adapter1,adapter2,adapter3;


    private NumberPicker timePicker,dayPicker,durationPicker;
    private OnFragmentInteractionListener mListener;

    private Spinner roomTypeSpinner;
    private Spinner ModuleChoiceView;
    private EditText roomView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getActivity().getIntent();

        if(intent.hasExtra("edit")){
            edit = true;
            add = false;
            //load the timetable ID
            String ID = intent.getStringExtra("edit");
            HashMap<String, String> request = new HashMap<String, String>();
            request.put("method", "timetable");
            request.put("action", "get");
            request.put("eventid", ID);
            apiEdit = new APIClass(getActivity(), new Callback());
            apiEdit.execute(request);
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
                eventEdit = new ModelEvent(eventInfo);
            } catch(JSONException e) {
                e.printStackTrace();
            }

            loadEdit();
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
                    }
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        root= inflater.inflate( R.layout.fragment_add_to_timetable, container, false);


        //Button to make new Event
        Button mButton = (Button) root.findViewById(R.id.add_new);
        mButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MakeRequest(v);
            }
        });

        //Request code to create event
        HashMap<String, String> request = new HashMap<String, String>();
        request.put("method", "module");
        request.put("action", "getall");
        api = new APIClass(getActivity(), new ModuleCallback());
        api.execute(request);

        //Fragment Title
        getActivity().setTitle("Add Event");

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
    private void loadEdit(){
        if(!active) return;
        //apply changes here
    }
    private void loadAdd(String[] times){
        //apply changes
        int index = -1;
        //gets index of time
        for (int i=0; i<times.length; i++ ){
            Log.d("time", time);
            Log.d("times", times[i]);
            if (times[i].equals(time)){
                index = i;
            }
        }
        dayPicker.setValue(Integer.parseInt(day));
        timePicker.setValue(index);
        durationPicker.setMaxValue(durationPicker.getMaxValue()-index);
        durationPicker.setWrapSelectorWheel(false);
    }
}
