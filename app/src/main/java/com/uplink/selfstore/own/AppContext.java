package com.uplink.selfstore.own;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.uplink.selfstore.activity.InitDataActivity;
import com.uplink.selfstore.utils.LogUtil;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by chingment on 2017/8/23.
 */

public class AppContext extends Application {

    private static final String UPLOAD_URL = "http://demo.api.term.17fanju.com/Api/Machine/UploadLog";
    private static AppContext app;

    public AppContext() {
        app = this;
    }

    public static synchronized AppContext getInstance() {
        if (app == null) {
            app = new AppContext();
        }
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        JPushInterface.setDebugMode(true);  // 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);          // 初始化 JPus

//        HttpParameters params = new HttpParameters();
//        params.add("key1", "value1");
//        params.add("key2", "value2");
//        params.add("key3", "value3");
//        LogCollector.setDebugMode(true);
//        LogCollector.init(getApplicationContext(), UPLOAD_URL, params);

        // 程序崩溃时触发线程以下用来捕获程序崩溃异常
        //Thread.setDefaultUncaughtExceptionHandler(handler);
    }

    private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            //LogCollector.upload(false);
            restartApp(); //发生崩溃异常时,重启应用
        }
    };

    private void restartApp() {
        Intent intent = new Intent(this, InitDataActivity.class);
        PendingIntent restartIntent = PendingIntent.getActivity(app.getApplicationContext(), 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
        //退出程序
        AlarmManager mgr = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                restartIntent); // 1秒钟后重启应用

        //结束进程之前可以把你程序的注销或者退出代码放在这段代码之前
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public String getDeviceId() {
        String DEVICE_ID = "000000000000000";
//        try {
//            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//            DEVICE_ID = tm.getDeviceId();
//        } catch (Exception ex) {
//
//        }
//
//        LogUtil.i("设备id：" + DEVICE_ID);

        return DEVICE_ID;
    }
}
