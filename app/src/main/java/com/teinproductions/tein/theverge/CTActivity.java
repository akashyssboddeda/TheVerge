package com.teinproductions.tein.theverge;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;


/**
 * Activity that manages a CustomTabsHelper
 */
public class CTActivity extends AppCompatActivity {

    protected CustomTabsHelper tabsHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tabsHelper = new CustomTabsHelper();
    }

    @Override
    protected void onStart() {
        super.onStart();
        tabsHelper.bindService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        tabsHelper.unbindService(this);
    }
}
