package org.esec.mcg.androidu2f;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import org.esec.mcg.utils.logger.LogUtils;


/**
 * The fragment which wraps token registration and authentication features.
 */
public class LoginFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public LoginFragment() {
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
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        view.findViewById(R.id.enroll_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                submit(false);
            }
        });
        view.findViewById(R.id.sign_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                submit(true);
            }
        });
        return view;
    }

    private void submit(boolean sign) {
        EditText userText = (EditText) getActivity().findViewById(R.id.username);
        String username = userText.getText().toString().trim();
        EditText passText = (EditText) getActivity().findViewById(R.id.password);
        String password = passText.getText().toString().trim();

        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(getActivity().getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getApplicationWindowToken(), 0);
        }

        if(username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Fill the username and password fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        userText.getText().clear();
        passText.getText().clear();

        mListener.onLoginEntered(username, password, sign);
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onLoginEntered(String username, String password, boolean sign);
    }

}
