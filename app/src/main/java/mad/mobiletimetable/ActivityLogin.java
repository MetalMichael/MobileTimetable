package mad.mobiletimetable;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceScreen;
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

import java.net.URL;
import java.util.HashMap;


/**
 * Created by Benjy on 26/11/2014.
 */

public class ActivityLogin extends Activity{
    public static final String PREFS_NAME = "MyAuthFile";
    private boolean authenticated;
    private boolean registered;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences authCheck = getSharedPreferences(PREFS_NAME, 0);
        if(authCheck.getString("Auth", "").equals("")) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            FragmentLogin fragmentLogin = new FragmentLogin();
            fragmentTransaction.replace(android.R.id.content, fragmentLogin);

            fragmentTransaction.commit();
        }else{
            Intent intent = new Intent(this, ActivityMain.class);
            finish();
            startActivity(intent);
        }
    }
    public void Skip(View view){
        Intent intent = new Intent(this, ActivityMain.class);
        finish();
        startActivity(intent);
    }
    public void RegisterFragment(View view){
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

        class Callback implements OnTaskCompleted {
            @Override
            public void onTaskCompleted(JSONObject result) {


                Log.d("Resulting Request", result.toString());
                try {
                    String status = result.getString("status");

                    if (status.equals("error")) {
                        authenticated = false;
                        String authentication = "not Authenticated";
                        Log.d("Authentication", authentication);
                    } else {
                        authenticated = true;
                        String auth;
                        auth = result.getString("auth");
                        Log.d("Authentication", auth);
                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("Auth", auth);
                        editor.commit();
                    }

                    Log.d("Resulting Request", status);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!authenticated) {
                    Context context = getApplicationContext();
                    CharSequence text = "Username or Password incorrect";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                } else {

                    Intent intent = new Intent(ActivityLogin.this, ActivityMain.class);
                    finish();
                    startActivity(intent);
                }
            }
        }
        HashMap<String,String> request = new HashMap<String, String>();
        request.put("method","user");
        request.put("action","auth");
        request.put("username",username.getText().toString());
        request.put("password",password.getText().toString());

        new APIClass(getApplicationContext(),new Callback()).execute(request);


    }
    public  void Register(View view) {

        EditText username = (EditText) findViewById(R.id.usernameField);
        EditText password = (EditText) findViewById(R.id.passwordField);
        EditText email = (EditText) findViewById(R.id.emailField);

        String mUsername = username.getText().toString();
        String mPassword = password.getText().toString();
        String mEmail = email.getText().toString();

        class Callback implements OnTaskCompleted {
            @Override
            public void onTaskCompleted(JSONObject result) {

                Log.d("Resulting Request", result.toString());
                try {
                    String status = result.getString("status");
                    if (status.equals("error")) {

                        registered = false;
                    } else {

                        String auth = result.getString("auth");
                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("Auth", auth);
                        editor.commit();
                        registered = true;

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!registered) {
                    String error = null;
                    try {
                        error = result.getString("error");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(),error, Toast.LENGTH_SHORT).show();

                } else {
                    String registered = "you are now registered";
                    Toast.makeText(getApplicationContext(), registered, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ActivityLogin.this, ActivityLogin.class);
                    finish();
                    startActivity(intent);
                }
            }
        }

        HashMap<String, String> request = new HashMap<String, String>();
        request.put("method", "user");
        request.put("action", "create");
        request.put("username", mUsername);
        request.put("password", mPassword);
        request.put("email", mEmail);
        new APIClass(getApplicationContext(), new Callback()).execute(request);
    }
}







