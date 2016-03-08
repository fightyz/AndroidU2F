package org.esec.mcg.androidu2f;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.esec.mcg.androidu2f.msg.U2FIntentType;

import java.net.MalformedURLException;
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
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final int SIGN_ACTIVITY_RES_2 = 2;

    private String username;
    private String password;

    private OnFragmentInteractionListener mListener;

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

        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
                i.addCategory("android.intent.category.DEFAULT");
                i.setType("application/fido.u2f_client+json");

                // Get the U2F Server's enroll url
                SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String endPoint = pf.getString("server_endpoint", "http://192.168.1.101:8000");
                String signPoint = pf.getString("sign", "/sign");

                final String response;
                try {
                    response = getServerRequest(new URL(endPoint + signPoint));
                    Bundle data = new Bundle();
                    data.putString("message", response);
                    data.putString("U2FIntentType", U2FIntentType.U2F_OPERATION_SIGN.name());
                    i.putExtras(data);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                getActivity().startActivityForResult(i, SIGN_ACTIVITY_RES_2);
            }
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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
