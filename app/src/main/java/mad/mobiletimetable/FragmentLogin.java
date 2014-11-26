package mad.mobiletimetable;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Benjy on 26/11/2014.
 */
public class FragmentLogin extends Fragment{
    public FragmentLogin(){
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View login = inflater.inflate(R.layout.fragment_login, container, false);
        return login;
    }
}
