package mad.mobiletimetable;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by M on 01/12/2014.
 */
public class OnSwipeListener implements View.OnTouchListener{
    private GestureDetector detector;
    public OnSwipeListener(Context c){
        detector = new GestureDetector(c, new GestureListener());
    }
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        return detector.onTouchEvent(motionEvent);
    }
}
