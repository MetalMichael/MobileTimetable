package mad.mobiletimetable;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.*;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

            //Get Notifications from API
            request = new HashMap<String, String>();
            request.put("method", "api");
            request.put("action", "getnotifications");

            pref = getSharedPreferences("MyAuthFile", 0);
        }

        private class Callback implements OnTaskCompleted {
            public void onTaskCompleted(JSONObject response) {
                String status;
                //Get API Response Status
                try {
                    status = response.getString("status");
                } catch(JSONException e) {
                    e.printStackTrace();
                    return;
                }
                if(status.equals("OK")) {
                    try {
                        //Load events
                        JSONArray jsonEvents = response.getJSONArray("events");
                        ArrayList<ModelEvent> events = new ArrayList<ModelEvent>();
                        for(int i = 0; i < jsonEvents.length(); i++) {
                            events.add(new ModelEvent((JSONObject)jsonEvents.get(i)));
                        }
                        //If we have events, process them
                        if(events.size() > 0) {
                            processEvents(events);
                        }
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                }
                //Start again in 1 minute
                mServiceHandler.sendMessageDelayed(Message.obtain(), 60*1000);
            }
        }

        private void processEvents(ArrayList<ModelEvent> events) {
            ModelEvent event;
            String title;
            String detail;
            String day;
            if(events.size() == 1) {
                //Simple Notification
                event = events.get(0);
                title = "Event Starting Soon";
                detail = "(" + event.getDate() + ") " + event.getModule().getTitle() + " - " +
                        event.getModule().getCode() + " is starting soon";
            } else {
                //Multiple Events Notification
                title = "Upcoming Events";
                detail = Integer.toString(events.size()) + " Events Starting Soon";
            }

            //Build notification
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic_activity_add_to_timetable)
                            .setContentTitle(title)
                            .setContentText(detail)
                            .setAutoCancel(true);

            //Build what happens when we click on the notification
            Intent resultIntent = new Intent(getApplicationContext(), ActivityMain.class);

            //Build back stack
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            //stackBuilder.addParentStack(ActivityAddToTimetable.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            //Set intent
            mBuilder.setContentIntent(resultPendingIntent);

            //Get notification service
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            //Send our notification
            mNotificationManager.notify(0, mBuilder.build());

        }

        public void handleMessage(Message msg) {
            checkNotifications();
        }

        private void checkNotifications() {
            //If API is already running, do nothing
            if(api != null && api.getStatus() == AsyncTask.Status.RUNNING) {
                return;
            }

            //Send API Request
            api = new APIClassBase(getApplicationContext(), new Callback());
            //Set our preference for when we want notifications
            request.put("notificationtime", pref.getString("notification_time", ""));
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
        //Run on background thread
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceHandler.sendMessage(Message.obtain());
        //Ensure it runs continuously
        return START_STICKY;
    }

    @Override
    //Not using binding
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

    }
}
