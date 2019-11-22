package com.uplink.selfstore.own;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.tamic.statinterface.stats.core.TcCrashHandler;
import com.tamic.statinterface.stats.db.DbManager;
import com.tamic.statinterface.stats.sp.SharedPreferencesHelper;
import com.uplink.selfstore.activity.InitDataActivity;
import cn.jpush.android.api.JPushInterface;
import com.tamic.statinterface.stats.core.TcStatInterface;
import com.uplink.selfstore.ui.CameraWindow;
import com.uplink.selfstore.utils.LogUtil;

import java.util.UUID;

/**
 * Created by chingment on 2017/8/23.
 */

public class AppContext extends Application {

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
        JPushInterface.init(this);  // 初始化 JPus

        //DbManager.getInstance().init(this);

        CameraWindow.show(this);

        TcStatInterface.setUrl(Config.URL.machine_UpLoadTraceLog);
        TcStatInterface.setUploadPolicy(TcStatInterface.UploadPolicy.UPLOAD_POLICY_REALTIME, TcStatInterface.UPLOAD_INTERVAL_REALTIME);
        TcStatInterface.initialize(this, 1, "com.uplink.selfstore", "stat_id.json", new TcCrashHandler.ExceptionHandler() {
            @Override
            public void Handler() {
                restartApp();
            }
        });
        TcStatInterface.recordAppStart();
        //AppCrashHandler.getInstance().init(this);

    }

    @Override
    public void onTerminate() {
        DbManager.getInstance().destroy();
        TcStatInterface.recordAppEnd();
        super.onTerminate();
    }

    private void restartApp() {
        SystemClock.sleep(2000);
        Intent intent = new Intent(app.getApplicationContext(), InitDataActivity.class);
        PendingIntent restartIntent = PendingIntent.getActivity(app.getApplicationContext(), 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
        //退出程序
        AlarmManager mgr = (AlarmManager) app.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                restartIntent); // 1秒钟后重启应用

        //结束进程之前可以把你程序的注销或者退出代码放在这段代码之前
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    public String getDeviceId() {
//        String DEVICE_ID = "000000000000000";
//        try {
//            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//            DEVICE_ID = tm.getDeviceId();
//        } catch (Exception ex) {
//
//        }
//
//        LogUtil.i("设备id：" + DEVICE_ID);
//
//        return DEVICE_ID;

        String DEVICE_ID = "DEVICE_ID";
        String device_id="000000000000000";
//        try {
//
//            SharedPreferencesHelper preferencesHelper = SharedPreferencesHelper.getInstance(app.getApplicationContext());
//            String s_device_id = preferencesHelper.getString(DEVICE_ID);
//            if(TextUtils.isEmpty(s_device_id)) {
//                s_device_id = UUID.randomUUID().toString();
//                preferencesHelper.putString(DEVICE_ID,s_device_id);
//            }
//
//            device_id=s_device_id;
//        }
//        catch (Exception ex) {
//
//        }

        LogUtil.i("设备id：" + device_id);
        return  device_id;
    }
}
