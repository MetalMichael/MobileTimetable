package mad.mobiletimetable;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class FragmentModules extends Fragment {

    private AdapterModules mAdapter;

    private APIClass api;
    private boolean active = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.modules, menu);
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
    public void onResume() {
        getActivity().setTitle(R.string.modules);

        //Send API request
        HashMap<String, String> request = new HashMap<String, String>();
        request.put("method", "module");
        request.put("action", "getall");
        api = new APIClass(getActivity(), new Callback());
        api.execute(request);

        super.onResume();
    }

    @Override
    public void onDestroy() {
        active = false;
        if(api != null) {
            api.cancel(true);
        }
        super.onDestroy();
    }

    private class Callback implements OnTaskCompleted {
        @Override
        public void onTaskCompleted(JSONObject result) {
            if(result.has("modules")) {
                ArrayList<ModelModule> modules = new ArrayList<ModelModule>();
                try{
                    JSONArray jsonModules = result.getJSONArray("modules");
                    for(int i = 0; i < jsonModules.length(); i++) {
                        modules.add(new ModelModule((JSONObject)jsonModules.get(i)));
                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                }
                mAdapter.clear();
                mAdapter.addAll(modules);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ListView listView = (ListView) inflater.inflate(R.layout.fragment_modules, container, false);

        mAdapter = new AdapterModules(getActivity(), new ArrayList<ModelModule>());
        listView.setAdapter(mAdapter);

        return listView;
    }
}
