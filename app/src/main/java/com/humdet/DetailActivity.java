package com.humdet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

public class DetailActivity extends AppCompatActivity {

    MyPermissions per = new MyPermissions(DetailActivity.this,DetailActivity.this);
    SharedPreferences mSettings;
    private MapView m_mapView;
    private String [] array;
    Conf conf = new Conf();

    JSONObject jsonObject = null;
    ImageView personImg;
    TextView percent;
    TextView photoDate2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoiYWx0dWhhIiwiYSI6ImNsNHFya3dqdzBya3kzZmxudTE0b3o4emgifQ._IFNc_dmOF_mQPrV6QX4ZA");
        mSettings = getSharedPreferences(conf.getShared_pref_name(), Context.MODE_PRIVATE);
        setContentView(R.layout.activity_detail);
        personImg = findViewById(R.id.personImg);
        percent = findViewById(R.id.percent);
        photoDate2 = findViewById(R.id.photoDate2);
        m_mapView = (MapView) findViewById(R.id.mapView);
        try{
            jsonObject = new JSONObject(getIntent().getStringExtra("jsonObject"));
        }catch (Exception e){}
        try{
            Picasso.get()
                    .load(conf.getDomen()+"image?imgname="+jsonObject.getString("photoName")+".jpg")
                    .placeholder(R.drawable.hum_icon)
                    .into(personImg);
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
                    mapboxMap.addMarker(new MarkerOptions().setPosition(new LatLng(jsonObject.getDouble("lat"), jsonObject.getDouble("lng"))).setTitle("").setIcon(paellaIcon)).setTitle("Место съемки");

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
        }/*else if(lang==conf.getAR()){
            array = getResources().getStringArray(R.array.app_lang_ar);
        }*/

        try{
            percent.setText(array[8]+" "+ Math.round(jsonObject.getDouble("percentage"))+"%");
            photoDate2.setText(array[9]+" "+jsonObject.getString("inpDate"));
            getSupportActionBar().setTitle(array[10]);
        }catch (Exception e){}

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
}