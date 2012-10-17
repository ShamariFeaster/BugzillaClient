
package edu.fsu.cs.bugzillaclient;

import java.util.concurrent.ExecutionException;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/* Main navigation for the application once user is logged in 
 */
public class HomeScreen extends BaseActivity {
    final String TAG = this.getClass().getSimpleName();

    private final Context mContext = this;

    static Boolean sFlag_products_cached = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen_layout);
        Button seeMyBugsButton = (Button)findViewById(R.id.SeeMyBugsButton);
        Button createBugButton = (Button)findViewById(R.id.CreateBugButton);
        Button browseBugsButton = (Button)findViewById(R.id.BrowseBugsButton);
        /*
         * avoiding expensive webservice calls by caching them in super class
         * member variables
         */
        if (!sFlag_products_cached) {
            AsyncTask<Void, Void, JSONObject> async_obj = new BugzillaAsyncGetAccessableProducts()
                    .execute();

            try {// try block 1
                sbJsonAccessibleProducts = async_obj.get();
                sFlag_products_cached = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                log("Try block 1 InterruptedException");
            } catch (ExecutionException e) {
                e.printStackTrace();
                log("Try block 1 ExecutionException");
            }
        }
        // SEE MY BUGS
        seeMyBugsButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DisplayBugs.class);
                intent.putExtra("search_by", "assigned_to");
                intent.putExtra("search_by_value", bUsername);
                bLoadingDialog = showProgressDialog(mContext, "Loading Bugs");
                startActivity(intent);

            }
        });

        // FILE A BUG
        createBugButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (!isBugzillaError(sbJsonAccessibleProducts)) {
                    Intent intent = new Intent(getApplicationContext(), DisplayProducts.class);
                    intent.putExtra("json_products_response", sbJsonAccessibleProducts.toString());
                    intent.putExtra("create_bug", true);
                    startActivity(intent);
                }
            }
        });

        // BROWSE BUGS BY PRODUCT
        browseBugsButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (!isBugzillaError(sbJsonAccessibleProducts)) {
                    Intent intent = new Intent(getApplicationContext(), DisplayProducts.class);
                    intent.putExtra("json_products_response", sbJsonAccessibleProducts.toString());
                    intent.putExtra("create_bug", false);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        dismissProgressDialog(bLoadingDialog);
    }
}
