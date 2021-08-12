package com.uplink.selfstore.own;

import android.app.Activity;

import com.uplink.selfstore.model.api.DeviceBean;
import com.uplink.selfstore.utils.StringUtil;

public class AppUtil {

    public  static  String getDeviceStatus() {
        String status = "unknow";

        DeviceBean device = AppCacheManager.getDevice();

        if (device == null)
            return status;

        if(StringUtil.isEmptyNotNull(device.getDeviceId()))
            return status;


        Activity activity = AppManager.getAppManager().currentActivity();
        if (activity != null) {
            String activityName = activity.getLocalClassName();
            if (activityName.contains(".Sm")) {
                status = "setting";
            } else {
                if (device.isExIsHas()) {
                    status = "exception";
                } else {
                    status = "running";
                }
            }
        }
        return status;
    }

    public  static Boolean deviceIsIdle() {

        Activity activity = AppManager.getAppManager().currentActivity();

        if (activity == null)
            return true;

        String activityName = activity.getLocalClassName();

        if (activityName.contains(".OrderDetails"))
            return false;

        if (activityName.contains(".SmDeviceStock"))
            return false;

        return true;
    }
}
