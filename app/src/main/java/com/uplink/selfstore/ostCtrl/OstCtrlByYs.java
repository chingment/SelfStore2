package com.uplink.selfstore.ostCtrl;

import android.content.Context;

import com.ys.rkapi.MyManager;

public class OstCtrlByYs implements IOstCtrl {

    public void  reboot(Context context){
        MyManager myManager=MyManager.getInstance(context);
        myManager.reboot();
    }

    public void  shutdown(Context context){
        MyManager myManager=MyManager.getInstance(context);
        myManager.shutdown();
    }

    public void  setHideStatusBar(Context context,boolean ishidden){

        MyManager myManager=MyManager.getInstance(context);
        myManager.hideNavBar(ishidden);
        myManager.setSlideShowNavBar(!ishidden);
        myManager.setSlideShowNotificationBar(!ishidden);
    }

    public void  installApk(Context context,String path){
        MyManager myManager=MyManager.getInstance(context);
        myManager.silentInstallApk(path);
    }
}
