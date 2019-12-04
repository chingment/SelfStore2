package com.uplink.selfstore.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.uplink.selfstore.utils.LogUtil;

/**
 * 隐藏的全局窗口，用于后台拍照
 *
 * @author WuRS
 */
public class CameraWindow {

    private static final String TAG = CameraWindow.class.getSimpleName();

    private static WindowManager windowManager;

    private static Context applicationContext;

    private static SurfaceView dummyCameraView;

    /**
     * 显示全局窗口
     *
     * @param context
     */
    public static void show(Context context) {
        if (applicationContext == null) {
            applicationContext = context.getApplicationContext();
            windowManager = (WindowManager) applicationContext
                    .getSystemService(Context.WINDOW_SERVICE);
            dummyCameraView = new SurfaceView(applicationContext);
            LayoutParams params = new LayoutParams();
            params.width = 1;
            params.height = 1;
            params.alpha = 0;
            params.type = LayoutParams.TYPE_SYSTEM_ALERT;
            // 屏蔽点击事件
            params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE
                    | LayoutParams.FLAG_NOT_TOUCHABLE;

            windowManager.addView(dummyCameraView, params);
            LogUtil.d(TAG, TAG + " showing");
        }
    }

    /**
     * @return 获取窗口视图
     */
    public static SurfaceView getDummyCameraView() {
        return dummyCameraView;
    }

    /**
     * 隐藏窗口
     */
    public static void dismiss() {
        try {
            if (windowManager != null && dummyCameraView != null) {
                windowManager.removeView(dummyCameraView);
                LogUtil.d(TAG, TAG + " dismissed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}