
package edu.fsu.cs.bugzillaclient;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Pattern;

public class SettingsScreen extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_screen_layout);
        // serverUrl should have a trailing backslash "/"
        final EditText serverEditText = (EditText) findViewById(R.id.serverUrlEditText);
        final EditText usernameEditText = (EditText) findViewById(R.id.UserNameEditText);
        final EditText passwordEditText = (EditText) findViewById(R.id.PasswordEditText);
        final Button storePrefsButton = (Button) findViewById(R.id.StorePrefsButton);

        // displaying currently set Url
        serverEditText.setText(mPreferences.getString("server_url", ""));

        serverEditText.addTextChangedListener(new MyTextWatcher(serverEditText));

        storePrefsButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                String serverHolder = serverEditText.getText().toString();
                String usernameHolder = usernameEditText.getText().toString();
                String passwordHolder = passwordEditText.getText().toString();
                Boolean submitFlag = false;
                if (!serverHolder.equals("")) {
                    mEditor.putString("server_url", serverHolder);
                    if (Pattern.matches("^http://{1}[a-zA-Z-0-9\\.\\-/]+[/]"
                            , serverEditText.getText())) {
                        submitFlag = true;

                    }
                }
                if (!usernameHolder.equals("")) {
                    mEditor.putString("username", usernameHolder);
                }
                if (!passwordHolder.equals("")) {
                    mEditor.putString("password", passwordHolder);
                }
                if (submitFlag) {
                    mEditor.commit();
                    showToast("Preferences Saved");
                } else {
                    showToast("You Input An Invalid Server Url. Preferences Not Saved");
                }
            }

        });

    }

    class MyTextWatcher implements TextWatcher {

        private final EditText et;

        private MyTextWatcher(EditText editText) {
            this.et = editText;
        }

        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            switch (et.getId()) {
                case R.id.serverUrlEditText: {
                    if (!Pattern.matches("^http://{1}[a-zA-Z-0-9\\.\\-/]+[/]", s)) {
                        et.setError("Server Url Must Start With " +
                                "\"http://\" and end with \"/\"");
                    }
                    break;
                }
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // TODO Auto-generated method stub

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

    }
}
