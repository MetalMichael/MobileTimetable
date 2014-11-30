package mad.mobiletimetable;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

public class FragmentModules extends Fragment {

    private AdapterModules mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        //TODO: API Request

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem add = menu.add("add");
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        add.setIcon(R.drawable.ic_action_new);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_new_module:
                Intent intent = new Intent(getActivity(), ActivityEditModule.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
