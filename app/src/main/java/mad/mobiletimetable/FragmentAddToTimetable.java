package mad.mobiletimetable;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.app.Activity;
import android.net.Uri;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.SeekBar.*;

import android.widget.SeekBar;
import android.widget.TableLayout;

import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.NumberPicker;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentAddToTimetable.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentAddToTimetable#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class FragmentAddToTimetable extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button addNew;
    private View root;
    private String roomTypes[],ModuleChoice[],rooms[],dates[],times[],durations[];
    private APIClass api;
    private ModelEvent event;
    private boolean active = true;
    private AdapterModules mAdapter;
    private ArrayList<String> moduleNameArray =new ArrayList<String>();
    private NumberPicker timePicker,dayPicker,durationPicker;
    private OnFragmentInteractionListener mListener;


    private Spinner roomTypeSpinner,ModuleChoiceView;
    private AutoCompleteTextView roomView;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentAddToTimetable.
     *
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentAddToTimetable newInstance(String param1, String param2) {
        FragmentAddToTimetable fragment = new FragmentAddToTimetable();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public FragmentAddToTimetable() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        //addNew = (Button) root.findViewById(R.id.add_new) ;



    }





    public View makeRequest(View v) {


        NumberPicker day_selector =(NumberPicker) root.findViewById(R.id.DAY);
        String day = Integer.toString(day_selector.getValue()+1);

        NumberPicker duration_selector =(NumberPicker) root.findViewById(R.id.Duration);
        String duration  = Integer.toString(duration_selector.getValue()+1);

        NumberPicker time_selector =(NumberPicker) root.findViewById(R.id.Time);
        String time = times[time_selector.getValue()]+":00";

        //String output="\nDay: "+day+"\nDur: "+duration+"\nTime"+time;

        AutoCompleteTextView room =(AutoCompleteTextView) root.findViewById(R.id.completeRoom);
        String selectedRoom=room.getText().toString();

        Spinner module =(Spinner) root.findViewById(R.id.completeModule);
        String selectedModule=module.getSelectedItem().toString();

        Spinner classType =(Spinner) root.findViewById(R.id.completeType);
        String selectedType=classType.getSelectedItem().toString();




        if( !day.isEmpty() && !duration.isEmpty() && !time.isEmpty() && !selectedRoom.isEmpty()
                && getIndex(selectedModule)!=-1 && !selectedType.isEmpty() ) {

            HashMap<String, String> request = new HashMap<String, String>();

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

            //clearAll();
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

                    for(int i = 0; i < jsonModules.length(); i++) {
                        ModelModule mod=new ModelModule((JSONObject)jsonModules.get(i));
                        modules.add(mod);
                        moduleNameArray.add(mod.getTitle());


                    }
                    Log.d("FragmentModules", moduleNameArray.get(0));
                } catch(JSONException e) {
                    e.printStackTrace();
                }
                mAdapter.clear();
                mAdapter.addAll(modules);
            }
        }
    }

    private int getIndex(String name){
        ArrayList<ModelModule> arrayList=mAdapter.getModulesArrayList();
        for (int i=0;i<arrayList.size();i++){
            if(arrayList.get(i).getTitle().equals(name)){
                return i;
            }
        }
        return -1;
    }

    private void clearAll() {
        //need to do

    }
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

        //Send API request

        root= inflater.inflate( R.layout.fragment_add_to_timetable, container, false);


        Button mButton = (Button) root.findViewById(R.id.add_new);
        mButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                makeRequest(v);
            }
        });

        HashMap<String, String> request = new HashMap<String, String>();
        request.put("method", "module");
        request.put("action", "getall");
        api = new APIClass(getActivity(), new ModuleCallback());
        api.execute(request);


        getActivity().setTitle(R.string.add_title);





        Context c = getActivity().getApplicationContext();

        Resources resources=getActivity().getResources();


        //Get String arrays for Spinners and Autocompletes
        roomTypes=resources.getStringArray(R.array.roomTypes);

        //need to load modules here
        //ModuleChoice=resources.getStringArray(R.array.ModuleChoices);

        rooms=resources.getStringArray(R.array.Rooms);
        dates=resources.getStringArray(R.array.Date);

        durations= new String[9];
        times= new String[4*25];

        int test=1;

        int count=0;
        for (int i=0;i<9;i++){

            int index=i+1;

            if(i==0){
                durations[count]=Integer.toString(index)+" period";
                count++;
            }
            else{
                durations[count]=Integer.toString(index)+" periods";
                count++;
            }

        }
        count=0;

        //time every 15 minutes
        for (int i=0;i<25;i++){
            int time=i+6;
            for (int j=0;j<4;j++){
                if(j==0){
                    times[count]=format(time)+":"+format(0);
                    count++;
                }
                else{
                    times[count]=format(time)+":"+format(j*15);
                    count++;
                }

            }

        }




        timePicker = (NumberPicker) root.findViewById(R.id.Time);
        durationPicker = (NumberPicker) root.findViewById(R.id.Duration);
        dayPicker = (NumberPicker) root.findViewById(R.id.DAY);



        timePicker.setMinValue(0);
        timePicker.setMaxValue(times.length-1);
        timePicker.setWrapSelectorWheel(false);
        timePicker.setDisplayedValues(times);
        timePicker.setValue(0);


        durationPicker.setMinValue(0);
        durationPicker.setMaxValue(durations.length-1);
        durationPicker.setWrapSelectorWheel(false);
        durationPicker.setDisplayedValues(durations);
        durationPicker.setValue(0);

        dayPicker.setMinValue(0);
        dayPicker.setMaxValue(dates.length-1);
        dayPicker.setWrapSelectorWheel(false);
        dayPicker.setDisplayedValues(dates);
        dayPicker.setValue(0);

        //ModuleChoice=resources.getStringArray(R.array.ModuleNames);

        //Set Adapters
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String> (c, R.layout.spinner_item,roomTypes);
        roomTypeSpinner = (Spinner) root.findViewById(R.id.completeType);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String> (c, R.layout.spinner_item, moduleNameArray);
        ModuleChoiceView = (Spinner) root.findViewById(R.id.completeModule);

        ArrayAdapter<String> adapter3 = new ArrayAdapter<String> (c, R.layout.spinner_item, rooms);
        roomView= (AutoCompleteTextView) root.findViewById(R.id.completeRoom);



        //Add Adapters to DropDown Views
        roomTypeSpinner.setAdapter(adapter1);
        ModuleChoiceView.setAdapter(adapter2);
        roomView.setAdapter(adapter3);


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

}
