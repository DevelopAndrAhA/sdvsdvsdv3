package com.humdet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    private String [] array;
    Conf conf = new Conf();
    SharedPreferences mSettings;
    SharedPreferences.Editor editor;
    Spinner spinner = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getSharedPreferences(conf.getShared_pref_name(), Context.MODE_PRIVATE);
        editor = mSettings.edit();
        int lang = mSettings.getInt(conf.getLANG(),0);
        if(lang==conf.getRU()){
            array = getResources().getStringArray(R.array.app_lang_ru);
        }else if(lang==conf.getEN()){
            array = getResources().getStringArray(R.array.app_lang_en);
        }/*else if(lang==conf.getAR()){
            array = getResources().getStringArray(R.array.app_lang_ar);
        }*/
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setTitle(array[21]);
        CheckBox checkBox = findViewById(R.id.checkBox);
        checkBox.setText(array[17]);
        checkBox.setChecked(true);
        if(mSettings.getBoolean("save_photo",false)){
            checkBox.setChecked(true);
        }
        TextView textView3 = findViewById(R.id.textView3);
        textView3.setText(array[24]);
        Button button = findViewById(R.id.button3);
        button.setText(array[18]);
        button.setOnClickListener(e -> {
            if(checkBox.isChecked()){
                editor.putBoolean("save_photo",true);
            }else{
                editor.putBoolean("save_photo",false);
            }
            String selected = spinner.getSelectedItem().toString();
            if(selected.equals("English")){
                editor.putInt(conf.getLANG(),2);
            }else if(selected.equals("??????????????")){
                editor.putInt(conf.getLANG(),1);
            }
            editor.apply();
            Toast.makeText(SettingsActivity.this,array[19],Toast.LENGTH_SHORT).show();
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        spinner = findViewById(R.id.spinner);


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