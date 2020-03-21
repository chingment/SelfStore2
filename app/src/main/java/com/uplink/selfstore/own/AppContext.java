package com.uplink.selfstore.own;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.tamic.statinterface.stats.core.TcCrashHandler;
import com.tamic.statinterface.stats.db.DbManager;
import com.tencent.bugly.crashreport.CrashReport;
import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.activity.InitDataActivity;
import cn.jpush.android.api.JPushInterface;
import com.tamic.statinterface.stats.core.TcStatInterface;
import com.uplink.selfstore.service.UpdateAppService;
import com.uplink.selfstore.ui.CameraWindow;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        TcCrashHandler.getInstance().init(this, new TcCrashHandler.ExceptionHandler() {
            @Override
            public void Handler() {
                restartApp();
            }
        });

        //腾讯Bugly 配置
        Context context = getApplicationContext();
        String packageName = context.getPackageName();
        String processName = getProcessName(android.os.Process.myPid());
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        CrashReport.initCrashReport(context, "b9d0425e4c", true);


        JPushInterface.setDebugMode(true);  // 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);  // 初始化 JPus

        //DbManager.getInstance().init(this);


        TcStatInterface.setUrl(Config.URL.machine_UpLoadTraceLog);
        TcStatInterface.setUploadPolicy(TcStatInterface.UploadPolicy.UPLOAD_POLICY_REALTIME, TcStatInterface.UPLOAD_INTERVAL_REALTIME);
        TcStatInterface.initialize(this, BuildConfig.APPLICATION_ID, "android.storeterm", "stat_id.json");
        TcStatInterface.recordAppStart();

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

//    public static String getDeviceId() {
//        String androidId = Settings.Secure.getString(app.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
//        return androidId;
//    }

    public String getDeviceId() {
        //todo 获取方式必须跟statinterface里获取的设备号一致

        String deviceId= "";

        try {

            deviceId=getImeiId();

            if(StringUtil.isEmptyNotNull(deviceId))
            {
                deviceId=getMacAddress();
            }
        }
        catch (Exception ex)
        {
            deviceId="";
        }

        if(Config.IS_BUILD_DEBUG) {
            return  "861712043256526";
            //return "862810045278347";
            //return "00:92:c5:06:6b:a9";
            //return "02:00:00:00:00:00";
        }

        return deviceId;
    }

    public String getImeiId() {
        String imeiId = "";
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if(tm!=null) {
                imeiId = tm.getDeviceId();
            }
        } catch (Exception ex) {
            imeiId="";
        }

        return imeiId;
    }

    public String getMacAddress() {
        WifiManager wifi = (WifiManager) app.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info != null ? info.getMacAddress() : "";
    }

}
