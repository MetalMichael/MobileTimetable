package mad.mobiletimetable;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by Michael on 13/11/2014.
 */

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
    protected void onRestart(){
        finish();
        startActivity(getIntent());
        Log.d("Restart","Restarted");
        super.onRestart();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            selectItem(1);
        }
    }

    @Override
    protected void onResume() {
        mDrawerList.setItemChecked(selectedIndex, true);
        super.onResume();
    }

    //Drawer item selected
    private void selectItem(int position) {
        //If the item is already selected, do nothing
        if(selectedIndex == position) return;

        Fragment frag = null;
        Intent intent;

        switch(position) {
            //Display Picture
            case 0:
                //Unset it as checked
                mDrawerList.setItemChecked(position, false);
                //Select the previous item as checked
                mDrawerList.setItemChecked(selectedIndex, true);
                //Do nothing
                return;
            //Timetable
            case 1:
                frag = new FragmentTimetable();
                break;
            //Modules
            case 2:
                frag = new FragmentModules();
                break;
            //Add To Timetable
            case 3:
                intent = new Intent(this, ActivityAddToTimetable.class);
                this.startActivity(intent);
                break;
            //Settings
            default:
                intent = new Intent(this, ActivitySettings.class);
                this.startActivity(intent);
        }
        if(frag != null) {
            //Store active fragment position
            selectedIndex = position;

            //Load fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, frag)
                    .commit();

            //Make sure the item is checked
            mDrawerList.setItemChecked(position, true);
        }
        //Close the drawer
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

    //If the back button is clicked when the drawer is open, close the drawer and nothing else.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && drawerOpen) {
            mDrawerLayout.closeDrawer(mDrawerList);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    //Handle this ourselves (below)
    @Override
    public void setTitle(CharSequence title) {
        setTitle(title, true);
    }

    //Handles changing/storing the title, and allowing it to be swapped out when the drawer is open
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
