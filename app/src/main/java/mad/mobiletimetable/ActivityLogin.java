package mad.mobiletimetable;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Benjy on 26/11/2014.
 */
public class ActivityLogin extends Activity{
    public ActivityLogin(){
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_login);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FragmentLogin fragmentLogin = new FragmentLogin();
        fragmentTransaction.replace(android.R.id.content, fragmentLogin);
        fragmentTransaction.commit();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void Skip(View view){
        Intent intent = new Intent(this, ActivityMain.class);
        finish();
        startActivity(intent);
    }
    public void Register(View view){
       FragmentRegister fragmentRegister = new FragmentRegister();
       FragmentManager fragmentManager = getFragmentManager();
       FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
       fragmentTransaction.setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out);
       fragmentTransaction.replace(android.R.id.content, fragmentRegister);
       fragmentTransaction.commit();

    }
    public void Cancel(View view){
        FragmentLogin fragmentLogin = new FragmentLogin();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.card_flip_left_in, R.animator.card_flip_left_out);
        fragmentTransaction.replace(android.R.id.content, fragmentLogin);
        fragmentTransaction.commit();
    }

}


