package com.learning.dino.photogalery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by dbulj on 26/10/2014.
 */
//public class PhotoGaleryFragment extends Fragment {
public class PhotoGaleryFragment extends VisibleFragment {

    private static final String TAG = "PhotoGaleryFragment";

    GridView mGridView;
    ArrayList<GaleryItem> mItems;
    ThumbnailDownloader<ImageView> mThumbnailThread;  //this is a background HandlerThread
    private LruCache<String, Bitmap> mMemoryCache; //LRU cashing

    public void updateItems(){
        new FetchItemsTask().execute();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);  //retain the fragment
        setHasOptionsMenu(true); //hookup options menu callbacks
        updateItems();
        //new FetchItemsTask().execute();

        // Start PollService
        //Intent i = new Intent(getActivity(), PollService.class);
        //getActivity().startService(i);
        //Moved to menu item
        //PollService.setServiceAlarm(getActivity(), true);

        //CHALLANGE, Ch27 - Get max VM memory.  Exceedint this amount will throw an OutOfMemory exception.
        //final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        //Use 1/5 of available memory for this cache
        //final int cacheSize = maxMemory / 5;
        //mMemoryCache = new LruCache<String, Bitmap>(cacheSize);

        //pass main thread's handler to ThumbnailDownloader background thread.  Remember, since this
        //handler is created in main thread, it will attach itsself to the main thread looper.
        mThumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());
        //set listener to set the returning bitmaps on the ImageView handles and update main thread
        //from the background thread
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                if (isVisible()) {
                    //We guard call to setImageBitmap with Fragment.isVisible() to ensure that we
                    //are not setting the image on a stale ImageView
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();
        Log.i(TAG, "Background thread ThumbnailDownloader started");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_photo_galery, container, false);
        mGridView = (GridView)v.findViewById(R.id.gridView);

        setupAdapter();
        return v;
    }

    @Override
    public void onDestroyView(){
        //Clean out downloader when view is destroyed.
        super.onDestroy();
        mThumbnailThread.clearQueue();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mThumbnailThread.quit();
        Log.i(TAG, "Background thread ThumbnailDownloader destroyed");
    }

    @Override
    @TargetApi(11)
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            //Pull out the SearchView
            MenuItem searchItem = menu.findItem(R.id.menu_item_search);
            SearchView searchView = (SearchView)searchItem.getActionView();

            //Get the data from our searchable.xml as a SearchableInfo
            SearchManager sm = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
            ComponentName cn = getActivity().getComponentName();
            SearchableInfo searchInfo = sm.getSearchableInfo(cn);
            searchView.setSearchableInfo(searchInfo);
            //CHALLANGE Ch28.
            // This will add 2 new icons to your SearchView (X and >, for delete search, and search).
            // Double click on X will return to the original search (clear SharedPreference persisted
            // search query) ->
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    Log.i(TAG, "onClose() called on our SearchView");

                    //Update GridView with a normal fetch search result (i.e. remove search results).
                    //We have to remove our persisted query, then updateItems() to query Thumbnails again.
                    PreferenceManager.getDefaultSharedPreferences(getActivity())
                            .edit()
                            .putString(FlickrFetchr.PREF_SEARCH_QUERY, null)
                            .commit();
                    updateItems();
                    return false;
                }
            });
            searchView.setSubmitButtonEnabled(true);
            //CHALLANGE Ch28.<-
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_item_search:
                getActivity().onSearchRequested(); //search button implementation but you must make activity an searchable activity for this to work
                return true;
            case R.id.menu_item_clear:
                //cancel search query from shared preferences
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(FlickrFetchr.PREF_SEARCH_QUERY, null)
                        .commit();
                updateItems();
                return true;
            case R.id.menu_item_toggle_polling:
                //toggle service polling
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);

                //We need to do this on post 3.0 devices to tell action bar to update
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                    getActivity().invalidateOptionsMenu();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        //This method is called every time a menu needs to be configured so it is ideal place
        //to put the code that changes the text of the menu item based on the state of application.
        //In pre 3.0 devices, ths method is called every time the menu is displayed which ensures
        //that your menu item always shows the right text.
        //However, on post 3.0 devices, this is not enought since ActionBar does not automatically
        //update itselt.  You have to manually tell it to call onPrepareOptionsMenu() and refresh
        //its items by calling Activity.invalidateOptionsMenu()
        super.onPrepareOptionsMenu(menu);

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())){
            toggleItem.setTitle(R.string.stop_polling);
        }else{
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    void setupAdapter(){
        if (getActivity() == null || mGridView == null) {
            return;
        }

        if (mItems != null){
            mGridView.setAdapter(new GaleryItemAdapter(mItems));
            //mGridView.setAdapter(new ArrayAdapter<GaleryItem>(getActivity(), android.R.layout.simple_gallery_item, mItems));
        } else {
            mGridView.setAdapter(null);
        }
    }

    /*
     * Inner class to fetch data in a background thread.
     */
    //private class FetchItemsTask extends AsyncTask<Void, Void, Void>{
    private class FetchItemsTask extends AsyncTask<Void, Void, ArrayList<GaleryItem>> {
        @Override
        //protected Void doInBackground(Void... params){
        protected ArrayList<GaleryItem> doInBackground(Void... params) {
            //String query = null;
            //String query = "android"; //Just for testing
            Activity a = getActivity();
            if (a == null){
                return new ArrayList<GaleryItem>();
            }

            String query = PreferenceManager.getDefaultSharedPreferences(a).getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
            if (query != null){
                return new FlickrFetchr().search(query);
            }else{
                return new FlickrFetchr().fetchItems();
            }
            //try{
            //    String result = new FlickrFetchr().getUrl("http://www.google.com");
            //    Log.i(TAG, "Fetched contents of URL: " + result);
            //}catch (IOException ioe){
            //    Log.e(TAG, "Failed to fetch URL: :", ioe);
            //}
            //new FlickrFetchr().fetchItems();
            //return new FlickrFetchr().fetchItems();
            //return null;
        }

        @Override
        protected void onPostExecute(ArrayList<GaleryItem> items){
            mItems = items;
            setupAdapter();
            //CHALLANGE, Ch28.->
            String resultSetSize = "Query returned " + mItems.size() + " result(s).";
            Toast.makeText(getActivity(), resultSetSize, Toast.LENGTH_SHORT).show();
            //CHALLANGE, Ch28.<-
        }
    }

    private class GaleryItemAdapter extends ArrayAdapter<GaleryItem>{

        public GaleryItemAdapter(ArrayList<GaleryItem> items){
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            if (convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.galery_item, parent, false);
            }

            ImageView imageView = (ImageView)convertView.findViewById(R.id.galery_item_imageView);
            imageView.setImageResource(R.drawable.blank_image);
            GaleryItem item = getItem(position);
            mThumbnailThread.queueThumbnail(imageView, item.getUrl());
            return convertView;
        }
    }
}
