package com.humdet;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Color;
import android.graphics.RectF;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.Face;

import com.humdet.tflite.AutoFitTextureView;
import com.humdet.tflite.BorderedText;
import com.humdet.tflite.ImageUtils;
import com.humdet.tflite.MultiBoxTracker;
import com.humdet.tflite.OverlayView;
import com.humdet.tflite.SimilarityClassifier;
import com.humdet.tflite.TFLiteObjectDetectionAPIModel;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;


public class MyService extends Service {
    SharedPreferences mSettings;
    SharedPreferences.Editor editor;

    ConstraintLayout layout = null;
    WindowManager windowManager = null;
    AutoFitTextureView texture;
    //String TAG = "MyService";
    private final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private Size imageDimension;
    protected CaptureRequest.Builder captureRequestBuilder;
    private String cameraId = "0";
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    private ImageReader imageReader;

    private Handler mBackgroundHandler;


    // Face detector
    private FaceDetector faceDetector;
    OverlayView trackingOverlay;
    protected int previewWidth = 0;
    protected int previewHeight = 0;
    private final float TEXT_SIZE_DIP = 10;
    private BorderedText borderedText;
    private MultiBoxTracker tracker;
    private SimilarityClassifier detector;
    private final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private final String TF_OD_API_MODEL_FILE = "mobile_face_net.tflite";
    private final int TF_OD_API_INPUT_SIZE = 112;
    private final boolean TF_OD_API_IS_QUANTIZED = false;
    private Integer sensorOrientation;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap portraitBmp = null;
    private Bitmap faceBmp = null;
    private Matrix frameToCropTransform;
    private  final boolean MAINTAIN_ASPECT = false;
    private Matrix cropToFrameTransform;
    private  final Size previewSize = new Size(640, 480);
    private int[] rgbBytes = null;
    private byte[][] yuvBytes = new byte[3][];
    private int yRowStride;
    private Runnable imageConverter;
    private Runnable postInferenceCallback;
    private long timestamp = 0;
    private Handler handler;
    private HandlerThread handlerThread;
    private boolean addPending = true;
    private Bitmap cropCopyBitmap = null;
    private long lastProcessingTimeMs;
    private boolean computingDetection = false;


    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private String[] array;
    Conf conf = new Conf();
    double lat=0,lng=0;
    SaveNewFace saveNewFace = null;
    public MyService() {


    }

