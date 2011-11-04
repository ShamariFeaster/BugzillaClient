
package edu.fsu.cs.bugzillaclient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BugzillaClientActivity extends Activity {
    /** Called when the activity is first created. */
    final String TAG = "Demo";
    Boolean first_login = true;
    static final DefaultHttpClient mHttpclient = new DefaultHttpClient();
    final String mServerURL = "http://mobiserve.cs.fsu.edu/bugzilla/jsonrpc.cgi";

    /*
     * Constructs a proper formatted JSON array to hold parameters Bugzilla
     * Webservice API functions using JSON-RPC create maps with the key/value
     * parameter pairs and pass the to this function then pass the return value
     * of this function to createJSONBugzillaRequest() forproperly formed JSON
     * requests
     */
    private JSONArray createBugzillaParamsArray(Map... arguments) {
        JSONObject json_params_object_child = new JSONObject();
        JSONArray json_params_values_array = new JSONArray();

        for (int var_arg_counter = 0; var_arg_counter < arguments.length; var_arg_counter++) {
            Map this_map = arguments[var_arg_counter];
            for (Iterator<Map> iter = this_map.entrySet().iterator(); iter.hasNext();) {
                Map.Entry mapEntry = (Map.Entry) iter.next();
                try {
                    json_params_object_child.put((String) mapEntry.getKey(), mapEntry.getValue());
                } catch (JSONException e1) {
                    Log.v("createBugzillaParamsArray()",
                            "Something is wrong with the JSON you passed in : first try block");
                }
            }
        }
        try {
            json_params_values_array.put(0, json_params_object_child);
        } catch (JSONException e) {
            Log.v("createBugzillaParamsArray()",
                    "Something is wrong with the JSON you passed in : second try block");
        }
        return json_params_values_array;
    }

    /*
     * Constructs a proper formatted JSON object to send to Bugzilla Webservice
     * API id - the "key" of the object should be the string "id" method - the
     * "key" of the object should be the string "method" params - construct
     * using createBugzillaParamsArray(Map... arguments)
     */
    private JSONObject createBugzillaJSONRequest(JSONObject id, JSONObject method, JSONArray params) {
        JSONArray json_request_array = new JSONArray();
        JSONObject json_request_object = new JSONObject();
        try {
            json_request_object.put("id", id.get("id"));
            json_request_object.put("method", method.get("method"));
            json_request_object.put("params", params);
        } catch (JSONException e) {
            Log.v("createBugzillaJSONRequest()", "Something is wrong with the JSON you passed in");
        }
        return json_request_object;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dummy);
        Button sendRequestButton = (Button) findViewById(R.id.SendRequestButton1);

        sendRequestButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                HttpEntity entity = null;
                HttpResponse response = null;
                String username = "alistproducer2@gmail.com";
                String password = "_drumnba422";

                JSONObject json_login = new JSONObject();
                JSONArray paramArray = new JSONArray();
                JSONObject rcp_call = new JSONObject();
                try {
                    json_login.put("login", username);
                    json_login.put("password", password);
                    paramArray.put(0, json_login);
                    rcp_call.put("params", paramArray);
                    rcp_call.put("method", "User.login");
                    rcp_call.put("id", 1);
                    String login = rcp_call.toString();
                    Log.d(TAG, login);
                    try {
                        StringEntity stringEntity = new StringEntity(login);
                        HttpPost post = new HttpPost(mServerURL);
                        post.setEntity(stringEntity);
                        try {
                            response = mHttpclient.execute(post);
                        } catch (ClientProtocolException e2) {
                            e2.printStackTrace();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e4) {
                    e4.printStackTrace();
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
                        Log.d(TAG,
                                "Error: UnsupportedEncodingException Caught - Line ~ 86");
                    }
                }

                List<Cookie> cookies = mHttpclient.getCookieStore().getCookies();
                if (cookies.isEmpty()) {
                    Log.d(TAG, "No cookies returned - Line ~ 94");
                } else {
                    first_login = false;
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
