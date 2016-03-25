package org.esec.mcg.androidu2f;

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

import org.esec.mcg.androidu2f.curl.FidoWebService;
import org.esec.mcg.androidu2f.msg.U2FIntentType;
import org.esec.mcg.androidu2f.msg.U2FRequestType;
import org.esec.mcg.utils.logger.LogUtils;
import org.json.JSONException;
import org.json.JSONObject;

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
    private TextView statusText;
    private ProgressBar progressBar;
    private UIHandler uiHandler;

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

        uiHandler = new UIHandler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Test for network
                ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo == null && !networkInfo.isConnected()) {
                    return;
                }

                Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
                i.addCategory("android.intent.category.DEFAULT");
                i.setType("application/fido.u2f_client+json");

                // Get the U2F Server's enroll url
                SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String endPoint = pf.getString("server_endpoint", "http://192.168.1.101:8000");
                String signPoint = pf.getString("sign", "/sign");

                final String response;
                try {
                    response = FidoWebService.callFidoWebService(FidoWebService.SKFE_PREAUTHENTICATE_WEBSERVICE, getActivity().getResources(), username, null);
//                    response = "{\"Challenge\":{\"SignRequest\":[{\"keyHandle\":\"2sH1ypMiPfGkYJDcvguRl-RuA1mzJTDRyLZBpFlhkxGdaiZq2AC1EJ_MBnopebE3yTkhhFYSGZWMHBkWm4mGja5BUwNwUNaGWL1dQUEXq7duWOgY1pJSqAZDClGVWJ4d2TYqS-tnQVubVzrkscsB12MoXubmi2OXjSkfxaPDYEKBG7aPfbRWDGBQdVL5m4AfozRuxhjS_URMQCiXEou2KapmB_tSYXOfCXOWLcD9YF0\",\"sessionId\":\"wc1jMSmzVkj8t89+xrJRiEUS4SvEdN0BSTq2CRq1vOI=\",\"challenge\":\"4a-o7-88Ehc_FJJ_97eafh31QixzA6jdOaemq9a0jHnzKscK2uvwV5R4bVxKFB6m2O-peR73O41UXNJVH-5vF5bOkp5Srm9IMwUCrnjgGg9iyUM6D78QDJ9ENUos8x8PZrj_XyGSC98D9s2q8GdFDmhupZYR_TwdPiwVGo2rChI\",\"version\":\"U2F_V2\",\"appId\":\"https://demo.strongauth.com:8181/app.json\"},{\"keyHandle\":\"G4Asdesb3I1qYjTzoaq8Cpoj3MZenqJAbTG2epM-8mYRnLDeX12xwE8ZeKo4_As04v32RwMLzPknkMxBZaeOOQ\",\"sessionId\":\"lj/j8AQEQbJ1cYlBEVnOFo+b84sWAQiobjK9gr/Y5FM=\",\"challenge\":\"h3zIjYVdtLH71a8Y_bAQDWJpnTMpQyLLxFjkB1S8qOssTBcydJLG9XYo8yMAE_-zzVxMZh9NiLt7_hr6Z4RW4cbRhxlZA2vovKcTiIopaHyNcupRjSFwWI0XbmQ26GzDPfbHwGlm8qzRh08ioh2fS48Ut8tU0TqhA5kXUkAcZIs\",\"version\":\"U2F_V2\",\"appId\":\"https://demo.strongauth.com:8181/app.json\"},{\"keyHandle\":\"gR9TtDoS_uSI7mNZK_lbW-6T2Vl3e1y7IkDO2mYMXJWGmfWNXUJr89zrVLu6a7A1gz_u6zhd0bdt-giIeK60HfogqT0of7RuTZ27N6d-zJ-3NqbXv8irhHXaE1Y9WgzsrwtazW8AJfq1XIR9h0qYNi0gQSVupVrX8R_S3R7SC15xnVOPZ9sJAiI8YU0uwd2HTnzRGQyp1Xdu9Yx3EJYKjfc2XSp5ciUgABpCeI7REXM\",\"sessionId\":\"yj7qvjI4FzAqAxXn6T32IcYToDLy0ymfunt4wmErwSY=\",\"challenge\":\"ODz6salYBN7_R7fARNZCgsfE32ng24rxGViVy5VvFcY0EnhRKz5HTUMezmSuXairFXDhICb_3Q0_PYDsE2S54hxbvJp6cCcqhKGhzw0O53YyPJhBoohUa4kAjqDqfEB80NX_8HtiVZVjqquVb1OVpAireWmgPGSJasFIkp06QRU\",\"version\":\"U2F_V2\",\"appId\":\"https://demo.strongauth.com:8181/app.json\"},{\"keyHandle\":\"IMzdqT8oamzXyj8PQkLX_3g9irzE-0U3X7k1_iUmERQixs9C0zvrGuDyoVkGUx_iddWb4WOuxQuJQh1bUSE7bYZ-Z8Fd4U76pSOm88VkqcVplFr3hehQWMjXlif9QiQLWMTL1DMFra6ch1h7FYIp1CpjLpLQZspXU3Ij64ERqJc6ITsGBG117zWIRUYensHV9_BYW3TMHffsO7Ae0N9FjDhyMkD5BHVCmfA08t5XVFg\",\"sessionId\":\"Xg6Q7dUtFfhfbEkSvPmp8vMLkqsh1UZKlIlcqaZfSiI=\",\"challenge\":\"axYn7f1__l4okRyJ-EV95QugXEAUUYz2HWevwYVO_CaBtKf4TZN_uIU2OTtMvjlkvEoaNrDton_cwngwLPYaD6rI4TyM3Qw4WSu1k919VU8ScZvqHUEJfRd_WmKVcA-6DrerNcakNhShU9yOCHSt5HNrnBqIt1FScl-qfo0x_dM\",\"version\":\"U2F_V2\",\"appId\":\"https://demo.strongauth.com:8181/app.json\"},{\"keyHandle\":\"kTkCQGYbOIIqvTd_tvLmqJEFS-0Raz4nQli_0VPegHx-1XgRmL7nz6d4bX3eeI4HKztzRVtVUbWPkxWFE9zI4Bwv_H6YSeij2QBeKKCJ3fmV-QEW78Y-LR5qhsWyttjY1uFl4y145fk7YN6gLG5qEthMF0-n2YHV2e_MrQUc7f-dzpdQJbvmPnAaayZgUwo3t6jHwN0iHhGOMV2oAn3MCajr6EfhD8qUYEickz0M2SM\",\"sessionId\":\"0EN+6faGUGr5nFYeHAZS4IwhOOFiOuG7JvoImKCe69g=\",\"challenge\":\"fOLyq1bkuJ2I6Y70ZGGthB1fsRISL6_DPlv8kybjHNQzt8Pt1xPCmSYX_yogrjhnwdHHODk358Hm4JPyPBeIK_Gw_1585s5CFzhoG2Pf3BkLNxvOnV2-VTmLv3IySsyf4nGDO7W8N2BI_XI4GxWAaALw7xsHAIjyzlK0Ok12swA\",\"version\":\"U2F_V2\",\"appId\":\"https://demo.strongauth.com:8181/app.json\"},{\"keyHandle\":\"NOqhEF7u2uy7LecEl4rhU-rg9o4T3UeCpYmhZzL_LTFv9nTa-S5iG05dp3IxLwkM-I8luSfpb-4pcz-IogXHZuMXwcM7f27C4peqbys3OOwjbCBPPAlh0jcAlZ6xImTG9bY79boNEIJhEHpKn6k7ncV6Uly1tS8DxJJz1xZbpyps77rv2JLdWUaAQw3YYOZt1AdnIpeIIYeMojYtvu9U2gPA-adSbMh-ybssOJpc1Dc\",\"sessionId\":\"zktla3cbxrQVO+wMbTOk4pgxcpehqd6EtiHPAeZNapo=\",\"challenge\":\"VyQm1O1qCudBbT11Yulj33o1jKggHueZhWTRcA_Iwe3E6YiJ50tmD0Pj3mc-oNw5VVFAWA4Es1WOhJCZ9pskj0LnAApYb_BwRS9xCGwmc6lIEbFu-eBQ3SsZNPDHUdsOE4ZFHCaG0nCk-6_LtaL2_4eQR8MPdhimfmSLhJvuLM8\",\"version\":\"U2F_V2\",\"appId\":\"https://demo.strongauth.com:8181/app.json\"}]},\"Message\":\"\",\"Error\":\"\"}";
                    LogUtils.d(response);
                    JSONObject formalResponse = new JSONObject(response);
                    JSONObject request = formalResponse.getJSONObject("Challenge");
                    JSONObject newRequest = new JSONObject();
                    newRequest.put("type", U2FRequestType.u2f_sign_request);
                    newRequest.put("signRequests", request.getJSONArray("SignRequest"));
                    Bundle data = new Bundle();
                    data.putString("Request", newRequest.toString());
                    data.putString("U2FIntentType", U2FIntentType.U2F_OPERATION_SIGN.name());
//                    i.putExtras(data);

                    Message msg = Message.obtain(null, MSG_START_ACTIVITY);
                    msg.setData(data);
                    uiHandler.sendMessage(msg);
                } catch (U2FException | JSONException e) {
                    e.printStackTrace();
                    Bundle data = new Bundle();
                    data.putString("Error", e.getMessage());
                    Message msg = Message.obtain(null, MSG_ERROR);
                    msg.setData(data);
                    uiHandler.sendMessage(msg);
                    return;
                }
//                getActivity().startActivityForResult(i, SIGN_ACTIVITY_RES_2);
            }
        }).start();
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

    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            switch (msg.what) {
                case MSG_ERROR:
                    statusText.setText(data.getString("Error"));
                    progressBar.setVisibility(View.INVISIBLE);
                    break;
                case MSG_START_ACTIVITY:
                    Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
                    i.addCategory("android.intent.category.DEFAULT");
                    i.setType("application/fido.u2f_client+json");
                    i.putExtras(msg.getData());

                    getActivity().startActivityForResult(i, SIGN_ACTIVITY_RES_2);
            }

        }
    }
}
