
package edu.fsu.cs.bugzillaclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/* <----------README--------->
 *  This class contains all the utility functions used throughout the application
 * as all other activities extend from this one.
 * 
 * Application-wide variable naming conventions:
 * 
 * 1. All Android specific variables are named using camel-case
 * 2. All Class member variables start with "m"
 * 3. All static variables start with "s"
 * 4. All BaseActivity member variables have "b" in the beginning
 * 5. Variables may have more than one starting marker i.e sbMyVar (static & BaseActivity member) 
 * 6. All non-Android specific variables are named using "_" syntax
 * 7. All JSON objects start with "json_"
 * 8. Method parameters may or may not follow the preceding conventions
 */
public class BaseActivity extends Activity {

    // static variables
    static final DefaultHttpClient sbHttpclient = new DefaultHttpClient();

    static JSONObject sbJsonAccessibleProducts = new JSONObject();

    static Boolean sLoggedIn = false;

    // android variables
    protected SharedPreferences bPreferences;

    protected SharedPreferences.Editor bEditor;

    protected ProgressDialog bLoadingDialog = null;

    // String variables
    protected final String TAG = this.getClass().getSimpleName();

    protected String bServer_url = "";

    protected String bUsername = "";

    protected String bPassword = "";

    // number variables
    final protected int LOGIN_STATUS_CODE_GOOD = 200;

    // Used to determine execution times
    private long bStartTime;

    private long bEndTime;

    /*
     * Constructs a proper formatted JSON array to hold parameters Bugzilla
     * Webservice API functions using JSON-RPC create Map(s) with the key/value
     * parameter pairs and pass the to this function then pass the return value
     * of this function to createJSONBugzillaRequest() for properly formed JSON
     * requests. To pass an array of values it must a List object
     */
    protected JSONArray createBugzillaParamsArray(Map<String, ?>... vararg_array) {
        String tag = "createBugzillaParamsArray(): ";
        JSONObject json_params_object_child = new JSONObject();
        JSONArray json_array_params_value = new JSONArray();
        List json_list = null;
        Boolean value_is_array = false;
        // Cycle through varargs Maps
        for (int var_arg_index = 0; var_arg_index < vararg_array.length; var_arg_index++) {
            Map this_map = vararg_array[var_arg_index];
            // Iterate through Map
            for (Iterator<Map> map_iterator = this_map.entrySet().iterator(); map_iterator
                    .hasNext();) {
                Map.Entry map_entry = (Map.Entry)map_iterator.next();
                // is the value a list?
                if (map_entry.getValue() instanceof Collection<?>) {
                    try {
                        json_list = (List)map_entry.getValue();
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                        log(tag + "Failure: Your map value was not a List. Exiting Function");
                        return json_array_params_value;
                    }
                    value_is_array = true;
                }
                try {
                    if (!value_is_array) {
                        json_params_object_child.put((String)map_entry.getKey(),
                                map_entry.getValue());
                    } else {
                        json_params_object_child.put((String)map_entry.getKey(), new JSONArray(
                                json_list));
                    }
                } catch (JSONException e1) {
                    log(tag + "Something is wrong with the JSON you passed in : first try block");
                    e1.printStackTrace();
                }
            }
        }
        try {
            json_array_params_value.put(0, json_params_object_child);
        } catch (JSONException e) {
            log(tag + "Something is wrong with the JSON you passed in : second try block");
            e.printStackTrace();
        }
        return json_array_params_value;
    }

    /*
     * Constructs a proper formatted JSON object to send to Bugzilla Webservice
     * API id - the "key" of the object should be the string "id" method - the
     * "key" of the object should be the string "method" params - construct
     * using createBugzillaParamsArray(Map... arguments). For functions that
     * need no parameters just pass an new JSONArray() object.
     */

