package com.uplink.selfstore.own;

import android.app.Activity;
import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

/**
 * Created by chingment on 2017/8/23.
 */

public class AppManager {
    private static LinkedList<Activity> activityStack;
    private static AppManager instance;
    private WeakReference sCurrentActivityWeakRef;
    private AppManager() {
    }

    /**
     * 单一实例
     */
    public static AppManager getAppManager() {
        if (instance == null) {
            instance = new AppManager();
        }
        return instance;
    }

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new LinkedList<Activity>();
        }
        activityStack.add(activity);
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public Activity currentActivity() {
        Activity currentActivity = null;
        if (sCurrentActivityWeakRef != null) {
            currentActivity =(Activity)sCurrentActivityWeakRef.get();
        }
        else {
            if(activityStack!=null) {
                currentActivity = activityStack.getLast();
            }
        }
        return currentActivity;
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    public void finishActivity() {
        Activity activity = activityStack.getLast();
        finishActivity(activity);
    }

    /**
     * 结束指定的Activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            if (activityStack != null) {
                activityStack.remove(activity);
            }
            activity.finish();
            activity = null;
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        if (activityStack != null) {
            if (activityStack.size() > 0) {
                for (int i = 0, size = activityStack.size(); i < size; i++) {
                    if (null != activityStack.get(i)) {
                        activityStack.get(i).finish();
                    }
                }
                activityStack.clear();
            }
        }
    }

    public void setCurrentActivity(Activity activity) {
        if (sCurrentActivityWeakRef == null || !activity.equals(sCurrentActivityWeakRef.get())) {
            sCurrentActivityWeakRef = new WeakReference<>(activity);
        }
    }

    /**
     * 退出应用程序
     */
    public void AppExit(Context context) {
        try {
            finishAllActivity();
            //ActivityManager activityMgr = (ActivityManager) context
                    //.getSystemService(Context.ACTIVITY_SERVICE);
            //activityMgr.restartPackage(context.getPackageName());

            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);


        } catch (Exception e) {
        }
    }

    public int ActivityStackSize() {
        return activityStack == null ? 0 : activityStack.size();
    }


    public LinkedList<Activity>  getActivityStack() {
        return  activityStack;
    }
}
