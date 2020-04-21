package com.uplink.selfstore.ostCtrl;

import android.content.Context;

public interface IOstCtrl {
    void reboot(Context context);
    void shutdown(Context context);
    void setHideStatusBar(Context context, boolean ishidden);
    void installApk(Context context,String path);
}
