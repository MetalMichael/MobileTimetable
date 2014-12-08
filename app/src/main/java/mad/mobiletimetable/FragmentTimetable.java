package mad.mobiletimetable;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import java.util.Calendar;

public class FragmentTimetable extends Fragment {

    private FragmentTimetableDay currentFragment;

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
        switch(item.getItemId()) {
            case R.id.action_add_timetable:
                Intent intent = new Intent(getActivity(), ActivityAddToTimetable.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check to see if we're in the tablet landscape viokkkew
        View timetable = inflater.inflate(R.layout.fragment_timetable, container, false);
        return timetable;
    }
    //for day changes
    public class GlobalInt {
        public int dayOfWeek = 0;
        public int day;
        public void GlobalInt() {
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //API

        if(view.findViewById(R.id.fullTimetable) != null) {

            int[] dayFragments = {
                    R.id.mondayFragment,
                    R.id.tuesdayFragment,
                    R.id.wednesdayFragment,
                    R.id.thursdayFragment,
                    R.id.fridayFragment
            };
            for(int i = 0; i < 5 ; i++) {
                addDayFragment(i,dayFragments[i]);
            }

        } else {
        final GlobalInt global = new GlobalInt();
        addDayFragment(global.returnDay(),R.id.dayFragment);
        View myView = view.findViewById(R.id.dayFragment);

        myView.setOnTouchListener(new OnSwipeListener(getActivity()) {
            @Override
            public void onSwipeLeft() {
                // Load day - 1
                Log.d("Resulting Request", "right");
                addDayFragment(global.increment(),R.id.dayFragment);
                //View myView = getActivity().findViewById(R.id.dayFragment);
            }

            @Override
            public void onSwipeRight() {
                // Load day + 1
                Log.d("Resulting Request", "left");
                addDayFragment(global.decrement(),R.id.dayFragment);
                //View myView = getActivity().findViewById(R.id.dayFragment);
            }
        });
        }
    }

    private void addDayFragment(int day, int dayID){
        currentFragment = FragmentTimetableDay.newInstance(day);
        getFragmentManager().beginTransaction().replace(dayID, currentFragment).commit();
    }

}
