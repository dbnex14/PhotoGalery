package com.learning.dino.photogalery;

import android.app.SearchManager;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;


public class PhotoGaleryActivity extends SingleFragmentActivity {

    private static final String TAG = "PhotoGaleryActivity";

    @Override
    public Fragment createFragment(){
        return new PhotoGaleryFragment();
    }

    @Override
    public void onNewIntent(Intent i){
        PhotoGaleryFragment f = (PhotoGaleryFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        if (i.ACTION_SEARCH.equals(i.getAction())){
            String query = i.getStringExtra(SearchManager.QUERY);
            Log.i(TAG, "Received a new search qeury: " + query);

            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putString(FlickrFetchr.PREF_SEARCH_QUERY, query)
                    .commit();
        }

        f.updateItems();
    }
}
