package com.uplink.selfstore.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.uplink.selfstore.utils.LogUtil;

public class UvcCameraService extends Service {

    private static final String TAG = "UvcCameraService";

    @Override
    public void onCreate() {
        LogUtil.d(TAG, "onCreate...");

        CommandReceiver cmdReceiver = new CommandReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.uvcCameraService");
        registerReceiver(cmdReceiver, filter);


        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy...");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class CommandReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            LogUtil.i(TAG, "onReceive");
        }
    }
}
