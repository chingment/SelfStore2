package com.tamic.statinterface.stats.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.tamic.statinterface.stats.constants.StaticsConfig;
import com.tamic.statinterface.stats.sp.SharedPreferencesHelper;

import java.util.UUID;


/**
 * DeviceUtil
 * Created by Tamic.
 */
public class DeviceUtil {


    /**
     * getAppVersionName
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // Get the package info
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (TextUtils.isEmpty(versionName)) {
                return "";
            }
        } catch (Exception e) {
        }
        return versionName;
    }

    /**
     * getAppVersionCode
     */
    public static int getAppVersionCode(Context context) {
        int versionCode = 0;
        try {
            // Get the package info
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionCode = pi.versionCode;
        } catch (Exception e) {
        }
        return versionCode;
    }

    /**
     * getSdkCode
     */
    public static int getSdkCode() {

        return StaticsConfig.SDK_VERSION_CODE;
    }

    /**
     * getSdkName
     */
    public static String getSdkName() {

        return StaticsConfig.SDK_VERSION_NAME;
    }

    /**
     * getMacAddress
     *
     * @param context
     * @return MAC
     */
    public static String getMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info != null ? info.getMacAddress() : "";
    }

    /**
     * getScreenDisplay
     *
     * @param activity
     * @return
     */
    public static DisplayMetrics getScreenDisplay(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    /**
     * getScreenWidth
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * getScreenHeight
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * etScreenDensity
     *
     * @param context
     * @return
     */
    public static float getScreenDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    /**
     * 获取当前手机型号
     */
    public static String getPhoneModel() {
        return android.os.Build.MODEL;
    }


    /**
     * 获取手机系统类型
     */
    public static String getSystemModel() {
        return Build.BRAND;
    }

    /**
     * 获取手机系统版本
     */
    public static int getSystemVersion() {
        return Build.VERSION.SDK_INT;
    }


    public static String getDeviceId(Context context){

        String deviceId= "";

        try {

            deviceId=getImeiId(context);

            if(deviceId==null||deviceId.length()==0)
            {
                deviceId=getMacAddress(context);
            }
        }
        catch (Exception ex)
        {
            deviceId="";
        }

        return deviceId;
    }

    public static String getImeiId(Context context) {
        String imeiId = "";
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if(tm!=null) {
                imeiId = tm.getDeviceId();
            }
        } catch (Exception ex) {
            imeiId="";
        }

        return imeiId;
    }

}
