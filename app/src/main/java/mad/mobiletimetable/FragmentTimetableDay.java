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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;


public class FragmentTimetableDay extends Fragment {
    private AdapterTimetable mAdapter;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DAY_NUM = "me.mobiletimetable.fragmenttimetableday";

    // TODO: Rename and change types of parameters
    private int day;
    private String dayName;

    private OnFragmentInteractionListener mListener;

    // TODO: Rename and change types and number of parameters
    public static FragmentTimetableDay newInstance(int day) {
        FragmentTimetableDay fragment = new FragmentTimetableDay();
        // Get arguments
        Bundle args = new Bundle();
        args.putInt(ARG_DAY_NUM, day);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentTimetableDay() {
        // Required empty public constructor
    }

    public void onResume() {
        getActivity().setTitle(R.string.app_name);
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //mDayName = getArguments().getString(ARG_PARAM1);
        day = getArguments().getInt(ARG_DAY_NUM);
        String[] dayNames = getResources().getStringArray(R.array.Date);
        dayName = dayNames[day];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View timetable = inflater.inflate(R.layout.fragment_timetable_day, container, false);

        TextView dayTextView = (TextView) timetable.findViewById(R.id.day_name);
        dayTextView.setText(dayName);

        HashMap<String,String> request = new HashMap<String, String>();
        request.put("method","timetable");
        request.put("action","getall");
        request.put("day", Integer.toString(day+1));
        new APIClass(getActivity(), new Callback()).execute(request);

        // Inflate the layout for this fragment
        mAdapter = new AdapterTimetable(getActivity(), new ArrayList<ModelEvent>());

        ListView list = (ListView) timetable.findViewById(R.id.day_list);
        list.setAdapter(mAdapter);
        return timetable;
    }

    class Callback implements OnTaskCompleted{
        @Override
        public void onTaskCompleted(JSONObject result) {

            ArrayList<ModelEvent> events = new ArrayList<ModelEvent>();
            ArrayList<ModelEvent> completeEvents = new ArrayList<ModelEvent>();
            try{
                if(result.length()!=0) {
                    JSONArray jsonEvents = result.getJSONArray("events");
                    int counter = 0;
                    for (int i = 0; i < jsonEvents.length(); i++) {
                        events.add(new ModelEvent((JSONObject) jsonEvents.get(i)));
                        Log.d("events", events.get(i).getTime().toString());
                    }
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
            try {
                int counter = 0;
                int durationCounter = 0;
                final String[] times = {"08:00","09:00","10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00","18:00","19:00","20:00","21:00"};
                for(int i = 0; i < 14; i++) {



                    if(events.size()>0 && counter < events.size()) {

                        Log.d("i", Integer.toString(i));
                        Log.d("events time", events.get(counter).getDate());
                        Log.d("turns times", times[i]);
                        Log.d("duration counter", Integer.toString(durationCounter));



                        if ((events.get(counter).getDate().equals(times[i])) || (durationCounter>0)){
                            ModelEvent currentEvent = new ModelEvent(events.get(counter).getId(),events.get(counter).getModuleId(),events.get(counter).getDuration(),events.get(counter).getDay(),events.get(counter).getTime(),events.get(counter).getModule(),events.get(counter).getLocation(),events.get(counter).getLessonType());
                            completeEvents.add(currentEvent);
                            if(events.get(counter).getDuration()-1 == durationCounter) {
                                completeEvents.get(i).changeTime(times[i]);
                                counter++;
                                durationCounter = 0;
                            }else{
                                completeEvents.get(i).changeTime(times[i]);
                                durationCounter++;
                            }

                        } else {
                            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                            completeEvents.add(new ModelEvent(day, df.parse(times[i])));
                            Log.d("skipped", "skippy");
                        }
                    }else{
                        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                        completeEvents.add(new ModelEvent(day, df.parse(times[i])));
                    }
                }
            }catch(ParseException e) {
                e.printStackTrace();
            }



            mAdapter.clear();
            mAdapter.addAll(completeEvents);
        }
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
