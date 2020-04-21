package com.uplink.selfstore.systemCtrl;

import android.content.Context;
import android.content.Intent;

import com.ys.rkapi.MyManager;

public class SystemCtrlByYs  implements ISystemCtrl {

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
