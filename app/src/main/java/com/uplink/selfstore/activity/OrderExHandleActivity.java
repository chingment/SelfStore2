package com.uplink.selfstore.activity;

import android.os.Bundle;
import android.view.View;

import com.uplink.selfstore.R;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;

public class OrderExHandleActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "OrderExHandleActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderexhandle);
    }
}
