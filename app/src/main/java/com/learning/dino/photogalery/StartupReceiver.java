package com.learning.dino.photogalery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by dbulj on 10/11/2014.
 * BroadcastReceiver to detect that device has booted up.  Used by PhotoGalery to wake up
 * alarm if user reboots so alarm does not get forgotten.
 */
public class StartupReceiver extends BroadcastReceiver{

    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent){
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());

        //get preference constant from PollService indicating if alarm should be turned on on bootup
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isOn = sp.getBoolean(PollService.PREF_IS_ALARM_ON, false);
        PollService.setServiceAlarm(context, isOn);
    }
}
