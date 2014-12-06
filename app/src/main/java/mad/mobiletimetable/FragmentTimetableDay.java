package mad.mobiletimetable;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentTimetableDay.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentTimetableDay#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentTimetableDay extends Fragment {
    private AdapterTimetable mAdapter;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "dayName";

    // TODO: Rename and change types of parameters
    private String mDayName;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param dayName Parameter 1.
     * @return A new instance of fragment FragmentTimetableDay.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentTimetableDay newInstance(String dayName) {
        FragmentTimetableDay fragment = new FragmentTimetableDay();
        // Get arguments
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, dayName);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentTimetableDay() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDayName = getArguments().getString(ARG_PARAM1);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View timetable = inflater.inflate(R.layout.fragment_fragment_timetable_day, container, false);

        // Set text if dayName supplied
        if(mDayName!=null) {
            TextView dayName = (TextView)timetable.findViewById(R.id.day_name);
            dayName.setText(mDayName);
        }
        class Callback implements OnTaskCompleted{
            @Override
            public void onTaskCompleted(JSONObject result) {
                Toast.makeText(getActivity(), "WORKED", Toast.LENGTH_SHORT).show();
                ArrayList<ModelEvent> events = new ArrayList<ModelEvent>();
                try{

                    JSONArray jsonEvents = result.getJSONArray("events");

                    for(int i = 0; i < jsonEvents.length(); i++) {

                        events.add(new ModelEvent((JSONObject)jsonEvents.get(i)));
                        Log.d("events", events.get(i).getTime().toString());
                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                }
                mAdapter.clear();
                mAdapter.addAll(events);
            }
        }
        HashMap<String,String> request = new HashMap<String, String>();
        request.put("method","timetable");
        request.put("action","getall");
        new APIClass(getActivity(), new Callback()).execute(request);

        // Inflate the layout for this fragment
        mAdapter = new AdapterTimetable(getActivity(), new ArrayList<ModelEvent>());

        ListView list = (ListView) timetable.findViewById(R.id.day_list);
        list.setAdapter(mAdapter);
        return timetable;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
