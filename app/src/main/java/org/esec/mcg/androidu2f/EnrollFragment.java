package org.esec.mcg.androidu2f;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.esec.mcg.androidu2f.client.op.U2FServerRequest;
import org.esec.mcg.androidu2f.curl.FidoWebService;
import org.esec.mcg.androidu2f.msg.U2FIntentType;
import org.esec.mcg.androidu2f.msg.U2FRequestType;
import org.esec.mcg.androidu2f.msg.U2FResponseType;
import org.esec.mcg.utils.logger.LogUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EnrollFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EnrollFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EnrollFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private String username;
    private String password;

    private TextView statusText;
    private ProgressBar progressBar;
    private UIHandler uiHandler;

    /**
     * Client's clientRegister operation.
     */
    private U2FServerRequest serverRequestMessage = new U2FServerRequest();

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param username Parameter 1.
     * @param password Parameter 2.
     * @return A new instance of fragment EnrollFragment.
     */
    public static EnrollFragment newInstance(String username, String password) {
        EnrollFragment fragment = new EnrollFragment();
        Bundle args = new Bundle();
        args.putString(USERNAME, username);
        args.putString(PASSWORD, password);
        fragment.setArguments(args);
        return fragment;
    }

    public EnrollFragment() {
        // Required empty public constructor
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

        uiHandler = new UIHandler();

        new Thread(new Runnable() {
            @Override
            public void run() {
//                try {
////                    final String response = HTTP.post(new URL("https://demo.yubico.com/wsapi/u2f/enroll"), parameters);
////                    final String response = HTTP.post(new URL("http://192.168.1.140:8080/fidouaf/v1/history"), parameters);
////                    final String response = HTTP.get(new URL("http://192.168.1.140:8080/fidouaf/v1/history"));
////                    final String response = HTTP.get(new URL("http://openidconnect.ebay.com/fidouaf/v1/public/regRequest/yg"));
////                    final String response = HTTP.get(new URL("http://192.168.1.128:8000"));
//
//                    LogUtils.d(response);
//                } catch (IOException e) {
//
//                }
                // Test for network
                ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo == null && !networkInfo.isConnected()) {
                    return;
                }

                // non-normative, defined according to UAF protocol.
                Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
                i.addCategory("android.intent.category.DEFAULT");
                i.setType("application/fido.u2f_client+json");

                // Get the U2F Server's enroll url
                SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String endPoint = pf.getString("server_endpoint", "https://demo.strongauth.com");
                String enrollPoint = pf.getString("enroll", "/");

                final String response;
                try {
                    // TODO: 2016/3/11 The response is specific to StrongAuth U2F Server.
//                    response = getServerRequest(new URL(endPoint + enrollPoint));
                    response = FidoWebService.callFidoWebService(FidoWebService.SKFE_PREREGISTER_WEBSERVICE, getActivity().getResources(), username, null);
                    JSONObject formalResponse = new JSONObject(response);
                    JSONObject request = formalResponse.getJSONObject("Challenge");
                    JSONObject formalRequest = new JSONObject();
                    formalRequest.put("type", U2FRequestType.u2f_register_request);
                    formalRequest.put("registerRequests", request.getJSONArray("RegisterRequest"));
                    formalRequest.put("signRequests", request.getJSONArray("SignRequest"));
                    MainActivity.sessionId = request.getString("sessionId");
                    LogUtils.d(formalRequest.toString());
                    Bundle data = new Bundle();
                    data.putString("Request", formalRequest.toString());
                    data.putString("U2FIntentType", U2FIntentType.U2F_OPERATION_REG.name());
                    i.putExtras(data);
                } catch (U2FException | JSONException e) {
//                    Toast.makeText(getActivity(), "Wrong URL", Toast.LENGTH_LONG).show();
                    Bundle data = new Bundle();
                    data.putString("Error", e.getMessage());
                    Message msg = new Message();
                    msg.setData(data);
                    uiHandler.sendMessage(msg);
                    return;
                }

                // call u2f client activity
                getActivity().startActivityForResult(i, Constants.REG_ACTIVITY_RES_1);
            }
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_enroll, container, false);
        statusText = (TextView)view.findViewById(R.id.enroll_status_text);
        progressBar = (ProgressBar)view.findViewById(R.id.enroll_progressBar);
        return view;
    }

    /**
     * Get U2F server request.
     * @return U2F server request.
     */
    private String getServerRequest(URL url) {
        return "{\"type\": \"u2f_register_request\", \"signRequest\": [], \"registerRequests\": [{\"challenge\": \"mLkHCmQZGbZEXefhWByeKo5zTFldYLIZFRGeHdvTFBc=\", \"version\": \"U2F_V2\", \"appId\": \"http://localhost:8000\"}]}";
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            statusText.setText(data.getString("Error"));
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
