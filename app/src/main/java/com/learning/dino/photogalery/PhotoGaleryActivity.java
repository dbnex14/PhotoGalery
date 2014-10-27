package com.learning.dino.photogalery;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class PhotoGaleryActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment(){
        return new PhotoGaleryFragment();
    }
}
