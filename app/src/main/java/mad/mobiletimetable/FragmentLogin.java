package mad.mobiletimetable;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Benjy on 29/11/2014.
 */
public class FragmentLogin extends Fragment {

    private void Skip(){
        Intent intent = new Intent(getActivity(), ActivityMain.class);
        getActivity().finish();
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View loginView = inflater.inflate(R.layout.fragment_login, container, false);
        loginView.setOnTouchListener(new OnSwipeListener(getActivity()) {
            @Override
            public void onSwipeLeft() {
                Skip();
            }
            @Override
            public void onSwipeRight() {
                Skip();
            }
        });
        return loginView;
    }

}