    @Override
    public void onCreate() {
        new SaveNewFace(getApplicationContext());
        super.onCreate();
        mSettings = getSharedPreferences(new Conf().getShared_pref_name(), Context.MODE_PRIVATE);
        editor = mSettings.edit();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        layout = (ConstraintLayout) inflater.inflate(R.layout.camera_view, null);
        texture = (AutoFitTextureView) layout.findViewById(R.id.texture);
        trackingOverlay = (OverlayView) layout.findViewById(R.id.tracking_overlay);
        int lang = mSettings.getInt(conf.getLANG(), 2);
        if (lang == conf.getRU()) {
            array = getResources().getStringArray(R.array.app_lang_ru);
        } else if (lang == conf.getEN()) {
            array = getResources().getStringArray(R.array.app_lang_en);
        }/* else if (lang == conf.getAR()) {
            array = getResources().getStringArray(R.array.app_lang_ar);
        }*/
        saveNewFace = new SaveNewFace(getApplicationContext());
        texture.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, final int width, final int height) {
                openCamera(cameraId);
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

            }

        });
        layout.setOnTouchListener(new View.OnTouchListener() {
                                      float dX, dY;

                                      @Override
                                      public boolean onTouch(View view, MotionEvent motionEvent) {

                                          switch (motionEvent.getAction()) {

                                              case MotionEvent.ACTION_DOWN:

                                                  dX = view.getX() - motionEvent.getRawX();
                                                  dY = view.getY() - motionEvent.getRawY();
                                                  break;

                                              case MotionEvent.ACTION_MOVE:

                                                  view.animate()
                                                          .x(motionEvent.getRawX() + dX)
                                                          .y(motionEvent.getRawY() + dY)
                                                          .setDuration(0)
                                                          .start();
                                                  break;
                                              default:
                                                  return false;
                                          }
                                          return false;
                                      }
                                  }
        );
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                // Make the underlying application window visible through any transparent parts
                PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.RIGHT;
        p.dimAmount = (float) 0.0;
        windowManager.addView(layout, p);
        Button button2 = layout.findViewById(R.id.button2);
        button2.setText(array[0]);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout.setVisibility(View.GONE);
            }
        });
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build();


        FaceDetector detector = FaceDetection.getClient(options);

        faceDetector = detector;


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(location!=null){
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                }
            }
            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onProviderEnabled(String provider) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onStatusChanged(String provider, int status,Bundle extras) {
                // TODO Auto-generated method stub
            }
        });
    }

    LocationManager locationManager = null;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onTaskRemoved(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void openCamera(String cameraId) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        //Log.e(TAG, "is camera open");
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            onPreviewSizeChosen(previewSize,90);
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        //Log.e(TAG, "openCamera X");
    }
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            //Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            try{
                cameraDevice.close();
                cameraDevice = null;
            }catch (Exception e){}
        }
    };


    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = this.texture.getSurfaceTexture();
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);


            backgroundThread = new HandlerThread("ImageListener");
            backgroundThread.start();
            backgroundHandler = new Handler(backgroundThread.getLooper());
            mBackgroundHandler = backgroundHandler;
            imageReader =
                    ImageReader.newInstance(
                            640, 480, ImageFormat.YUV_420_888, 2);



            imageReader.setOnImageAvailableListener(readerListener, backgroundHandler);
            //previewReader.setOnImageAvailableListener(readerListener, backgroundHandler);

            captureRequestBuilder.addTarget(imageReader.getSurface());



            cameraDevice.createCaptureSession(
                    Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            //The camera is already closed
                            if (null == cameraDevice) {
                                return;
                            }
                            // When the session is ready, we start displaying the preview.
                            cameraCaptureSessions = cameraCaptureSession;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(MyService.this, "Configuration change", Toast.LENGTH_SHORT).show();
                        }
                    }, null);
        } catch (Exception e) {
            e.printStackTrace();
            //Log.e(TAG,e.toString());
        }
    }
    protected void updatePreview() {
        if (null == cameraDevice) {
            //Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    protected void takePicture(long currTimestamp) {
        if (null == cameraDevice) {
            //Log.e(TAG, "cameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(texture.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int rotation = windowManager.getDefaultDisplay().getRotation();

            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation)+90);

            final File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "DCIM" + File.separator + "HUMDET");
            if(!dir.exists()){
                dir.mkdir();
            }
            final File file = new File(Environment.getExternalStorageDirectory() + File.separator + "DCIM" + File.separator +"HUMDET"+File.separator+ currTimestamp+".jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    bitmap = resizeImage(bitmap,640,true);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

                    Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);


                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] cropByteArray = stream.toByteArray();
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(cropByteArray);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                    saveNewFace.setLargePohto(file);
                    saveNewFace.execute();
                }
            };

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(MyService.this, "Image saved", Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(this);


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
            //Log.e(TAG, e.toString());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            previewWidth = size.getWidth();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            previewHeight = size.getHeight();
        }

        sensorOrientation = rotation - getScreenOrientation();
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.RGB_565);//RGB_565 ARGB_8888


        int targetW, targetH;
        if (sensorOrientation == 90 || sensorOrientation == 270) {
            targetH = previewWidth;
            targetW = previewHeight;
        }
        else {
            targetW = previewWidth;
            targetH = previewHeight;
        }
        int cropW = (int) (targetW / 2.0);
        int cropH = (int) (targetH / 2.0);

        croppedBitmap = Bitmap.createBitmap(cropW, cropH, Bitmap.Config.RGB_565);

        portraitBmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.RGB_565);
        faceBmp = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Bitmap.Config.RGB_565);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropW, cropH,
                        sensorOrientation, MAINTAIN_ASPECT);

