package com.humdet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    private String [] array;
    Conf conf = new Conf();
    SharedPreferences mSettings;
    SharedPreferences.Editor editor;
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

        if(mSettings.getBoolean("save_photo",false)){
            checkBox.setChecked(true);
        }

        Button button = findViewById(R.id.button3);
        button.setText(array[18]);
        button.setOnClickListener(e -> {
            if(checkBox.isChecked()){
                editor.putBoolean("save_photo",true);
                editor.apply();
            }else{
                editor.putBoolean("save_photo",false);
                editor.apply();
            }
            Toast.makeText(SettingsActivity.this,array[19],Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}