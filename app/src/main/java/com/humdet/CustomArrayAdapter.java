package com.humdet;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;


import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;

import androidx.annotation.ColorRes;

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
            holder.imageView2 = (ImageView) v.findViewById(R.id.imageView2);
            holder.imageView3 = (ImageView) v.findViewById(R.id.imageView3);
            holder.imageView4 = (ImageView) v.findViewById(R.id.imageView4);
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
                        intent.putExtra("jsonObject",jsonObjects.get(position)[2].toString());
                        context.startActivity(intent);
                    }
                });
            }catch (Exception e){}
        }


        String [] urls = url.get(position);

        Picasso.get()
                .load(conf.getDomen()+"image?imgname="+urls[0]+".jpg")
                .placeholder(R.drawable.ic_baseline_person_24)
                .error(R.drawable.person_ic)
                .into(holder.imageView2);


        if(urls[1]!=null){
            Picasso.get()
                    .load(conf.getDomen()+"image?imgname="+urls[1]+".jpg")
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.person_ic)
                    .into(holder.imageView3);
        }else{
            holder.imageView3.setVisibility(View.INVISIBLE);
        }
        if(urls[2]!=null){
            Picasso.get()
                    .load(conf.getDomen()+"image?imgname="+urls[2]+".jpg")
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.person_ic)
                    .into(holder.imageView4);
        }else{
            holder.imageView4.setVisibility(View.INVISIBLE);
        }



        return v;

    }
    private static class ViewHolder {
        private ImageView imageView2;
        private ImageView imageView3;
        private ImageView imageView4;

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