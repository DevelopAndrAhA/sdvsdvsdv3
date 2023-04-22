package com.lesa_humdet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.lesa_humdet.db.AlohaDb;
import com.lesa_humdet.db.City;
import com.lesa_humdet.db.Country;
import com.lesa_humdet.db.Region;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ResultSearchActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    int REQUEST_OVERLAY_PERMISSION=1000;
    MyPermissions per = new MyPermissions(ResultSearchActivity.this,ResultSearchActivity.this);
    OkHttpClient client = new OkHttpClient();
    private String [] array;
    Conf conf = new Conf();


    SharedPreferences mSettings;
    SharedPreferences.Editor editor;
    JSONArray jsonArray;
    ListView listView = null;
    List<String[]> list = null;
    List<JSONObject[]> jsonObjects = null;
    SwipeRefreshLayout swipeRefreshLayout = null;
    TextView textView = null;
    TextView textView3 = null;


    Button searchBtn = null;
    Button trainBtn = null;
    int city_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getSharedPreferences(conf.getShared_pref_name(), Context.MODE_PRIVATE);
        editor = mSettings.edit();
        setContentView(R.layout.activity_result_search);
        int langTmp = mSettings.getInt(conf.getLANG(),0);


        String custom_font = "fonts/JackportRegularNcv.ttf";
        Typeface CF = Typeface.createFromAsset(getAssets(), custom_font);
        textView = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        textView.setTypeface(CF);
        textView3.setTypeface(CF);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorScheme(R.color.purple_200, R.color.teal_200, R.color.purple_200, R.color.teal_200);

        int lang = mSettings.getInt(conf.getLANG(),0);
        if(lang==conf.getRU()){
            array = getResources().getStringArray(R.array.app_lang_ru);
        }else if(lang==conf.getEN()){
            array = getResources().getStringArray(R.array.app_lang_en);
        }else if(lang==conf.getAR()){
            array = getResources().getStringArray(R.array.app_lang_ar);
        }else{
            array = getResources().getStringArray(R.array.app_lang_ru);
        }
        boolean main = getIntent().getBooleanExtra("main",false);
        if(main){
            textView.setText(array[22]);//metka 2
            getSupportActionBar().setTitle(array[39]);
        }else{
            textView.setText(array[16]);
            textView3.setText(array[34]);
            getSupportActionBar().setTitle(array[7]);
        }

        searchBtn = findViewById(R.id.button3);
        trainBtn = findViewById(R.id.button4);
        searchBtn.setText(array[2]);
        trainBtn.setText(array[11]);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),SearchActivity.class);
                startActivity(intent );
            }
        });
        trainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),NeuroTrainingActivity.class);
                startActivity(intent );
            }
        });

        ImageButton img_activity = findViewById(R.id.img_activity);
        img_activity.setOnClickListener(e -> {
            if(isOnline()){
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent );
            }else{
                Toast.makeText(ResultSearchActivity.this,array[35],Toast.LENGTH_LONG).show();
            }
        });
        ImageButton settings_btn = findViewById(R.id.settings_btn);
        settings_btn.setOnClickListener(e -> {
            Intent intent2 = new Intent(this,SettingsActivity.class);
            startActivity(intent2);
        });
        listView = findViewById(R.id.lisView);
        list = new ArrayList<String[]>();
        jsonObjects = new ArrayList<JSONObject[]>();
        per.getMyApplicationPermissions();
        try{
            jsonArray = new JSONArray(getIntent().getStringExtra("jsonArray"));
        }catch (Exception e){}
        try{
            if(jsonArray!=null){
                for(int i=0;i<jsonArray.length();i++){
                    String[] urlMas3 = new String[3];
                    JSONObject [] jsonObject = new JSONObject[3];
                    try{
                        urlMas3[0] = jsonArray.getJSONObject(i).getString("photoName");
                        jsonObject[0] = jsonArray.getJSONObject(i);
                    }catch (Exception e){}
                    try{
                        urlMas3[1] = jsonArray.getJSONObject(i+1).getString("photoName");
                        jsonObject[1] = jsonArray.getJSONObject(i+1);
                        i = i+1;
                    }catch (Exception e){}
                    try{
                        urlMas3[2] = jsonArray.getJSONObject(i+1).getString("photoName");
                        jsonObject[2] = jsonArray.getJSONObject(i+1);
                        i = i+1;
                    }catch (Exception e){}

                    list.add(urlMas3);
                    jsonObjects.add(jsonObject);

                    CustomArrayAdapter adapter = new CustomArrayAdapter(ResultSearchActivity.this, list,jsonObjects,jsonArray.toString());
                    runOnUiThread(new Runnable() {
                        public void run() {
                            adapter.alertDialogBuilder();
                        }
                    });
                    listView.setAdapter(adapter);
                    listView.setClickable(false);
                }
            }
            if(list==null || list.size()==0 || jsonArray==null) textView.setVisibility(View.VISIBLE);
        }catch (Exception e){}


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(this)){
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(Settings.canDrawOverlays(this)){
                try{
                    Intent intentService = new Intent(ResultSearchActivity.this,MyService.class);
                    startService(intentService);
                }catch (Exception e){}
            }else{
                Toast.makeText(ResultSearchActivity.this,array[30],Toast.LENGTH_LONG).show();
            }
        }

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
        if(langTmp==0){
            showAlertDialog();
        }
        if(isOnline()&&jsonArray==null){
            getDataWithoutMap(true);
            new StatusOfBanner().execute();
        }else if(!isOnline()){
            Toast.makeText(ResultSearchActivity.this,array[35],Toast.LENGTH_LONG).show();
        }

        String city = mSettings.getString("city","");
        if(city.isEmpty()){
            showCountryAlert();
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    public void onRefresh() {
        if(isOnline()){
            getDataWithoutMap(true);
        }else if(!isOnline()){
            Toast.makeText(ResultSearchActivity.this,array[35],Toast.LENGTH_LONG).show();
        }
        swipeRefreshLayout.setRefreshing(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_bookmarks) {
            Intent intent = new Intent(getApplicationContext(),BookmarksActivity.class);
            startActivity(intent);
            return true;
        }else if(item.getItemId() == R.id.history) {
            Intent intent = new Intent(getApplicationContext(),HistoryActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void  getDataWithoutMap(boolean refresh){
        ProgressDialog dialog = null;
        if(!refresh){
            dialog = new ProgressDialog(ResultSearchActivity.this);
            dialog.setMessage(array[1]);
            dialog.show();
        }


        JSONArray[] jsonArray = new JSONArray[1];
        String url = "getFirstData4imgs/";
        com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                .url(conf.getDomen()+ url)
                .build();
        Call call = client.newCall(request);
        ProgressDialog finalDialog = dialog;
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {}
            @Override
            public void onResponse(Response response) throws IOException {
                listView = findViewById(R.id.lisView);
                list = new ArrayList<String[]>();
                jsonObjects = new ArrayList<JSONObject[]>();
                String res = response.body().string();
                if(response.code()!=200){
                    return;
                }
                    try{
                        jsonArray[0] = new JSONArray(res);
                    }catch (Exception e){}
                    if(jsonArray[0] != null && jsonArray[0].length()!=0){
                        for(int i=0;i<jsonArray[0].length();i++){
                            String[] urlMas3 = new String[3];
                            JSONObject [] jsonObject = new JSONObject[3];
                            try{
                                urlMas3[0] = jsonArray[0].getJSONObject(i).getString("photoName");
                                jsonObject[0] = jsonArray[0].getJSONObject(i);
                            }catch (Exception e){}
                            try{
                                urlMas3[1] = jsonArray[0].getJSONObject(i+1).getString("photoName");
                                jsonObject[1] = jsonArray[0].getJSONObject(i+1);
                                i = i+1;
                            }catch (Exception e){}
                            try{
                                urlMas3[2] = jsonArray[0].getJSONObject(i+1).getString("photoName");
                                jsonObject[2] = jsonArray[0].getJSONObject(i+1);
                                i = i+1;
                            }catch (Exception e){}
                            list.add(urlMas3);
                            jsonObjects.add(jsonObject);
                        }
                        CustomArrayAdapter adapter = new CustomArrayAdapter(ResultSearchActivity.this, list,jsonObjects,jsonArray.toString());
                        runOnUiThread(new Runnable() {
                            public void run() {
                                adapter.alertDialogBuilder();
                                listView.setAdapter(adapter);
                                listView.setVisibility(View.VISIBLE);
                                textView.setVisibility(View.GONE);
                                textView3.setVisibility(View.GONE);
                                if(finalDialog!=null){
                                    finalDialog.dismiss();
                                }
                            }
                        });
                        swipeRefreshLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }, 3000);
                    }else{
                        runOnUiThread(new Runnable() {
                            public void run() {
                                textView.setVisibility(View.VISIBLE);
                                textView3.setVisibility(View.VISIBLE);
                                listView.setVisibility(View.GONE);
                            }
                        });
                        swipeRefreshLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }, 3000);

                    }
            }
        });


    }


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    AlertDialog alert = null;
    private void showAlertDialog() {
        final int[] lang = {0};
        final int[] arr_lang_index = {0};
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ResultSearchActivity.this);

        alertDialog.setTitle("Language");
        String[] items = getResources().getStringArray(R.array.languages);
        int checkedItem = 0;
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        lang[0] =  1;
                        editor.putInt(conf.getLANG(),1);
                        editor.apply();
                        arr_lang_index[0] = R.array.app_lang_ru;
                        array = getResources().getStringArray(R.array.app_lang_ru);
                        break;
                    case 1:
                        lang[0] =  2;
                        editor.putInt(conf.getLANG(),2);
                        editor.apply();
                        arr_lang_index[0] = R.array.app_lang_en;
                        array = getResources().getStringArray(R.array.app_lang_en);
                        break;
                    case 2:
                        lang[0] =  3;
                        editor.putInt(conf.getLANG(),3);
                        editor.apply();
                        arr_lang_index[0] = R.array.app_lang_ar;
                        array = getResources().getStringArray(R.array.app_lang_ar);
                        break;
                }
            }
        });
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(lang[0]<=0){
                    editor.putInt(conf.getLANG(),1);
                    editor.apply();
                    array = getResources().getStringArray(R.array.app_lang_ru);
                }
                searchBtn.setText(array[2]);
                trainBtn.setText(array[11]);
                alert.dismiss();
                Intent intent = new Intent(ResultSearchActivity.this, MyService.class);
                startService(intent);
            }
        });
        alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    class StatusOfBanner extends AsyncTask<Void,Void,Void>{
        String statusCode = "N";
        @Override
        protected Void doInBackground(Void... voids) {
            String url = "ads_sts/";
            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(conf.getDomen()+ url)
                    .build();
            Call call = client.newCall(request);
            try{
                Response response = call.execute();
                if(response.code()==200){
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if(!jsonObject.getString("status").equals("N")){
                        statusCode = jsonObject.getString("status");
                    }
                }
            }catch (Exception e){}
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            AdView mAdView = findViewById(R.id.adView);
            if(!statusCode.equals("N")){
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }else{
                mAdView.setVisibility(View.GONE);
            }

        }
    }

    public void showCountryAlert(){
        AlohaDb alohaDb = new AlohaDb(ResultSearchActivity.this);
        SQLiteDatabase sqLiteDatabase = alohaDb.getReadableDatabase();
        alohaDb.iniDb(sqLiteDatabase);
        List<Country> list = alohaDb.getAllCountry();


        AlertDialog.Builder builderSingle = new AlertDialog.Builder(ResultSearchActivity.this);
        builderSingle.setIcon(R.drawable.location_icon);
        builderSingle.setTitle(array[27]);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ResultSearchActivity.this, android.R.layout.select_dialog_singlechoice);
        for(int i=0;i<list.size();i++){
            arrayAdapter.add(list.get(i).getName());
        }
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<Region> regions = alohaDb.getRegions(which);
                final ArrayAdapter<String> arrayAdapterReg = new ArrayAdapter<String>(ResultSearchActivity.this, android.R.layout.select_dialog_singlechoice);
                for(int i=0;i<regions.size();i++){
                    arrayAdapterReg.add(regions.get(i).getName());
                }


                AlertDialog.Builder builderInnerRegion = new AlertDialog.Builder(ResultSearchActivity.this);
                builderInnerRegion.setAdapter(arrayAdapterReg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        List<City> cities = alohaDb.getCities(regions.get(i).getId());
                        final ArrayAdapter<String> arrayCity = new ArrayAdapter<String>(ResultSearchActivity.this, android.R.layout.select_dialog_singlechoice);
                        for(int k=0;k<cities.size();k++){
                            arrayCity.add(cities.get(k).getName());
                        }

                        AlertDialog.Builder builderInnerCity = new AlertDialog.Builder(ResultSearchActivity.this);
                        builderInnerCity.setAdapter(arrayCity, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int c) {
                                editor.putString("city",arrayCity.getItem(c));
                                editor.putInt("city_id",cities.get(c).getId());
                                editor.apply();
                                city_id = cities.get(c).getId();
                                dialog.dismiss();
                                Toast.makeText(ResultSearchActivity.this,array[19],Toast.LENGTH_LONG).show();
                            }
                        });
                        builderInnerCity.show();
                    }
                });
                builderInnerRegion.show();
            }
        });
        builderSingle.show();
    }
}