package com.uplink.selfstore.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.uplink.selfstore.R;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;

public class ExOrderHandleActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "ExOrderHandleActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exorderhandle);
    }
}
