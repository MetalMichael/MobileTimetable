package mad.mobiletimetable;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import android.graphics.Bitmap;
import org.json.JSONException;
import org.json.JSONObject;

import mad.mobiletimetable.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

/**
 * Activity for the settings page
 * Editing done by Sam
 */
public class ActivitySettings extends PreferenceActivity {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = true;
    private APIClass api;
    private final String PREFS_NAME = "MyAuthFile";

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setupSimplePreferencesScreen();
    }
    @Override
    public void onDestroy() {
        if(api != null) {
            api.cancel(true);
        }
        super.onDestroy();
    }
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }
    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference("notification_time"));
        SharedPreferences auth = getSharedPreferences(PREFS_NAME, 0);
        if(auth.getString("Auth","").equals("")) {
            addPreferencesFromResource(R.xml.pref_not_auth);
            findPreference("login").setOnPreferenceChangeListener(UserListener);
        } else {
            // Setting listeners for each preference
            addPreferencesFromResource(R.xml.pref_auth);
            findPreference("change_password").setOnPreferenceChangeListener(UserListener);
            findPreference("change_password").setDefaultValue("");
            findPreference("logout").setOnPreferenceChangeListener(UserListener);
            findPreference("display_picture").setOnPreferenceChangeListener(UserListener);
        }

    }

    /** {@inheritDoc} */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /** {@inheritDoc} */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * Click listener for the change display pic preference

    private Preference.OnPreferenceClickListener ClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {

            return true;
        }

    };*/

    /**
     * Saves camera image to local storage, to be used as a display pic.
     * Uploaded image is saved to local storage with the name user-[auth]-pic and encoded to PNG.
     * To load it again, decode it from png to bitmap
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode >= 1 && resultCode == RESULT_OK) {
            Bitmap imageBitmap;
            SharedPreferences auth = getSharedPreferences(PREFS_NAME, 0);
            if(requestCode == 1) {
                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get("data");

            } else if(requestCode == 2) {
                try {
                    Uri imageUri = Uri.parse(data.getDataString());
                    ContentResolver cr = getContentResolver();
                    InputStream in = cr.openInputStream(imageUri);
                    imageBitmap = BitmapFactory.decodeStream(in,null,new BitmapFactory.Options());

                } catch(Exception e) {
                    e.printStackTrace();
                    return;

                }


            } else
                return;

            File displayPicFile = new File(ActivitySettings.this.getFilesDir(), "user-" +
                                  auth.getString("Auth", "") + "-pic");
            FileOutputStream outputStream;

            try {
                outputStream = new FileOutputStream(displayPicFile);
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
                Toast.makeText(getApplicationContext(), "Image saved!", Toast.LENGTH_SHORT).show();

                // Update drawer
                ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
                String[] mDrawItems = getResources().getStringArray(R.array.drawer_items);
                mDrawerList.setAdapter(new AdapterDrawer(getApplicationContext(),mDrawItems));

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Image upload failed!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(),"No image saved.",Toast.LENGTH_SHORT ).show();

        }

    }

    /**
     * Preference change listener for changing password and logging out
     * separate from other listener because static
     */
    private Preference.OnPreferenceChangeListener UserListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if(preference.getTitle().equals("Change Display Picture")) {
                if(stringValue.equals("1")) {
                    Intent pictureUpload = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (pictureUpload.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(pictureUpload, 1);
                    }
                } else if(stringValue.equals("2")) {
                    Intent pictureUpload = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    startActivityForResult(pictureUpload,2);
                }
            } else if (preference.getTitle().equals("Change Password")) {
                if (stringValue.length() >= 6 && stringValue.length() <= 20) {
                    Log.d("Password",stringValue);
                    requests(stringValue);

                } else
                    Toast.makeText(getApplicationContext(),"Password must be between 6 and 20 chars",Toast.LENGTH_SHORT ).show();

            } else if(preference.getTitle().equals("Logout")) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.clear();
                editor.commit();

                Toast.makeText(getApplicationContext(),"Logged out!",Toast.LENGTH_SHORT ).show();

                Intent intent = new Intent(ActivitySettings.this, ActivityLogin.class);
                startActivity(intent);

            } else if(preference.getTitle().equals("Login")) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.clear();
                editor.commit();

                Intent intent = new Intent(ActivitySettings.this, ActivityLogin.class);
                startActivity(intent);
            }
            return true;
        }
    };
    /**
     * Preference change listener for listPreference default value
     * mainly generated code
     */
    private static Preference.OnPreferenceChangeListener PreferenceListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            }
            return true;
        }
    };

    /**
     * API Callback class
     */

    static class Callback implements OnTaskCompleted{
        @Override
        public void onTaskCompleted(JSONObject result) {
            try {
                result.get("error");
                Log.d("Error!",result.toString());

            } catch(JSONException e) {
               Log.d("Success~",result.toString());

            }
        }
    }

    private void requests(String stringValue) {
        HashMap<String,String> request = new HashMap<String, String>();
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME,0);
        sharedPreferences.getString("Auth","");

        request.put("method","user");
        request.put("action","editpassword");
        request.put("password",stringValue);

        api = new APIClass(ActivitySettings.this,new Callback());
        api.execute(request);

    }
    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #PreferenceListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(PreferenceListener);

        // Trigger the listener immediately with the preference's
        // current value.
        PreferenceListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_general);
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            findPreference("change_password").setOnPreferenceChangeListener(PreferenceListener);
            findPreference("display_picture").setOnPreferenceChangeListener(PreferenceListener);
            bindPreferenceSummaryToValue(findPreference("notification_time"));

        }
    }


}
