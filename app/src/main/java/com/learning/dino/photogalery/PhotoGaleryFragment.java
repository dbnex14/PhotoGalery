package com.learning.dino.photogalery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * Created by dbulj on 26/10/2014.
 */
public class PhotoGaleryFragment extends Fragment {

    GridView mGridView;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);  //retain the fragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_photo_galery, container, false);
        mGridView = (GridView)v.findViewById(R.id.gridView);
        return v;
    }
}
