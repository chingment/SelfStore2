package com.uplink.selfstore.own;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.activity.InitDataActivity;
import cn.jpush.android.api.JPushInterface;
import com.uplink.selfstore.ostCtrl.OstCtrlInterface;
import com.uplink.selfstore.utils.EMPreferenceManager;
import com.uplink.selfstore.utils.StringUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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

        Context context = getApplicationContext();

        AppCrashHandler.getInstance().init(context, new AppCrashHandler.HandlerResult() {
            @Override
            public void complete(Thread thread, Throwable ex) {
                restartApp();
            }
        }); //初始异常日志收集器

        OstCtrlInterface.init(Build.MODEL);//  初始化Ost控制
        JPushInterface.setDebugMode(true);  // 设置开启日志,发布时请关闭日志
        JPushInterface.init(context);  // 初始化 JPus

        EMOptions options = new EMOptions();
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
        // 是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载，如果设为 false，需要开发者自己处理附件消息的上传和下载
        options.setAutoTransferMessageAttachments(true);
        // 是否自动下载附件类消息的缩略图等，默认为 true 这里和上边这个参数相关联
        options.setAutoDownloadThumbnail(true);
        //初始化
        EMClient.getInstance().init(context, options);
        //在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(true);

        EMPreferenceManager.init(context);
        //DbManager.getInstance().init(this);
    }

    @Override
    public void onTerminate() {
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
            return "02:00:00:00:00:00";
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
