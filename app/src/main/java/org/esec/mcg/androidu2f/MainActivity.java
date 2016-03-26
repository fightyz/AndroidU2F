package org.esec.mcg.androidu2f;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.esec.mcg.androidu2f.client.curl.Curl;
import org.esec.mcg.androidu2f.curl.FidoWebService;
import org.esec.mcg.utils.HTTP;
import org.esec.mcg.utils.logger.LogUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements LoginFragment.OnFragmentInteractionListener, EnrollFragment.OnFragmentInteractionListener,
                    SignFragment.OnFragmentInteractionListener {

    public static final int RESPONSE = 1;
    public static final int ERROR = 2;
    private final Map<String,String> details = new LinkedHashMap<String, String>();
    public static String sessionId;
    private UIHandler uiHandler;

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

        uiHandler = new UIHandler();
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String webResponse = null;
                        try {
                            webResponse = FidoWebService.callFidoWebService(FidoWebService.SKFE_REGISTER_WEBSERVICE, getResources(), "yz", response);
                            LogUtils.d(webResponse);
                            Bundle data = new Bundle();
                            data.putString("WebResponse", webResponse);
                            Message msg = Message.obtain();
                            msg.what = RESPONSE;
                            msg.setData(data);
                            uiHandler.sendMessage(msg);
                        } catch (U2FException e) {
                            e.printStackTrace();
                            Bundle data = new Bundle();
                            data.putString("Error", e.getMessage());
                            Message msg = Message.obtain();
                            msg.what = ERROR;
                            msg.setData(data);
                            uiHandler.sendMessage(msg);
                            return;
                        }
                    }
                }).start();

            } catch (JSONException e) {
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject response = new JSONObject(signResponse).getJSONObject("responseData");
                            response.put("sessionId", sessionId);
                            LogUtils.d("response: " + response.toString());
                            String webResponse = FidoWebService.callFidoWebService(FidoWebService.SKFE_AUTHENTICATE_WEBSERVICE, getResources(), "iceespresso101", response);
                            LogUtils.d(webResponse);
                            Bundle data = new Bundle();
                            data.putString("WebResponse", webResponse);
                            Message msg = Message.obtain();
                            msg.what = RESPONSE;
                            msg.setData(data);
                            uiHandler.sendMessage(msg);
                        } catch (JSONException | U2FException e) {
                            e.printStackTrace();
                            Bundle data = new Bundle();
                            data.putString("Error", e.getMessage());
                            Message msg = Message.obtain();
                            msg.what = ERROR;
                            msg.setData(data);
                            uiHandler.sendMessage(msg);
                            return;
                        }
                    }
                }).start();
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

    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            Fragment currentFragment = getFragmentManager().findFragmentByTag("currentFragment");
            String message;
            switch (msg.what) {
                case RESPONSE:
                    message = data.getString("WebResponse");
                    break;
                case ERROR:
                    message = data.getString("Error");
                    break;
                default:
                    message = "Ah o!";
            }
            if (currentFragment instanceof EnrollFragment) {
                TextView tx = (TextView)currentFragment.getView().findViewById(R.id.enroll_status_text);

                tx.setText(message);
                currentFragment.getView().findViewById(R.id.enroll_progressBar).setVisibility(View.INVISIBLE);
            } else if (currentFragment instanceof SignFragment) {

                TextView tx = (TextView)currentFragment.getView().findViewById(R.id.sign_status_text);
                currentFragment.getView().findViewById(R.id.sign_progressBar).setVisibility(View.INVISIBLE);
                tx.setText(message);
            }
        }
    }
}
