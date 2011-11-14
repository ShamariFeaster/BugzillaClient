
package edu.fsu.cs.bugzillaclient;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LoginScreen extends BaseActivity {
    final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen_layout);
        Button loginButton = (Button) findViewById(R.id.LoginButton);
        Button settingButton = (Button) findViewById(R.id.SettingButton);

        settingButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SettingsScreen.class));
            }
        });

        loginButton.setOnClickListener(new OnClickListener() {


            JSONObject loggedIn;


            public void onClick(View v) {
                Log.w(TAG, mServer_url);
                loggedIn = loginToBugzilla(mUsername, mPassword,
                        mServer_url,

                        BaseActivity.sHttpclient);
                Log.w(TAG, loggedIn.toString());
                // if there isn't a response object the response didn't come
                // back from a bugzilla
                // install

                if (!isBugzillaError(loggedIn)) {

                    showToast("You're Logged In To Bugzilla!");
                    startActivity(new Intent(getApplicationContext(), HomeScreen.class));
                }

            }
        });

    }
}
