package mad.mobiletimetable;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class FragmentEditModule extends Fragment implements View.OnClickListener {

    private boolean edit;
    private ModelModule module;

    private APIClass api;
    private boolean active;

    private FrameLayout view;
    private LayoutInflater inflater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getActivity().getIntent();

        //Editing existing module
        if(intent.hasExtra("moduleid")) {
            edit = true;

            //Send API request
            HashMap<String, String> request = new HashMap<String, String>();
            request.put("method", "module");
            request.put("action", "get");
            request.put("moduleid", intent.getStringExtra("moduleid"));
            api = new APIClass(getActivity(), new Callback());
            api.execute(request);
        } else {
            //Creating new module
            //Nothing else to do
            edit = false;
        }
    }

    @Override
    public void onResume() {
        if(edit) {
            setEditTitle();
        }
        super.onResume();
    }

    private void setEditTitle() {
        getActivity().setTitle(R.string.edit_module);
    }


    private class Callback implements OnTaskCompleted {
        @Override
        public void onTaskCompleted(JSONObject result) {
            if(!active) return;

            Log.v("FragmentEditModule", result.toString());
            try {
                JSONObject moduleInfo = result.getJSONObject("module");
                module = new ModelModule(moduleInfo);
            } catch(JSONException e) {
                e.printStackTrace();
            }

            loadEdit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.inflater = inflater;
        view = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);

        View v2;

        //Show loading screen for edit
        if(edit) {
            v2 = inflater.inflate(R.layout.loading, view, false);
        } else {
            v2 = getMainView();
        }

        if(edit) {
            Log.v("FragmentEditModule", "Editing");
        } else {
            Log.v("FragmentEditModule", "Creating");
        }
        view.addView(v2);
        return view;
    }

    private View getMainView() {
        View mainView = inflater.inflate(R.layout.fragment_edit_module, view, false);

        Button button = (Button) mainView.findViewById(R.id.module_create);
        button.setOnClickListener(this);

        return mainView;
    }

    @Override
    public void onClick(View v) {
        api = new APIClass(getActivity(), new CreateEditCallback());

        HashMap<String, String> request = new HashMap<String, String>();
        request.put("method", "module");
        if(edit) {
            request.put("action", "edit");
            request.put("moduleid", Integer.toString(module.getId()));
        } else {
            request.put("action", "create");
        }

        //Fields
        //TODO: Validation
        request.put("moduletitle",
                ((TextView)view.findViewById(R.id.module_title)).getText().toString());
        request.put("modulecode",
                ((TextView)view.findViewById(R.id.module_code)).getText().toString());
        request.put("lecturer",
                ((TextView)view.findViewById(R.id.module_lecturer)).getText().toString());

        api.execute(request);
    }

    private class CreateEditCallback implements OnTaskCompleted {
        @Override
        public void onTaskCompleted(JSONObject result) {
            if(!active) return;

            Log.v("FragmentEditModule", result.toString());
            if(edit) {
                Toast.makeText(getActivity(), "Edited", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Created", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        active = false;
        if(api != null) {
            api.cancel(true);
        }
        super.onDestroy();
    }

    private void loadEdit() {
        if(!active) return;

        View v2 = getMainView();

        ((TextView)v2.findViewById(R.id.module_title)).setText(module.getTitle());
        ((TextView)v2.findViewById(R.id.module_code)).setText(module.getCode());
        ((TextView)v2.findViewById(R.id.module_lecturer)).setText(module.getLecturer());

        ((Button)v2.findViewById(R.id.module_create)).setText(R.string.create);

        view.removeAllViews();
        view.addView(v2);
    }
}
