
package edu.fsu.cs.bugzillaclient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/* This class is made generic by accepting two extras:
 * 1.) "search_by" which is the criteria used to select bugs from the bugzilla installation
 *  example: if a button is pressed in some other activity that says "show all windows bugs"
 *  it should send "op_sys" as the value to the "search_by" extra 
 * 2.) "search_by_value" is the value being searched for within the context of the "search_by"
 *  parameter.
 *  example:  continuing with the above example, to get all windows bugs the value of 
 *  "search_by_value" should be "windows". 
 */
public class DisplayBugs extends BaseActivity {
    final String TAG = this.getClass().getSimpleName();

    private Context mContext = this;

    private ListView mBugsListView;

    static Boolean flag_bugs_cached = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_product_bugs_listview_layout);

        // Views
        mBugsListView = (ListView)findViewById(R.id.productBugsListView);
        TextView bugTitleText = (TextView)findViewById(R.id.product_bugs_listview_layout_title);
        TextView bugInfoText = (TextView)findViewById(R.id.productBugsListViewTitleBar);

        // get info sent from launching activity
        Intent fromIntent = getIntent();
        String search_by = fromIntent.getStringExtra("search_by");
        String search_by_value = fromIntent.getStringExtra("search_by_value");

        // Changes the title of the listview depending on the search
        String title = search_by;
        if ("assigned_to".equals(search_by)) {
            bugTitleText.setText("Bugs");
            title = "Assigned To You";
        }
        if ("op_sys".equals(search_by)) {
            title = "Operating System";
        }
        if ("creation_time".equals(search_by)) {
            title = "Creation Time";
        }
        if ("product".equals(search_by)) {
            bugTitleText.setText("Displaying The Bugs Of");
            title = search_by_value;
        }
        bugInfoText.setText(title);
        // end title formatting

        // variables
        JSONObject json_bug;
        JSONArray json_array_my_bugs = new JSONArray();
        List<String> bug_severity_list = new ArrayList<String>();
        List<String> bug_status_list = new ArrayList<String>();
        List<String> bug_summary_list = new ArrayList<String>();
        List<Integer> bug_id_list = new ArrayList<Integer>();
        // end variables

        AsyncTask<Void, Void, JSONArray> async_obj = new BugzillaAsyncSearchBugsBy(search_by,
                search_by_value).execute();
        dismissProgressDialog(bLoadingDialog);
        // Retrieve the bugs and pull the info we want
        try {
            /*
             * Moved dialog from home screen to here to try and avoid blocking
             * tentative
             */

            json_array_my_bugs = async_obj.get();

        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        }

        log(json_array_my_bugs.toString());
        if (json_array_my_bugs.length() > 0) {
            try {// try block 1
                for (int array_idx = 0; array_idx < json_array_my_bugs.length(); array_idx++) {
                    json_bug = (JSONObject)json_array_my_bugs.get(array_idx);
                    bug_severity_list.add(json_bug.getString("severity"));
                    bug_status_list.add(json_bug.getString("status"));
                    bug_summary_list.add(json_bug.getString("summary"));
                    bug_id_list.add(json_bug.getInt("id"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                log("JSONException while loading ListView Adapters: try block 1");
            }

            // instantiate ListView adapter with the bug info
            final BugAdapter myAdapter = new BugAdapter(this, bug_severity_list, bug_status_list,
                    bug_summary_list, bug_id_list);

            mBugsListView.setAdapter(myAdapter);

            mBugsListView.setOnItemClickListener(new OnItemClickListener() {

                // Launch SingleBug.java if ListView item (i.e a bug) is pressed
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Integer bug_id = (Integer)myAdapter.getItem(position);
                    Intent intent = new Intent(getApplicationContext(), SingleBug.class);
                    intent.putExtra("bug_id", bug_id);
                    startActivity(intent);
                }
            });

        } else {
            showToast("There Are No Bugs That Match Your Search Criteria.");
        }
    }

    /*
     * Customized Adapter that takes multiple data structures -- Blog Post
     * Material --
     */
    private class BugAdapter extends BaseAdapter {
        private List<String> mSeverityList = new ArrayList<String>();

        private List<String> mStatusList = new ArrayList<String>();

        private List<String> mSummaryList = new ArrayList<String>();

        private List<Integer> mIdList = new ArrayList<Integer>();

        private final Context mContext;

        private LayoutInflater mInflater;

        public BugAdapter(Context context, List<String> severity, List<String> status,
                List<String> summary, List<Integer> id) {
            mSeverityList = severity;
            mStatusList = status;
            mSummaryList = summary;
            mIdList = id;
            mContext = context;
        }

        public int getCount() {
            return mSeverityList.size();
        }

        public Object getItem(int position) {
            return mIdList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                mInflater = (LayoutInflater)mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.listview_item_product_bug, null);
                holder = new ViewHolder();
                holder.bugSeverity = (TextView)convertView.findViewById(R.id.productBugSeverity);
                holder.bugStatus = (TextView)convertView.findViewById(R.id.productBugStatus);
                holder.bugSummary = (TextView)convertView.findViewById(R.id.productBugSummary);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            String severity_holder = "Severity: " + mSeverityList.get(position);
            String status_holder = "Status: " + mStatusList.get(position);
            holder.bugSeverity.setText(severity_holder);
            holder.bugStatus.setText(status_holder);
            holder.bugSummary.setText(mSummaryList.get(position));
            return convertView;
        }

    }

    // used for ListView Recycling
    public static class ViewHolder {
        public TextView bugSeverity;

        public TextView bugStatus;

        public TextView bugSummary;
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if (bLoadingDialog != null && bLoadingDialog.isShowing()) {
            bLoadingDialog.dismiss();
        }
    }
}
