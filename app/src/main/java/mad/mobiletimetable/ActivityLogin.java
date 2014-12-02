package mad.mobiletimetable;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;


/**
 * Created by Benjy on 26/11/2014.
 */

public class ActivityLogin extends Activity{
    private boolean authenticated;
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

    public void Login(View view){


        EditText username = (EditText)findViewById(R.id.username);

        EditText password = (EditText)findViewById(R.id.userpassword);
        HashMap<String,String> request = new HashMap<String, String>();
        request.put("method","user");
        request.put("action","auth");
        request.put("username",username.getText().toString());
        request.put("password",password.getText().toString());

        new APIClass(getApplicationContext(),new Callback()).execute(request);

         if(!authenticated)   {
            Context context = getApplicationContext();
            CharSequence text = "Username or Password incorrect";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

        }else{
           // TODO: store user authentication
           // TODO: pass user authentication through to server request for stored timetable information

            Intent intent = new Intent(this, ActivityMain.class);
            finish();
            startActivity(intent);
        }
    }
    class Callback implements OnTaskCompleted{
        @Override
        public void onTaskCompleted(JSONObject result) {


            Log.d("Resulting Request", result.toString());
            try {
            String status = result.getString("status");

                    if (status.equals("error")){
                     authenticated = false;
                     String authentication = "not Authenticated";
                     Log.d("Authentication", authentication );
                    }else{
                     authenticated = true;
                        String auth = result.getString("auth");
                        Log.d("Authentication", auth);
                    }

            Log.d("Resulting Request", status);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

}


