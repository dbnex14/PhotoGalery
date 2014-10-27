package com.learning.dino.photogalery;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by dbulj on 28/09/2014.
 */
public abstract class SingleFragmentActivity extends android.support.v4.app.FragmentActivity {
    protected abstract Fragment createFragment();

    protected int getLayoutResId(){
        return R.layout.activity_fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_fragment);
        setContentView(getLayoutResId());
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.Fragment fragment = fm.findFragmentById(R.id.fragmentContainer); //fragment is id-ed by its container

        if (fragment == null){
            fragment = createFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
    }
}