//    frameToCropTransform =
//            ImageUtils.getTransformationMatrix(
//                    previewWidth, previewHeight,
//                    previewWidth, previewHeight,
//                    sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);


        Matrix frameToPortraitTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        targetW, targetH,
                        sensorOrientation, MAINTAIN_ASPECT);



        trackingOverlay.addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        String str = tracker.draw(canvas);
                        if(str!=null){
                            //То снимок и шлем на сервак
                        }
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    }

    protected int getScreenOrientation() {
        switch (windowManager.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }



    protected void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                Log.d("Buffer at size", i+ buffer.capacity()+"");
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;


        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

        InputImage image = InputImage.fromBitmap(croppedBitmap, 0);

        try{
            faceDetector
                    .process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                        @Override
                        public void onSuccess(List<Face> faces) {
                            if (faces.size() == 0) {
                                updateResults(currTimestamp, new LinkedList<>(),null);
                                return;
                            }
                            runInBackground(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            onFacesDetected(currTimestamp, faces/*, addPending*/);
                                            addPending = false;
                                        }
                                    });
                        }

                    }).getResult();
        }catch (Exception e){}


    }
    protected int[] getRgbBytes() {
        imageConverter.run();
        return rgbBytes;
    }


    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }

    public synchronized void onResume() {
        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }
    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }
    private void updateResults(long currTimestamp, final List<SimilarityClassifier.Recognition> mappedRecognitions,Bitmap crop ) {
        tracker.trackResults(mappedRecognitions, currTimestamp);
        trackingOverlay.postInvalidate();
        computingDetection = false;

        Date date = new Date();
        String dateStr = date.toLocaleString().split(" ")[0];

        //Log.e(TAG,"updateResults");
        if (mappedRecognitions.size() > 0) {
            SimilarityClassifier.Recognition rec = mappedRecognitions.get(0);
            if ((rec.getExtra() != null) && (rec.getTitle()==null||rec.getTitle().equals(""))) {
                detector.register(currTimestamp+"", rec);
                if(!(rec.getDate().equals(dateStr))&&rec.getDate().equals("")){
                    if(crop!=null){
                        SimilarityClassifier.Recognition finalRec = rec;
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                /*final File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "DCIM" + File.separator + "HUMDET"+ File.separator + currTimestamp+"_small"+".jpg");
                                try (FileOutputStream out = new FileOutputStream(dir)) {
                                    crop.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }*/
                                try{
                                    saveNewFace.setLat(lat);
                                    saveNewFace.setLng(lng);
                                }catch (Exception e){}
                                saveNewFace.setUsername(currTimestamp+"");
                                saveNewFace.setTitle(currTimestamp+"");
                                float mas[][] = (float[][]) finalRec.getExtra();
                                String masToSend = "";
                                for(int i=0;i<mas[0].length;i++){
                                    masToSend += mas[0][i];
                                    if(i!=mas[0].length-1){
                                        masToSend = masToSend+",";
                                    }
                                }
                                saveNewFace.setCrop(masToSend);
                            }
                        });
                        t.start();
                        Thread t2 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                takePicture(currTimestamp);
                            }
                        });
                        t2.start();
                    }
                }else if(rec.getDate().equals("")){
                    if(crop!=null){
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                /*final File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "DCIM" + File.separator + "HUMDET"+ File.separator + currTimestamp+"_small"+".jpg");
                                try (FileOutputStream out = new FileOutputStream(dir)) {
                                    crop.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }*/
                            }
                        });
                        t.start();
                        Thread t2 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                takePicture(currTimestamp);
                            }
                        });
                        t2.start();
                    }
                }

            }else if(rec.getExtra() != null){
                rec = mappedRecognitions.get(0);
                detector.register(currTimestamp+"", rec);
            }

        }


    }

    private boolean onFacesDetected(long currTimestamp, List<Face> faces) {
        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
        Bitmap crop = null;
        final Canvas canvas = new Canvas(cropCopyBitmap);
        final Paint paint = new Paint();
        paint.setColor(Color.TRANSPARENT);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(2.0f);
        final List<SimilarityClassifier.Recognition> mappedRecognitions =
                new LinkedList<SimilarityClassifier.Recognition>();
        int sourceW = rgbFrameBitmap.getWidth();
        int sourceH = rgbFrameBitmap.getHeight();
        int targetW = portraitBmp.getWidth();
        int targetH = portraitBmp.getHeight();
        Matrix transform = createTransform(
                sourceW,
                sourceH,
                targetW,
                targetH,
                sensorOrientation);
        final Canvas cv = new Canvas(portraitBmp);
        cv.drawBitmap(rgbFrameBitmap, transform, null);
        final Canvas cvFace = new Canvas(faceBmp);
        for (Face face : faces) {
            final RectF boundingBox = new RectF(face.getBoundingBox());
            final boolean goodConfidence = true;
            if (boundingBox != null && goodConfidence) {
                cropToFrameTransform.mapRect(boundingBox);
                RectF faceBB = new RectF(boundingBox);
                transform.mapRect(faceBB);
                cv.drawRect(faceBB, paint);
                float sx = ((float) TF_OD_API_INPUT_SIZE) / faceBB.width();
                float sy = ((float) TF_OD_API_INPUT_SIZE) / faceBB.height();
                Matrix matrix = new Matrix();
                matrix.postTranslate(-faceBB.left, -faceBB.top);
                matrix.postScale(sx, sy);
                cvFace.drawBitmap(portraitBmp, matrix, null);
                canvas.drawRect(faceBB, paint);
                String label = "";
                float confidence = -1f;
                Integer color = Color.BLUE;
                Object extra = null;
                try{
                    crop = Bitmap.createBitmap(portraitBmp,
                            (int) faceBB.left,
                            (int) faceBB.top,
                            (int) faceBB.width(),
                            (int) faceBB.height());
                }catch (Exception e){e.printStackTrace();}
                if(crop!=null){
                    final long startTime = SystemClock.uptimeMillis();
                    final List<SimilarityClassifier.Recognition> resultsAux = detector.recognizeImage(faceBmp, true);
                    lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                    if (resultsAux.size() > 0) {
                        SimilarityClassifier.Recognition result = resultsAux.get(0);
                        extra = result.getExtra();
                        float conf = result.getDistance();
                        if (conf < 1.0f) {
                            confidence = conf;
                            label = result.getTitle();
                            if (result.getId().equals("0")) {
                                color = Color.TRANSPARENT;
                            }else {
                                color = Color.TRANSPARENT;
                            }
                        }
                    }
                    final SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition("0", label, confidence, boundingBox,"");
                    result.setColor(color);
                    result.setLocation(boundingBox);
                    result.setExtra(extra);
                    result.setCrop(crop);
                    mappedRecognitions.add(result);
                }
            }
        }
        updateResults(currTimestamp, mappedRecognitions,crop);
        return true;
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        /*Intent restartServiceIntent = new Intent(getApplicationContext(),this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);*/
        super.onTaskRemoved(rootIntent);
    }

    private Matrix createTransform(
            final int srcWidth,
            final int srcHeight,
            final int dstWidth,
            final int dstHeight,
            final int applyRotation) {

        Matrix matrix = new Matrix();
        if (applyRotation != 0) {

            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);

            // Rotate around origin.
            matrix.postRotate(applyRotation);
        }

