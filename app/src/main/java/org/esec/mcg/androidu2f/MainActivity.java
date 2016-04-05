package org.esec.mcg.androidu2f;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.esec.mcg.androidu2f.curl.FidoWebService;
import org.esec.mcg.androidu2f.curl.HttpServiceClient;
import org.esec.mcg.androidu2f.curl.SampleJSON;
import org.esec.mcg.androidu2f.msg.U2FIntentType;
import org.esec.mcg.androidu2f.msg.U2FRequestType;
import org.esec.mcg.utils.logger.LogUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity
        implements LoginFragment.OnFragmentInteractionListener, EnrollFragment.OnFragmentInteractionListener,
                    SignFragment.OnFragmentInteractionListener {

    private final Map<String,String> details = new LinkedHashMap<String, String>();
    public static String sessionId;

    private HttpServiceClient httpServiceClient = new HttpServiceClient(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        LoginFragment fragment = new LoginFragment();
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

        httpServiceClient.setResponseHandler(new BaseJsonHttpResponseHandler<SampleJSON>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, SampleJSON response) {
                LogUtils.d(HttpServiceClient.debugHeaders(headers));
                LogUtils.d(HttpServiceClient.dubugStatusCode(statusCode));
                if (response != null) {
                    LogUtils.d(rawJsonResponse);
                    try {
                        Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
                        i.addCategory("android.intent.category.DEFAULT");
                        i.setType("application/fido.u2f_client+json");
                        Bundle data = new Bundle();
                        JSONObject formalResponse;
                        JSONObject request;
                        JSONObject formalRequest;

                        Fragment currentFragment = getFragmentManager().findFragmentByTag("currentFragment");
                        TextView tx;

                        switch (HttpServiceClient.op) {
                            case u2f_register_request:

                                formalResponse = new JSONObject(rawJsonResponse);
                                request = formalResponse.getJSONObject("Challenge");
                                formalRequest = new JSONObject();

                                formalRequest.put("type", U2FRequestType.u2f_register_request);
                                formalRequest.put("registerRequests", request.getJSONArray("RegisterRequest"));
                                formalRequest.put("signRequests", request.getJSONArray("SignRequest"));
                                MainActivity.sessionId = request.getString("sessionId");

                                data.putString("Request", formalRequest.toString());
                                data.putString("U2FIntentType", U2FIntentType.U2F_OPERATION_REG.name());
                                i.putExtras(data);
                                startActivityForResult(i, Constants.REG_ACTIVITY_RES_1);
                                break;
                            case u2f_sign_request:
                                formalResponse = new JSONObject(rawJsonResponse);
                                request = formalResponse.getJSONObject("Challenge");
                                formalRequest = new JSONObject();
                                formalRequest.put("type", U2FRequestType.u2f_sign_request);
                                formalRequest.put("signRequests", request.getJSONArray("SignRequest"));
                                MainActivity.sessionId = request.getJSONArray("SignRequest").getJSONObject(0).getString("sessionId");
                                data.putString("Request", formalRequest.toString());
                                data.putString("U2FIntentType", U2FIntentType.U2F_OPERATION_SIGN.name());
                                i.putExtras(data);
                                startActivityForResult(i, Constants.SIGN_ACTIVITY_RES_2);
                                break;
                            case u2f_register_response:
                                tx = (TextView)currentFragment.getView().findViewById(R.id.enroll_status_text);
                                tx.setText(rawJsonResponse);
                                currentFragment.getView().findViewById(R.id.enroll_progressBar).setVisibility(View.INVISIBLE);
                                break;
                            case u2f_sign_response:
                                tx = (TextView)currentFragment.getView().findViewById(R.id.sign_status_text);
                                tx.setText(rawJsonResponse);
                                currentFragment.getView().findViewById(R.id.sign_progressBar).setVisibility(View.INVISIBLE);
                                break;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, SampleJSON errorResponse) {
                LogUtils.d(HttpServiceClient.debugHeaders(headers));
                LogUtils.d(HttpServiceClient.dubugStatusCode(statusCode));
                if (statusCode == 0) {
                    rawJsonData = "Connection Timeout!";
                }
                LogUtils.d(rawJsonData);
                Fragment currentFragment = getFragmentManager().findFragmentByTag("currentFragment");
                if (currentFragment instanceof EnrollFragment) {
                    TextView tx = (TextView)currentFragment.getView().findViewById(R.id.enroll_status_text);
                    tx.setText(rawJsonData);
                    currentFragment.getView().findViewById(R.id.enroll_progressBar).setVisibility(View.INVISIBLE);
                } else if (currentFragment instanceof SignFragment) {
                    TextView tx = (TextView)currentFragment.getView().findViewById(R.id.sign_status_text);
                    currentFragment.getView().findViewById(R.id.sign_progressBar).setVisibility(View.INVISIBLE);
                    tx.setText(rawJsonData);
                }
            }

            @Override
            protected SampleJSON parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return new ObjectMapper().readValues(new JsonFactory().createParser(rawJsonData), SampleJSON.class).next();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent("org.esec.mcg.androidu2f.SettingsActivity"));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REG_ACTIVITY_RES_1) {
            if (resultCode == RESULT_CANCELED) { // Register failed
                Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
                ((TextView)currentFragment.getView().findViewById(R.id.enroll_status_text)).setText(data.getExtras().getString("Response"));
                return;
            }
            String registerResponse = data.getStringExtra("Response");
            LogUtils.d(registerResponse);
            //TODO send register response to StrongAuth U2F Server
            try {
                final JSONObject response = new JSONObject(registerResponse).getJSONObject("responseData").put("sessionId", sessionId);
                httpServiceClient.callFidoWebService(HttpServiceClient.SKFE_REGISTER_WEBSERVICE, getResources(), null, response);

            } catch (JSONException | U2FException e) {
                e.printStackTrace();
            }
        } else if (Constants.SIGN_ACTIVITY_RES_2 == requestCode) {
            if (resultCode == RESULT_CANCELED) {
                Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
                ((TextView)currentFragment.getView().findViewById(R.id.sign_status_text)).setText(data.getStringExtra("Response"));
                return;
            } else if (resultCode == RESULT_OK) {
                final String signResponse = data.getStringExtra("Response");
                LogUtils.d(signResponse);
                try {
                    final JSONObject response = new JSONObject(signResponse).getJSONObject("responseData").put("sessionId", sessionId);
                    httpServiceClient.callFidoWebService(HttpServiceClient.SKFE_AUTHENTICATE_WEBSERVICE, getResources(), null, response);
                } catch (U2FException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Callback of the LoginFragment.
     * After user inputs username and password, do register or authenticate the android token
     * @param username
     * @param password
     * @param sign If false, then register; If true, then authenticate.
     */
    @Override
    public void onLoginEntered(String username, String password, boolean sign) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        details.clear();
        Fragment fragment = sign ? (Fragment)SignFragment.newInstance(username, password) : (Fragment)EnrollFragment.newInstance(username, password);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.fragment_container, fragment, "currentFragment");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public HttpServiceClient getHttpServiceClient() {
        return httpServiceClient;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d("Number of handles found: %d", httpServiceClient.getRequestHandles().size());
        int counter = 0;
        for(RequestHandle handle : httpServiceClient.getRequestHandles()) {
            if (!handle.isCancelled() && !handle.isFinished()) {
                LogUtils.d("Cancelling handle %d", counter);
                LogUtils.d(String.format("Handle %d cancel", counter) + (handle.cancel(true) ? " succeeded" : " failed"));
            } else {
                LogUtils.d("Handle %d already non-cancellable", counter);
            }
            counter++;
        }
    }
}
