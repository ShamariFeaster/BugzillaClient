
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class BaseActivity extends Activity {
    /** Called when the activity is first created. */

    static final DefaultHttpClient sHttpclient = new DefaultHttpClient();
    protected final String TAG = this.getClass().getSimpleName();
    protected SharedPreferences mPreferences;
    protected SharedPreferences.Editor mEditor;
    protected String mServer_url = "";
    protected String mUsername = "";
    protected String mPassword = "";
    final protected int LOGIN_STATUS_CODE_GOOD = 200;

    /*
     * Constructs a proper formatted JSON array to hold parameters Bugzilla
     * Webservice API functions using JSON-RPC create Map(s) with the key/value
     * parameter pairs and pass the to this function then pass the return value
     * of this function to createJSONBugzillaRequest() for properly formed JSON
     * requests. To pass an array of values it must a List object
     */
    protected JSONArray createBugzillaParamsArray(Map... vararg_array) {
        JSONObject json_params_object_child = new JSONObject();
        JSONArray json_params_values_array = new JSONArray();
        List json_list = null;
        Boolean value_is_array = false;
        for (int var_arg_index = 0; var_arg_index < vararg_array.length; var_arg_index++) {
            Map this_map = vararg_array[var_arg_index];
            for (Iterator<Map> map_iterator = this_map.entrySet().iterator(); map_iterator
                    .hasNext();) {
                Map.Entry map_entry = (Map.Entry) map_iterator.next();
                if (map_entry.getValue() instanceof Collection<?>) {
                    json_list = (List) map_entry.getValue();
                    JSONArray tester = new JSONArray(json_list);
                    value_is_array = true;
                }
                try {
                    if (!value_is_array) {
                        json_params_object_child.put((String) map_entry.getKey(),
                                map_entry.getValue());
                    } else {
                        json_params_object_child.put((String) map_entry.getKey(),
                                new JSONArray(json_list));
                    }
                } catch (JSONException e1) {
                    Log.w("createBugzillaParamsArray()",

                            "Something is wrong with the JSON you passed in : first try block");

                }
            }
        }
        try {
            json_params_values_array.put(0, json_params_object_child);
        } catch (JSONException e) {
            Log.w("createBugzillaParamsArray()",
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

    protected JSONObject createBugzillaJSONRequestObject(JSONObject id,
            JSONObject method, JSONArray params) {

        JSONObject json_request_object = new JSONObject();
        try {
            json_request_object.put("id", id.get("id"));
            json_request_object.put("method", method.get("method"));
            json_request_object.put("params", params);
        } catch (JSONException e) {

            Log.w("createBugzillaJSONRequest()",
                    "Something is wrong with the JSON you passed in");

        }
        return json_request_object;
    }

    /*
     * This is used to login. It returns a JSONObject with two objects:
     * "response", whose value is another JSONObject that is returned by
     * Bugzilla. The second object is "status_code" which is the HTTP status
     * code returned by Bugzilla. This can be fed to the
     * buzillaStatusCodeTranslator() for a string readout of the error (i.e to
     * put in a toast to notify the user)
     */
    protected JSONObject loginToBugzilla(String username, String password,
            String server_url, DefaultHttpClient client) {
        JSONObject json_login_object = null;
        JSONObject json_reponse_object = new JSONObject();
        Map<String, String> login_map = new HashMap<String, String>();
        Map<String, String> password_map = new HashMap<String, String>();
        login_map.put("login", username);
        password_map.put("password", password);

        try {// block 1
            json_login_object = createBugzillaJSONRequestObject(new JSONObject(
                    "{\"id\":1}"),
                    new JSONObject("{\"method\":\"User.login\"}"),
                    createBugzillaParamsArray(login_map, password_map));
        } catch (JSONException e) {
            Log.w(TAG,
                    "loginToBugzilla() Error: UnsupportedEncodingException Caught");
        }
        json_reponse_object = makeBugzillaPostRequest(json_login_object, server_url, client);
        return json_reponse_object;
    }

    // protected String getAccessableProducts() { }
    /*
     * This function returns a JSONObject with two objects: "response", whose
     * value is another JSONObject that is returned by Bugzilla. The second
     * object is "status_code" which is the HTTP status code returned by
     * Bugzilla. This can be fed to the buzillaStatusCodeTranslator() for a
     * string readout of the error (i.e to put in a toast to notify the user)
     */
    protected JSONObject makeBugzillaPostRequest(JSONObject json_rpc_object, String server_url,
            DefaultHttpClient client) {
        HttpEntity http_entity = null;
        HttpResponse http_response = null;
        int http_status_val = 0;
        JSONObject json_response_object = new JSONObject();

        try {// block 1
            StringEntity post_entity = new StringEntity(
                    json_rpc_object.toString());
            HttpPost post_request = new HttpPost(server_url);
            post_request.setEntity(post_entity);
            try {// block 2
                http_response = client.execute(post_request);
            } catch (IllegalStateException e) {
                Log.w(TAG,
                        "block 2 makeBugzillaPostRequest() Error: IllegalStateException Caught");
            } catch (ClientProtocolException e2) {
                Log.w(TAG,
                        "block 2 makeBugzillaPostRequest() Error: ClientProtocolException Caught");
            } catch (IOException e2) {
                Log.w(TAG,
                        "block 2 makeBugzillaPostRequest() Error: IOException Caught");
            }

        } catch (UnsupportedEncodingException e) {
            Log.w(TAG,
                    "block 1 makeBugzillaPostRequest() Error: UnsupportedEncodingException Caught");
        }

        if (http_response != null) {
            http_entity = http_response.getEntity();
            StringBuilder builder = new StringBuilder();
            StatusLine http_response_status = http_response.getStatusLine();
            http_status_val = http_response_status.getStatusCode();
            InputStream http_entity_content = null;
            try {// block 4
                http_entity_content = http_entity.getContent();
            } catch (IllegalStateException e1) {
                Log.w(TAG,
                        "block 4 makeBugzillaPostRequest() Error: IllegalStateException Caught");
            } catch (IOException e1) {
                Log.w(TAG,
                        "block 4 makeBugzillaPostRequest() Error: IOException Caught");
            }

            BufferedReader buffer_reader = new BufferedReader(
                    new InputStreamReader(http_entity_content));
            String buffer_line;
            try {// block 5
                while ((buffer_line = buffer_reader.readLine()) != null) {
                    builder.append(buffer_line);
                }
            } catch (IOException e) {
                Log.w(TAG,
                        "block 5 makeBugzillaPostRequest() Error: IOException Caught");
            }

            try {// block 6
                json_response_object.put("response", builder.toString());
                json_response_object.put("status_code", http_status_val);
                Log.w(TAG, json_response_object.toString());
            } catch (JSONException e) {
                Log.w(TAG,
                        "block 6 makeBugzillaPostRequest() Error: JSONException Caught");
            }
        }

        if (http_entity != null) {
            try {// block 7
                http_entity.consumeContent();
            } catch (IOException e) {
                Log.w(TAG,
                        "block 7 makeBugzillaPostRequest() Error: IOException Caught");
            }
        }

        List<Cookie> cookies = client.getCookieStore().getCookies();
        if (cookies.isEmpty()) {
            Log.w(TAG, "No cookies returned - Line ~ 94");
        } else {
            for (int cookie_index = 0; cookie_index < cookies.size(); cookie_index++) {
                Log.w(TAG, cookies.get(cookie_index).toString());
            }
        }

        return json_response_object;
    }

    /*
     * feed this the object returned from moakeBugzillaPostReqests
     */
    protected Boolean isBugzillaError(JSONObject response) {
        String error_message;
        Boolean isError = false;
        try {
            // otherwise get the response object and see if there
            // was a login error
            JSONObject response_object =
                    new JSONObject(response.get("response").toString());
            // if there is, get the message out and display to user
            if (!response_object.isNull("error")) {
                JSONObject error_object =

                        new JSONObject(response_object.get("error").toString());

                error_message = error_object.get("message").toString();
                showToast(error_message);
                isError = true;
            }
        } catch (JSONException e) {
            Log.w(TAG, "isBugzillaError JSONException: " +
                    "Error retireveing 'response' or 'error' object value");

            showToast("An unspecified error occured");
            isError = true;
        }

        return isError;
    }

    /*
     * PARAMETER CREATION HELPER FUNCTIONS Use the following functions to create
     * key/value pairs that are used as parameters for
     * createBugzillaParametersArray().
     */
    protected Map<String, String> stringStringMap(String key, String value) {
        Map<String, String> string_string_map = new HashMap<String,
                String>();
        string_string_map.put(key, value);
        return string_string_map;
    }

    protected Map<String, Integer> stringIntegerMap(String key, Integer value) {
        Map<String, Integer> string_int_map = new HashMap<String,
                Integer>();
        string_int_map.put(key, value);
        return string_int_map;
    }

    protected Map<String, List<Integer>> stringIntegerListMap(String key, Integer[] int_array) {
        Map<String, List<Integer>> string_integer_list_map = new HashMap<String,
                List<Integer>>();
        List<Integer> intergerList = new ArrayList<Integer>();
        intergerList = Arrays.asList(int_array);
        string_integer_list_map.put(key, intergerList);
        return string_integer_list_map;
    }

    protected Map<String, List<String>> stringStringListMap(String key, String[] str_array) {
        Map<String, List<String>> string_string_list_map = new HashMap<String,
                List<String>>();
        List<String> stringList = new ArrayList<String>();
        stringList = Arrays.asList(str_array);
        string_string_list_map.put(key, stringList);
        return string_string_list_map;
    }

    protected void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = getSharedPreferences("preferences", MODE_WORLD_READABLE);
        mEditor = mPreferences.edit();
        mServer_url = mPreferences.getString("server_url", "default");

        mServer_url += "jsonrpc.cgi";

        mUsername = mPreferences.getString("username", "default");
        mPassword = mPreferences.getString("password", "default");

    }

}
