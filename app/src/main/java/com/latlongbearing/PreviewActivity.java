package com.latlongbearing;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;

public class PreviewActivity extends AppCompatActivity {

    String url;
    ImageView imgPreview;
    Button btnRetake,btnSave;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);


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

        imgPreview=findViewById(R.id.imgPreview);
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
    public void onResume(){
        super.onResume();
        if(!Utils.isPermissionGranted(this))
        {
            new AlertDialog.Builder(this)
                    .setTitle("All files permission")
                    .setMessage("Due to Android 11 restrictions,this app requires all files permission ")
                    .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            takePermission();
                        }
                    })
                    .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else
        {
            Toast.makeText(this,"Permission already granted",Toast.LENGTH_LONG).show();
        }
    }

    private void takePermission() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R)
        {   try {
            Intent intent = new Intent((Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION));
            intent.addCategory("android.intent.category.DEFAULT");
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, 101);
        }
        catch (Exception e){
            e.printStackTrace();
            Intent intent=new Intent();
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            startActivityForResult(intent, 101);
        }
        }
        else
        {
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE
            },101);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>=0){
            if(requestCode==101){
                boolean readExt=grantResults[0]== PackageManager.PERMISSION_GRANTED;
                if(!readExt){
                    takePermission();
                }
            }
        }
    }
}