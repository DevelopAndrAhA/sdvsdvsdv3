package com.humdet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;


import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/*import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;*/

/*import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;*/

public class MainActivity extends AppCompatActivity implements  MapboxMap.OnMarkerClickListener , LocationListener {
    int REQUEST_OVERLAY_PERMISSION=1000;
    MyPermissions per = new MyPermissions(MainActivity.this,MainActivity.this);
    private MapView m_mapView;
    MapboxMap m_map;
    SharedPreferences mSettings;
    SharedPreferences.Editor editor;
    private String [] array;
    Conf conf = new Conf();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, "pk.eyJ1IjoiYWx0dWhhIiwiYSI6ImNsNHFya3dqdzBya3kzZmxudTE0b3o4emgifQ._IFNc_dmOF_mQPrV6QX4ZA");

        mSettings = getSharedPreferences(conf.getShared_pref_name(), Context.MODE_PRIVATE);
        editor = mSettings.edit();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setContentView(R.layout.activity_main);

        m_mapView = (MapView) findViewById(R.id.mapView);
        //m_mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(this)){
                // ask for setting
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
                    }
                });

                Icon paellaIcon = IconFactory.getInstance(MainActivity.this).defaultMarker();
                //Icon paellaIcon = IconFactory.getInstance(MainActivity.this).fromBitmap(bmp)
                Marker marker =  mapboxMap.addMarker(new MarkerOptions().setPosition(new LatLng(lat, lng)).setTitle("").setIcon(paellaIcon));

            }
        });


        Intent intent = new Intent(MainActivity.this,MyService.class);
        startService(intent);
        getSupportActionBar().hide();

        int lang = mSettings.getInt(conf.getLANG(),1);
        if(lang==conf.getRU()){
            array = getResources().getStringArray(R.array.app_lang_ru);
        }else if(lang==conf.getEN()){
            array = getResources().getStringArray(R.array.app_lang_en);
        }/*else if(lang==conf.getAR()){
            array = getResources().getStringArray(R.array.app_lang_ar);
        }*/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        onLocationChanged(location);
        
        new GetData().execute();
        Button button3 = findViewById(R.id.button3);
        Button button4 = findViewById(R.id.button4);
        button3.setText(array[2]);
        button4.setText(array[11]);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),SearchActivity.class);
                startActivity(intent );
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),NeuroTrainingActivity.class);
                startActivity(intent );
            }
        });
        int langTmp = mSettings.getInt(conf.getLANG(),0);
        if(langTmp==0){
            showAlertDialog();
        }



        /*MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Toast.makeText(MainActivity.this, "onInitializationComplete", Toast.LENGTH_SHORT).show();
            }
        });

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);*/
        ImageButton settings_btn = findViewById(R.id.settings_btn);
        ImageButton img_activity = findViewById(R.id.img_activity);
        settings_btn.setOnClickListener(e -> {
            Intent intent2 = new Intent(this,SettingsActivity.class);
            startActivity(intent2);
        });

        img_activity.setOnClickListener(e -> {
            new GetDataWithoutLoc().execute();
        });
    }
    AlertDialog alert = null;
    private void showAlertDialog() {
        final int[] lang = {0};
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        alertDialog.setTitle("Language");
        String[] items = {"English", "Русский"};
        int checkedItem = 1;
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        lang[0] =  2;
                        editor.putInt(conf.getLANG(),2);
                        editor.apply();
                        break;
                    case 1:
                        lang[0] =  1;
                        editor.putInt(conf.getLANG(),1);
                        editor.apply();
                        break;
                }
            }
        });
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
               if( lang[0] == 0){
                   editor.putInt(conf.getLANG(),1);
                   editor.apply();
                   Toast.makeText(MainActivity.this, "В сервисе язык поменяется при след.запуске приложения", Toast.LENGTH_SHORT).show();
               };
                alert.dismiss();
            }
        });
        alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
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
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Toast.makeText(MainActivity.this,marker.getTitle(),Toast.LENGTH_LONG).show();
        return true;
    }

    LocationManager locationManager = null;
    double lat=42.877107,lng=74.578294;
    JSONArray humPhotos;
    List<HumData> humDataList = new ArrayList<HumData>();

    @Override
    public void onLocationChanged(@NonNull Location location) {
             if(location!=null){
                 lat = location.getLatitude();
                 lng = location.getLongitude();
             }
    }

    Bitmap[] bmps;
    class DownloadImages extends AsyncTask<Void,Void,Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bmps = new Bitmap[humDataList.size()];
            SaveNewFace.flagShotAndSave=false;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            OkHttpClient client1 = new  OkHttpUtils().getInstance();
            for(int i=0;i<humDataList.size();i++){
                Request request1 = new Request.Builder()
                        .url(conf.getDomen()+ "image?imgname="+humDataList.get(i).getPhotoName()+"_SMALL.jpg")
                        .build();
                Call call1 = client1.newCall(request1);
                try{
                    final Response response = call1.execute();
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    bmps[i] = bitmap;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            for(int i =0;i<bmps.length;i++){
                Icon paellaIcon = IconFactory.getInstance(MainActivity.this).fromBitmap(bmps[i]);
                try{
                    m_map.addMarker(new MarkerOptions().setPosition(new LatLng(humDataList.get(i).getLat(), humDataList.get(i).getLng())).setTitle(humDataList.get(i).getDate()).setIcon(paellaIcon));
                }catch (Exception e){}
            }
            SaveNewFace.flagShotAndSave=true;
        }
    }

    class GetData extends AsyncTask<Void,Void,Void>{
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage(array[1]);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            OkHttpClient client = new  OkHttpUtils().getInstance();
            Request request = new Request.Builder()
                    .url(conf.getDomen()+ "getInitData?lat="+lat+"&lng="+lng)
                    .build();
            Call call = client.newCall(request);
            try{
                Response response = call.execute();
                if(response.code()==200){
                    try{
                        humPhotos = new JSONArray(response.body().string());
                    }catch (Exception e){e.printStackTrace();}
                }else{}
            }catch (Exception e){e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if(humPhotos!=null){
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
                    new DownloadImages().execute();
                }
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

        }
    }



    class GetDataWithoutLoc extends AsyncTask<Void,Void,Void> {
        JSONArray jsonArray;
        private ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage(array[2]);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            try {
                String url = "getFirstData4imgs";
                com.squareup.okhttp.Request request1 = new com.squareup.okhttp.Request.Builder()
                        .url(conf.getDomen()+ url)
                        .build();
                Call call1 = client.newCall(request1);
                final Response response = call1.execute();
                String res = response.body().string();
                Log.e("res",res);
                try{
                    jsonArray = new JSONArray(res);
                }catch (Exception e){}
            }catch (Exception e){}
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if(jsonArray!=null){
                Intent intent = new Intent(MainActivity.this,ResultSearchActivity.class);
                intent.putExtra("jsonArray",jsonArray.toString());
                startActivity(intent);
            }else{
                Toast.makeText(MainActivity.this,array[22],Toast.LENGTH_SHORT).show();
            }
        }
    }

}