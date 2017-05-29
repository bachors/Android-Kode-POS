package com.bachors.kodepos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by user on 5/28/2017.
 */

public class PropinsiActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private String TAG = PropinsiActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;

    // URL to get kota list JSON
    private static String url;

    ArrayList<HashMap<String, String>> posList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_propinsi);

        Bundle b = getIntent().getExtras();
        // get id propinsi
        url = "https://kodepos-2d475.firebaseio.com/list_kotakab/" + b.getString("idp") + ".json";

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        // set toolbar title
        actionBar.setTitle(b.getString("name"));

        // menampung data kota
        posList = new ArrayList<>();

        // listview untuk menampilkan daftar kota
        lv = (ListView) findViewById(R.id.list_pos);
        lv.setOnItemClickListener(this);

        // Get JSON data
        new GetPost().execute();
    }

    // Async task class to get json by making HTTP call
    private class GetPost extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(PropinsiActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Parser flat object
                    Iterator<String> iter = jsonObj.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        try {
                            Object value = jsonObj.get(key);
                            String nama = value.toString();

                            // tmp hash map for single GetPost
                            HashMap<String, String> pos = new HashMap<>();

                            // adding each child node to HashMap key => value
                            pos.put("name", nama);
                            pos.put("idp", key);

                            // adding pos to pos list
                            posList.add(pos);

                        } catch (JSONException e) {
                            // Something went wrong!
                        }
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Data tidak ada.",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Terjadi kesalahan coba lain waktu.",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            // Sorting a-z
            Collections.sort(posList, new Comparator<HashMap< String,String >>() {

                @Override
                public int compare(HashMap<String, String> lhs,
                                   HashMap<String, String> rhs) {
                    return lhs.get("name").compareTo(rhs.get("name"));
                }
            });

            // Updating parsed JSON data into ListView
            ListAdapter adapter = new SimpleAdapter(
                    PropinsiActivity.this, posList,
                    R.layout.list_daerah, new String[]{"name"}, new int[]{R.id.name});

            lv.setAdapter(adapter);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // mengambil idp & name dari list yang kita click
        String idp = posList.get(position).get("idp");
        String name = posList.get(position).get("name");

        // mengirim parameter idp & name ke KotaActivity
        Intent intent = new Intent(PropinsiActivity.this, KotaActivity.class);
        intent.putExtra("idp", posList.get(position).get("idp"));
        intent.putExtra("name", posList.get(position).get("name"));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}