    protected JSONObject createBugzillaJSONRequestObject(JSONObject id, JSONObject method,
            JSONArray params) {
        String tag = "createBugzillaJSONRequestObject(): ";
        JSONObject json_request_object = new JSONObject();
        try {
            json_request_object.put("id", id.get("id"));
            json_request_object.put("method", method.get("method"));
            json_request_object.put("params", params);
        } catch (JSONException e) {

            log(tag + "Something is wrong with the JSON you passed in");
            e.printStackTrace();

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
    protected JSONObject loginToBugzilla(String username, String password, String server_url,
            DefaultHttpClient client) {

        String tag = "loginToBugzilla(): ";
        JSONObject json_login_object = null;
        JSONObject json_reponse_object = new JSONObject();

        json_login_object = createBugzillaJSONRequestObject(
                new JSONObject(stringIntegerMap("id", 1)),
                new JSONObject(stringStringMap("method", "User.login")),
                createBugzillaParamsArray(stringStringMap("login", username),
                        stringStringMap("password", password)));

        json_reponse_object = makeBugzillaPostRequest(json_login_object, server_url, client);
        return json_reponse_object;
    }

    /*
     * As the name suggests we login asynchronously because the task takes over
     * a second
     */
    protected class BugzillaAsyncLogin extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject json_login_response = loginToBugzilla(bUsername, bPassword, bServer_url,
                    BaseActivity.sbHttpclient);
            return json_login_response;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (!isBugzillaError(result)) {
                sLoggedIn = true;
                showToast("You're Logged In To Bugzilla");
                startActivity(new Intent(getApplicationContext(), HomeScreen.class));
            } else {
                dismissProgressDialog(bLoadingDialog);
            }
        }

    }

