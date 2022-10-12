package com.humdet;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;


import com.jsibbold.zoomage.ZoomageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AlertDialog;

public class CustomArrayAdapter extends BaseAdapter{
    private LayoutInflater inflater;
    private List<String[]>url;
    private  List<JSONObject[]> jsonObjects;
    String jsonData;
    Context context;
    Conf conf = new Conf();
    public CustomArrayAdapter(Context context,List<String[]> url, List<JSONObject[]> jsonObjects,String jsonData) {
        this.url = url;
        this.jsonObjects = jsonObjects;
        this.jsonData = jsonData;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getCount() {
        return url.size();
    }

    @Override
    public Object getItem(int position) {
        return url.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView  = inflater.inflate(R.layout.custom_list, parent, false);
        ImageView imageView2 =  convertView.findViewById(R.id.imageView2);
        ImageView imageView3 =  convertView.findViewById(R.id.imageView3);
        ImageView imageView4 =  convertView.findViewById(R.id.imageView4);
        ProgressBar progressBar1 =  convertView.findViewById(R.id.progressBar1);
        ProgressBar progressBar2 =  convertView.findViewById(R.id.progressBar2);
        ProgressBar progressBar3 =  convertView.findViewById(R.id.progressBar3);

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,DetailActivity.class);
                intent.putExtra("jsonObject",jsonObjects.get(position)[0].toString());
                intent.putExtra("position",position);
                intent.putExtra("allJsonObject",jsonData);
                context.startActivity(intent);
            }
        });


        if(jsonObjects.get(position)[1]!=null){
            try{
                imageView3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context,DetailActivity.class);
                        intent.putExtra("jsonObject",jsonObjects.get(position)[1].toString());
                        intent.putExtra("position",position+1);
                        intent.putExtra("allJsonObject",jsonData);
                        context.startActivity(intent);
                    }
                });
            }catch (Exception e){}
        }
        if(jsonObjects.get(position)[2]!=null){
            try{
                imageView4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context,DetailActivity.class);
                        intent.putExtra("jsonObject",jsonObjects.get(position)[2].toString());
                        intent.putExtra("position",position+2);
                        intent.putExtra("allJsonObject",jsonData);
                        context.startActivity(intent);
                    }
                });
            }catch (Exception e){}
        }


        String [] urls = url.get(position);


        Picasso.with(context)
                .load(conf.getDomen()+"image?imgname="+urls[0]+"_SMALL.jpg")
                .placeholder(R.drawable.man)
                .error(R.drawable.person_ic)
                .fit().centerCrop()
                .into(imageView2, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar1.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        progressBar1.setVisibility(View.GONE);
                    }
                });


        if(urls[1]!=null){
            Picasso.with(context)
                    .load(conf.getDomen()+"image?imgname="+urls[1]+"_SMALL.jpg")
                    .placeholder(R.drawable.man)
                    .error(R.drawable.person_ic)
                    .fit().centerCrop()
                    .into(imageView3, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar2.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            progressBar2.setVisibility(View.GONE);
                        }
                    });
        }else{
            imageView3.setVisibility(View.INVISIBLE);
            progressBar2.setVisibility(View.GONE);
        }
        if(urls[2]!=null){
            Picasso.with(context)
                    .load(conf.getDomen()+"image?imgname="+urls[2]+"_SMALL.jpg")
                    .placeholder(R.drawable.man)
                    .error(R.drawable.person_ic)
                    .fit().centerCrop()
                    .into(imageView4, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar3.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            progressBar3.setVisibility(View.GONE);
                        }
                    });
        }else{
            imageView4.setVisibility(View.INVISIBLE);
            progressBar3.setVisibility(View.GONE);
        }



        return convertView;

    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }
    public int color(@ColorRes int resId) {
        return context.getResources().getColor(resId);
    }




}