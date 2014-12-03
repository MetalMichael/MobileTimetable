package mad.mobiletimetable;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.util.Log;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;

public class FragmentTimetable extends Fragment {
    public FragmentTimetable() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: Initiate API Requests etc.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View timetable = inflater.inflate(R.layout.fragment_fragment_timetable, container, false);
        //Check to see if we're in the tablet landscape view

        return timetable;
    }
    //for day changes
    public class GlobalInt {
        public int dayIncrement = 0;
        public int increment(){
            this.dayIncrement ++;
            return dayIncrement;
        }
        public int decrement(){
            this.dayIncrement --;
            return dayIncrement;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Toast.makeText(getActivity(),"Ahoy there!",Toast.LENGTH_SHORT).show();

        if(getView().findViewById(R.id.fullTimetable) != null) {
            String[] dayNames = {"Monday","Tuesday","Wednesday","Thursday","Friday"};
            int[] dayFragments = {
                    R.id.mondayFragment,
                    R.id.tuesdayFragment,
                    R.id.wednesdayFragment,
                    R.id.thursdayFragment,
                    R.id.fridayFragment
            };
            for(int i = 0; i < dayNames.length ; i++) {
                addDayFragment(dayNames[i],dayFragments[i]);

            }
            class Callback implements OnTaskCompleted{
                @Override
                public void onTaskCompleted(JSONObject result) {
                    Toast.makeText(getActivity(),"WORKED",Toast.LENGTH_SHORT).show();
                    Log.d("Resulting Request",result.toString());
                }
            }
            HashMap<String,String> request = new HashMap<String, String>();
            request.put("method","module");
            request.put("action","getall");
            request.put("auth","debug");
            new APIClass(getActivity().getApplicationContext(),new Callback()).execute(request);
        } else {
            Calendar c = Calendar.getInstance();
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            int day = 1;
            switch(dayOfWeek){
                case Calendar.SUNDAY:
                    day = 0;
                    break;
                case Calendar.MONDAY:
                    day = 0;
                    break;
                case Calendar.TUESDAY:
                    day = 1;
                    break;
                case Calendar.WEDNESDAY:
                    day = 2;
                    break;
                case Calendar.THURSDAY:
                    day = 3;
                    break;
                case Calendar.FRIDAY:
                    day = 4;
                    break;
                case Calendar.SATURDAY:
                    day = 0;
                    break;
            }
            final int today = day;
            final String[] dayNames = {"Monday","Tuesday","Wednesday","Thursday","Friday"};
            addDayFragment(dayNames[day], R.id.dayFragment);
            View myView = getActivity().findViewById(R.id.dayFragment);
            final GlobalInt global = new GlobalInt();


            myView.setOnTouchListener(new OnSwipeListener(getActivity()) {
                @Override
                public void onSwipeLeft() {
                    // Load day - 1
                    Log.d("Resulting Request","right");
                    addDayFragment(dayNames[today+(global.increment())], R.id.dayFragment);
                    View myView = getActivity().findViewById(R.id.dayFragment);
                }
                @Override
                public void onSwipeRight() {
                    // Load day + 1
                    Log.d("Resulting Request","left");
                    addDayFragment(dayNames[today+(global.decrement())], R.id.dayFragment);
                    View myView = getActivity().findViewById(R.id.dayFragment);
                }
            });
        }
    }
    private void addDayFragment(String dayName, int dayFragmentID){
        FragmentTimetableDay fragment = FragmentTimetableDay.newInstance(dayName);
        FragmentTransaction fTransaction = getFragmentManager().beginTransaction();
        fTransaction.replace(dayFragmentID, fragment);
        fTransaction.commit();
    }
}
