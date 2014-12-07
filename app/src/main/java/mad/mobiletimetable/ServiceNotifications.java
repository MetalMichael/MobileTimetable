package mad.mobiletimetable;

import android.app.IntentService;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Michael on 07/12/2014.
 */
public class ServiceNotifications extends IntentService {

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private final class ServiceHandler extends Handler {
        private APIClassBase api;

        public ServiceHandler(Looper looper) {
            super(looper);

            api = new APIClassBase(getApplicationContext(), new Callback());
            HashMap<String, String> request = new HashMap<String, String>();
            request.put("method", "api");
            api.execute();
        }

        private class Callback implements OnTaskCompleted {
            public void onTaskCompleted(JSONObject response) {
                Log.d("ServiceNotifications", "completed");
            }
        }

        public void handleMessage(Message msg) {
            while(true) {
                checkNotifications();

                try {
                    wait(60);
                } catch (Exception e) {

                }
            }
        }

        private void checkNotifications() {
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
