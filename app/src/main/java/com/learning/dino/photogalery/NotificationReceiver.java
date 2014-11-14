package com.learning.dino.photogalery;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by dbulj on 13/11/2014.
 */
public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context c, Intent i){
        Log.i(TAG, "received result: " + getResultCode());
        if (getResultCode() != Activity.RESULT_OK){
            // A foreground actifity cancelled the broadcast
            return;
        }

        int requestCode = i.getIntExtra("REQUEST_CODE", 0);
        Notification notification = (Notification)i.getParcelableExtra("NOTIFICATION");
        NotificationManager nm = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(requestCode, notification);
    }
}
