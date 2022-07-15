package com.latlongbearing;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.DITHER_FLAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;



import java.io.FileOutputStream;
import java.io.IOException;

public class CameraXActivity extends AppCompatActivity  implements SensorEventListener {



    VerticalSeekBar seekBar1,seekBar2;
    ImageView imgBrightness,imgZoom;
    float [] mGravity;
    float [] mGeomagnetic;

    double new_latitude=0.0D, new_longitude=0.0D,cos=0.0D,your_metres=/*399000*/0.0D,bearing_angle=0.0D;
    float degree;

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    TextureView textureView;
    ImageView imgArrow,imgCapture,imageView/*imageLine*/;
    TextView txtBearingAngle,txtObjectSide,txtHint,txtYourSide;

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;

    private float[] floatGravity = new float[3];
    private float[] floatGeoMagnetic = new float[3];

    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];

    ImageView zoom_out,zoom_in;
    Button btnLock;
    Preview preview;
    int right=0,left=0,top=0,bottom=0;
    int max=0,min=0;
    AlertDialog.Builder builder;

    String clickedNext,latitude,longitude,distance;
    double latitude_double,longitude_double;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;

    ConstraintLayout rootLayout;

    SharedPreferences sharedPreferences;


    public Bitmap addWatermark(Bitmap bitmap, String waterMarkText){

        Bitmap result = bitmap.copy(bitmap.getConfig(), true);
        Canvas canvas = new Canvas(result);
        Paint paint =  new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(10f);

       /* val textSize = result.width * *//*options.textSizeToWidthRatio*//*0.04f;
        paint.textSize = textSize;
        paint.color = options.textColor
        if (options.shadowColor != null) {
            paint.setShadowLayer(textSize / 2, 0f, 0f, options.shadowColor)
        }
        if (options.typeface != null) {
            paint.typeface = options.typeface
        }
        val padding = result.width * options.paddingToWidthRatio*/
        PointF coordinates =
                calculateCoordinates(waterMarkText, paint, /*canvas.width*/1000, /*canvas.height*/500, /*padding*/100);
        canvas.drawText("watermarkText", coordinates.x, coordinates.y, paint);
        return  bitmap;
    }

    public PointF calculateCoordinates(String watermarkText, Paint paint, int width, int height, float padding){
        int x = (int) (width-padding);
        int y /*= (int) (height-padding)*/;

        Rect bounds = new Rect();
        paint.getTextBounds(watermarkText, 0, watermarkText.length(), bounds);
        int textHeight = bounds.height();
        y= (int) (textHeight + padding);

      return new PointF(x, y);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_xactivity);

        File folder = new File(Environment.getExternalStorageDirectory() + "/FarsiteGPS");
        if (!folder.exists()) {
            folder.mkdirs();
        }




        // Storing data into SharedPreferences
        sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);


        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        imgZoom = findViewById(R.id.imgZoom);
        seekBar2 = findViewById(R.id.seekBar2);
        seekBar1 = findViewById(R.id.seekBar1);
        imgBrightness = findViewById(R.id.imgBrightness);
        rootLayout = findViewById(R.id.rootLayout);
        txtObjectSide = findViewById(R.id.txtObjectSide);
        txtHint = findViewById(R.id.txtHint);
        txtYourSide = findViewById(R.id.txtYourSide);
        txtBearingAngle = findViewById(R.id.txtBearingAngle);
        imgArrow = findViewById(R.id.imgArrow);

        textureView = findViewById(R.id.view_finder);
        imgCapture = findViewById(R.id.imgCapture);
        imageView = findViewById(R.id.imageView);
        // imageLine = findViewById(R.id.imageLine);
        btnLock = findViewById(R.id.btnLock);
        zoom_out = findViewById(R.id.zoom_out);
        zoom_in = findViewById(R.id.zoom_in);

        Intent intent = getIntent();
        clickedNext = intent.getStringExtra("clickedNext");
        latitude = intent.getStringExtra("latitude");
        longitude = intent.getStringExtra("longitude");
        distance = intent.getStringExtra("distance");
        degree = intent.getFloatExtra("degree",degree);

        if (clickedNext.equals("1")) {

            // imageLine.setVisibility(View.GONE);
            // imageView.setVisibility(View.VISIBLE);
            txtBearingAngle.setVisibility(View.GONE);
            txtObjectSide.setVisibility(View.GONE);
            txtHint.setVisibility(View.GONE);
            btnLock.setVisibility(View.GONE);
            imgArrow.setVisibility(View.GONE);
            txtYourSide.setVisibility(View.GONE);
            imgCapture.setVisibility(View.VISIBLE);
            imgBrightness.setVisibility(View.VISIBLE);
            seekBar1.setVisibility(View.VISIBLE);
            /*seekBar2.setVisibility(View.VISIBLE);
            imgZoom.setVisibility(View.VISIBLE);*/

            rootLayout.setBackgroundColor(0);
        }
        else{
            textureView.setVisibility(View.GONE);
            txtBearingAngle.setVisibility(View.VISIBLE);
            txtObjectSide.setVisibility(View.VISIBLE);
            txtHint.setVisibility(View.VISIBLE);
            txtYourSide.setVisibility(View.VISIBLE);
            imgArrow.setVisibility(View.VISIBLE);


        }

        //Seekbar brightness
        seekBar1.setMax(255);
        float curBrightnessValue = 0;
        try {
            curBrightnessValue = android.provider.Settings.System.getInt(
                    getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        int screen_brightness = (int) curBrightnessValue;
        seekBar1.setProgress(screen_brightness);

        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Nothing handled here
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // Nothing handled here
            }

            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // Set the minimal brightness level
                // if seek bar is 20 or any value below
                screenBrightness(progress);
                //editor.putInt("brightness",progress);
                //editor.commit();
            }
        });

        //Zoom image
        Rational aspectRatio = new Rational (textureView.getWidth(), textureView.getHeight());
        Size screen = new Size(textureView.getWidth(), textureView.getHeight()); //size of the screen

        PreviewConfig pConfig = new PreviewConfig.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setTargetResolution(screen)
                //.setLensFacing(CameraX.LensFacing.FRONT)
                .build();
        preview = new Preview(pConfig);

        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Nothing handled here
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // Nothing handled here
            }

            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (progress > 0) {
                    right += 100;
                    bottom += 100;
                    left += 100;
                    top += 100;
                    Rect my = new Rect(left, top, right, bottom);
                    preview.zoom(my);
                }
                if (progress < 100) {
                    right -= 100;
                    bottom -= 100;
                    left -= 100;
                    top -= 100;
                    Rect my = new Rect(left, top, right, bottom);
                    preview.zoom(my);
                }



                Log.d("progress",""+progress);
            }
        });

        zoom_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (right < 100) {
                    right += 100;
                    bottom += 100;
                    left += 100;
                    top += 100;
                    Rect my = new Rect(left, top, right, bottom);
                    preview.zoom(my);
                }


            }
        });

        zoom_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (right > 100) {
                    right -= 100;
                    bottom -= 100;
                    left -= 100;
                    top -= 100;
                    Rect my = new Rect(left, top, right, bottom);
                    preview.zoom(my);
                }
            }
        });

        Log.d("right", ""+right);

        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        btnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //lock the lat long
                // Creating an Editor object to edit(write to the file)
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("latitude", latitude);
                myEdit.putString("longitude", longitude);
                myEdit.putString("bearing_angle", String.valueOf(degree));

                myEdit.commit();



                // String new_lat_long= getNewLatLong(Double.parseDouble(latitude),Double.parseDouble(longitude),Double.parseDouble("0"),degree);
                String new_lat_long= newLatLong(Double.parseDouble(latitude),Double.parseDouble(longitude),Double.parseDouble("0"),degree);
                String [] arr_latlong= new_lat_long.split(",");

                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(), CaptureImage.class);
                        intent.putExtra("msg", "msg");
                        intent.putExtra("device_latitude", latitude);
                        intent.putExtra("device_longitude", longitude);
                        intent.putExtra("object_latitude", arr_latlong[0]);
                        intent.putExtra("object_longitude", arr_latlong[1]);
                        intent.putExtra("distance", "0");
                        intent.putExtra("bearing_angle", String.valueOf(degree));
                        startActivity(intent);
                        finish();
                    }
                }, 1000);

            }
        });
    }

    private void screenBrightness(double newBrightnessValue) {
        /*
         * WindowManager.LayoutParams settings = getWindow().getAttributes();
         * settings.screenBrightness = newBrightnessValue;
         * getWindow().setAttributes(settings);
         */
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        float newBrightness = (float) newBrightnessValue;
        lp.screenBrightness = newBrightness / (float) 255;
        getWindow().setAttributes(lp);
    }

    void changeBrightnessMode() {

        try {
            int brightnessMode = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }

        } catch (Exception e) {
            // do something useful

        }
    }

    private String getDirection(double angle){
        String direction = "";

        if (angle >= 350 || angle <= 10)
            direction = "N";
        if (angle < 350 && angle > 280)
            direction = "NW";
        if (angle <= 280 && angle > 260)
            direction = "W";
        if (angle <= 260 && angle > 190)
            direction = "SW";
        if (angle <= 190 && angle > 170)
            direction = "S";
        if (angle <= 170 && angle > 100)
            direction = "SE";
        if (angle <= 100 && angle > 80)
            direction = "E";
        if (angle <= 80 && angle > 10)
            direction = "NE";

        return direction=" "+direction;
    }

    private void showDialogMsg() {
        builder = new AlertDialog.Builder(this);

        //Setting message manually and performing action on button click
        builder= builder.setMessage("Please hold your mobile screen parallel to the ground and at your eye level. Position the mobile properly so that the arrow will be in a line between you and the object.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                    }
                });

        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        //alert.setTitle("AlertDialogExample");
        alert.show();
    }


    private void startCamera() {

        CameraX.unbindAll();



        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(Preview.PreviewOutput output){
                        ViewGroup parent = (ViewGroup) textureView.getParent();
                        parent.removeView(textureView);
                        parent.addView(textureView, 0);

                        textureView.setSurfaceTexture(output.getSurfaceTexture());
                        updateTransform();
                    }
                });



        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
        final ImageCapture imgCap = new ImageCapture(imageCaptureConfig);

        imgCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // getting the locked/saved lat long

                // Fetching the stored data
                // from the SharedPreference
                SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);

                String latitude = sh.getString("latitude", "");
                String longitude = sh.getString("longitude", "");
                String bearing_angle = sh.getString("bearing_angle", "");



                String new_lat_long= newLatLong(Double.parseDouble(latitude),Double.parseDouble(longitude),Double.parseDouble(distance),degree);

                String [] arr_latlong= new_lat_long.split(",");

                // File file = new File(Environment.getExternalStorageDirectory() + "/" + /*System.currentTimeMillis()*/"Device_latitude_"+latitude+"Device_longitude_"+longitude +".png");
                File file = new File(Environment.getExternalStorageDirectory(), "/FarsiteGPS/"+System.currentTimeMillis()+"_lat_"+arr_latlong[0]+"long_"+arr_latlong[1] +".png");



                imgCap.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        String msg = "Pic captured at " + file.getAbsolutePath();
                        Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();

                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        addWatermark(bitmap,"23.0003,34.4049");

                        geoTag(file.getAbsolutePath(), Double.parseDouble(arr_latlong[0]), Double.parseDouble(arr_latlong[1]));


                        Intent intent = new Intent(CameraXActivity.this,PreviewActivity.class);
                        intent.putExtra("url",file.getAbsolutePath());
                        startActivity(intent);



                        geoTag(file.getAbsolutePath(), Double.parseDouble(latitude), Double.parseDouble(longitude));

                        String new_lat_long= newLatLong(Double.parseDouble(latitude),Double.parseDouble(longitude),Double.parseDouble(distance),degree);

                        String [] arr_latlong= new_lat_long.split(",");

                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(getApplicationContext(), CaptureImage.class);
                                intent.putExtra("msg", msg);
                                intent.putExtra("device_latitude", latitude);
                                intent.putExtra("device_longitude", longitude);
                                intent.putExtra("object_latitude", arr_latlong[0]);
                                intent.putExtra("object_longitude", arr_latlong[1]);
                                intent.putExtra("distance", distance);
                                intent.putExtra("bearing_angle",  bearing_angle);
                                startActivity(intent);
                                finish();
                            }
                        }, 1000);

                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                        String msg = "Pic capture failed : " + message;
                        Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
                        if(cause != null){
                            cause.printStackTrace();
                        }
                    }
                });
            }
        });

        //bind to lifecycle:
        CameraX.bindToLifecycle((LifecycleOwner)this, preview, imgCap);

        imageView.setVisibility(View.VISIBLE);

    }

    public void geoTag(String filename, double latitude, double longitude){
        ExifInterface exif;

        try {
            exif = new ExifInterface(filename);
            int num1Lat = (int)Math.floor(latitude);
            int num2Lat = (int)Math.floor((latitude - num1Lat) * 60);
            double num3Lat = (latitude - ((double)num1Lat+((double)num2Lat/60))) * 3600000;

            int num1Lon = (int)Math.floor(longitude);
            int num2Lon = (int)Math.floor((longitude - num1Lon) * 60);
            double num3Lon = (longitude - ((double)num1Lon+((double)num2Lon/60))) * 3600000;

            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, num1Lat+"/1,"+num2Lat+"/1,"+num3Lat+"/1000");
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, num1Lon+"/1,"+num2Lon+"/1,"+num3Lon+"/1000");


            if (latitude > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
            }

            if (longitude > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            }

            exif.saveAttributes();

        } catch (IOException e) {
            Log.e("PictureActivity", e.getLocalizedMessage());
        }

    }


    public static File savebitmap(Bitmap bmp) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, bytes);
        File f = new File(Environment.getExternalStorageDirectory()
                + File.separator + System.currentTimeMillis() + ".png");
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();
        return f;
    }

    private String newLatLong(double old_latitude, double old_longitude, double your_metres , float brng){



        double earth = 6378137;  //radius of the earth in meter
        double pi = Math.PI;
        // double m = (1 / ((2 * pi / 360) * earth)) / 1000;  //1 meter in degree
        brng= (float) Math.toRadians(brng);


        old_latitude = Math.toRadians(old_latitude);
        old_longitude = Math.toRadians(old_longitude);

        double  new_latitude = Math.asin( Math.sin(old_latitude)*Math.cos(your_metres/earth) +
                Math.cos(old_latitude)*Math.sin(your_metres/earth)*Math.cos(brng));

        double new_longitude = old_longitude + Math.atan2(Math.sin(brng)*Math.sin(your_metres/earth)*Math.cos(old_latitude),
                Math.cos(your_metres/earth)-Math.sin(old_latitude)*Math.sin(new_latitude));

        new_latitude = Math.toDegrees(new_latitude);
        new_longitude = Math.toDegrees(new_longitude);

        String new_lat_long = String.valueOf(Round_off(new_latitude,8.0)) + ","+ String.valueOf(Round_off(new_longitude,8.0));

        return new_lat_long;
    }


    public static double Round_off(double N, double n)
    {
        int h;
        double l, a, b, c, d, e, i, j, m, f, g;
        b = N;
        c = Math.floor(N);

        // Counting the no. of digits to the left of decimal point
        // in the given no.
        for (i = 0; b >= 1; ++i)
            b = b / 10;

        d = n - i;
        b = N;
        b = b * Math.pow(10, d);
        e = b + 0.5;
        if ((float)e == (float)Math.ceil(b)) {
            f = (Math.ceil(b));
            h = (int)(f - 2);
            if (h % 2 != 0) {
                e = e - 1;
            }
        }
        j = Math.floor(e);
        m = Math.pow(10, d);
        j = j / m;
        return j;
    }



    private void updateTransform(){
        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int)textureView.getRotation();

        switch(rotation){
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float)rotationDgr, cX, cY);
        textureView.setTransform(mx);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // get the angle around the z-axis rotated
        double compass_degree  = Math.round(sensorEvent.values[0]);

        txtBearingAngle.setText(/*Float.toString(degree)*/ String.format("%.0f", compass_degree) +" \u00B0" +  getDirection(compass_degree));

        degree = (float) compass_degree;
        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        imageView.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }


    public String getNewLatLong(double old_latitude, double old_longitude, double your_metres , float  degree ){

        String new_lat_long="";


        double R = 6378.137;  //radius of the earth in kilometer  R = 6378.1 #Radius of the Earth
        double brng= Math.toRadians(degree); // brng = 1.57 #Bearing is 90 degrees converted to radians.

        double d =(your_metres)/100000; // 15 #Distance in km



        double lat1 = old_latitude *(Math.PI * 180);// Math.toRadians(old_latitude); ;//#Current lat point converted to radians
         double lon1  = old_longitude * (Math.PI * 180) ; // Math.toRadians(old_longitude) //#Current long point converted to radians

        /*double lat2 = Math.asin((Math.cos(lat1)*Math.sin(d/R)) +( Math.sin(lat1)* Math.sin(d/R) * Math.cos(brng)));
        double lon2  = lon1 + Math.atan2((Math.sin(brng) *Math.sin(d/R) * Math.cos(lat1)), Math.cos(d/R)-(Math.sin(lat1) * Math.sin(lat2)));*/

        //NEW_X = OLD_X + (SIN(RADIANS(BEARING)) * DISTANCE)
        double lat2 = old_latitude + (Math.sin(brng) * d);

        //NEW_Y = OLD_Y + (COS(RADIANS(BEARING)) * DISTANCE)
        double lon2 =  old_longitude +  Math.cos(brng) * d;

        new_lat_long = String.valueOf(Round_off(lat2,8.0)) + ","+ String.valueOf(Round_off(lon2,8.0));

        return new_lat_long;
    }






}
