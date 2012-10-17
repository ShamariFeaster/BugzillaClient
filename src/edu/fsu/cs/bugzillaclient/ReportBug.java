
package edu.fsu.cs.bugzillaclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

/* This is launched by DisplayProducts.java. It is sent with a single extra, "product_name"
 * which is used throughout this class
 */
public class ReportBug extends BaseActivity {
    final String TAG = this.getClass().getSimpleName();
    List<String> mComponentList = new ArrayList<String>();
    List<String> mVersionList = new ArrayList<String>();
    List<String> mSeverityList = new ArrayList<String>();
    List<String> mPlatformList = new ArrayList<String>();
    List<String> mOpSysList = new ArrayList<String>();
    List<String> mPriorityList = new ArrayList<String>();
    List<String> mStatusList = new ArrayList<String>();
    String mProductName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_bug_layout);
        // Get info sent from launching activity
        Intent fromDisplayProducts = getIntent();
        mProductName = fromDisplayProducts.getStringExtra("product_name");

        Spinner componentSpinner = (Spinner) findViewById(R.id.createBugComponent);
        Spinner versionSpinner = (Spinner) findViewById(R.id.createBugVersion);
        Spinner opSysSpinner = (Spinner) findViewById(R.id.createBugOpSys);
        Spinner severitySpinner = (Spinner) findViewById(R.id.createBugSeverity);
        Spinner platformSpinner = (Spinner) findViewById(R.id.createBugPlatform);
        Spinner prioritySpinner = (Spinner) findViewById(R.id.createBugPriority);
        Spinner statusSpinner = (Spinner) findViewById(R.id.createBugStatus);
        final TextView assignedToTextView = (TextView)
                findViewById(R.id.createBugAssignedToContent);
        final TextView summaryTextView = (TextView)
                findViewById(R.id.createBugSummaryContent);
        final TextView descriptionTextView = (TextView)
                findViewById(R.id.createBugDecriptionEditText);
        Button submitButton = (Button) findViewById(R.id.createBugSubmitButton);

        JSONObject json_bug_fields_response;
        JSONObject json_result;
        JSONObject json_field;
        JSONArray json_array_fields;
        String field_name_string;

        // Getting valid field values from the server
        json_bug_fields_response = bugzillaGetValidFields(new String[] {
                "component", "version", "op_sys", "bug_severity",
                "rep_platform", "priority", "bug_status"
        });

        /*
         * Populating field value lists to be fed to Spinner adapter
         */
        if (!isBugzillaError(json_bug_fields_response)) {
            try {// try block 1
                json_result = bugzillaGetResultFromResponse(json_bug_fields_response);
                json_array_fields = json_result.getJSONArray("fields");

                for (int field_idx = 0; field_idx < json_array_fields.length(); field_idx++) {
                    json_field = json_array_fields.getJSONObject(field_idx);
                    field_name_string = json_field.getString("name");

                    if (field_name_string.equals("component")) {
                        bugzillaBuildProductSpecificFieldList(json_field,
                                mComponentList, mProductName);
                        // go to the next field
                        continue;
                    }

                    if (field_name_string.equals("version")) {
                        bugzillaBuildProductSpecificFieldList(json_field,
                                mVersionList, mProductName);
                        // go to the next field
                        continue;
                    }

                    if (field_name_string.equals("bug_severity")) {
                        bugzillaBuildFieldList(json_field, mSeverityList);
                        // go to the next field
                        continue;
                    }

                    if (field_name_string.equals("rep_platform")) {

                        bugzillaBuildFieldList(json_field, mPlatformList);
                        // go to the next field
                        continue;
                    }

                    if (field_name_string.equals("op_sys")) {
                        bugzillaBuildFieldList(json_field, mOpSysList);
                        // go to the next field
                        continue;
                    }

                    if (field_name_string.equals("priority")) {
                        bugzillaBuildFieldList(json_field, mPriorityList);
                        // go to the next field
                        continue;
                    }

                    if (field_name_string.equals("bug_status")) {
                        bugzillaBuildFieldList(json_field, mStatusList);
                        // go to the next field
                        continue;
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
                log("JSONException try block 1");
            }

        }

        componentSpinner.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, mComponentList));
        versionSpinner.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, mVersionList));
        opSysSpinner.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, mOpSysList));
        severitySpinner.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, mSeverityList));
        platformSpinner.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, mPlatformList));
        prioritySpinner.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, mPriorityList));
        statusSpinner.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, mStatusList));

        // Setting "default" values of the spinners
        componentSpinner.setSelection(0);
        versionSpinner.setSelection(0);
        opSysSpinner.setSelection(0);
        severitySpinner.setSelection(0);
        platformSpinner.setSelection(0);
        prioritySpinner.setSelection(0);
        statusSpinner.setSelection(0);

        summaryTextView.requestFocus();
        // Let user know they can leave this field blank
        assignedToTextView.setOnFocusChangeListener(new OnFocusChangeListener() {

            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    showToast("Leave \"Assigned To\" Blank To Send Bug To Default" +
                            "Assignee");
                }

            }
        });

        final MyItemClickListener spinner_click_listener = new MyItemClickListener();

        componentSpinner.setOnItemSelectedListener(spinner_click_listener);
        versionSpinner.setOnItemSelectedListener(spinner_click_listener);
        opSysSpinner.setOnItemSelectedListener(spinner_click_listener);
        severitySpinner.setOnItemSelectedListener(spinner_click_listener);
        platformSpinner.setOnItemSelectedListener(spinner_click_listener);
        prioritySpinner.setOnItemSelectedListener(spinner_click_listener);
        statusSpinner.setOnItemSelectedListener(spinner_click_listener);

        // Handle submit button press
        submitButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                String assigned_to = assignedToTextView.getText().toString();
                String summary = summaryTextView.getText().toString();
                String description = descriptionTextView.getText().toString();
                Boolean submit_flag = true;

                // No submission if no description given
                if (description.equals("")) {
                    submit_flag = false;
                }

                // Get data from form and submit it to bugzilla
                if (submit_flag) {
                    JSONObject json_response = makeBugzillaPostRequest(
                            createBugzillaJSONRequestObject(
                                    new JSONObject(stringIntegerMap("id", 1)),
                                    new JSONObject(stringStringMap("method",
                                            "Bug.create")),
                                    createBugzillaParamsArray(
                                            stringStringMap("assigned_to", assigned_to),
                                            stringStringMap("description", description),
                                            stringStringMap("product", mProductName),
                                            stringStringMap("summary", summary),
                                            stringStringMap("component", spinner_click_listener
                                                            .getSpinnerValue("component")),
                                            stringStringMap("version", spinner_click_listener
                                                            .getSpinnerValue("version")),
                                            stringStringMap("bug_severity", spinner_click_listener
                                                            .getSpinnerValue("severity")),
                                            stringStringMap("rep_platform", spinner_click_listener
                                                            .getSpinnerValue("platform")),
                                            stringStringMap("op_sys", spinner_click_listener
                                                            .getSpinnerValue("op_sys")),
                                            stringStringMap("priority", spinner_click_listener
                                                            .getSpinnerValue("priority")),
                                            stringStringMap("bug_status", spinner_click_listener
                                                            .getSpinnerValue("status")))),
                                                            bServer_url, BaseActivity.sbHttpclient);

                    // If bug creation was successful show the new bug in a bug
                    // list
                    if (!isBugzillaError(json_response)) {
                        Intent intent = new Intent(getApplicationContext(), DisplayBugs.class);
                        intent.putExtra("search_by", "product");
                        intent.putExtra("search_by_value", mProductName);
                        startActivity(intent);
                    }
                } else {
                    showToast("You Must Write A Description Of The Bug To Submit It.");
                }

            }
        });
    }

    /*
     * Tutorial subject matter on using one listener for multiple spinners. I
     * looked all over the new and counldn't find a single one. if
     * setSelection() is used on spinners, onItemSelected() will be ran once for
     * each call to setSelection().
     */
    private class MyItemClickListener implements OnItemSelectedListener {
        private Map<String, String> mSelectionMap = new HashMap<String, String>();
        private String mValueHolder;

        public String getSpinnerValue(String key) {
            return mSelectionMap.get(key);
        }

        public Set getSpinnerMapKeySet() {
            return mSelectionMap.keySet();
        }

        public void onItemSelected(AdapterView<?> parent, View view,
                int position, long id) {
            View view_parent = (View) view.getParent();
            mValueHolder = (String) parent.getItemAtPosition(position);
            switch (view_parent.getId()) {
                case R.id.createBugComponent:
                    mSelectionMap.put("component", mValueHolder);
                    break;
                case R.id.createBugVersion:
                    mSelectionMap.put("version", mValueHolder);
                    break;
                case R.id.createBugOpSys:
                    mSelectionMap.put("op_sys", mValueHolder);
                    break;
                case R.id.createBugSeverity:
                    mSelectionMap.put("severity", mValueHolder);
                    break;
                case R.id.createBugPlatform:
                    mSelectionMap.put("platform", mValueHolder);
                    break;
                case R.id.createBugPriority:
                    mSelectionMap.put("priority", mValueHolder);
                    break;
                case R.id.createBugStatus:
                    mSelectionMap.put("status", mValueHolder);
                    break;
            }
            mValueHolder = "";
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
}
