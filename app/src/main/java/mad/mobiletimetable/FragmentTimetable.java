package mad.mobiletimetable;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
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

    private int getToday() {
        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int day;
        switch (dayOfWeek) {
            case Calendar.TUESDAY:
                return 1;
            case Calendar.WEDNESDAY:
                return 2;
            case Calendar.THURSDAY:
                return 3;
            case Calendar.FRIDAY:
                return 4;
            default:
                return 0;
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
            SharedPreferences pref = getActivity().getSharedPreferences("day", Activity.MODE_PRIVATE);

            Log.d("FT", Integer.toString(pref.getInt("day", -1)));

            mViewPager = (ViewPager) view.findViewById(R.id.pager);
            mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    updateCurrentPage(position);
                }

                @Override
                public void onPageSelected(int i) {
                    updateCurrentPage(i);
                }

                @Override
                public void onPageScrollStateChanged(int i) {

                }
            });
            collectionPagerAdapter = new CollectionPagerAdapter(getFragmentManager());
            mViewPager.setAdapter(collectionPagerAdapter);
            mViewPager.setCurrentItem(pref.getInt("day", getToday() + collectionPagerAdapter.LOOPS_COUNT/2));
        }
    }

    public void updateCurrentPage(int page) {
        SharedPreferences pref = getActivity().getSharedPreferences("day", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("day", page).commit();
    }

    private void addDayFragment(int day, int dayID){
        currentFragment = FragmentTimetableDay.newInstance(day);
        getFragmentManager().beginTransaction().replace(dayID, currentFragment).commit();
    }


    public class CollectionPagerAdapter extends FragmentStatePagerAdapter {
        //Must be even
        public int LOOPS_COUNT = 100;
        private int ACTUAL_TOTAL = 5;


        public CollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return FragmentTimetableDay.newInstance(i % ACTUAL_TOTAL);
        }

        @Override
        public int getCount() {
            return ACTUAL_TOTAL * LOOPS_COUNT;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            Log.d("FragmentTimetable", "Setting Day: " + dayNames[position % ACTUAL_TOTAL]);
            getActivity().setTitle(dayNames[position % ACTUAL_TOTAL]);
            super.setPrimaryItem(container, position % ACTUAL_TOTAL, object);
        }
    }
}
