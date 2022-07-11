package com.humdet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class LaunchActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ImageView imageView = findViewById(R.id.imageView);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.iconhumdet);
        imageView.setImageBitmap(bitmap);
        getSupportActionBar().hide();

        String custom_font = "fonts/JackportRegularNcv.ttf";
        Typeface CF = Typeface.createFromAsset(getAssets(), custom_font);
        ((TextView) findViewById(R.id.textView)).setTypeface(CF);

        ImageButton button = findViewById(R.id.imageButton2);

        button.setOnClickListener(e -> {
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
        });


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

}