package org.esec.mcg.androidu2f;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import org.esec.mcg.utils.logger.LogUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements LoginFragment.OnFragmentInteractionListener, EnrollFragment.OnFragmentInteractionListener{

    private final Map<String,String> details = new LinkedHashMap<String, String>();

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
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
////        TextView tv = (TextView) findViewById(R.id.text_view);
////        SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(this);
////        tv.setText(pf.getString("server_endpoint", "yz"));
//
//    }

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
        if (requestCode == 1) {
            LogUtils.d("resultCode = " + resultCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onLoginEntered(String username, String password, boolean sign) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        details.clear();
//        Fragment fragment = sign ? new SignFragment(details, username, password) : new EnrollFragment(details, username, password);
        Fragment fragment = sign ? null : (Fragment)EnrollFragment.newInstance(username, password);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}