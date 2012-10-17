
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
    private final String TAG = this.getClass().getSimpleName();
    private static String sTooltipMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_screen_layout);
        final String reg_ex_pattern = getResources().getString(R.string.UrlRegEx);
        sTooltipMessage = getResources().getString(R.string.server_tooltip);

        // serverUrl should have a trailing backslash "/"
        final EditText serverEditText = (EditText) findViewById(R.id.serverUrlEditText);
        final EditText usernameEditText = (EditText) findViewById(R.id.UserNameEditText);
        final EditText passwordEditText = (EditText) findViewById(R.id.PasswordEditText);
        final Button storePrefsButton = (Button) findViewById(R.id.StorePrefsButton);

        // displaying currently set Url
        serverEditText.setText(bPreferences.getString("server_url", ""));

        serverEditText.addTextChangedListener(new MyTextWatcher(serverEditText, reg_ex_pattern));

        storePrefsButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                String server_holder = serverEditText.getText().toString();
                String username_holder = usernameEditText.getText().toString();
                String password_holder = passwordEditText.getText().toString();
                Boolean submit_flag = false;

                // Ensure well formed Url using RegEx. If not no submission
                // allowed
                if (!server_holder.equals("")) {
                    bEditor.putString("server_url", server_holder);
                    if (Pattern.matches(reg_ex_pattern, serverEditText.getText())) {
                        submit_flag = true;

                    }
                }

                /*
                 * this logic allows us to leave field blank without saving
                 * empty string to preferences
                 */
                if (!username_holder.equals("")) {
                    bEditor.putString("username", username_holder);
                }
                if (!password_holder.equals("")) {
                    bEditor.putString("password", password_holder);
                }
                if (submit_flag) {
                    bEditor.commit();
                    showToast("Preferences Saved");
                } else {
                    showToast("You Input An Invalid Server Url. Preferences Not Saved");
                }
            }

        });
    }

    /*
     * This will display warning tool tip when URL is malformed
     */
    class MyTextWatcher implements TextWatcher {

        private final EditText mEditText;
        private final String regex;

        private MyTextWatcher(EditText editText, String rx) {
            mEditText = editText;
            regex = rx;
        }

        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            switch (mEditText.getId()) {
                case R.id.serverUrlEditText: {
                    if (!Pattern.matches(regex, s)) {
                        mEditText.setError(sTooltipMessage);
                    }
                    break;
                }
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
