package com.uplink.selfstore.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.uplink.selfstore.service.HeartbeatService;

public class HeartbeatRecevier extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //循环启动Service
        Intent i = new Intent(context, HeartbeatService.class);
        context.startService(i);
    }
}
