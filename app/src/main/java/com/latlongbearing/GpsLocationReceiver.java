package com.latlongbearing;

import static com.latlongbearing.BearingUtil.reloadonce;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class GpsLocationReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

         if(reloadonce==0){

             reloadonce=1;
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                Toast.makeText(context, "in android.location.PROVIDERS_CHANGED",
                        Toast.LENGTH_SHORT).show();
            /*Intent pushIntent = new Intent(context, GpsTracker.class);
            context.startService(pushIntent);*/
                Intent intentRestart = new Intent("restart-activity");
                intentRestart.putExtra("reload", "1");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intentRestart);

            }
        }
    }
}