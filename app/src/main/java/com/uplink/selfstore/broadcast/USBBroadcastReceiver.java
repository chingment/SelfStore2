package com.uplink.selfstore.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.uplink.selfstore.utils.LogUtil;

public class USBBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        LogUtil.i("USBBroadcastReceiver");
        String action=intent.getAction();
        if (action.equals("android.hardware.usb.action.USB_STATE")) {
            if (intent.getExtras().getBoolean("connected")) {
                // usb 插入
                LogUtil.i("USB插入");
            } else {
                LogUtil.i("USB拔出");
            }
        }
    }
}