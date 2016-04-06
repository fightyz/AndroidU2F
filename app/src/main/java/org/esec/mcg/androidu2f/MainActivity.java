package org.esec.mcg.androidu2f;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.esec.mcg.utils.logger.LogUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements BackHandledInterface,
                    RegisterFragment.OnFragmentInteractionListener,
                    LoginFragment.OnFragmentInteractionListener,
                    EnrollFragment.OnFragmentInteractionListener,
                    SignFragment.OnFragmentInteractionListener {

    public static final int RESPONSE = 1;
    public static final int ERROR = 2;
    private final Map<String,String> details = new LinkedHashMap<String, String>();
    public static String sessionId;
    private UIHandler uiHandler;

    private BackHandledFragment mBackHandledFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



//        RegisterFragment fragment = new RegisterFragment();
//        getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

        InfoFragment fragment = new InfoFragment();
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

    public void onClick(View view) {
        int id = view.getId();

        if(id == R.id.register_button) {
            RegisterFragment fragment = new RegisterFragment();
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                    .addToBackStack("registerFragment").commit();

        } else if(id == R.id.open_door_button) {
            LoginFragment fragment = new LoginFragment();
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                    .addToBackStack("loginFragment").commit();
        }
    }

    @Override
    public void setSelectedFragment(BackHandledFragment selectedFragment) {
        this.mBackHandledFragment = selectedFragment;
    }

    @Override
    public void onBackPressed() {
        Log.e("mainActivity", "onBackPressed");
        if(mBackHandledFragment == null || !mBackHandledFragment.onBackPressed()){
            if(getFragmentManager().getBackStackEntryCount() == 0){
                super.onBackPressed();
            }else{
                getFragmentManager().popBackStack();
            }
        }
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
                final JSONObject response = new JSONObject(registerResponse).getJSONObject("responseData");

                RequestQueue queue = Volley.newRequestQueue(this);
                String url = "http://192.168.1.22:8000/com_register";
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, response, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
                        currentFragment.getView().findViewById(R.id.enroll_progressBar).setVisibility(View.GONE);
                        LogUtils.d("response: " + response.toString());
                        int result = 0;
                        try {
                            result = response.getInt("result");
                            if (result == 1) {
                                ((TextView)currentFragment.getView().findViewById(R.id.enroll_status_text)).setText("register successful");
                                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                                Fragment fragment = (Fragment) org.esec.mcg.androidu2f.RegisterFragment.newInstance(null, null);
                                fragmentTransaction.replace(R.id.fragment_container, fragment, "currentFragment");
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                            } else {
                                ((TextView)currentFragment.getView().findViewById(R.id.enroll_status_text)).setText("register failed!");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
                        currentFragment.getView().findViewById(R.id.enroll_progressBar).setVisibility(View.GONE);
                        ((TextView)currentFragment.getView().findViewById(R.id.enroll_status_text)).setText("response error: " + error.getMessage());
                        LogUtils.d("response error: " + error);
                    }
                });
                jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 5, 1.0f));

                queue.add(jsonObjectRequest);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (Constants.SIGN_ACTIVITY_RES_2 == requestCode) {
            LogUtils.d("==================");
            if (resultCode == RESULT_CANCELED) {
                Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
                ((TextView)currentFragment.getView().findViewById(R.id.sign_status_text)).setText(data.getStringExtra("Response"));
                return;
            } else if (resultCode == RESULT_OK) {
                final String signResponse = data.getStringExtra("Response");

                LogUtils.d(signResponse);

                RequestQueue queue = Volley.newRequestQueue(this);
                String url = "http://192.168.1.22:8000/com_auth";
                final JSONObject response;
                try {
                    response = new JSONObject(signResponse).getJSONObject("responseData");
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, response, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
                            currentFragment.getView().findViewById(R.id.sign_progressBar).setVisibility(View.GONE);
                            LogUtils.d("response: " + response.toString());
                            int result = 0;
                            try {
                                result = response.getInt("result");
                                if (result == 1) {
                                    ((TextView)currentFragment.getView().findViewById(R.id.sign_status_text)).setText("sign successful");
                                } else {
                                    ((TextView)currentFragment.getView().findViewById(R.id.sign_status_text)).setText("sign failed!");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
                            currentFragment.getView().findViewById(R.id.sign_progressBar).setVisibility(View.GONE);
                            ((TextView)currentFragment.getView().findViewById(R.id.sign_status_text)).setText("response error: " + error.getMessage());
                            LogUtils.d("response error: " + error);
                        }
                    });
                    jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 5, 1.0f));
                    queue.add(jsonObjectRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Callback of the RegisterFragment.
     * After user inputs username and password, do register or authenticate the android token
     * @param username
     * @param password
     * @param sign If false, then register; If true, then authenticate.
     */
    @Override
    public void onLoginEntered(String username, String password, boolean sign) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        details.clear();
        Fragment fragment = sign ? (Fragment) SignFragment.newInstance(username, password) : (Fragment) EnrollFragment.newInstance(username, password);
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
