package com.learning.dino.photogalery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.ArrayList;

/**
 * Created by dbulj on 26/10/2014.
 */
public class PhotoGaleryFragment extends Fragment {

    private static final String TAG = "PhotoGaleryFragment";

    GridView mGridView;
    ArrayList<GaleryItem> mItems;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);  //retain the fragment

        new FetchItemsTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_photo_galery, container, false);
        mGridView = (GridView)v.findViewById(R.id.gridView);
        setupAdapter();
        return v;
    }

    void setupAdapter(){
        if (getActivity() == null || mGridView == null) {
            return;
        }

        if (mItems != null){
            mGridView.setAdapter(new ArrayAdapter<GaleryItem>(getActivity(), android.R.layout.simple_gallery_item, mItems));
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
}
