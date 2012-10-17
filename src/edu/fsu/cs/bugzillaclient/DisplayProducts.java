
package edu.fsu.cs.bugzillaclient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class DisplayProducts extends BaseActivity {
    final String TAG = this.getClass().getSimpleName();
    private ListView mProductsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_product_bugs_listview_layout);

        mProductsListView = (ListView) findViewById(R.id.productBugsListView);
        TextView topTitleText = (TextView) findViewById(R.id.product_bugs_listview_layout_title);
        TextView bottomTitleText = (TextView) findViewById(R.id.productBugsListViewTitleBar);
        topTitleText.setText("");
        bottomTitleText.setText("Your Available Products");
        /*
         * Getting info sent from launching activity (i.e HomeScreen.java).
         * There are multiple child activities that may be launched from this
         * activity so we use info from the launching activity to tell us where
         * to go from here, when a product is selected from our product list.
         */
        Intent fromHomeScreen = getIntent();
        // if "create_bug" is sent as true we branch to ReportBug.java
        final Boolean create_bug = fromHomeScreen.getBooleanExtra("create_bug", false);

        // intermediary objects for traversing json objects sent from bugzilla
        JSONObject json_from_homescreen;
        JSONObject json_response;
        JSONObject json_result;
        JSONArray json_array_products;
        JSONObject json_product;
        JSONObject json_internals;
        // used for the ListView adapters
        List<String> product_id_list = new ArrayList<String>();
        List<String> product_name_list = new ArrayList<String>();
        try {
            /*
             * HomeScreen.java sends the result from
             * Product.get_accessable_products over as the intent extra
             * "json_product_response". I use new JSONObject constructors rather
             * than using get() and casting to JSONObjects, which causes class
             * cast errors
             */
            json_from_homescreen =
                    new JSONObject(fromHomeScreen.getStringExtra("json_products_response"));
            json_response = new JSONObject(json_from_homescreen.get("response").toString());
            json_result = new JSONObject(json_response.get("result").toString());
            json_array_products = json_result.getJSONArray("products");

            for (int array_idx = 0; array_idx < json_array_products.length(); array_idx++) {
                json_product = (JSONObject) json_array_products.get(array_idx);
                json_internals = (JSONObject) json_product.get("internals");
                product_id_list.add(json_internals.getString("id"));
                product_name_list.add(json_internals.getString("name"));
            }

        } catch (JSONException e) {
            log("Problem parsing response from Products.get_accessable_products");
            showToast("There was a problem retrieving your available products.");
        }
        mProductsListView.setAdapter(new ProductAdapter(this, product_id_list, product_name_list));
        mProductsListView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder holder = new ViewHolder();
                holder.mProductNameTextView = (TextView) view.findViewById(R.id.productName);
                String product_name = holder.mProductNameTextView.getText().toString();

                Intent intent = null;
                if (create_bug == false) {
                    intent = new Intent(getApplicationContext(), DisplayBugs.class);
                    intent.putExtra("search_by", "product");
                    intent.putExtra("search_by_value", product_name);
                } else {
                    intent = new Intent(getApplicationContext(), ReportBug.class);
                    intent.putExtra("product_name", product_name);
                }
                if (intent != null) {
                    startActivity(intent);
                }

            }
        });
    }

    private class ProductAdapter extends BaseAdapter {
        private List<String> mIdList = new ArrayList<String>();
        private List<String> mNameList = new ArrayList<String>();
        private Context mContext;
        private LayoutInflater mInflater;

        public ProductAdapter(Context context, List<String> ids, List<String> names) {
            mIdList = ids;
            mNameList = names;
            mContext = context;
        }

        public int getCount() {

            return mIdList.size();
        }

        public Object getItem(int position) {

            return null;
        }

        public long getItemId(int position) {

            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                mInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.listview_item_product, null);
                holder = new ViewHolder();
                holder.mProductNameTextView = (TextView) convertView.findViewById(R.id.productName);
                holder.mProductIdTextView = (TextView) convertView.findViewById(R.id.ProductId);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String id_holder = "Product ID: " + mIdList.get(position);
            holder.mProductNameTextView.setText(mNameList.get(position));
            holder.mProductIdTextView.setText(id_holder);
            return convertView;
        }

    }

    public static class ViewHolder {
        public TextView mProductNameTextView;
        public TextView mProductIdTextView;
    }
}
