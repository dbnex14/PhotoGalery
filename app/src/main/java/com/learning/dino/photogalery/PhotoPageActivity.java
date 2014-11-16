package com.learning.dino.photogalery;

import android.support.v4.app.Fragment;

/**
 * Created by dbulj on 15/11/2014.
 */
public class PhotoPageActivity extends SingleFragmentActivity {
    @Override
    public Fragment createFragment(){
        return new PhotoPageFragment();
    }
}
