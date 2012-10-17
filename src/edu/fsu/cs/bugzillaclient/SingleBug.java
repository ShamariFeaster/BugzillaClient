
package edu.fsu.cs.bugzillaclient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SingleBug extends BaseActivity {
    final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_bug_layout);

        // Get info sent from launching activity
        Intent fromListViewSelection = getIntent();
        Integer bug_id = fromListViewSelection.getIntExtra("bug_id", 0);
        TextView bugIdTextView = (TextView) findViewById(R.id.showBugBugId);
        TextView productTitleTextView = (TextView) findViewById(R.id.showBugProductTitle);
        TextView severityTextView = (TextView) findViewById(R.id.showBugSeverity);
        TextView versionTextView = (TextView) findViewById(R.id.showBugVersion);
        TextView componentTextView = (TextView) findViewById(R.id.showBugComponent);
        TextView opSysTextView = (TextView) findViewById(R.id.showBugOpSys);
        TextView bugCreatorTextView = (TextView) findViewById(R.id.showBugCreator);
        TextView bugResolutionTextView = (TextView) findViewById(R.id.showBugResolution);
        TextView bugReportedTextView = (TextView) findViewById(R.id.showBugReported);

        // Variables used for parsing Bug.get response JSON object
        JSONObject json_response_obj;
        JSONObject json_result_obj;
        JSONArray json_array_bugs;
        JSONObject json_bug;

        // Fetch the bug info from bugzilla
        JSONObject json_bug_get_response = makeBugzillaPostRequest(createBugzillaJSONRequestObject(
                new JSONObject(stringIntegerMap("id", 1)),
                new JSONObject(stringStringMap("method", "Bug.get")),
                createBugzillaParamsArray(stringIntegerListMap("ids", new Integer[] {
                        bug_id
                }))), bServer_url, BaseActivity.sbHttpclient);

        try { // try block 1
            // Parse Response
            json_response_obj = new JSONObject(json_bug_get_response.get("response").toString());
            json_result_obj = new JSONObject(json_response_obj.get("result").toString());
            json_array_bugs = json_result_obj.getJSONArray("bugs");
            json_bug = (JSONObject) json_array_bugs.get(0);
            // Fill In Layout
            bugIdTextView.setText(json_bug.getString("id"));
            productTitleTextView.setText(json_bug.getString("product"));
            severityTextView.setText(json_bug.getString("severity"));
            versionTextView.setText(json_bug.getString("version"));
            componentTextView.setText(json_bug.getString("component"));
            opSysTextView.setText(json_bug.getString("op_sys"));
            bugCreatorTextView.setText(json_bug.getString("creator"));
            bugResolutionTextView.setText(json_bug.getString("resolution"));
            bugReportedTextView.setText(json_bug.getString("creation_time"));
        } catch (JSONException e) {
            e.printStackTrace();
            log("Error parsing JSON: try block 1");
        }

        // Fetch comments for this bug
        JSONObject bug_comments_response = makeBugzillaPostRequest(createBugzillaJSONRequestObject(
                new JSONObject(stringIntegerMap("id", 1)),
                new JSONObject(stringStringMap("method", "Bug.comments")),
                createBugzillaParamsArray(stringIntegerListMap("ids", new Integer[] {
                        bug_id
                }))), bServer_url, BaseActivity.sbHttpclient);

        // Variables used for parsing Bug.comments response JSON object
        JSONObject json_query_response;
        JSONObject json_response;
        JSONObject json_result;
        JSONObject json_bugs;
        JSONObject json_bug_id;
        JSONArray json_array_comments;
        JSONObject json_comment;
        try {
            LinearLayout scrollViewLayout = (LinearLayout) findViewById(R.id.scrollViewLinearLayout);

            log(bug_comments_response.toString());
            json_query_response = new JSONObject(bug_comments_response.toString());
            json_response = new JSONObject(json_query_response.get("response").toString());
            json_result = new JSONObject(json_response.get("result").toString());
            json_bugs = new JSONObject(json_result.get("bugs").toString());
            json_bug_id = new JSONObject(json_bugs.get(bug_id.toString()).toString());
            json_array_comments = json_bug_id.getJSONArray("comments");
            for (int array_idx = 0; array_idx < json_array_comments.length(); array_idx++) {
                json_comment = (JSONObject) json_array_comments.get(array_idx);
                TextView authorTextView = new TextView(this);
                authorTextView.setText(json_comment.getString("author"));
                authorTextView.setTextColor(Color.RED);
                TextView timeTextView = new TextView(this);
                timeTextView.setText(json_comment.getString("time"));
                TextView commentTextView = new TextView(this);
                commentTextView.setText(json_comment.getString("text"));
                scrollViewLayout.addView(authorTextView);
                scrollViewLayout.addView(timeTextView);
                scrollViewLayout.addView(commentTextView);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
