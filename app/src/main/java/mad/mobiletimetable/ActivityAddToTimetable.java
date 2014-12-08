package mad.mobiletimetable;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;


public class ActivityAddToTimetable extends FragmentActivity
        implements FragmentTimetableDay.OnFragmentInteractionListener, FragmentAddToTimetable.OnFragmentInteractionListener {

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_timetable);

        if(savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                .add(R.id.container, new FragmentAddToTimetable()).commit();
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
}
