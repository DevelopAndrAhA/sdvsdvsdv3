package com.humdet;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.humdet.tflite.SimilarityClassifier;
import com.humdet.tflite.TFLiteObjectDetectionAPIModel;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.List;

public class NeuroTrainingActivity extends AppCompatActivity {
    final int Pic_image=299;
    private String [] array;
    Conf conf = new Conf();

    SharedPreferences mSettings;
    Button buttonUpload = null;
    private FaceDetector faceDetector;

    BitmapFactory.Options bOptions = null;
    Bitmap bitmap = null;
    InputStream input = null;


    private SimilarityClassifier detector;
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final String TF_OD_API_MODEL_FILE = "mobile_face_net.tflite";
    private static final int TF_OD_API_INPUT_SIZE = 112;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    TextView editTextTextPersonName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getSharedPreferences(conf.getShared_pref_name(), Context.MODE_PRIVATE);
        setContentView(R.layout.activity_neuro_training);
        bOptions = new BitmapFactory.Options();
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build();

        faceDetector = FaceDetection.getClient(options);

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
        getSupportActionBar().setTitle(array[11]);

        Button button = findViewById(R.id.button);
        button.setText(array[3]);
        ImageView imageView = findViewById(R.id.imageView);
        bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.hum_icon);
        imageView.setImageBitmap(bitmap);
        buttonUpload = findViewById(R.id.buttonUpload);
        buttonUpload.setText(array[12]);
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SaveNewFace saveNewFace = new SaveNewFace(NeuroTrainingActivity.this);
                saveNewFace.setLat(0);
                saveNewFace.setLng(0);
                saveNewFace.setUsername("username");
                final List<SimilarityClassifier.Recognition> resultsAux = detector.recognizeImage(faceBmp112_112, true);
                SimilarityClassifier.Recognition result = resultsAux.get(0);
                float mas[][] = (float[][]) result.getExtra();
                String masToSend = "";
                for(int i=0;i<mas[0].length;i++){
                    masToSend += mas[0][i];
                    if(i!=mas[0].length-1){
                        masToSend = masToSend+",";
                    }
                }
                //save(masToSend,editTextTextPersonName.getText().toString());
                saveNewFace.setCrop(masToSend);
                saveNewFace.setLargePohto(fileToSend);
                saveNewFace.setTitle(array[14]);
                saveNewFace.setTitleProgress(array[12]);
                saveNewFace.setUploadFromActivity(true);
                saveNewFace.setContext(NeuroTrainingActivity.this);
                saveNewFace.execute();
            }
        });
        editTextTextPersonName = findViewById(R.id.editTextTextPersonName);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Button DELDATA = findViewById(R.id.DELDATA);
        DELDATA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                class DeleteTask extends AsyncTask<Void,Void,Void>{
                    private ProgressDialog dialog;
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        dialog = new ProgressDialog(NeuroTrainingActivity.this);
                        dialog.setMessage(array[1]);
                        dialog.show();
                    }

                    int status=0;
                    @Override
                    protected Void doInBackground(Void... voids) {
                        OkHttpClient client = new  OkHttpUtils().getInstance();
                        Request request = new Request.Builder()
                                .url(conf.getDomen()+ "deleteFullFace")
                                .build();
                        Call call = client.newCall(request);
                        try{
                            Response response = call.execute();
                            status = response.code();
                        }catch (Exception e){e.printStackTrace();}
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void unused) {
                        super.onPostExecute(unused);
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Toast.makeText(NeuroTrainingActivity.this, ""+status, Toast.LENGTH_SHORT).show();
                    }
                }
                new DeleteTask().execute();
            }

        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }

    public void faceDatector(Bitmap bitmap){
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        try {
            faceDetector
                    .process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                        @Override
                        public void onSuccess(List<Face> faces) {
                            if (faces.size() == 0) {
                                Toast.makeText(NeuroTrainingActivity.this,array[6],Toast.LENGTH_LONG).show();
                                buttonUpload.setVisibility(View.INVISIBLE);
                                return;
                            }
                            faceDetect(bitmap,faces);
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
    File fileToSend = null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Pic_image) {
            uriToSend = data.getData();
            try {
                input = getContentResolver().openInputStream(uriToSend);
                //bOptions.inSampleSize = 2;
                bitmap = BitmapFactory.decodeStream(input, null, bOptions);
                fileToSend = new File(getRealPathFromUri(uriToSend));
                ImageView imageView = findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);
                faceDatector(bitmap);
            } catch (FileNotFoundException e) {e.printStackTrace();}
        }
    }
    Bitmap faceBmp112_112=null;
    public void faceDetect(Bitmap bitmap,List<Face> faces){
        for(Face f : faces){
            try{
                Rect rect = f.getBoundingBox();
                Bitmap orBmp = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false);
                Bitmap faceBmp = Bitmap.createBitmap(orBmp, rect.left, rect.top, rect.width(), rect.height());
                faceBmp112_112 = Bitmap.createScaledBitmap(faceBmp, 112, 112, false);
                buttonUpload.setVisibility(View.VISIBLE);
            }catch (Exception e){
                Toast.makeText(NeuroTrainingActivity.this,array[15],Toast.LENGTH_SHORT).show();
                buttonUpload.setVisibility(View.INVISIBLE);
            }
        }
    }
    public void save(String strmas,String name){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                final File file = new File(Environment.getExternalStorageDirectory() + File.separator + "Documents" + File.separator + name+".txt");
                try{
                    file.createNewFile();
                    FileWriter fileOutputStream = new FileWriter(file);
                    fileOutputStream.write(strmas);
                    fileOutputStream.close();
                }catch (Exception e){e.printStackTrace();}
            }
        });
        t.start();

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
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}