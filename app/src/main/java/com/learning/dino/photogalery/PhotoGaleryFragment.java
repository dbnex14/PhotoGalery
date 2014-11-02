package com.learning.dino.photogalery;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by dbulj on 26/10/2014.
 */
public class PhotoGaleryFragment extends Fragment {

    private static final String TAG = "PhotoGaleryFragment";

    GridView mGridView;
    ArrayList<GaleryItem> mItems;
    ThumbnailDownloader<ImageView> mThumbnailThread;  //this is a background HandlerThread

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);  //retain the fragment
        new FetchItemsTask().execute();

        //pass main thread's handler to ThumbnailDownloader background thread.  Remember, since this
        //handler is created in main thread, it will attach itsself to the main thread looper.
        mThumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());
        //set listener to set the returning bitmaps on the ImageView handles and update main thread
        //from the background thread
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>(){
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail){
                if (isVisible()){
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
            //try{
            //    String result = new FlickrFetchr().getUrl("http://www.google.com");
            //    Log.i(TAG, "Fetched contents of URL: " + result);
            //}catch (IOException ioe){
            //    Log.e(TAG, "Failed to fetch URL: :", ioe);
            //}
            //new FlickrFetchr().fetchItems();
            return new FlickrFetchr().fetchItems();
            //return null;
        }

        @Override
        protected void onPostExecute(ArrayList<GaleryItem> items){
            mItems = items;
            setupAdapter();
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
