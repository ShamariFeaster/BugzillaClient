
package edu.fsu.cs.bugzillaclient;

import org.json.JSONException;

import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

public class HomeScreen extends BaseActivity {
    final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen_layout);

        JSONObject a = new JSONObject(stringIntegerMap("id", 1));
        Log.w(TAG, a.toString());

        JSONObject json_request_object = createBugzillaJSONRequestObject(
                new JSONObject(stringIntegerMap("id", 1)),
                new JSONObject(stringStringMap("method", "Bug.search")),
                createBugzillaParamsArray(stringStringMap("assigned_to",
                        "channiganfsu@gmail.com")));

        JSONObject json_response_from_bugzilla = makeBugzillaPostRequest(json_request_object,
                mServer_url,
                BaseActivity.sHttpclient);

        if (!isBugzillaError(json_response_from_bugzilla)) {
            Log.w(TAG, json_response_from_bugzilla.toString());

        } else {
            showToast("There was an error with your request");

        }

    }
}