    protected class BugzillaAsyncLogout extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject json_logout_response = makeBugzillaPostRequest(
                    createBugzillaJSONRequestObject(new JSONObject(stringIntegerMap("id", 1)),
                            new JSONObject(stringStringMap("method", "User.logout")),
                            new JSONArray()), bServer_url, BaseActivity.sbHttpclient);
            return json_logout_response;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (!isBugzillaError(result)) {
                sLoggedIn = false;
                showToast("You're Logged Out Of Bugzilla");
                dismissProgressDialog(bLoadingDialog);
            }
        }
    }

    /*
     * As the name suggests we login asynchronously because the task takes over
     * a second
     */
    protected class BugzillaAsyncGetAccessableProducts extends AsyncTask<Void, Void, JSONObject> {

        private JSONObject json_accessable_products_response;

        @Override
        protected JSONObject doInBackground(Void... params) {
            json_accessable_products_response = bugzillaGetAccessibleProducts();
            return json_accessable_products_response;
        }

    }

    protected class BugzillaAsyncSearchBugsBy extends AsyncTask<Void, Void, JSONArray> {
        String search_by;

        String search_by_value;

        private JSONArray json_search_results;

        public BugzillaAsyncSearchBugsBy(String sb, String sbv) {
            search_by = sb;
            search_by_value = sbv;
        }

        @Override
        protected JSONArray doInBackground(Void... params) {
            json_search_results = bugzillaSearchBugsBy(search_by, search_by_value);
            return json_search_results;
        }

    }

    /*
     * This function returns a JSONObject with two objects: "response", whose
     * value is another JSONObject that is returned by Bugzilla. The second
     * object is "status_code" which is the HTTP status code returned by
     * Bugzilla. This can be fed to the buzillaStatusCodeTranslator() for a
     * string readout of the error (i.e to put in a toast to notify the user).
     * It should be noted that the structure of this object returned is not
     * exactly what bugzilla returns as the "status" object has been added by
     * me.
     */
    protected JSONObject makeBugzillaPostRequest(JSONObject json_rpc_object, String server_url,
            DefaultHttpClient client) {
        logExecutionTimeStart();
        String tag = "makeBugzillaPostRequest(): ";
        HttpEntity http_entity = null;
        HttpResponse http_response = null;
        int http_status_val = 0;
        JSONObject json_response_object = new JSONObject();

        try {// block 1
            StringEntity post_entity = new StringEntity(json_rpc_object.toString());
            HttpPost post_request = new HttpPost(server_url);
            post_request.setEntity(post_entity);
            try {// block 2
                http_response = client.execute(post_request);
            } catch (IllegalStateException e) {
                log("block 2" + tag + "Error: IllegalStateException Caught");
                e.printStackTrace();
            } catch (ClientProtocolException e2) {
                log("block 2" + tag + "Error: ClientProtocolException Caught");
                e2.printStackTrace();
            } catch (IOException e2) {
                log("block 2 " + tag + "Error: IOException Caught");
                e2.printStackTrace();
            }

        } catch (UnsupportedEncodingException e) {
            log("block 1 " + tag + "Error: UnsupportedEncodingException Caught");
            e.printStackTrace();
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
                log("block 4 " + tag + "Error: IllegalStateException Caught");
                e1.printStackTrace();
            } catch (IOException e1) {
                log("block 4 " + tag + "Error: IOException Caught");
                e1.printStackTrace();
            }

            BufferedReader buffer_reader = new BufferedReader(new InputStreamReader(
                    http_entity_content));
            String buffer_line;
            try {// block 5
                while ((buffer_line = buffer_reader.readLine()) != null) {
                    builder.append(buffer_line);
                }
            } catch (IOException e) {
                log("block 5 " + tag + "Error: IOException Caught");
                e.printStackTrace();
            }

            try {// block 6
                json_response_object.put("response", builder.toString());
                json_response_object.put("status_code", http_status_val);
                Log.w(TAG, json_response_object.toString());
            } catch (JSONException e) {
                log("block 6 " + tag + "Error: JSONException Caught");
                e.printStackTrace();
            }
        }

        if (http_entity != null) {
            try {// block 7
                http_entity.consumeContent();
            } catch (IOException e) {
                log("block 7 " + tag + "Error: IOException Caught");
                e.printStackTrace();
            }
        }

        List<Cookie> cookies = client.getCookieStore().getCookies();
        if (cookies.isEmpty()) {
            log("No cookies returned - Line ~ 94");
        } else {
            for (int cookie_index = 0; cookie_index < cookies.size(); cookie_index++) {
                log(tag + cookies.get(cookie_index).toString());
            }
        }
        logExecutionTimeEnd();
        return json_response_object;
    }

    /*
     * Wrapper for Bug.search. See bugzilla webservices API to see exactly how
     * the server will treat your search request. Feed this function and valid
     * search parameter and the search value. It will return the result of the
     * query as a JSONArray. If your search parameter is not a valid one it
     * fails gracefully, prints to log telling you so, and returns an empty
     * JSONArray. If your parameter is valid but there are no results it just
     * returns an empty JSONArray, as bugzilla normally would, but no error
     * message is printed to logcat.
     */
    protected JSONArray bugzillaSearchBugsBy(String parameter, String value) {

        JSONObject json_search_response;
        JSONObject json_response_obj;
        JSONObject json_result_obj;
        JSONArray json_bugs_array = new JSONArray();

        JSONObject json_bug_search_response = makeBugzillaPostRequest(
                createBugzillaJSONRequestObject(new JSONObject(stringIntegerMap("id", 1)),
                        new JSONObject(stringStringMap("method", "Bug.search")),
                        createBugzillaParamsArray(stringStringMap(parameter, value))), bServer_url,
                BaseActivity.sbHttpclient);

        try {
            json_search_response = new JSONObject(json_bug_search_response.toString());
            json_response_obj = new JSONObject(json_search_response.get("response").toString());
            // if bugzilla cries about bad paramter we fail gracefully
            if (json_response_obj.isNull("error")) {
                json_result_obj = new JSONObject(json_response_obj.get("result").toString());
                json_bugs_array = json_result_obj.getJSONArray("bugs");
            } else {
                log("bugzillaSearchBy(): " + parameter + "is not a valid search parameter");
            }

        } catch (JSONException e) {
            log("bugzillaSearchBy(): error parsing json response");
            e.printStackTrace();
        }

        return json_bugs_array;

    }

    /*
     * Overload of the above
     */
    protected JSONArray bugzillaSearchBugsBy(String parameter, Integer[] int_array) {

        JSONObject json_search_response;
        JSONObject json_response_obj;
        JSONObject json_result_obj;
        JSONArray json_bugs_array = new JSONArray();

        JSONObject bug_search_response = makeBugzillaPostRequest(
                createBugzillaJSONRequestObject(new JSONObject(stringIntegerMap("id", 1)),
                        new JSONObject(stringStringMap("method", "Bug.search")),
                        createBugzillaParamsArray(stringIntegerListMap(parameter, int_array))),
                bServer_url, BaseActivity.sbHttpclient);

        try {
            json_search_response = new JSONObject(bug_search_response.toString());
            json_response_obj = new JSONObject(json_search_response.get("response").toString());
            // if bugzilla cries about bad paramter we fail gracefully
            if (json_response_obj.isNull("error")) {
                json_result_obj = new JSONObject(json_response_obj.get("result").toString());
                json_bugs_array = json_result_obj.getJSONArray("bugs");
            } else {
                log("bugzillaSearchBy(): " + parameter + "is not a valid search parameter");
            }

        } catch (JSONException e) {
            log("bugzillaSearchBy(): error parsing json response");
            e.printStackTrace();
        }

        return json_bugs_array;

    }

    // Overload of the above
    protected JSONArray bugzillaSearchBugsBy(String parameter, String[] str_array) {
        String tag = "bugzillaSearchBugsBy()";
        JSONObject json_search_response;
        JSONObject json_response_obj;
        JSONObject json_result_obj;
        JSONArray json_array_bugs = new JSONArray();

        JSONObject json_bug_search_response = makeBugzillaPostRequest(
                createBugzillaJSONRequestObject(new JSONObject(stringIntegerMap("id", 1)),
                        new JSONObject(stringStringMap("method", "Bug.search")),
                        createBugzillaParamsArray(stringStringListMap(parameter, str_array))),
                bServer_url, BaseActivity.sbHttpclient);

        try {
            json_search_response = new JSONObject(json_bug_search_response.toString());
            json_response_obj = new JSONObject(json_search_response.get("response").toString());
            // if bugzilla cries about bad paramter we fail gracefully
            if (json_response_obj.isNull("error")) {
                json_result_obj = new JSONObject(json_response_obj.get("result").toString());
                json_array_bugs = json_result_obj.getJSONArray("bugs");
            } else {
                log(tag + parameter + "is not a valid search parameter");
            }

        } catch (JSONException e) {
            log(tag + "error parsing json response");
            e.printStackTrace();
        }

        return json_array_bugs;

    }

    /*
     * Get all products the user can see and modify
     */
    protected JSONObject bugzillaGetAccessibleProducts() {
        String tag = "bugzillaGetAccessibleProducts()";
        // getting product ids
        JSONArray json_array_ids = null;
        JSONObject json_request_object = createBugzillaJSONRequestObject(new JSONObject(
                stringIntegerMap("id", 1)),
                new JSONObject(stringStringMap("method", "Product.get_accessible_products")),
                new JSONArray());

        JSONObject accessible_products_response = makeBugzillaPostRequest(json_request_object,
                bServer_url, BaseActivity.sbHttpclient);

        if (!isBugzillaError(accessible_products_response)) {
            log(accessible_products_response.toString());
            try {
                json_array_ids = new JSONObject(new JSONObject(accessible_products_response.get(
                        "response").toString()).get("result").toString()).getJSONArray("ids");
            } catch (JSONException e) {
                log(tag + "Error getting ids json array");
                e.printStackTrace();
            }
        } else {
            showToast("There was an error getting the available products");
        }

        // getting product names
        JSONObject json_product_get_response = null;
        if (json_array_ids != null) {
            JSONObject json_get_request = createBugzillaJSONRequestObject(new JSONObject(
                    stringIntegerMap("id", 1)),
                    new JSONObject(stringStringMap("method", "Product.get")),
                    createBugzillaParamsArray(stringJSONArrayMap("ids", json_array_ids)));
            // log("json_get_request: " + json_get_request.toString());
            json_product_get_response = makeBugzillaPostRequest(json_get_request, bServer_url,
                    BaseActivity.sbHttpclient);
        }
        return json_product_get_response;
    }

    /*
     * Parses a response from the server and gets the result object. Always
     * check the response using isBugzillaError before calling this function. It
     * won't crash if you don't but it just makes sense to do so.
     */
    protected JSONObject bugzillaGetResultFromResponse(JSONObject json_response_obj) {
        String tag = "bugzillaGetResultFromResponse(): ";
        JSONObject json_result = new JSONObject();
        try {
            JSONObject json_response = new JSONObject(json_response_obj.get("response").toString());
            json_result = new JSONObject(json_response.get("result").toString());
        } catch (JSONException e) {
            e.printStackTrace();
            log(tag + "Error parsing JSON");
        }
        return json_result;
    }

    /*
     * returns a JSONObject with all the valid values for the fields you entered
     * if a field name you entered isn't valid, the returned object will have an
     * error message which you can see if you feed the response to
     * isBugzillaError()
     */
    protected JSONObject bugzillaGetValidFields(String[] field_names) {
        JSONObject json_bug_fields_response = makeBugzillaPostRequest(
                createBugzillaJSONRequestObject(new JSONObject(stringIntegerMap("id", 1)),
                        new JSONObject(stringStringMap("method", "Bug.fields")),
                        createBugzillaParamsArray(stringStringListMap("names", field_names))),
                bServer_url, BaseActivity.sbHttpclient);
        return json_bug_fields_response;
    }

    /*
     * This would be used to populate a list to be used as an Adapter for a
     * Spinner Adapter. Because the Bug.fields RPC call is listed as unstable in
     * the API This function has a high probability of future-proof failure
     */
    protected void bugzillaBuildFieldList(JSONObject json_field_obj, List<String> list) {
        String tag = "bugzillaBuildFieldList(): ";
        try {
            JSONArray json_array_values = json_field_obj.getJSONArray("values");
            for (int values_idx = 0; values_idx < json_array_values.length(); values_idx++) {
                JSONObject json_value = json_array_values.getJSONObject(values_idx);
                list.add(json_value.getString("name"));
                log(json_value.getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            log(tag + "Error parsing JSON");
        }
    }

    /*
     * This would be used to populate a list to be used as an Adapter for a
     * Spinner Adapter. Because the Bug.fields RPC call is listed as unstable in
     * the API This function has a high probability of future-proof failure. As
     * the name suggests, use this function on fields that have different values
     * depending on the product
     */
    protected void bugzillaBuildProductSpecificFieldList(JSONObject json_field_obj,
            List<String> list, String product_name) {
        String tag = "bugzillaBuildProductSpecificFieldList(): ";
        String temp_product_name;
        try {
            JSONArray json_array_values = json_field_obj.getJSONArray("values");
            for (int values_idx = 0; values_idx < json_array_values.length(); values_idx++) {
                JSONObject json_value = json_array_values.getJSONObject(values_idx);
                temp_product_name = (json_value.getJSONArray("visibility_values")).get(0)
                        .toString();
                if (temp_product_name.equals(product_name)) {
                    list.add(json_value.getString("name"));
                    log(json_value.getString("name"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            log(tag + "Error parsing JSON");
            log(tag + json_field_obj.toString());
        }
    }

    /*
     * Feed this the object returned from moakeBugzillaPostReqests It will
     * display the error that came back from the bugzilla server as a toast, or
     * log the JSON object that caused the error if the response is malformed,
     * i.e it didn't come from a bugzilla installation, you tried to call a
     * function that doesn't exist, ect.
     */
    protected Boolean isBugzillaError(JSONObject json_response) {
        String tag = "isBugzillaError(): ";
        String error_message;
        Boolean isError = false;
        JSONObject json_response_object = new JSONObject();
        try {
            // otherwise get the response object and see if there
            // was a login error
            json_response_object = new JSONObject(json_response.get("response").toString());
            // if there is, get the message out and display to user
            if (!json_response_object.isNull("error")) {
                JSONObject error_object =

                new JSONObject(json_response_object.get("error").toString());

                error_message = error_object.get("message").toString();
                showToast(error_message);
                isError = true;
            }
        } catch (JSONException e) {
            log(tag + "JSONException: " + "Error retireveing 'response' or 'error' object value");
            log(tag + "this was passed in: " + json_response.toString());
            showToast("An unspecified error occured. See Logcat for more details");
            isError = true;
            e.printStackTrace();
        }

        return isError;
    }

    /*
     * PARAMETER CREATION HELPER FUNCTIONS Use the following functions to create
     * key/value pairs that are used as parameters for
     * createBugzillaParametersArray().
     */
    protected Map<String, String> stringStringMap(String key, String value) {
        Map<String, String> string_string_map = new HashMap<String, String>();
        string_string_map.put(key, value);
        return string_string_map;
    }

    protected Map<String, Integer> stringIntegerMap(String key, Integer value) {
        Map<String, Integer> string_int_map = new HashMap<String, Integer>();
        string_int_map.put(key, value);
        return string_int_map;
    }

    protected Map<String, List<Integer>> stringIntegerListMap(String key, Integer[] int_array) {
        Map<String, List<Integer>> string_integer_list_map = new HashMap<String, List<Integer>>();
        List<Integer> intergerList = new ArrayList<Integer>();
        intergerList = Arrays.asList(int_array);
        string_integer_list_map.put(key, intergerList);
        return string_integer_list_map;
    }

    protected Map<String, List<String>> stringStringListMap(String key, String[] str_array) {
        Map<String, List<String>> string_string_list_map = new HashMap<String, List<String>>();
        List<String> stringList = new ArrayList<String>();
        stringList = Arrays.asList(str_array);
        string_string_list_map.put(key, stringList);
        return string_string_list_map;
    }

    protected Map<String, JSONArray> stringJSONArrayMap(String key, JSONArray json_array) {
        Map<String, JSONArray> string_json_array_map = new HashMap<String, JSONArray>();
        string_json_array_map.put(key, json_array);
        return string_json_array_map;
    }

    /*
     * Quick way to show a toast
     */
    protected void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    /*
     * Quick way to log something. Ideally the class extending from this one
     * should have it's own TAG but this class lends its TAG to any of its
     * children that don't so the function doesn't crash.
     */
    protected void log(String message) {
        Log.w(this.TAG, message);
    }

    /*
     * Used to see log execution times
     */
    protected void logExecutionTimeStart() {
        bStartTime = android.os.SystemClock.uptimeMillis();
    }

    protected void logExecutionTimeEnd() {
        bEndTime = android.os.SystemClock.uptimeMillis();
        log("Excution time: " + (bEndTime - bStartTime) + " ms");
    }

    /*
     * Used to show and dismiss Progress dialogs during long loads
     */
    protected ProgressDialog showProgressDialog(Context context, String message) {
        return (ProgressDialog.show(context, "", message, true));
    }

    protected void dismissProgressDialog(ProgressDialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_application, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(getApplicationContext(), SettingsScreen.class));
                return true;
            case R.id.menu_home_screen:
                Intent i = new Intent();
                i.setClass(this, LoginScreen.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bPreferences = getSharedPreferences("preferences", MODE_PRIVATE);
        bEditor = bPreferences.edit();
        bServer_url = bPreferences.getString("server_url", "default");
        bServer_url += "jsonrpc.cgi";
        bUsername = bPreferences.getString("username", "default");
        bPassword = bPreferences.getString("password", "default");
    }

    @Override
    protected void onResume() {
        super.onResume();
        bServer_url = bPreferences.getString("server_url", "default");
        bServer_url += "jsonrpc.cgi";
        bUsername = bPreferences.getString("username", "default");
        bPassword = bPreferences.getString("password", "default");
    }

}
