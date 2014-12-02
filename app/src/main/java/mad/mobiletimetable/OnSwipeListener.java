package mad.mobiletimetable;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Created by M on 01/12/2014.
 */
public class OnSwipeListener implements OnTouchListener {
    private GestureDetector detector;
    public OnSwipeListener(Context c){
        detector = new GestureDetector(c, new GestureListener());
    }
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        return detector.onTouchEvent(motionEvent);
    }
    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int VELOCITY_THRESHOLD = 100;
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if ((e2.getX() - e1.getX()) > 0 && Math.abs(velocityX) > VELOCITY_THRESHOLD) {
                onSwipeRight();
            } else {
                onSwipeLeft();
            }
            return false;
        }
        @Override
        public boolean onDown(MotionEvent e){
            return true;
        }
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }
}
