package com.latlongbearing;


import static com.latlongbearing.BearingUtil.reloadonce;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CaptureImage extends AppCompatActivity {

    String reload="";
    Button btnNext;
    ProgressBar progress_circular;
    private GpsTracker gpsTracker;
    TextView txtGPSLocked, txtDirectionSet;
    EditText edit_distance;
    double latitude=0.0D,longitude=0.0D;
    float bearing=0.0f;
    AlertDialog.Builder builder;
    GpsLocationReceiver gpsLocationReceiver = new GpsLocationReceiver();
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);


        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("restart-activity"));

        btnNext = findViewById(R.id.btnNext);
        progress_circular = findViewById(R.id.progress_circular);
        txtGPSLocked = findViewById(R.id.txtGPSLocked);
        txtDirectionSet = findViewById(R.id.txtDirectionSet);
        edit_distance = findViewById(R.id.edit_distance);


        // create the get Intent object
        Intent intent = getIntent();

        String msg = intent.getStringExtra("msg");
        String device_latitude = intent.getStringExtra("device_latitude");
        String device_longitude = intent.getStringExtra("device_longitude");
        String object_latitude = intent.getStringExtra("object_latitude");
        String object_longitude = intent.getStringExtra("object_longitude");
        String distance = intent.getStringExtra("distance");
        String bearing_angle = intent.getStringExtra("bearing_angle");

        if( msg!=null && msg.equals("msg")){
            edit_distance.setText(distance);
            txtDirectionSet.setText("LOCKED");
            progress_circular.setVisibility(View.GONE);
            txtGPSLocked.setVisibility(View.VISIBLE);

           // getLocation(); commented

            //added these
            progress_circular.setVisibility(View.GONE);
            txtGPSLocked.setVisibility(View.VISIBLE);
            txtGPSLocked.append("\n"+"("+device_latitude+","+device_longitude+")");


        }
       else if(msg!=null && device_latitude!=null && device_longitude!=null &&  object_latitude!=null && object_longitude!=null && distance!=null & bearing_angle!=null)
        {
            if(device_latitude.charAt(0)=='-')
                device_latitude = device_latitude.substring(1,device_latitude.length()-1)+ " " +"S";
            else
                device_latitude = device_latitude+ " " +"N";

            if(device_longitude.charAt(0)=='-')
                device_longitude=device_longitude.substring(1,device_longitude.length()-1) + " " +"W";
            else
                device_longitude=device_longitude + " " +"E";


            if(object_latitude.charAt(0)=='-')
                object_latitude = object_latitude.substring(1,object_latitude.length()-1)+ " " +"S";
            else
                object_latitude = object_latitude+ " " +"N";

            if(object_longitude.charAt(0)=='-')
                object_longitude=object_longitude.substring(1,object_longitude.length()-1) + " " +"W";
            else
                object_longitude=object_longitude + " " +"E";


            String data= /*"Image Path Saved : "+msg +*/ "\ndevice latitude : "+device_latitude + "\ndevice longitude : "+ device_longitude + "\ndistance(metres) : "+distance +
                    "\nobject latitude : "+object_latitude +"\nobject longitude : "+object_longitude+"\nbearing angle : "+bearing_angle;
            edit_distance.setText(distance);
            txtDirectionSet.setText("LOCKED");
            progress_circular.setVisibility(View.GONE);
            txtGPSLocked.setVisibility(View.VISIBLE);
            showDialogMsg(data);

        }
        else
        getLocation();


        if(txtDirectionSet.getText().equals("LOCKED"))
        {
            txtDirectionSet.setEnabled(false);
            txtDirectionSet.setClickable(false);
        }

        txtDirectionSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(progress_circular.getVisibility()==View.VISIBLE){

                }
                else {
                    View view1 = getCurrentFocus();
                    if (view1 != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    Intent intent = new Intent(CaptureImage.this, CameraXActivity.class);
                    intent.putExtra("clickedNext", "0");
                    intent.putExtra("latitude", String.valueOf(latitude));
                    intent.putExtra("longitude", String.valueOf(longitude));
                    intent.putExtra("distance", edit_distance.getText().toString());
                    intent.putExtra("degree", bearing);
                    startActivity(intent);
                }
            }
        });


        if(edit_distance.length()>0){
            btnNext.setEnabled(true);
            btnNext.setBackgroundColor(R.color.purple_500);
        }
        edit_distance.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // TODO Auto-generated method stub
            }



            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                // TODO Auto-generated method stub
            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void afterTextChanged(Editable s) {

                // TODO Auto-generated method stub

                if(s.length()==0){
                    btnNext.setEnabled(false);

                    btnNext.setBackgroundColor(android.R.color.darker_gray);
                }
                if(s.length()>0){
                    btnNext.setEnabled(true);

                    btnNext.setBackgroundColor(R.color.purple_500);
                }
            }
        });
    }




    @Override
    protected void onRestart() {
        super.onRestart();

        if(!txtGPSLocked.getText().toString().contains("("))
        getLocation();
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
             reload = intent.getStringExtra("reload");

             if(reload.equals("1")){

                 Intent intentRestart = getIntent();
                 intentRestart.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                 finish();
                 overridePendingTransition(0, 0);

                 startActivity(intentRestart);
                 overridePendingTransition(0, 0);

              //   gpsTracker.showSettingsAlert(reload);
                 getLocation();
             }

        }
    };

    private void showDialogMsg(String data) {
        builder = new AlertDialog.Builder(this);

        //Setting message manually and performing action on button click
        builder= builder.setMessage(data)
                .setCancelable(false)
                .setPositiveButton("EXIT APP", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        try {
                            // clearing app data
                            String packageName = getApplicationContext().getPackageName();
                            Runtime runtime = Runtime.getRuntime();
                            runtime.exec("pm clear "+packageName);



                        } catch (Exception e) {
                            e.printStackTrace();
                        }



                    }
                });

        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        //alert.setTitle("AlertDialogExample");
        alert.show();
    }
    @SuppressLint("ResourceAsColor")
    public void getLocation() {

        try {
            gpsTracker = new GpsTracker(CaptureImage.this);
            if (gpsTracker.canGetLocation()) {
                latitude = gpsTracker.getLatitude();
                longitude = gpsTracker.getLongitude();
               // bearing = Float.parseFloat(""+gpsTracker.getBearing());



                if(!String.valueOf(latitude).equals("0.0") &&  !String.valueOf(longitude).equals("0.0")) {
                  //  Toast.makeText(CaptureImage.this, "latitude:" + String.valueOf(latitude) + "\n" + "longitude:" + String.valueOf(longitude)/*+"\n"+"bearing angle:"+bearing*/, Toast.LENGTH_SHORT).show();
                    reloadonce=0;
                    progress_circular.setVisibility(View.GONE);
                    txtGPSLocked.setVisibility(View.VISIBLE);
                    txtGPSLocked.append("\n"+"("+String.valueOf(latitude)+","+String.valueOf(longitude)+")");
                }

                else{
                    getLocation();
                }
            } else {


                gpsTracker.showSettingsAlert("");
            }
        } catch (Exception e) {
            Toast.makeText(CaptureImage.this, e.toString()
                    , Toast.LENGTH_SHORT).show();
        }
    }

    public void gotoCameraActivity(View view) {

       /* if(edit_distance.getText().toString().equals(""))
            Toast.makeText(getBaseContext(), "Please enter the distance from the object", Toast.LENGTH_SHORT).show();

        else {*/
            // Check if no view has focus:
            View view1 = this.getCurrentFocus();
            if (view1 != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            Intent intent = new Intent(this, CameraXActivity.class);
            intent.putExtra("clickedNext", "1");
            intent.putExtra("latitude", String.valueOf(latitude));
            intent.putExtra("longitude", String.valueOf(longitude));
            intent.putExtra("distance", edit_distance.getText().toString());
            intent.putExtra("bearing", bearing);

            startActivity(intent);
            finish();
        //}
    }





    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

}