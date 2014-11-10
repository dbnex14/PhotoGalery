package com.learning.dino.photogalery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by dbulj on 09/11/2014.
 * Service to poll for search results.  Service must be declared in manifest like Activity.
 */
public class PollService extends IntentService {

    private static final String TAG = "PollService";
    private static final int POLL_INTERVAL = 1000 * 15; //15 seconds
    //private static final int POLL_INTERVAL = 1000 * 60 * 5; //5 minutes

    public PollService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        //Check for network availability since Android provides the ability for the user to turn off
        //networking for background applications which can improve performance.  So, you need to check
        //for network availability and you need to check it in 2 ways:
        //1. For older versions of Android, you check by using getBackgroundDataSetting() and if it
        //   returns false, bail out.  The problem with this check was that it could be forgotten
        //   accidentally
        //2. In Android 4.0 (Ice Cream Sandwich), this was changed so that the background data
        //   setting simply disabled the network entirely.  And this is why you have to call
        //   getActiveNetworkInfo() and check if it returns null.  If it is null, then networking
        //   does not work at all.  To use getActiveNetworkInfo(), you need to aquire
        //   ACCESS_NETWORK_STATE permission, so add it in AndroidManifest.xml
        @SuppressWarnings("deprecation")
        boolean isNetworkAvailable = (cm.getBackgroundDataSetting() && (cm.getActiveNetworkInfo() != null));
        if (!isNetworkAvailable){
            Log.i(TAG, "No network available");
            return;
        }

        //To fill out your service, you need:
        //1. Pull out the current query and result Id from the default SharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String query = prefs.getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
        String lastResultId = prefs.getString(FlickrFetchr.PREF_LAST_RESULT_ID, null);

        //2. Fetch the latest result set with FlickrFetchr
        ArrayList<GaleryItem> items;
        if (query != null){
            items = new FlickrFetchr().search(query);
            Log.i(TAG, "Fetched using query: " + query);
        }else{
            items = new FlickrFetchr().fetchItems();
            Log.i(TAG, "Fetched using fetchItems()");
        }
        if (items.size() == 0){
            return;
        }

        //3. If there are results, grab the first one
        String resultId = items.get(0).getId();

        //4. Check to seee if it is different from the last result Id
        if (!resultId.equals(lastResultId)){
            Log.i(TAG, "Got a new result: " + resultId);

            //Make PollService notify the user that a new result is ready.
            Resources r = getResources();
            PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, PhotoGaleryActivity.class), 0);
            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(r.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(r.getString(R.string.new_pictures_title))
                    .setContentText(r.getString(R.string.new_pictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();
            NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            nm.notify(0, notification);
        }else{
            Log.i(TAG, "Got an old result: " + resultId);
        }

        //5. Store the first result back in SharedPreferences
        prefs.edit()
                .putString(FlickrFetchr.PREF_LAST_RESULT_ID, resultId)
                .commit();
    }

    public static void setServiceAlarm(Context context, boolean isOn){
        Intent i = new Intent(context, PollService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        if (isOn){
            am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_INTERVAL, pi);
        }else{
            am.cancel(pi);
            pi.cancel();
        }
    }

    //Check if alarm is on or not
    public static boolean isServiceAlarmOn(Context context){
        Intent i = new Intent(context, PollService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }
}
