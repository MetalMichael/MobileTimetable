package mad.mobiletimetable;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
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
    private CollectionPagerAdapter collectionPagerAdapter;
    private ViewPager mViewPager;

    private String[] dayNames;

    public FragmentTimetable() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); //allows menu

        dayNames = getResources().getStringArray(R.array.Date);
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
        return inflater.inflate(R.layout.fragment_timetable, container, false);
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

        //Tablet View
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

            mViewPager = (ViewPager) view.findViewById(R.id.pager);
            collectionPagerAdapter = new CollectionPagerAdapter(getFragmentManager());
            mViewPager.setAdapter(collectionPagerAdapter);
        }
    }

    private void addDayFragment(int day, int dayID){
        currentFragment = FragmentTimetableDay.newInstance(day);
        getFragmentManager().beginTransaction().replace(dayID, currentFragment).commit();
    }


    public class CollectionPagerAdapter extends FragmentPagerAdapter {
        public CollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int i) {
            return FragmentTimetableDay.newInstance(i);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            getActivity().setTitle(dayNames[position]);
            super.setPrimaryItem(container, position, object);
        }
    }
}
