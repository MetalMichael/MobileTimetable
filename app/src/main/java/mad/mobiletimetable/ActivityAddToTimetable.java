package mad.mobiletimetable;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;


public class ActivityAddToTimetable extends FragmentActivity
        implements FragmentTimetableDay.OnFragmentInteractionListener, FragmentAddToTimetable.OnFragmentInteractionListener {

    FragmentAddToTimetable fragment;
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_timetable);

        if(savedInstanceState == null) {
            fragment = new FragmentAddToTimetable();
            getFragmentManager().beginTransaction()
                .add(R.id.container, fragment).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Back button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, intent);
        }
    }
}
