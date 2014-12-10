package mad.mobiletimetable;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


public class ActivityMain extends FragmentActivity
        implements FragmentTimetableDay.OnFragmentInteractionListener, FragmentAddToTimetable.OnFragmentInteractionListener {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mDrawItems;
    private boolean drawerOpen = false;
    
    //Start with no fragment loaded
    private int selectedIndex = 99;

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getActionBar();

        //Service
        ServiceNotifications.ensureRunning(this.getApplicationContext());

        mTitle = mDrawerTitle = getTitle();
        mDrawItems = getResources().getStringArray(R.array.drawer_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the modules content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new AdapterDrawer(this, mDrawItems));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                drawerOpen = false;
                setTitle(mTitle, false);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                drawerOpen = true;
                setTitle(mDrawerTitle, false);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    @Override
    protected void onResume() {
        mDrawerList.setItemChecked(selectedIndex, true);
        super.onResume();
    }

    private void selectItem(int position) {
        if(selectedIndex == position) return;

        Fragment frag = null;
        Intent intent;

        switch(position) {
            case 0:
                frag = new FragmentTimetable();
                break;
            case 1:
                frag = new FragmentModules();
                break;
            case 2:
                intent = new Intent(this, ActivityAddToTimetable.class);
                this.startActivity(intent);
                break;
            default:
                intent = new Intent(this, ActivitySettings.class);
                this.startActivity(intent);
        }
        if(frag != null) {
            selectedIndex = position;

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, frag)
                    .commit();
            mDrawerList.setItemChecked(position, true);
        }

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        setTitle(title, true);
    }

    private void setTitle(CharSequence title, boolean setTitle) {
        if(setTitle) {
            mTitle = title;
        }
        if(!drawerOpen || !setTitle) {
            ActionBar ab = getActionBar();
            super.setTitle(title);
            ab.setDisplayShowTitleEnabled(false);
            super.setTitle(mTitle);
            ab.setTitle(title);
            ab.setDisplayShowTitleEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


}
