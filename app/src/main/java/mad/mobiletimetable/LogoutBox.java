package mad.mobiletimetable;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * Created by pigsdofly on 03/12/2014.
 * Mandatory subclass of dialogPreference for logging out
 */
public class LogoutBox extends DialogPreference {
    private final String PREFS_NAME = "MyAuthFile";

    public LogoutBox(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if(positiveResult) {
            callChangeListener(positiveResult);
        }
    }

}
