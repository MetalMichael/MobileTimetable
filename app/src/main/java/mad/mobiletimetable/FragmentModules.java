package mad.mobiletimetable;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import de.timroes.android.listview.EnhancedListView;

/**
 * Created by Michael on 30/11/2014.
 */

public class FragmentModules extends Fragment {

    private AdapterModules mAdapter;
    private EnhancedListView mListView;

    private APIClass api;
    private boolean active = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Make sure we display an options menu
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Create our options menu with add module button
        inflater.inflate(R.menu.modules, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        //Find out which item was selected
        switch(item.getItemId()) {
            case R.id.action_new_module:
                //Start activity to add a new module
                Intent intent = new Intent(getActivity(), ActivityEditModule.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        //Set the title
        getActivity().setTitle(R.string.modules);


        //Refresh the icons
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
        //Ensure we're not responding to any old requests
        active = false;
        if(api != null) {
            api.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    public void onStop() {
        //Discard "deleted" items
        if(mListView != null) {
            mListView.discardUndo();
        }
        super.onStop();
    }

    private class Callback implements OnTaskCompleted {
        @Override
        public void onTaskCompleted(JSONObject result) {
            if(result.has("modules")) {
                //Extract modules info
                ArrayList<ModelModule> modules = new ArrayList<ModelModule>();
                try{
                    //Get module info from JSON String
                    JSONArray jsonModules = result.getJSONArray("modules");
                    //For each JSON Module
                    for(int i = 0; i < jsonModules.length(); i++) {
                        //Create a new model and store it
                        modules.add(new ModelModule((JSONObject)jsonModules.get(i)));
                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                }
                //Empty list and add new modules
                mAdapter.clear();
                mAdapter.addAll(modules);
            }
        }
    }

    //Permanently delete a module (and events)
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

        //Create Modules Adapter with empty data set
        mAdapter = new AdapterModules(getActivity(), new ArrayList<ModelModule>());
        mListView.setAdapter(mAdapter);

        //Set what happens for delete and undo
        mListView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {
                //The deleted item
                final ModelModule item = mAdapter.getItem(position);
                //Remove from list
                mAdapter.remove(position);
                return new EnhancedListView.Undoable() {
                    @Override
                    public void undo() {
                        //Put the item back in our list
                        mAdapter.insert(position, item);
                    }

                    @Override
                    public String getTitle() {
                        //Tell the user what was deleted
                        return "Deleted '" + item.getCode() + "'";
                    }

                    @Override
                    public void discard() {
                        //Permanently delete item
                        deleteItem(item);
                    }
                };
            }
        });

        // Go to edit activity when item has been clicked on
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ActivityEditModule.class);
                intent.putExtra("moduleid", (String)view.getTag());
                startActivity(intent);
            }
        });

        //Tell the listView which part to swipe
        mListView.setSwipingLayout(R.id.swiping_layout);

        //Set the style of undo (stacking)
        EnhancedListView.UndoStyle style = EnhancedListView.UndoStyle.MULTILEVEL_POPUP;
        mListView.setUndoStyle(style);

        //Enable swipe to delete
        mListView.enableSwipeToDismiss();

        //Set the swipe direction
        EnhancedListView.SwipeDirection direction = EnhancedListView.SwipeDirection.BOTH;
        mListView.setSwipeDirection(direction);

        return mListView;
    }

}