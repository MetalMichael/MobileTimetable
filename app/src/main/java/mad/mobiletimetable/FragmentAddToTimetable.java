package mad.mobiletimetable;

import android.os.Bundle;
import android.widget.Button;
import android.app.Activity;
import android.net.Uri;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TableLayout;
import android.util.Log;
import android.widget.Toast;
import android.content.Context;
import android.widget.Spinner;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentAddToTimetable.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentAddToTimetable#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class FragmentAddToTimetable extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button addNew;
    private TableLayout layoutNew;
    private View root;
    private String roomTypes[];
    private String ModuleChoice[];
    private String rooms[];

    private OnFragmentInteractionListener mListener;

    private Spinner roomTypeSpinner;
    private AutoCompleteTextView ModuleChoiceView;
    private AutoCompleteTextView roomView;



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentAddToTimetable.
     *
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentAddToTimetable newInstance(String param1, String param2) {
        FragmentAddToTimetable fragment = new FragmentAddToTimetable();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public FragmentAddToTimetable() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        root= inflater.inflate(R.layout.fragment_add_to_timetable, container, false);

        addNew= (Button) root.findViewById(R.id.Add);

        Context c = getActivity().getApplicationContext();

        roomTypes=getActivity().getResources().getStringArray(R.array.roomTypes);
        ModuleChoice=getActivity().getResources().getStringArray(R.array.ModuleChoices);
        rooms=getActivity().getResources().getStringArray(R.array.Rooms);


        ArrayAdapter<String> adapter1 = new ArrayAdapter<String> (c, android.R.layout.simple_dropdown_item_1line, roomTypes);
        roomTypeSpinner = (Spinner) root.findViewById(R.id.completeType);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String> (c, android.R.layout.simple_dropdown_item_1line, ModuleChoice);
        ModuleChoiceView = (AutoCompleteTextView) root.findViewById(R.id.completeModule);

        ArrayAdapter<String> adapter3 = new ArrayAdapter<String> (c, android.R.layout.simple_dropdown_item_1line, rooms);
        roomView= (AutoCompleteTextView) root.findViewById(R.id.completeRoom);

        roomTypeSpinner.setAdapter(adapter1);
        ModuleChoiceView.setAdapter(adapter2);
        roomView.setAdapter(adapter3);
        return root;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
