package com.latlongbearing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

public class LandingPage extends AppCompatActivity {

    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.ACCESS_FINE_LOCATION"};
    private int REQUEST_CODE_PERMISSIONS = 101;
    Button btnGetStarted;

    SensorManager sensorManager;
    String sensors_list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        // getSensorList(Sensor.TYPE_ALL) lists all the sensors present in the device
       //  List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);


        // Converting List to String and displaying
        // every sensor and its information on a new line
       /* for (Sensor sensors : deviceSensors) {

           sensors_list=sensors_list + sensors.toString() + "\n\n";

        }*/



    /*    if(!sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD).getName().toString().toLowerCase().contains("magnetometer")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("MAGNETOMETER SENSOR NOT AVAILABLE").setCancelable(false)
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //  Action for 'NO' Button
                            dialog.cancel();
                            finish();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }*/

        btnGetStarted=findViewById(R.id.btnGetStarted);

        /*try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);

            }
        } catch (Exception e){
            e.printStackTrace();
        }*/

        if(allPermissionsGranted()){
            btnGetStarted.setEnabled(true);
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                // higlight the next button
                btnGetStarted.setEnabled(true);
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();

            }
        }
    }
    public void gotoNext(View v){
        startActivity(new Intent(this, StartActivity.class));

    }

    public void gotoCameraActivity(View view) {
    }

    public void gotoCaptureActivity(View view) {
    }
}