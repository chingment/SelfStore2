package com.uplink.selfstore.ui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.R;
import com.uplink.selfstore.jpush.LocalBroadcastManager;
import com.uplink.selfstore.model.api.GlobalDataSetBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppContext;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.http.HttpClient;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.dialog.CustomDialogLoading;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;
import com.uplink.selfstore.utils.ToastUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by chingment on 2017/8/23.
 */

public class BaseFragmentActivity extends FragmentActivity implements View.OnClickListener {
    private String TAG = "BaseFragmentActivity";
    private AppContext appContext;
    public static boolean isForeground = false;
    private MessageReceiver mJpush_MessageReceiver;
    public static final String mJpush_MESSAGE_RECEIVED_ACTION = "com.uplink.selfstore.MESSAGE_RECEIVED_ACTION";
    public static final String mJpush_KEY_TITLE = "title";
    public static final String mJpush_KEY_MESSAGE = "message";
    public static final String mJpush_KEY_EXTRAS = "extras";
    private CustomDialogLoading customDialogLoading;


    private GlobalDataSetBean globalDataSet;

    public CustomDialogLoading getCustomDialogLoading() {
        return customDialogLoading;
    }

    public void setNavTtile(String title) {
        TextView nav_title = (TextView) findViewById(R.id.nav_title);
        if (nav_title != null) {
            nav_title.setText(title);
        }
    }

    public void setNavBackVisible(boolean isVisible) {
        View nav_back = findViewById(R.id.nav_back);
        if (isVisible) {
            nav_back.setVisibility(View.VISIBLE);
            nav_back.setOnClickListener(this);
        } else {
            nav_back.setVisibility(View.GONE);
        }
    }

    public void setNavBtnVisible(boolean isVisible) {

        View nav_back = findViewById(R.id.nav_back);
        if (isVisible) {
            nav_back.setVisibility(View.VISIBLE);
            nav_back.setOnClickListener(this);
        } else {
            nav_back.setVisibility(View.GONE);
        }

    }

    public GlobalDataSetBean getGlobalDataSet() {

        return AppCacheManager.getGlobalDataSet();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appContext = (AppContext) getApplication();
        customDialogLoading = new CustomDialogLoading(this);

        AppManager.getAppManager().addActivity(this);


    }


    public void setShowStatuBar(boolean isshow) {

        Intent intent = new Intent();
        intent.setAction("android.intent.action.hidenavigation");
        intent.putExtra("enable", isshow == false ? "true" : "false");
        sendBroadcast(intent);
    }


    @Override
    public void onClick(View v) {


    }

    public AppContext getAppContext() {
        return appContext;
    }

    public void showToast(String txt) {
        if (!StringUtil.isEmpty(txt)) {
            ToastUtil.showMessage(BaseFragmentActivity.this, txt, Toast.LENGTH_LONG);
        }
    }

    /**
     * Activity从后台重新回到前台时被调用
     */
    @Override
    protected void onRestart() {
        super.onRestart();
    }

    /**
     * Activity创建或者从后台重新回到前台时被调用
     */
    @Override
    protected void onStart() {
        super.onStart();
    }


    /**
     * Activity创建或者从被覆盖、后台重新回到前台时被调用
     */
    @Override
    protected void onResume() {
        isForeground = true;
        super.onResume();
        MobclickAgent.onResume(this);
    }

