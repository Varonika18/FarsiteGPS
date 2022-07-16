package com.latlongbearing;

import static com.latlongbearing.CameraActivity.bitmap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.io.File;

public class PreviewActivity extends AppCompatActivity {

    String url;
    ImageView imgPreview;
    Button btnRetake,btnSave;
    FusedLocationProviderClient mFusedLocationClient;


    TextView latitudeTextView, longitTextView;
    int PERMISSION_ID = 44;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        imgPreview=findViewById(R.id.imgPreview);
        latitudeTextView = findViewById(R.id.latTextView);
        longitTextView = findViewById(R.id.lonTextView);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // method to get the location
        getLastLocation();
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                url= null;
            } else {
                url= extras.getString("url");
            }
        } else {
            url= (String) savedInstanceState.getSerializable("url");
        }


        btnRetake=findViewById(R.id.btnRetake);
        btnSave=findViewById(R.id.btnSave);

        imgPreview.setImageURI(Uri.fromFile(new File(url)));

        btnRetake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File fdelete = new File(Uri.fromFile(new File(url)).getPath());
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        System.out.println("file Deleted :" + Uri.fromFile(new File(url)).getPath());
                    } else {
                        System.out.println("file not Deleted :" + Uri.fromFile(new File(url)).getPath());
                    }
                }
                finish();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            latitudeTextView.setText(location.getLatitude() + "");
                            longitTextView.setText(location.getLongitude() + "");
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latitudeTextView.setText("Latitude: " + mLastLocation.getLatitude() + "");
            longitTextView.setText("Longitude: " + mLastLocation.getLongitude() + "");
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        File fdelete = new File(Uri.fromFile(new File(url)).getPath());
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("file Deleted :" + Uri.fromFile(new File(url)).getPath());
            } else {
                System.out.println("file not Deleted :" + Uri.fromFile(new File(url)).getPath());
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    /*public void addwatermarklocation(View view) {
    }*/

    /*public void addwatermarklocation(View view){

        private BitmapDrawable writeTextOnDrawable(int drawableId, String latitudeTextView) {

            Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId)
                    .copy(Bitmap.Config.ARGB_8888, true);

            Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setTypeface(tf);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(convertToPixels(mContext, 11));

            Rect textRect = new Rect();
            paint.getTextBounds(text, 0, text.length(), textRect);

            Canvas canvas = new Canvas(bm);

            //If the text is bigger than the canvas , reduce the font size
            if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
                paint.setTextSize(convertToPixels(mContext, 7));        //Scaling needs to be used for different dpi's

            //Calculate the positions
            int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

            //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
            int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

            canvas.drawText(text, xPos, yPos, paint);

            return new BitmapDrawable(getResources(), bm);
        }



        public static int convertToPixels(Context context, int nDP)
        {
            final float conversionScale = context.getResources().getDisplayMetrics().density;

            return (int) ((nDP * conversionScale) + 0.5f) ;
    }*/

}