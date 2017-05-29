package com.bachors.kodepos;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by user on 5/28/2017.
 */

public class KotaActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private String TAG = KotaActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;

    // URL to get kodepos list JSON
    private static String url;

    ArrayList<HashMap<String, String>> posList;
    // array untuk menampung data filter
    ArrayList<HashMap<String, String>> tmpList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kota);

        Bundle b = getIntent().getExtras();
        // get id propinsi
        url = "https://kodepos-2d475.firebaseio.com/kota_kab/" + b.getString("idp") + ".json";

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

        // filter berdasarkan nama kecamatan atau kelurahan
        final EditText filter = (EditText) findViewById(R.id.filter);
        filter.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String title = filter.getText().toString().trim().toLowerCase();
                if(!TextUtils.isEmpty(title)) {
                    // clear tmpList
                    tmpList = new ArrayList<>();
                    for (int i = 0; i < posList.size(); i++) {
                        String kecamatan = posList.get(i).get("kecamatan").toLowerCase();
                        String kelurahan = posList.get(i).get("kelurahan").toLowerCase();
                        if (kecamatan.contains(title) || kelurahan.contains(title))  {
                            tmpList.add(posList.get(i));
                        }
                    }
                    // menampilkan hasil filter
                    ListAdapter adapter = new SimpleAdapter(
                            KotaActivity.this, tmpList,
                            R.layout.list_pos, new String[]{"kecamatan", "kelurahan", "kodepos"}, new int[]{R.id.kecamatan, R.id.kelurahan, R.id.kodepos});

                    lv.setAdapter(adapter);
                } else {
                    // menampilkan original data
                    ListAdapter adapter = new SimpleAdapter(
                            KotaActivity.this, posList,
                            R.layout.list_pos, new String[]{"kecamatan", "kelurahan", "kodepos"}, new int[]{R.id.kecamatan, R.id.kelurahan, R.id.kodepos});

                    lv.setAdapter(adapter);
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });
    }

    // Async task class to get json by making HTTP call
    private class GetPost extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(KotaActivity.this);
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
                    JSONArray data = new JSONArray(jsonStr);

                    // Parser object array
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject c = data.getJSONObject(i);

                        String kecamatan = c.getString("kecamatan");
                        String kelurahan = c.getString("kelurahan");
                        String kodepos = c.getString("kodepos");

                        HashMap<String, String> pos = new HashMap<>();

                        // adding each child node to HashMap key => value
                        pos.put("kecamatan", kecamatan);
                        pos.put("kelurahan", kelurahan);
                        pos.put("kodepos", kodepos);

                        // adding contact to contact list
                        posList.add(pos);
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
                    return lhs.get("kecamatan").compareTo(rhs.get("kecamatan"));
                }
            });

            // Updating parsed JSON data into ListView
            ListAdapter adapter = new SimpleAdapter(
                    KotaActivity.this, posList,
                    R.layout.list_pos, new String[]{"kecamatan", "kelurahan", "kodepos"}, new int[]{R.id.kecamatan, R.id.kelurahan, R.id.kodepos});

            lv.setAdapter(adapter);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // mengambil idp & name dari list yang kita click
        String kode = posList.get(position).get("kodepos");
        Toast.makeText(getApplicationContext(), kode, Toast.LENGTH_LONG).show();
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