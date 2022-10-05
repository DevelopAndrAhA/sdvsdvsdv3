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

import java.util.List;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AlertDialog;

public class CustomArrayAdapter extends BaseAdapter{
    private LayoutInflater inflater;
    private List<String[]>url;
    private  List<JSONObject[]> jsonObjects;
    Context context;
    Conf conf = new Conf();
    public CustomArrayAdapter(Context context,List<String[]> url, List<JSONObject[]> jsonObjects) {
        this.url = url;
        this.jsonObjects = jsonObjects;
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
        ViewHolder holder = null;
        View v = convertView;
        if ( v == null){
            holder = new ViewHolder();
            v = inflater.inflate(R.layout.custom_list, parent, false);
            holder.imageView2 = (ZoomageView) v.findViewById(R.id.imageView2);
            holder.imageView3 = (ZoomageView) v.findViewById(R.id.imageView3);
            holder.imageView4 = (ZoomageView) v.findViewById(R.id.imageView4);
            holder.progressBar1 = (ProgressBar) v.findViewById(R.id.progressBar1);
            holder.progressBar2 = (ProgressBar) v.findViewById(R.id.progressBar2);
            holder.progressBar3 = (ProgressBar) v.findViewById(R.id.progressBar3);
            v.setTag(holder);
        }

        holder.imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,DetailActivity.class);
                intent.putExtra("jsonObject",jsonObjects.get(position)[0].toString());
                context.startActivity(intent);
            }
        });


        if(jsonObjects.get(position)[1]!=null){
            try{
                holder.imageView3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context,DetailActivity.class);
                        intent.putExtra("jsonObject",jsonObjects.get(position)[1].toString());
                        context.startActivity(intent);
                    }
                });
            }catch (Exception e){}
        }
        if(jsonObjects.get(position)[2]!=null){
            try{
                holder.imageView4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context,DetailActivity.class);
                        Log.e("json",jsonObjects.get(position)[2].toString());
                        intent.putExtra("jsonObject",jsonObjects.get(position)[2].toString());
                        context.startActivity(intent);
                    }
                });
            }catch (Exception e){}
        }


        String [] urls = url.get(position);


        ViewHolder finalHolder = holder;
        Picasso.with(context)
                .load(conf.getDomen()+"image?imgname="+urls[0]+"_SMALL.jpg")
                .placeholder(R.drawable.man)
                .error(R.drawable.person_ic)
                .fit().centerCrop()
                .into(holder.imageView2, new Callback() {
                    @Override
                    public void onSuccess() {
                        finalHolder.progressBar1.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        finalHolder.progressBar1.setVisibility(View.GONE);
                    }
                });


        if(urls[1]!=null){
            Picasso.with(context)
                    .load(conf.getDomen()+"image?imgname="+urls[1]+"_SMALL.jpg")
                    .placeholder(R.drawable.man)
                    .error(R.drawable.person_ic)
                    .fit().centerCrop()
                    .into(holder.imageView3, new Callback() {
                        @Override
                        public void onSuccess() {
                            finalHolder.progressBar2.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            finalHolder.progressBar2.setVisibility(View.GONE);
                        }
                    });
        }else{
            holder.imageView3.setVisibility(View.INVISIBLE);
            finalHolder.progressBar2.setVisibility(View.GONE);
        }
        if(urls[2]!=null){
            Picasso.with(context)
                    .load(conf.getDomen()+"image?imgname="+urls[2]+"_SMALL.jpg")
                    .placeholder(R.drawable.man)
                    .error(R.drawable.person_ic)
                    .fit().centerCrop()
                    .into(holder.imageView4, new Callback() {
                        @Override
                        public void onSuccess() {
                            finalHolder.progressBar3.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            finalHolder.progressBar3.setVisibility(View.GONE);
                        }
                    });
        }else{
            holder.imageView4.setVisibility(View.INVISIBLE);
            finalHolder.progressBar3.setVisibility(View.GONE);
        }



        return v;

    }
    private class ViewHolder {
        private com.jsibbold.zoomage.ZoomageView imageView2;
        private com.jsibbold.zoomage.ZoomageView imageView3;
        private com.jsibbold.zoomage.ZoomageView imageView4;
        private ProgressBar progressBar1;
        private ProgressBar progressBar2;
        private ProgressBar progressBar3;

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