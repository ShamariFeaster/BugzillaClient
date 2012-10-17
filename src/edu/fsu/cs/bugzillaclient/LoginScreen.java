
package edu.fsu.cs.bugzillaclient;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/* Launcher Activity
 */
public class LoginScreen extends BaseActivity {
    final String TAG = this.getClass().getSimpleName();

    private final Context mContext = this;

    private ProgressDialog mLoadingDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen_layout);

        Button loginButton = (Button)findViewById(R.id.LoginButton);
        Button settingButton = (Button)findViewById(R.id.SettingButton);
        Button logoutButton = (Button)findViewById(R.id.LogoutButton);

        settingButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SettingsScreen.class));
            }
        });

        logoutButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (sLoggedIn) {
                    bLoadingDialog = showProgressDialog(mContext, "Logging Out");
                    new BugzillaAsyncLogout().execute();
                } else {
                    showToast("You're Already Logged Out");
                }

            }
        });
        loginButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // are we already logged in
                if (!sLoggedIn) {
                    bLoadingDialog = showProgressDialog(mContext, "Logging In");
                    new BugzillaAsyncLogin().execute();
                } else {
                    startActivity(new Intent(getApplicationContext(), HomeScreen.class));
                }

            }
        });

    }

    @Override
    protected void onStop() {
        // dismiss dialog
        super.onStop();
        dismissProgressDialog(bLoadingDialog);
    }
}
