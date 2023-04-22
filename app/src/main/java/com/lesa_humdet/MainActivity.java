package com.lesa_humdet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements  MapboxMap.OnMarkerClickListener , LocationListener {
    int REQUEST_OVERLAY_PERMISSION=1000;
    MyPermissions per = new MyPermissions(MainActivity.this,MainActivity.this);
    private MapView m_mapView;
    SharedPreferences mSettings;
    SharedPreferences.Editor editor;
    private String [] array;
    Conf conf = new Conf();
    JSONArray humPhotos;
    Button searchBtn = null;
    Button trainBtn = null;
    MapboxMap m_map = null;
    OkHttpClient client = new OkHttpClient();
    int city_id = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getApplicationContext(), "pk.eyJ1IjoiYWx0dWhhIiwiYSI6ImNsNHFya3dqdzBya3kzZmxudTE0b3o4emgifQ._IFNc_dmOF_mQPrV6QX4ZA");
        mSettings = getSharedPreferences(conf.getShared_pref_name(), Context.MODE_PRIVATE);
        editor = mSettings.edit();
        int lang = mSettings.getInt(conf.getLANG(),1);
        city_id = mSettings.getInt("city_id",0);
        if(lang==conf.getRU()){
            array = getResources().getStringArray(R.array.app_lang_ru);
        }else if(lang==conf.getEN()){
            array = getResources().getStringArray(R.array.app_lang_en);
        }else if(lang==conf.getAR()){
            array = getResources().getStringArray(R.array.app_lang_ar);
        }else{
            array = getResources().getStringArray(R.array.app_lang_ru);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setContentView(R.layout.activity_main);

        m_mapView =  findViewById(R.id.mapView);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(this)){
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            }
        }


        per.getMyApplicationPermissions();
        m_mapView.onCreate(savedInstanceState);
        m_mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                m_map = mapboxMap;
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        cityChanged(mapboxMap);
                        if(isOnline()){
                            getData(mapboxMap);
                        }else{
                            Toast.makeText(MainActivity.this,array[35],Toast.LENGTH_LONG).show();
                        }

                    }
                });
                Icon paellaIcon = IconFactory.getInstance(MainActivity.this).defaultMarker();

                mapboxMap.addMarker(new MarkerOptions().setPosition(new LatLng(lat, lng)).setTitle("").setIcon(paellaIcon));


                mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                                try{
                                    JSONObject jsonObject = humPhotos.getJSONObject((int)marker.getId()-1);
                                    Intent intent = new Intent(MainActivity.this,DetailActivity.class);
                                    intent.putExtra("jsonObject",jsonObject.toString());
                                    startActivity(intent);
                                }catch (Exception e){e.printStackTrace();}
                        return false;
                    }
                });
                getData(m_map);
            }
        });




        getSupportActionBar().hide();


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

        ImageButton settings_btn = findViewById(R.id.settings_btn);
        settings_btn.setOnClickListener(e -> {
            Intent intent2 = new Intent(this,SettingsActivity.class);
            startActivity(intent2);
        });


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        Location gpsLoc = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        if(providerStatus(locationManager)){
            onLocationChanged(gpsLoc);
        }
        Location networkLoc = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        if(providerStatus(locationManager)){
            onLocationChanged(networkLoc);
        }
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        String city = mSettings.getString("city","");
        if(city.isEmpty()){
            showCountryAlert();
        }

        if(isOnline()){
            new StatusOfBanner().execute();
        }else{
            Toast.makeText(MainActivity.this,array[35],Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(Settings.canDrawOverlays(this)){
                Intent intentService = new Intent(MainActivity.this,MyService.class);
                startService(intentService);
            }else{
                Toast.makeText(MainActivity.this,array[30],Toast.LENGTH_LONG).show();
            }
        }
    }

    private void cityChanged(MapboxMap mapboxMap) {
        String city = mSettings.getString("city","");
        Geocoder gc = new Geocoder(this);
        try {
            List<Address> list= gc.getFromLocationName(city,1);
            Address address = list.get(0);
            if(address!=null){
                lat=address.getLatitude();
                lng=address.getLongitude();
                Location location = new Location("");
                location.setLatitude(lat);
                location.setLongitude(lng);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(lat, lng))
                        .zoom(15)
                        .bearing(90)
                        .tilt(40)
                        .build();
                if(mapboxMap!=null){
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        } catch (Exception e) { }
    }




    @Override
    protected void onStart() {
        super.onStart();
        if (m_mapView != null) {
            m_mapView.onStart();
            cityChanged(m_map);
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
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Toast.makeText(MainActivity.this,marker.getTitle(),Toast.LENGTH_LONG).show();
        return true;
    }

    LocationManager locationManager = null;
    double lat=0,lng=0;


    @Override
    public void onLocationChanged(@NonNull Location location) {
             if(location!=null){
                 lat = location.getLatitude();
                 lng = location.getLongitude();
                 CameraPosition cameraPosition = new CameraPosition.Builder()
                         .target(new LatLng(lat, lng))
                         .zoom(20)
                         .bearing(90)
                         .tilt(40)
                         .build();
                 m_map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
             }

    }

    public boolean providerStatus(LocationManager locManager){
        boolean gps_enabled=locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network_enabled=locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if(gps_enabled){
        }else if(network_enabled){
            return network_enabled;
        }
        return false;
    }



    List<HumData> humDataList = new ArrayList<HumData>();
    public void getData(MapboxMap mapboxMap){
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage(array[1]);
        dialog.setCancelable(false);
        dialog.show();
        Request request = new Request.Builder()
                .url(conf.getDomen()+ "getInitData?city_id="+city_id)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {}

            @Override
            public void onResponse(Response response) throws IOException {
                try{
                    humPhotos = new JSONArray(response.body().string());
                    for(int i=0;i<humPhotos.length();i++){
                        try {
                            JSONObject tmpHum = humPhotos.getJSONObject(i);
                            HumData humData = new HumData();
                            humData.setId(tmpHum.getInt("fullFaceFeatures_id"));
                            humData.setDate(tmpHum.getString("inp_date"));
                            humData.setPhotoName(tmpHum.getString("photoName"));
                            humData.setLat(tmpHum.getDouble("lat"));
                            humData.setLng(tmpHum.getDouble("lng"));
                            humDataList.add(humData);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }catch (Exception e){e.printStackTrace();}
                if(isOnline()&&humDataList!=null){
                    downloadImages(mapboxMap);
                }else{
                    Toast.makeText(MainActivity.this,array[35],Toast.LENGTH_LONG).show();
                }
            }
        });

        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    public void downloadImages(MapboxMap mapboxMap){
        for(int i=0;i<humDataList.size();i++){
            int finalI = i;
            /*Request request = new Request.Builder()
                    .url(conf.getDomen()+ "image?imgname="+humDataList.get(i).getPhotoName()+"_SMALL.jpg/")
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {e.printStackTrace();}

                @Override
                public void onResponse(Response response){
                    try{
                        InputStream inputStream = response.body().byteStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        Icon paellaIcon = IconFactory.getInstance(MainActivity.this).fromBitmap(bitmap);
                        try{
                            LatLng latLng = new LatLng(humDataList.get(finalI).getLat(), humDataList.get(finalI).getLng());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mapboxMap.addMarker(new MarkerOptions()
                                            .position(latLng)
                                            .title(humDataList.get(finalI).getDate())
                                            .icon(paellaIcon));
                                }
                            });
                        }catch (Exception e){e.printStackTrace();}
                    }catch (Exception e){e.printStackTrace();}
                }
            });*/
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LatLng latLng = new LatLng(humDataList.get(finalI).getLat(), humDataList.get(finalI).getLng());
                    mapboxMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(humDataList.get(finalI).getDate()));
                    //.icon(paellaIcon));
                }
            });

        }
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



    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    public void showCountryAlert(){
        AlohaDb alohaDb = new AlohaDb(MainActivity.this);
        SQLiteDatabase sqLiteDatabase = alohaDb.getReadableDatabase();
        alohaDb.iniDb(sqLiteDatabase);
        List<Country> list = alohaDb.getAllCountry();


        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
        builderSingle.setIcon(R.drawable.location_icon);
        builderSingle.setTitle(array[27]);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
        for(int i=0;i<list.size();i++){
            arrayAdapter.add(list.get(i).getName());
        }
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<Region> regions = alohaDb.getRegions(which);
                final ArrayAdapter<String> arrayAdapterReg = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
                for(int i=0;i<regions.size();i++){
                    arrayAdapterReg.add(regions.get(i).getName());
                }


                AlertDialog.Builder builderInnerRegion = new AlertDialog.Builder(MainActivity.this);
                builderInnerRegion.setAdapter(arrayAdapterReg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        List<City> cities = alohaDb.getCities(regions.get(i).getId());
                        final ArrayAdapter<String> arrayCity = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
                        for(int k=0;k<cities.size();k++){
                            arrayCity.add(cities.get(k).getName());
                        }

                        AlertDialog.Builder builderInnerCity = new AlertDialog.Builder(MainActivity.this);
                        builderInnerCity.setAdapter(arrayCity, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int c) {
                                editor.putString("city",arrayCity.getItem(c));
                                editor.putInt("city_id",cities.get(c).getId());
                                editor.apply();
                                city_id = cities.get(c).getId();
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this,array[19],Toast.LENGTH_LONG).show();
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