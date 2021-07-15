package com.uplink.selfstore.own;

import android.app.Activity;

import com.uplink.selfstore.model.api.DeviceBean;

public class AppDeviceUtil {

    public  static  String getDeviceStatus(){
        DeviceBean device = AppCacheManager.getDevice();

        String status = "unknow";
        String activityName = "";
        Activity activity = AppManager.getAppManager().currentActivity();
        if (activity != null) {
            activityName = activity.getLocalClassName();
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
}
