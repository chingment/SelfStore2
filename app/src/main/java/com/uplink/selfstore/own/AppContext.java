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
import android.os.Environment;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.tencent.bugly.crashreport.CrashReport;
import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.activity.InitDataActivity;
import com.uplink.selfstore.db.DbManager;
import com.uplink.selfstore.model.api.DeviceBean;
import com.uplink.selfstore.ostCtrl.OstCtrlInterface;
import com.uplink.selfstore.utils.EMPreferenceManager;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by chingment on 2017/8/23.
 */

public class AppContext extends Application {
    private static final String TAG = "AppContext";
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

        CrashReport.UserStrategy strategy=new CrashReport.UserStrategy(context);
        strategy.setDeviceID(getDeviceId());
        CrashReport.initCrashReport(getApplicationContext(), "b9d0425e4c", true,strategy);

//        AppCrashHandler.getInstance().init(context, new AppCrashHandler.HandlerResult() {
//            @Override
//            public void complete(Thread thread, Throwable ex) {
//
//                OstCtrlInterface.getInstance().setHideStatusBar(context, false);
//
//                restartApp();
//            }
//        }); //初始异常日志收集器

        OstCtrlInterface.init(context);//  初始化Ost控制

        DbManager.getInstance().init();

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
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }

    private void restartApp() {
        try{
            Thread.sleep(3000);
        }catch (InterruptedException e){
            LogUtil.e(TAG, "error : ", e);
        }

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



       //return  "202004220047";

       //return "202101040205";

        if(Config.IS_BUILD_DEBUG) {
//           // return "202012110204";
 //         return "202101040206";
           return "202004220011";
  //          return  "202004220047";
 //          return "202107270001";
        }


//        try {
//            File file = new File(Environment.getExternalStorageDirectory(), "deviceinfo.txt");
//            if (file.exists()) {
//                BufferedReader br = new BufferedReader(new FileReader(file));
//                String readline = "";
//                StringBuffer sb = new StringBuffer();
//                while ((readline = br.readLine()) != null) {
//                    sb.append(readline);
//                }
//                br.close();
//                return sb.toString();
//            }
//        }
//        catch (Exception ex){
//
//        }


        return Build.SERIAL;
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


    public static String getMacAddress() {
//        String mac = "02:00:00:00:00:00";
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            mac = getMacDefault(app.getApplicationContext());
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//            mac = getMacFromFile();
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        String    mac = getMacFromHardware();
       // }
        return mac;
    }

    private static String getMacFromFile() {
        String WifiAddress = "02:00:00:00:00:00";
        try {
            WifiAddress = new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address"))).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return WifiAddress;
    }

    private static String getMacFromHardware() {

        if(Config.IS_BUILD_DEBUG) {
            return "02:00:00:00:00:00";
        }

        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    private static String getMacDefault(Context context) {
        String mac = "02:00:00:00:00:00";
        if (context == null) {
            return mac;
        }

        WifiManager wifi = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return mac;
        }
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {
        }
        if (info == null) {
            return null;
        }
        mac = info.getMacAddress();
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        return mac;
    }


}
