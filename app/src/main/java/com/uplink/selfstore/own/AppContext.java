package com.uplink.selfstore.own;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.tamic.statinterface.stats.db.DbManager;
import com.uplink.selfstore.activity.InitDataActivity;
import cn.jpush.android.api.JPushInterface;
import com.tamic.statinterface.stats.core.TcStatInterface;
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
        JPushInterface.init(this);  // 初始化 JPus

        DbManager.getInstance().init(this);

        int appId = 21212;
        String fileName = "stat_id.json";
        TcStatInterface.initialize(this, appId, "zuber", fileName);
        TcStatInterface.setUrl("http://api.term.17fanju.com/api/Machine/Login");
        TcStatInterface.setUploadPolicy(TcStatInterface.UploadPolicy.UPLOAD_POLICY_DEVELOPMENT, TcStatInterface.UPLOAD_INTERVAL_REALTIME);
        TcStatInterface.recordAppStart();

        AppCrashHandler.getInstance().init(this);

    }

    @Override
    public void onTerminate() {
       // LogUtil.i(debug, TAG, "【StatAppliation.onTerminate()】【start】");
        DbManager.getInstance().destroy();
        TcStatInterface.recordAppEnd();
        super.onTerminate();
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
