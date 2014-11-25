package mad.mobiletimetable;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

public class FragmentModules extends Fragment {

    private AdapterModules mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: API Request
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ListView listView = (ListView) inflater.inflate(R.layout.fragment_modules, container, false);

        ArrayList<ModelModule> dummyModules = new ArrayList<ModelModule>();
        ModelModule m;
        for(int i = 0; i < 10; i++) {
            m = new ModelModule();
            dummyModules.add(m);
        }

        mAdapter = new AdapterModules(getActivity(), dummyModules);
        listView.setAdapter(mAdapter);

        return listView;
    }
}
