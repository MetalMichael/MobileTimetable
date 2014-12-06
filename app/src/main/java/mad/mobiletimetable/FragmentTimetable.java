package mad.mobiletimetable;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class FragmentTimetable extends Fragment {

    public FragmentTimetable() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); //allows menu
        //TODO: Initiate API Requests etc.
    }

    @Override

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.timetable, menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, new FragmentAddToTimetable())
                .commit();
        return true;
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check to see if we're in the tablet landscape viokkkew
        View timetable = inflater.inflate(R.layout.fragment_fragment_timetable_day, container, false);
        return timetable;
    }
    //for day changes
    public class GlobalInt {
        public int dayOfWeek = 0;
        public int day;
        GlobalInt() {
            Calendar c = Calendar.getInstance();
            this.dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            switch (this.dayOfWeek) {
                case Calendar.SUNDAY:
                    this.day = 0;
                    break;
                case Calendar.MONDAY:
                    this.day = 0;
                    break;
                case Calendar.TUESDAY:
                    this.day = 1;
                    break;
                case Calendar.WEDNESDAY:
                    this.day = 2;
                    break;
                case Calendar.THURSDAY:
                    this.day = 3;
                    break;
                case Calendar.FRIDAY:
                    this.day = 4;
                    break;
                case Calendar.SATURDAY:
                    this.day = 0;
                    break;
            }
            Log.d("Construct",String.valueOf(this.day));
        }
        public int returnDay() {
            Log.d("returnDay",String.valueOf(this.day));
            return this.day;
        }
        public int increment(){
            this.day ++;
            if(this.day >4){
                this.day = 0;
            }
            return this.day;
        }
        public int decrement(){
            this.day --;
            if(this.day <0){
                this.day = 4;
            }
            return this.day;
        }
    }
    /*class EventsArray {
        private ModelEvent[] array;

        public void Add(ModelEvent  x){
            this.array[this.array.length] = x;
        }

        public ModelEvent Get(int x){
            return this.array[x];
        }

        public ModelEvent[] getAll(){
            return this.array;
        }

        public int getLength(){
            return this.array.length;
        }
    }*/
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //API



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

        } else {
            final GlobalInt global = new GlobalInt();
            final String[] dayNames = {"Monday","Tuesday","Wednesday","Thursday","Friday"};
            addDayFragment(dayNames[global.returnDay()], R.id.dayFragment);
            View myView = getActivity().findViewById(R.id.dayFragment);

            myView.setOnTouchListener(new OnSwipeListener(getActivity()) {
                @Override
                public void onSwipeLeft() {
                    // Load day - 1
                    Log.d("Resulting Request","right");
                    addDayFragment(dayNames[global.increment()], R.id.dayFragment);
                    View myView = getActivity().findViewById(R.id.dayFragment);
                }
                @Override
                public void onSwipeRight() {
                    // Load day + 1
                    Log.d("Resulting Request","left");
                    addDayFragment(dayNames[global.decrement()], R.id.dayFragment);
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
