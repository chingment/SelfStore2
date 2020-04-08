package com.uplink.selfstore.systemCtrl;

import android.content.Context;
import android.content.Intent;

public class SystemCtrlBySx implements ISystemCtrl {

    public void  reboot(Context context){
        Intent it = new Intent();
        it.setAction("com.fourfaith.reboot");
        it.putExtra("mode", "0");//0 重启 1 关机
        context.sendBroadcast(it);
    }

    public void  shutdown(Context context){

    }

    public void  setHideStatusBar(Context context,boolean ishidden){
        Intent intent = new Intent();
        intent.setAction("android.intent.action.hidenavigation");
        intent.putExtra("enable", ishidden);
        context.sendBroadcast(intent);
    }

    public void  installApk(Context context,String path){
        Intent intent = new Intent();
        intent.setAction("android.intent.action.installslient");
        intent.putExtra("uri", path);
        intent.putExtra("component", "com.uplink.selfstore/.activity.InitDataActivity");
        context.sendBroadcast(intent);
    }
}
