package org.esec.mcg.androidu2f;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SignFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SignFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignFragment extends Fragment {
    private TextView statusText;
    private ProgressBar progressBar;

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final int SIGN_ACTIVITY_RES_2 = 2;

    private String username;
    private String password;

    private OnFragmentInteractionListener mListener;

    private static final int MSG_START_ACTIVITY = 0;
    private static final int MSG_ERROR = 1;

    public SignFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param username Parameter 1.
     * @param password Parameter 2.
     * @return A new instance of fragment SignFragment.
     */
    public static SignFragment newInstance(String username, String password) {
        SignFragment fragment = new SignFragment();
        Bundle args = new Bundle();
        args.putString(USERNAME, username);
        args.putString(PASSWORD, password);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString(USERNAME);
            password = getArguments().getString(PASSWORD);
        }

        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("username", username);
        parameters.put("password", password);
        parameters.put("version", "U2F_V2");

        mListener.presign(username);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign, container, false);
        statusText = (TextView)view.findViewById(R.id.sign_status_text);
        progressBar = (ProgressBar)view.findViewById(R.id.sign_progressBar);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
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
        void presign(String username);
    }

    // TODO: 2016/3/7 Change the register request to sign request.
    /**
     * Get U2F server request.
     * @return U2F server request.
     */
    private String getServerRequest(URL url) {
        return "{\"type\": \"u2f_sign_request\", \"signRequests\": [{\"keyHandle\":\"ASF4Os1wJysH6uWvJV9PvyNiph4y4O84tGCHj1FZEE8Wjy4TySErklcH0BQNz6lSbRpiDi2XE6we2bcJ1DSUaw==\",\"challenge\": \"mLkHCmQZGbZEXefhWByeKo5zTFldYLIZFRGeHdvTFBc=\", \"version\": \"U2F_V2\", \"appId\": \"http://localhost:8000\"}]}";
    }
}
