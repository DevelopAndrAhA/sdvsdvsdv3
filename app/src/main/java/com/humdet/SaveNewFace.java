package com.humdet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



public class SaveNewFace {
    SharedPreferences mSettings;
    Conf conf = new Conf();
    private String crop;
    private File largePohto;
    private String username;
    private double lat;
    private double lng;
    private String title;
    private String titleProgress;
    private Context context;
    private boolean uploadFromActivity;
    OkHttpClient client = new OkHttpClient();

    public SaveNewFace(Context context) {
        mSettings = context.getSharedPreferences(conf.getShared_pref_name(), Context.MODE_PRIVATE);
    }
    public void execute(){
        try{
            sendData();
            //new SendData().execute();
        }catch (Exception e){}
    }


    void sendData() throws IOException {
        ProgressDialog dialog = null;
        int city_id = mSettings.getInt("city_id",0);
        boolean  delFlag = mSettings.getBoolean("save_photo",false);

        if(uploadFromActivity){
            dialog = new ProgressDialog(context);
            dialog.setMessage(titleProgress);
            dialog.show();
        }

        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("crop", crop)
                .addFormDataPart("largePohto", largePohto.getName(),
                        RequestBody.create(MediaType.parse("text/plain"), largePohto))
                .addFormDataPart("city_id", city_id+"")
                .addFormDataPart("username", username)
                .addFormDataPart("lat", lat+"")
                .addFormDataPart("lng", lng+"")
                .build();

        Request request = new Request.Builder().url(conf.getDomen()+"new_face").post(formBody).build();

        ProgressDialog tmpDialog = dialog;
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        // Error
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        String resss = response.body().string();
                        if(tmpDialog!=null){
                            if (tmpDialog.isShowing()) {
                                tmpDialog.dismiss();
                            }
                        }
                        boolean bool = mSettings.getBoolean("save_photo",false);
                        if(!bool){
                            largePohto.delete();
                        }
                        // Do something with the response
                    }
                });
    }


    class SendData extends AsyncTask<Void,Void,Void>{
        private ProgressDialog dialog;
        int status;
        int city_id;
        String resss = "";
        boolean delFlag = false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(uploadFromActivity){
                dialog = new ProgressDialog(context);
                dialog.setMessage(titleProgress);
                dialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            delFlag = mSettings.getBoolean("save_photo",false);
            city_id = mSettings.getInt("city_id",0);
            try {
                RequestBody formBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("crop", crop)
                        .addFormDataPart("largePohto", largePohto.getName(),
                                RequestBody.create(MediaType.parse("text/plain"), largePohto))
                        .addFormDataPart("city_id", city_id+"")
                        .addFormDataPart("username", username)
                        .addFormDataPart("lat", lat+"")
                        .addFormDataPart("lng", lng+"")
                        .build();
                Request request = new Request.Builder().url(conf.getDomen()+"new_face").post(formBody).build();
                Log.e("request",request.toString());
                Response response = client.newCall(request).execute();
                Log.e("response",response.toString());
                status = response.code();
                resss = response.body().string();
            }catch (Exception e){}
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if(dialog!=null){
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
            if(status==200){
                Log.e("DELETE","DELETE");
                if(delFlag){
                    boolean delRes = largePohto.delete();
                    if(delRes){
                        Log.e("DELETE","true");
                    }else{
                        Log.e("DELETE","false");
                    }
                }
            }
        }
    }





    public String getCrop() {
        return crop;
    }

    public void setCrop(String crop) {
        this.crop = crop;
    }

    public Conf getConf() {
        return conf;
    }

    public void setConf(Conf conf) {
        this.conf = conf;
    }

    public File getLargePohto() {
        return largePohto;
    }

    public void setLargePohto(File largePohto) {
        this.largePohto = largePohto;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isUploadFromActivity() {
        return uploadFromActivity;
    }

    public void setUploadFromActivity(boolean uploadFromActivity) {
        this.uploadFromActivity = uploadFromActivity;
    }

    public String getTitleProgress() {
        return titleProgress;
    }

    public void setTitleProgress(String titleProgress) {
        this.titleProgress = titleProgress;
    }
}
