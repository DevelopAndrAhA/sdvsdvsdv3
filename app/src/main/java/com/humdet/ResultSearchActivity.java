package com.humdet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ResultSearchActivity extends AppCompatActivity {

    private String [] array;
    Conf conf = new Conf();


    SharedPreferences mSettings;
    JSONArray jsonArray;
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

        ListView listView = findViewById(R.id.lisView);
        List<String[]> list = new ArrayList<String[]>();
        List<JSONObject[]> jsonObjects = new ArrayList<JSONObject[]>();

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
}