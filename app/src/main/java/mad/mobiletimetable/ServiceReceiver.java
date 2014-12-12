package mad.mobiletimetable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Michael on 07/12/2014.
 */
public class ServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //This receives broadcasts (e.g phone booting).
        //Ensure our service is running
        ServiceNotifications.ensureRunning(context);
    }
}
