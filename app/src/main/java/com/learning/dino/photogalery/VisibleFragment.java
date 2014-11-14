package com.learning.dino.photogalery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by dbulj on 13/11/2014.
 * Generic Fragment that hides foreground notifications.
 * Uses dynamic broadcast receiver which must be registered and unregistered in code.
 */
public class VisibleFragment extends Fragment {
    public static final String TAG = "VisibleFragment";

    //Define broadcast receiver as a variable since we must register/unregister it in different places in code.
    //Typically, you register broadcast reciever in a startup lifecycle method and unregister it in
    //a corresponding shutdown lifecycle method.
    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(getActivity(), "Got a broadcast: " + intent.getAction(), Toast.LENGTH_LONG).show();
            //If we receive this, we'are visible, so cancel the notification
            Log.i(TAG, "cancelling notification");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null);
    }

    @Override
    public void onPause(){
        super.onPause();
        getActivity().unregisterReceiver(mOnShowNotification);
    }
}
