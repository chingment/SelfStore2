package com.uplink.selfstore.systemCtrl;

import android.content.Context;

public interface ISystemCtrl {
    void reboot(Context context);
    void shutdown(Context context);
    void setHideStatusBar(Context context, boolean ishidden);
    void installApk(Context context,String path);
}
