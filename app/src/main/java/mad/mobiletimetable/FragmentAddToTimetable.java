package mad.mobiletimetable;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import android.app.Activity;
import android.net.Uri;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import android.widget.TableLayout;

import android.widget.Toast;
import android.content.Context;
import android.widget.Spinner;


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
public class FragmentAddToTimetable extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button addNew;
    private TableLayout layoutNew;
    private View root;
    private String roomTypes[],ModuleChoice[],rooms[],dates[],times[];
    private APIClass api;
    private ModelEvent event;
    private boolean active = true;
    private AdapterModules mAdapter;
    private String moduleNames[];



    private OnFragmentInteractionListener mListener;

    private Spinner roomTypeSpinner,DateView,TimeView;
    private AutoCompleteTextView ModuleChoiceView,roomView;


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


    }
    public int returnDayInt(String day){
       String[] Days={"Monday","Tuesday","Wednesday","Thursday","Friday"};
       for (int i=0;i<Days.length;i++){
           if (Days[i].equals(day)){
               return i;
           }
       }
    }
    public void onClick(View v) {

        Intent intent = getActivity().getIntent();



        Spinner daySpinner =(Spinner) root.findViewById(R.id.DAY);
        String day=Integer.toString(returnDayInt(daySpinner.getSelectedItem().toString()));

        Spinner durationSpinner =(Spinner) root.findViewById(R.id.DURATION);
        String duration=durationSpinner.getSelectedItem().toString();

        Spinner timeSpinner =(Spinner) root.findViewById(R.id.TIME);
        String time=timeSpinner.getSelectedItem().toString();

        AutoCompleteTextView room =(AutoCompleteTextView) root.findViewById(R.id.completeRoom);
        String selectedRoom=room.getText().toString();

        AutoCompleteTextView module =(AutoCompleteTextView) root.findViewById(R.id.completeModule);
        String selectedModule=module.getText().toString();

        Spinner classType =(Spinner) root.findViewById(R.id.completeType);
        String selectedType=classType.getSelectedItem().toString();

        if( !day.isEmpty() && !duration.isEmpty() && !time.isEmpty() && !selectedRoom.isEmpty()
                && getIndex(selectedModule)!=-1 && !selectedType.isEmpty() ) {

            HashMap<String, String> request = new HashMap<String, String>();
            request.put("method", "timetable");
            request.put("action", "add");
            request.put("moduleid", Integer.toString(getIndex(selectedModule)));


            request.put("day", day);
            request.put("time", time);
            request.put("duration", duration);
            //request.put("type",selectedType);
            request.put("Room", selectedType);


            api = new APIClass(getActivity(), new CreateEventCallback());
            api.execute(request);
        }
        else{
            //Toast Here
        }
    }

    private class CreateEventCallback implements OnTaskCompleted {
        @Override
        public void onTaskCompleted(JSONObject result) {
            if(!active) return;

            if(!result.has("module")) {
                getActivity().finish();
                Toast.makeText(getActivity(), "Added to Timetable", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                JSONObject eventID = result.getJSONObject("module");
                event = new ModelEvent(eventID);

            } catch(JSONException e) {
                e.printStackTrace();
            }

            clearAll();
        }
    }

    private class ModuleCallback implements OnTaskCompleted {
        @Override
        public void onTaskCompleted(JSONObject result) {
            if(result.has("modules")) {
                ArrayList<ModelModule> modules = new ArrayList<ModelModule>();
                try{
                    JSONArray jsonModules = result.getJSONArray("modules");
                    for(int i = 0; i < jsonModules.length(); i++) {
                        ModelModule mod=new ModelModule((JSONObject)jsonModules.get(i));
                        modules.add(mod);
                        moduleNames[moduleNames.length]=mod.getTitle();

                    }
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        getActivity().setTitle(R.string.add_title);

        root= inflater.inflate( R.layout.fragment_add_to_timetable, container, false);

        addNew= (Button) root.findViewById(R.id.Add);

        Context c = getActivity().getApplicationContext();

        Resources resources=getActivity().getResources();


        //Get String arrays for Spinners and Autocompletes
        roomTypes=resources.getStringArray(R.array.roomTypes);

        //need to load modules here
        //ModuleChoice=resources.getStringArray(R.array.ModuleChoices);

        rooms=resources.getStringArray(R.array.Rooms);
        dates=resources.getStringArray(R.array.Date);
        times=resources.getStringArray(R.array.Time);


        //Set Adapters
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String> (c, R.layout.spinner_item, roomTypes);

        roomTypeSpinner = (Spinner) root.findViewById(R.id.completeType);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String> (c, R.layout.spinner_item, moduleNames);
        ModuleChoiceView = (AutoCompleteTextView) root.findViewById(R.id.completeModule);

        ArrayAdapter<String> adapter3 = new ArrayAdapter<String> (c, R.layout.spinner_item, rooms);
        roomView= (AutoCompleteTextView) root.findViewById(R.id.completeRoom);

        ArrayAdapter<String> adapter4 = new ArrayAdapter<String> (c, R.layout.spinner_item, dates);
        DateView= (Spinner) root.findViewById(R.id.DATE);

        ArrayAdapter<String> adapter5 = new ArrayAdapter<String> (c, R.layout.spinner_item, times);
        TimeView= (Spinner) root.findViewById(R.id.TIME);


        //Add Adapters to DropDown Views
        roomTypeSpinner.setAdapter(adapter1);
        ModuleChoiceView.setAdapter(adapter2);
        roomView.setAdapter(adapter3);
        DateView.setAdapter(adapter4);
        TimeView.setAdapter(adapter5);

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

        //Send API request
        HashMap<String, String> request = new HashMap<String, String>();
        request.put("method", "module");
        request.put("action", "getall");
        api = new APIClass(getActivity(), new ModuleCallback());
        api.execute(request);

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
