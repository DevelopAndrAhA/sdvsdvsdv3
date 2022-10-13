package com.humdet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResultSearchActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    OkHttpClient client = new OkHttpClient();
    private String [] array;
    Conf conf = new Conf();


    SharedPreferences mSettings;
    JSONArray jsonArray;
    ListView listView = null;
    List<String[]> list = null;
    List<JSONObject[]> jsonObjects = null;
    SwipeRefreshLayout swipeRefreshLayout = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getSharedPreferences(conf.getShared_pref_name(), Context.MODE_PRIVATE);
        setContentView(R.layout.activity_result_search);


        String custom_font = "fonts/JackportRegularNcv.ttf";
        Typeface CF = Typeface.createFromAsset(getAssets(), custom_font);
        TextView textView = findViewById(R.id.textView2);
        TextView textView3 = findViewById(R.id.textView3);
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
        }
        boolean main = getIntent().getBooleanExtra("main",false);
        if(main){
            textView.setText(array[22]);//metka 2
            getSupportActionBar().setTitle("");
        }else{
            textView.setText(array[16]);
            textView3.setText(array[34]);
            getSupportActionBar().setTitle(array[7]);
        }


        try{
            jsonArray = new JSONArray(getIntent().getStringExtra("jsonArray"));
        }catch (Exception e){}

        listView = findViewById(R.id.lisView);
        list = new ArrayList<String[]>();
        jsonObjects = new ArrayList<JSONObject[]>();

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
        }
        if(list==null || list.size()==0) textView.setVisibility(View.VISIBLE);
        CustomArrayAdapter adapter = new CustomArrayAdapter(ResultSearchActivity.this, list,jsonObjects,jsonArray.toString());
        listView.setAdapter(adapter);
        listView.setClickable(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
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
            getDataWithoutMap();
        }else if(!isOnline()){
            Toast.makeText(ResultSearchActivity.this,"Нет связи с интернетом!",Toast.LENGTH_LONG).show();
        }
        swipeRefreshLayout.setRefreshing(true);

    }


    public void getDataWithoutMap(){
        JSONArray[] jsonArray = new JSONArray[1];
        String url = "getFirstData4imgs";
        com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                .url(conf.getDomen()+ url)
                .build();
        Call call = client.newCall(request);
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
                    if(jsonArray[0] != null){
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
                            @Override
                            public void run() {
                                listView.setAdapter(adapter);
                            }
                        });
                        swipeRefreshLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }, 3000);
                    }else{
                        Toast.makeText(ResultSearchActivity.this,array[22],Toast.LENGTH_SHORT).show();
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
}