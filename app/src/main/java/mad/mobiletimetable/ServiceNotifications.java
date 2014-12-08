package mad.mobiletimetable;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.*;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Michael on 07/12/2014.
 */
public class ServiceNotifications extends IntentService {

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    //Static Method to ensure that this service is running.
    public static void ensureRunning(Context context) {
        if(!isServiceRunning(context)) {
            Intent intent = new Intent(context, ServiceNotifications.class);
            context.startService(intent);
        }
    }

    //Check if this service is running, statically
    private static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ServiceNotifications.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private final class ServiceHandler extends Handler {
        private APIClassBase api;
        HashMap<String, String> request;
        SharedPreferences pref;

        public ServiceHandler(Looper looper) {
            super(looper);

            request = new HashMap<String, String>();
            request.put("method", "api");
            request.put("action", "getnotifications");
            pref = getSharedPreferences("MyAuthFile", 0);
        }

        private class Callback implements OnTaskCompleted {
            public void onTaskCompleted(JSONObject response) {
                String status;
                try {
                    status = response.getString("status");
                } catch(JSONException e) {
                    e.printStackTrace();
                    return;
                }
                if(status == "OK") {
                    //
                }
                mServiceHandler.sendMessageDelayed(Message.obtain(), 60*1000);
            }
        }

        public void handleMessage(Message msg) {
            checkNotifications();
        }

        private void checkNotifications() {
            if(api != null && api.getStatus() == AsyncTask.Status.RUNNING) {
                return;
            }

            api = new APIClassBase(getApplicationContext(), new Callback());

            //request.put("notificationtime", pref.getString("notification_time", "never"));
            api.execute(request);
        }
    }

    public ServiceNotifications() {
        super("ServiceNotifications");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceHandler.sendMessage(Message.obtain());

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

    }
}
