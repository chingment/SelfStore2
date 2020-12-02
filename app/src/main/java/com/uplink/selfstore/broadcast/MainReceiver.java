package com.uplink.selfstore.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.uplink.selfstore.activity.InitDataActivity;
import com.uplink.selfstore.utils.LogUtil;

public class MainReceiver extends BroadcastReceiver {
    private static final String TAG = "MainReceiver";
    static final String ACTION = "android.intent.action.PACKAGE_REPLACED";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d(TAG,"onReceive");
        if (intent.getAction().equals(ACTION)) {
            LogUtil.d(TAG,ACTION);
            Intent it = new Intent(context, InitDataActivity.class);  // 要启动的Activity
            //1.如果自启动APP，参数为需要自动启动的应用包名
            //Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
            //下面这句话必须加上才能开机自动运行app的界面
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //2.如果自启动Activity
            context.startActivity(it);
            //3.如果自启动服务
            //context.startService(intent);
        }
    }
}