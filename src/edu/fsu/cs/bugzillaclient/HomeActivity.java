
package edu.fsu.cs.bugzillaclient;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

public class HomeActivity extends BaseActivity {
    final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen_layout);

        JSONObject a = new JSONObject(stringIntegerMap("id", 1));
        Log.w(TAG, a.toString());

        JSONObject json_request_object = createBugzillaJSONRequestObject(
                new JSONObject(stringIntegerMap("id", 1)),
                new JSONObject(stringStringMap("method", "Product.get")),
                createBugzillaParamsArray(stringIntegerListMap("ids", new Integer[] {
                        3
                })));

        JSONObject json_response_from_bugzilla = makeBugzillaPostRequest(json_request_object,
                mServer_url,
                BaseActivity.sHttpclient);

        if (!isBugzillaError(json_response_from_bugzilla)) {
            Log.w(TAG, json_response_from_bugzilla.toString());

        }

    }
}
