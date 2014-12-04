package mad.mobiletimetable;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import de.timroes.android.listview.EnhancedListView;

public class FragmentModules extends Fragment {

    private AdapterModules mAdapter;
    private EnhancedListView mListView;

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

    @Override
    public void onStop() {
        if(mListView != null) {
            mListView.discardUndo();
        }
        super.onStop();
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

    public void deleteItem(ModelModule module) {
        //Send API request
        HashMap<String, String> request = new HashMap<String, String>();
        request.put("method", "module");
        request.put("action", "delete");
        request.put("moduleid", Integer.toString(module.getId()));
        api = new APIClass(getActivity(), new DeleteCallback());
        api.execute(request);
    }

    public class DeleteCallback implements OnTaskCompleted {
        public void onTaskCompleted(JSONObject response) {
            Log.d("FragmentModules", "Module Deleted");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mListView = (EnhancedListView) inflater.inflate(R.layout.fragment_modules, container, false);

        mAdapter = new AdapterModules(getActivity(), new ArrayList<ModelModule>());

        mListView.setAdapter(mAdapter);

        mListView.setDismissCallback(new de.timroes.android.listview.EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {
                final ModelModule item = mAdapter.getItem(position);
                mAdapter.remove(position);
                return new EnhancedListView.Undoable() {
                    @Override
                    public void undo() {
                        mAdapter.insert(position, item);
                    }

                    @Override
                    public String getTitle() {
                        return "Deleted '" + item.getCode() + "'";
                    }

                    @Override
                    public void discard() {
                        deleteItem(item);
                    }
                };
            }
        });

        // Show toast message on click and long click on list items.
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "Clicked on item " + mAdapter.getItem(position), Toast.LENGTH_SHORT).show();
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "Long clicked on item " + mAdapter.getItem(position), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mListView.setSwipingLayout(R.id.swiping_layout);

        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);

        EnhancedListView.UndoStyle style = EnhancedListView.UndoStyle.SINGLE_POPUP;
        //style = EnhancedListView.UndoStyle.MULTILEVEL_POPUP;
        //style = EnhancedListView.UndoStyle.COLLAPSED_POPUP;
        mListView.setUndoStyle(style);

        mListView.enableSwipeToDismiss();

        // Set the swipe direction
        EnhancedListView.SwipeDirection direction = EnhancedListView.SwipeDirection.BOTH;
        mListView.setSwipeDirection(direction);

        mListView.setSwipingLayout(R.id.swiping_layout);

        return mListView;
    }

}