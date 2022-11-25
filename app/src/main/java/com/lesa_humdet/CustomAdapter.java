package com.lesa_humdet;

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {


    private MyPermissions per = null;
    private SharedPreferences mSettings;
    private MapView m_mapView;
    private String [] array;
    private Conf conf = new Conf();

    private String jsonObjectsStr;
    private JSONObject jsonObject = null;
    private ImageView personImg;
    //private TextView percent;
    private TextView photoDate2;
    private int position;
    private JSONArray jsonArray;


    Context context;
    Activity activity;
    Bundle savedInstanceState;
    private static final String TAG = "CustomAdapter";

    private String[] mDataSet;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View v) {
            super(v);
            Mapbox.getInstance(context, "pk.eyJ1IjoiYWx0dWhhIiwiYSI6ImNsNHFya3dqdzBya3kzZmxudTE0b3o4emgifQ._IFNc_dmOF_mQPrV6QX4ZA");
            mSettings = context.getSharedPreferences(conf.getShared_pref_name(), Context.MODE_PRIVATE);
            personImg = v.findViewById(R.id.personImg);
            //percent = findViewById(R.id.percent);
            photoDate2 = v.findViewById(R.id.photoDate2);
            m_mapView = (MapView) v.findViewById(R.id.mapView);
            ProgressBar progressBar = v.findViewById(R.id.progressBar);
            try{
                jsonObject = new JSONObject(activity.getIntent().getStringExtra("jsonObject"));
                position = activity.getIntent().getIntExtra("position",0);
                jsonObjectsStr = activity.getIntent().getStringExtra("allJsonObject");
                jsonArray = new JSONArray(jsonObjectsStr);
            }catch (Exception e){}

            try{
                Picasso.with(context)
                        .load(conf.getDomen()+"image?imgname="+jsonObject.getString("photoName")+".jpg/")
                        .placeholder(R.drawable.hum_icon)
                        .fit().centerCrop()
                        .into(personImg, new Callback() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(View.GONE);
                                personImg.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        try{
                                            Intent intent = new Intent(context,FullImageActivity.class);
                                            intent.putExtra("photoName",jsonObject.getString("photoName"));
                                            activity.startActivity(intent);
                                        }catch (Exception e){}
                                    }
                                });
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

                    Icon paellaIcon = IconFactory.getInstance(context).defaultMarker();
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
                array = context.getResources().getStringArray(R.array.app_lang_ru);
            }else if(lang==conf.getEN()){
                array = context.getResources().getStringArray(R.array.app_lang_en);
            }else if(lang==conf.getAR()){
                array = context.getResources().getStringArray(R.array.app_lang_ar);
            }else{
                array = context.getResources().getStringArray(R.array.app_lang_ru);
            }

            try{
                photoDate2.setText(array[9]+" "+jsonObject.getString("inpDate"));
            }catch (Exception e){
                try {
                    photoDate2.setText(array[9]+" "+jsonObject.getString("inp_date"));
                } catch (JSONException jsonException) {}
            }

            Button pingBtn = v.findViewById(R.id.pingBtn);
            pingBtn.setText(array[25]);
            pingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context,array[26],Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
    public CustomAdapter(String[] dataSet, Context context, Activity activity, Bundle savedInstanceState) {
        mDataSet = dataSet;
        this.context = context;
        this.activity = activity;
        this.savedInstanceState = savedInstanceState;
        per = new MyPermissions(context,activity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.text_row_item, viewGroup, false);
        return new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        m_mapView.onStart();
    }
    @Override
    public int getItemCount() {
        return mDataSet.length;
    }

}