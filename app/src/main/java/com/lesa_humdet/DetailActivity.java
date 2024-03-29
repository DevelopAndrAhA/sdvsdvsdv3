package com.lesa_humdet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class DetailActivity extends AppCompatActivity {

    OkHttpClient client = new OkHttpClient();
    private MyPermissions per = new MyPermissions(DetailActivity.this,DetailActivity.this);
    SharedPreferences.Editor editor;
    private SharedPreferences mSettings;
    private MapView m_mapView;
    private String [] array;
    private Conf conf = new Conf();

    private JSONObject jsonObject = null;
    private ImageView personImg;
    private TextView photoDate2;
    private int position;
    private JSONArray jsonArray;
    private String jsonObjectsStr;
    private TextView percent;
    long fullFaceFeatures_id = 0;
    long faceFeaturesId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            jsonObject = new JSONObject(getIntent().getStringExtra("jsonObject"));
            position = getIntent().getIntExtra("position",0);
            fullFaceFeatures_id = jsonObject.getLong("fullFaceFeatures_id");
            faceFeaturesId = jsonObject.getLong("facefeatures_id");
            jsonObjectsStr = getIntent().getStringExtra("allJsonObject");
            jsonArray = new JSONArray(jsonObjectsStr);
        }catch (Exception e){e.printStackTrace();}
        Mapbox.getInstance(this, "pk.eyJ1IjoiYWx0dWhhIiwiYSI6ImNsNHFya3dqdzBya3kzZmxudTE0b3o4emgifQ._IFNc_dmOF_mQPrV6QX4ZA");
        mSettings = getSharedPreferences(conf.getShared_pref_name(), Context.MODE_PRIVATE);
        editor = mSettings.edit();
        deviceId = mSettings.getString("deviceId","*");
        setContentView(R.layout.activity_detail);
        personImg = findViewById(R.id.personImg);
        //percent = findViewById(R.id.percent);
        photoDate2 = findViewById(R.id.photoDate2);
        m_mapView = (MapView) findViewById(R.id.mapView);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        personImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Intent intent = new Intent(DetailActivity.this,FullImageActivity.class);
                    intent.putExtra("photoName",jsonObject.getString("photoName"));
                    startActivity(intent);
                }catch (Exception e){}
            }
        });


        try{
            Picasso.with(this)
                    .load(conf.getDomen()+"image?imgname="+jsonObject.getString("photoName")+".jpg/")
                    .placeholder(R.drawable.hum_icon)
                    .fit().centerCrop()
                    .into(personImg, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                        }
                        @Override
                        public void onError() {

                        }
                    });
        }catch (Exception e){}
        per.getMyApplicationPermissions();
        m_mapView.onCreate(savedInstanceState);
        m_mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                    }
                });

                Icon paellaIcon = IconFactory.getInstance(DetailActivity.this).defaultMarker();
                //Icon paellaIcon = IconFactory.getInstance(MainActivity.this).fromBitmap(bmp)
                try{
                    mapboxMap.addMarker(new MarkerOptions().setPosition(new LatLng(jsonObject.getDouble("lat"), jsonObject.getDouble("lng"))).setTitle("").setIcon(paellaIcon))
                            .setTitle(array[31]);

                    mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(jsonObject.getDouble("lat"), jsonObject.getDouble("lng")), 13));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(jsonObject.getDouble("lat"), jsonObject.getDouble("lng")))      // Sets the center of the map to location user
                            .zoom(17)                   // Sets the zoom
                            .bearing(90)                // Sets the orientation of the camera to east
                            .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                }catch (Exception e){e.printStackTrace();}

            }
        });
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

        try{
            photoDate2.setText(array[9]+" "+jsonObject.getString("inpDate"));
        }catch (Exception e){
            try {
                photoDate2.setText(array[9]+" "+jsonObject.getString("inp_date"));
            } catch (JSONException jsonException) {}
        }

        getSupportActionBar().setTitle(array[10]);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Button pingBtn = findViewById(R.id.pingBtn);
        pingBtn.setText(array[25]);
        pingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(DetailActivity.this,array[26],Toast.LENGTH_SHORT).show();
            }
        });

    }
    JSONArray bookmarks = null;
    String deviceId = "";
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        MenuItem item = menu.findItem(R.id.action_bookmarks);
        MenuItem action_delete = menu.findItem(R.id.action_delete);
        action_delete.setVisible(false);
        item.setTitle(array[40]);
        try {
            bookmarks = new JSONArray(mSettings.getString("bookmarks","[]"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            for (int i = 0; i < bookmarks.length(); i++) {
                JSONObject tmpJsonObj = bookmarks.getJSONObject(i);
                if (tmpJsonObj.getString("photoName").equals(jsonObject.getString("photoName"))) {
                    item.setIcon(R.drawable.ic_baseline_bookmark_added_24);
                    break;
                }
            }
        }catch (Exception e){e.printStackTrace();}

        // Формируем URL для запроса
        String url = conf.getDomen()+"getFullFaceFeatures?fullFaceFeatures_id=" + fullFaceFeatures_id;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        // Отправляем запрос асинхронно
        client.newCall(request).enqueue(new  com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    String str = response.body().string();
                    JSONObject jsonObject = new JSONObject(str);
                    if(deviceId.equals(jsonObject.get("deviceId")) || deviceId.equals("dbc2febcd263956b")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                action_delete.setVisible(true);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = true;
        if (item.getItemId() == R.id.action_bookmarks) {
            try {
                for (int i = 0; i < bookmarks.length(); i++) {
                    JSONObject tmpJsonObj = bookmarks.getJSONObject(i);
                    if (tmpJsonObj.getString("photoName").equals(jsonObject.getString("photoName"))) {
                        result = false;
                        break;
                    }
                }
            }catch (Exception e){e.printStackTrace();}

            if(result){
                item.setIcon(R.drawable.ic_baseline_bookmark_added_24);
                bookmarks.put(jsonObject);
                editor.putString("bookmarks",bookmarks.toString());
                editor.apply();
            }

            return true;
        }else if (item.getItemId() == R.id.action_delete) {
            deleteFace(deviceId);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (m_mapView != null) {
            m_mapView.onStart();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (m_mapView != null) {
            m_mapView.onStop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (m_mapView != null) {
            m_mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (m_mapView != null) {
            m_mapView.onPause();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (m_mapView != null) {
            m_mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (m_mapView != null) {
            m_mapView.onLowMemory();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_mapView != null) {
            m_mapView.onDestroy();
        }
        finish();
    }

    public void deleteFace(String deviceId) {
        String url = conf.getDomen()+"delete?deviceId=" + deviceId + "&faceFeatures_id=" + faceFeaturesId+"&fullFaceFeatures_id="+fullFaceFeatures_id;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try{
                        JSONObject status = new JSONObject(responseBody);
                        if(status.getInt("status") == 200){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DetailActivity.this,array[44],Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(DetailActivity.this, ResultSearchActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    finish();
                                }
                            });
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}