//        // Account for the already applied rotation, if any, and then determine how
//        // much scaling is needed for each axis.
//        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;
//        final int inWidth = transpose ? srcHeight : srcWidth;
//        final int inHeight = transpose ? srcWidth : srcHeight;

        if (applyRotation != 0) {

            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
        }

        return matrix;

    }

    ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(final ImageReader reader) {
            // We need wait until we have some size from onPreviewSizeChosen
            if (previewWidth == 0 || previewHeight == 0) {
                return;
            }
            if (rgbBytes == null) {
                rgbBytes = new int[previewWidth * previewHeight];
            }
            try {
                final Image image = reader.acquireLatestImage();

                if (image == null) {
                    return;
                }

                final Image.Plane[] planes = image.getPlanes();
                fillBytes(planes, yuvBytes);
                yRowStride = planes[0].getRowStride();
                final int uvRowStride = planes[1].getRowStride();
                final int uvPixelStride = planes[1].getPixelStride();

                imageConverter =
                        new Runnable() {
                            @Override
                            public void run() {
                                ImageUtils.convertYUV420ToARGB8888(
                                        yuvBytes[0],
                                        yuvBytes[1],
                                        yuvBytes[2],
                                        previewWidth,
                                        previewHeight,
                                        yRowStride,
                                        uvRowStride,
                                        uvPixelStride,
                                        rgbBytes);
                            }
                        };

                postInferenceCallback =
                        new Runnable() {
                            @Override
                            public void run() {
                                image.close();
                            }
                        };

                onResume();
                processImage();
            } catch (final Exception e) {
                //Log.e(TAG,e.toString());
                e.printStackTrace();
                return;
            }
        }
    };


    public Bitmap resizeImage(Bitmap realImage, float maxImageSize,boolean filter) {
        float ratio = Math.min((float) maxImageSize / realImage.getWidth(),(float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,height, filter);
        return newBitmap;
    }

}