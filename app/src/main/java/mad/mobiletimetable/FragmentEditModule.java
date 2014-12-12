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
    private boolean active = true;

    private FrameLayout view;
    private LayoutInflater inflater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getActivity().getIntent();

        //Editing existing module
        if(intent.hasExtra("moduleid")) {
            //Store the fact we're editing
            edit = true;
            Log.d("FragmentEditModule", "Editing Module");

            //Send API request
            HashMap<String, String> request = new HashMap<String, String>();
            request.put("method", "module");
            request.put("action", "get");
            request.put("moduleid", intent.getStringExtra("moduleid"));
            api = new APIClass(getActivity(), new EditCallback());
            api.execute(request);
        } else {
            //Creating new module
            //Nothing else to do
            Log.d("FragmentEditModule", "Creating New Module");
            edit = false;
        }
    }

    @Override
    public void onResume() {
        //Set the title if we're editing
        if(edit) {
            setEditTitle();
        }
        super.onResume();
    }

    private void setEditTitle() {
        getActivity().setTitle(R.string.edit_module);
    }


    private class EditCallback implements OnTaskCompleted {
        @Override
        public void onTaskCompleted(JSONObject result) {
            //If the fragment has been destroyed, do nothing
            if(!active) return;

            //If there was a problem getting the info
            if(!result.has("module")) {
                getActivity().finish();
                return;
            }
            try {
                //Extract the info and load it into a model
                JSONObject moduleInfo = result.getJSONObject("module");
                module = new ModelModule(moduleInfo);
            } catch(JSONException e) {
                e.printStackTrace();
            }

            //Set the info in the form
            loadEdit();
        }
    }

    private void loadEdit() {
        //If the fragment has been destroyed, do nothing
        if(!active) return;

        //Get the view
        View v2 = getMainView();

        //Set Title
        ((TextView)v2.findViewById(R.id.module_title)).setText(module.getTitle());
        //Set Code
        ((TextView)v2.findViewById(R.id.module_code)).setText(module.getCode());
        //Set Lecturer
        ((TextView)v2.findViewById(R.id.module_lecturer)).setText(module.getLecturer());

        //Set the button to say update, rather than create
        ((Button)v2.findViewById(R.id.module_create)).setText(R.string.edit_label);

        //Remove loading spinner
        view.removeAllViews();
        //Add our view
        view.addView(v2);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.inflater = inflater;

        //Create frame layout that matches parent height and width
        view = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);

        View v2;

        //Show loading screen for edit
        if(edit) {
            v2 = inflater.inflate(R.layout.loading, view, false);
            Log.v("FragmentEditModule", "Editing");
        } else {
            v2 = getMainView();
            Log.v("FragmentEditModule", "Creating");
        }

        //Return main view
        view.addView(v2);
        return view;
    }

    //Get the main view, with the input boxes, and tell the button what to do
    private View getMainView() {
        //Inflate view
        View mainView = inflater.inflate(R.layout.fragment_edit_module, view, false);

        //Set Button Action
        Button button = (Button) mainView.findViewById(R.id.module_create);
        button.setOnClickListener(this);

        return mainView;
    }

    @Override
    public void onClick(View v) {
        //Send API Request
        api = new APIClass(getActivity(), new CreateEditCallback());

        HashMap<String, String> request = new HashMap<String, String>();
        request.put("method", "module");
        //Change action and add moduleID if editing
        if(edit) {
            request.put("action", "edit");
            request.put("moduleid", Integer.toString(module.getId()));
        } else {
            request.put("action", "add");
        }

        //Fields
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
            //If the fragment has been destroyed, do nothing
            if(!active) return;

            //Get Response Status
            String status;
            try {
                status = result.getString("status");
            } catch(JSONException e) {
                e.printStackTrace();
                return;
            }

            //If successful
            if(status.equals("OK")) {
                if (edit) {
                    Toast.makeText(getActivity(), "Edited", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Created", Toast.LENGTH_LONG).show();
                    try {
                        //Set Result, needed sometimes
                        int moduleId = result.getInt("moduleid");
                        Intent intent = new Intent();
                        intent.putExtra("moduleid", moduleId);
                        getActivity().setResult(1, intent);
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                }
                //Finish this activity
                getActivity().finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        //Make sure no internal functions respond after this has been destroyed
        active = false;
        //Cancel the api if active
        if(api != null) {
            api.cancel(true);
        }
        super.onDestroy();
    }
}
