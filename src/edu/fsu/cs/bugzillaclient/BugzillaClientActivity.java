
package edu.fsu.cs.bugzillaclient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class BugzillaClientActivity extends Activity {
    /** Called when the activity is first created. */
    final String TAG = "Demo";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dummy);
        final EditText methodFromInput = (EditText) findViewById(R.id.MethodEditText1);
        Button sendRequestButton = (Button) findViewById(R.id.SendRequestButton1);

        sendRequestButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpEntity entity = null;
                String login = null;
                HttpResponse response = null;

                String serverURL = "http://mobiserve.cs.fsu.edu/bugzilla/jsonrpc.cgi";
                String method = "?method=Product.get_selectable_products";
                String params = "&params=";
                String thisMethod = methodFromInput.getText().toString();
                login =
                        "[{ \"Bugzilla_login\": \"alistproducer2@gmail.com\"," +
                                "\"Bugzilla_password\": \"_drumnba422\"," +
                                " \"Bugzilla_rememberLogin\":\"true\"}]";

                try {
                    login = URLEncoder.encode(login, "utf-8");
                } catch (UnsupportedEncodingException e3) {
                    // TODO Auto-generated catch block
                    e3.printStackTrace();
                }
                serverURL += method + params + login;

                Log.d(TAG, serverURL);
                HttpGet httpget = new HttpGet(serverURL);

                try {
                    response = httpclient.execute(httpget);
                } catch (ClientProtocolException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                } catch (IOException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }

                if (response != null) {
                    entity = response.getEntity();
                    StringBuilder builder = new StringBuilder();
                    StatusLine status = response.getStatusLine();
                    int statusval = status.getStatusCode();
                    Log.d(TAG, "status: " + Integer.toString(statusval));
                    Log.d(TAG, entity.getContentType().toString());
                    InputStream content = null;
                    try {
                        content = entity.getContent();
                    } catch (IllegalStateException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    BufferedReader reader = new BufferedReader(
                                     new InputStreamReader(content));
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "response: " + builder.toString());
                }

                if (entity != null) {
                    try {
                        entity.consumeContent();
                    } catch (IOException e) {
                        Log.d(TAG, "Error: UnsupportedEncodingException Caught - Line ~ 86");
                    }
                }

                System.out.println("Post logon cookies:");
                List<Cookie> cookies = httpclient.getCookieStore().getCookies();
                if (cookies.isEmpty()) {
                    Log.d(TAG, "No cookies returned - Line ~ 94");
                } else {
                    for (int i = 0; i < cookies.size(); i++) {
                        Log.d(TAG, cookies.get(i).toString());
                    }
                }

                // When HttpClient instance is no longer needed,
                // shut down the connection manager to ensure
                // immediate deallocation of all system resources
                // httpclient.getConnectionManager().shutdown();

            }
        });
    }
}
