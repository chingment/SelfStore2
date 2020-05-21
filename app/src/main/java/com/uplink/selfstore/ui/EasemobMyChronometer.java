package com.uplink.selfstore.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Chronometer;

public class EasemobMyChronometer extends Chronometer{

    public EasemobMyChronometer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EasemobMyChronometer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EasemobMyChronometer(Context context) {
        super(context);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        //continue when view is hidden
        visibility = View.VISIBLE;
        super.onWindowVisibilityChanged(visibility);
    }
}