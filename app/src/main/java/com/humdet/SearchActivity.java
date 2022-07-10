package com.humdet;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.Face;
import com.humdet.tflite.SimilarityClassifier;
import com.humdet.tflite.TFLiteObjectDetectionAPIModel;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

public class SearchActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    final int Pic_image=299;
    private String [] array;
    Conf conf = new Conf();

    SharedPreferences mSettings;
    SharedPreferences.Editor editor;
    Button buttonSearch = null;
    private FaceDetector faceDetector;
    EditText dateEdit = null;
    TextView percentTxt=null;
    JSONArray jsonArray;



    private SimilarityClassifier detector;
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final String TF_OD_API_MODEL_FILE = "mobile_face_net.tflite";
    private static final int TF_OD_API_INPUT_SIZE = 112;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSettings = getSharedPreferences(conf.getShared_pref_name(), Context.MODE_PRIVATE);
        editor = mSettings.edit();

        bOptions = new BitmapFactory.Options();
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build();

        faceDetector = FaceDetection.getClient(options);


        /*Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                faceDatector(null,"eva3");
            }
        });
        t.start();*/
        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
        } catch (final Exception e) {
            e.printStackTrace();
        }


        int lang = mSettings.getInt(conf.getLANG(),0);
        if(lang==conf.getRU()){
            array = getResources().getStringArray(R.array.app_lang_ru);
        }else if(lang==conf.getEN()){
            array = getResources().getStringArray(R.array.app_lang_en);
        }/*else if(lang==conf.getAR()){
            array = getResources().getStringArray(R.array.app_lang_ar);
        }*/

        getSupportActionBar().setTitle(array[2]);
        Button button = findViewById(R.id.button);
        button.setText(array[3]);
        ImageView imageView = findViewById(R.id.imageView);
        bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.hum_icon);
        imageView.setImageBitmap(bitmap);
        buttonSearch = findViewById(R.id.buttonUpload);
        buttonSearch.setText(array[2]);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SearchTask().execute();
            }
        });
        SeekBar seekBar = findViewById(R.id.seekBar);
        percentTxt = findViewById(R.id.seekBarValue);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                percentTxt.setText(String.valueOf(progress)+" %");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        TextView seeklabel = findViewById(R.id.seeklabel);
        TextView datelabel = findViewById(R.id.dateLabel);
        seeklabel.setText(array[4]);
        datelabel.setText(array[5]);
        Calendar now = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            now = Calendar.getInstance();
        }
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                SearchActivity.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dateEdit = findViewById(R.id.editTextDate2);
        DatePickerDialog finalDpd = dpd;
        dateEdit.setFocusable(false);
        dateEdit.setFocusableInTouchMode(false);
        dateEdit.setClickable(true);
        dateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalDpd.show(getSupportFragmentManager(), "Datepickerdialog");
            }
        });
        Date tmpDate = new Date();
        String dateMas [] = tmpDate.toString().split(" ");
        int locDateDay = Integer.parseInt(dateMas[2]);
        int locDatemMonth = tmpDate.getMonth()+1;
        String locDateDayStr = null;
        String locDatemMonthStr = null;
        if(locDateDay<10){
            locDateDayStr="0"+locDateDay;
        }else{
            locDateDayStr=locDateDay+"";
        }
        if(locDatemMonth<10){
            locDatemMonthStr="0"+locDatemMonth;
        }else{
            locDatemMonthStr=locDatemMonth+"";
        }
        String locDateYear = dateMas[5]+"-"+locDatemMonthStr+"-"+locDateDayStr;
        dateEdit.setText(locDateYear);
    }


    public void faceDatector(Bitmap bitmap,String imgname){
        InputImage image = InputImage.fromBitmap(bitmap, 0);
       try {
           faceDetector
                   .process(image)
                   .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                       @Override
                       public void onSuccess(List<Face> faces) {
                           if (faces.size() == 0) {
                               Toast.makeText(SearchActivity.this,array[6],Toast.LENGTH_LONG).show();
                               buttonSearch.setVisibility(View.INVISIBLE);
                               return;
                           }
                           faceDetect(bitmap,faces,imgname);
                       }
                   }).getResult();
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    public void selectImage(View view){
        Intent intentImage = new Intent(Intent.ACTION_PICK);
        intentImage.setType("image/*");
        startActivityForResult(intentImage, Pic_image);

    }

    BitmapFactory.Options bOptions = null;
    Bitmap bitmap = null;
    InputStream input = null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Pic_image) {
            uriToSend = data.getData();
            try {
                input = getContentResolver().openInputStream(uriToSend);
                //bOptions.inSampleSize = 2;
                bitmap = BitmapFactory.decodeStream(input, null, bOptions);
                File fileToSend = new File(getRealPathFromUri(uriToSend));
                ImageView imageView = findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);
                faceDatector(bitmap,fileToSend.getName());
            } catch (FileNotFoundException e) {e.printStackTrace();}
        }
    }
    Bitmap faceBmp112_112=null;
    String masToSend = "";
    public void faceDetect(Bitmap bitmap,List<Face> faces,String imageName){
        for(Face f : faces){
            try{
                Rect rect = f.getBoundingBox();
                Bitmap orBmp = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false);
                Bitmap faceBmp = Bitmap.createBitmap(orBmp, rect.left, rect.top, rect.width(), rect.height());
                faceBmp112_112 = Bitmap.createScaledBitmap(faceBmp, 112, 112, false);
                buttonSearch.setVisibility(View.VISIBLE);
                final List<SimilarityClassifier.Recognition> resultsAux = detector.recognizeImage(faceBmp112_112, true);
                SimilarityClassifier.Recognition result = resultsAux.get(0);
                float mas[][] = (float[][]) result.getExtra();
                for(int i=0;i<mas[0].length;i++){
                    masToSend += mas[0][i];
                    if(i!=mas[0].length-1){
                        masToSend = masToSend+",";
                    }
                }
            }catch (Exception e){
                Toast.makeText(SearchActivity.this,array[15],Toast.LENGTH_SHORT).show();
                buttonSearch.setVisibility(View.INVISIBLE);
            }
        }
    }

    Uri uriToSend=null;
    private String getRealPathFromUri(Uri contentUri){
        String result=null;
        Cursor cursor = getContentResolver().query(contentUri,null,null,null,null);
        if(cursor == null){
            result = contentUri.getPath();
        }else{
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        monthOfYear = monthOfYear+1;
        if(monthOfYear<10){
            dateEdit.setText(year+"-0"+monthOfYear+"-"+dayOfMonth);
        }else{
            dateEdit.setText(year+"-"+monthOfYear+"-"+dayOfMonth);
        }
    }

    class SearchTask extends AsyncTask<Void,Void,Void>{
        private ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(SearchActivity.this);
            dialog.setMessage(array[2]);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //faceBmp112_112.compress(Bitmap.CompressFormat.PNG, 100, stream);
            //byte[] cropByteArray = stream.toByteArray();
            OkHttpClient client = new OkHttpClient();
            try {
                String url = "search?percent="+percentTxt.getText().toString().split(" ")[0]+"&inpDate="+dateEdit.getText().toString()+"&crop="+ masToSend;
                Log.e("url",url);
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
                Intent intent = new Intent(SearchActivity.this,ResultSearchActivity.class);
                intent.putExtra("jsonArray",jsonArray.toString());
                startActivity(intent);
            }else{
                Toast.makeText(SearchActivity.this,array[13],Toast.LENGTH_SHORT).show();
            }
        }
    }

    //private SimilarityClassifier detector;
    /*private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final String TF_OD_API_MODEL_FILE = "mobile_face_net.tflite";
    private static final int TF_OD_API_INPUT_SIZE = 112;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;


    final List<SimilarityClassifier.Recognition> mappedRecognitions =
            new LinkedList<SimilarityClassifier.Recognition>();*/

    /*public void teach(Bitmap faceBmp,RectF boundingBox,String image){
        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            //cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        float confidence = -1f;

        final List<SimilarityClassifier.Recognition> resultsAux = detector.recognizeImage(faceBmp, true);
        final SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition("0", "label", confidence, boundingBox,"");
        result.setColor(123);
        result.setLocation(boundingBox);
        result.setExtra(resultsAux.get(0).getExtra());
        float f [][] = (float[][])resultsAux.get(0).getExtra();
        for(int i=0;i<f[0].length;i++){
            Log.e(image,f[0][i]+"");
        }
        result.setCrop(faceBmp);
        mappedRecognitions.add(result);
        SimilarityClassifier.Recognition rec = mappedRecognitions.get(0);
        detector.register("label001", rec);

    }*/
}