    /**
     * Activity被覆盖到下面或者锁屏时被调用
     */
    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
        MobclickAgent.onPause(this);
    }


    /**
     * 退出当前Activity或者跳转到新Activity时被调用
     */
    @Override
    protected void onStop() {
        isForeground = false;
        super.onStop();
    }

    /**
     * 退出当前Activity时被调用,调用之后Activity就结束了
     */
    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mJpush_MessageReceiver);
        AppManager.getAppManager().finishActivity(this);
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();

    }

    public boolean isAppOnForeground() {
        // Returns a list of application processes that are running on the
        // device

        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }


    public void getByMy(String url, Map<String, String> params, final Boolean isShowLoading, final String loadingMsg, final HttpResponseHandler handler) {

        HttpClient.getByAppSecret(BuildConfig.APPKEY, BuildConfig.APPSECRET, url, params, new HttpResponseHandler() {

            @Override
            public void onBeforeSend() {

                if (isShowLoading) {
                    if (!StringUtil.isEmptyNotNull(loadingMsg)) {
                        if(!customDialogLoading.isShowing()) {
                            customDialogLoading.setProgressText(loadingMsg);
                            customDialogLoading.showDialog();

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    if (customDialogLoading != null && customDialogLoading.isShowing()) {
                                        customDialogLoading.cancelDialog();
                                    }
                                }
                            }, 6000);
                        }

                    }
                }
            }

            @Override
            public void onSuccess(String response) {
                final String s = response;

                if (s.indexOf("\"result\":") > -1) {
                    //运行在子线程,,
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handler.onSuccess(s);
                        }
                    });
                } else {

                    LogUtil.e("解释错误：原始数据》》" + s);
                    handler.onFailure("解释原始数据发生异常", null);
                }
            }

            @Override
            public void onFailure(String msg, Exception e) {
                handler.onFailure(msg, e);
            }

            @Override
            public void onComplete(String response) {
                if (isShowLoading) {
                    customDialogLoading.cancelDialog();
                }

                handler.onComplete(response);
            }
        });
    }

    public void postByMy(String url, Map<String, Object> params, Map<String, String> filePaths, final Boolean isShowLoading, final String loadingMsg, final HttpResponseHandler handler) {

        HttpClient.postByAppSecret(BuildConfig.APPKEY, BuildConfig.APPSECRET, url, params, filePaths, new HttpResponseHandler() {

            @Override
            public void onBeforeSend() {
                if (isShowLoading) {
                    if (!StringUtil.isEmptyNotNull(loadingMsg)) {
                        customDialogLoading.setProgressText(loadingMsg);
                        customDialogLoading.showDialog();
                    }
                }
            }

            @Override
            public void onSuccess(String response) {

                final String s = response;
                if (s.indexOf("\"result\":") > -1) {
                    //运行在子线程,,
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handler.onSuccess(s);
                        }
                    });
                } else {
                    showToast("服务器数据异常");
                    LogUtil.e("解释错误：原始数据》》" + s);
                }
            }

            @Override
            public void onFailure(String msg, Exception e) {

                if (customDialogLoading != null) {
                    customDialogLoading.cancelDialog();
                }

                handler.onFailure(msg, e);
            }

            @Override
            public void onComplete(String response) {
                if (customDialogLoading != null) {
                    customDialogLoading.cancelDialog();
                }

                handler.onComplete(response);
            }
        });
    }

    private String getVersionName() {
        String versionName = "1.0";
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (mJpush_MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                    String messge = intent.getStringExtra(mJpush_KEY_MESSAGE);
                    String extras = intent.getStringExtra(mJpush_KEY_EXTRAS);
                    StringBuilder showMsg = new StringBuilder();
                    showMsg.append(mJpush_KEY_MESSAGE + " : " + messge + "\n");
                    if (!StringUtil.isEmpty(extras)) {
                        showMsg.append(mJpush_KEY_EXTRAS + " : " + extras + "\n");
                    }
                }
            } catch (Exception e) {
            }
        }
    }


    public void orderCancle(String orderId, String reason) {

        MachineBean machine = AppCacheManager.getMachine();

        Map<String, Object> params = new HashMap<>();
        params.put("machineId", machine.getId() + "");
        params.put("orderId", orderId);
        params.put("reason", reason);

        postByMy(Config.URL.order_Cancle, params, null, true, "", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
            }
        });
    }

    public void sendRunStatus(String status){

        MachineBean machine = AppCacheManager.getMachine();

        Map<String, Object> params = new HashMap<>();
        params.put("machineId", machine.getId() + "");
        params.put("status", status);


        postByMy(Config.URL.machine_SendRunStatus, params, null, false, "", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {

            }
        });
    }

}
