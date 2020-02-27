package com.uplink.selfstore.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.serenegiant.dialog.MessageDialogFragmentV4;
import com.serenegiant.utils.HandlerThreadHandler;
import com.serenegiant.utils.PermissionCheck;
import com.tamic.statinterface.stats.core.TcStatInterface;
import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.InitDataActivity;
import com.uplink.selfstore.activity.MainActivity;
import com.uplink.selfstore.jpush.LocalBroadcastManager;
import com.uplink.selfstore.model.api.GlobalDataSetBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppContext;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.http.HttpClient;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.service.HeartbeatService;
import com.uplink.selfstore.ui.dialog.CustomDialogLoading;
import com.uplink.selfstore.utils.LocationUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;
import com.uplink.selfstore.utils.ToastUtil;

import org.json.JSONArray;
import org.json.JSONObject;

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
    private Handler mWorkerHandler;
    private long mWorkerThreadID = -1;
    public static boolean isForeground = false;
    private MessageReceiver mJpush_MessageReceiver;
    public static final String mJpush_MESSAGE_RECEIVED_ACTION = "com.uplink.selfstore.MESSAGE_RECEIVED_ACTION";
    public static final String mJpush_KEY_TITLE = "title";
    public static final String mJpush_KEY_MESSAGE = "message";
    public static final String mJpush_KEY_EXTRAS = "extras";
    private CustomDialogLoading customDialogLoading;
    private ClosePageCountTimer closePageCountTimer;
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

        if (StringUtil.isEmptyNotNull(AppCacheManager.getMachine().getId())) {

            Activity activity= AppManager.getAppManager().currentActivity();

            if(activity instanceof InitDataActivity){
                if (AppCacheManager.getGlobalDataSet() != null) {
                    Intent intent = new Intent(getAppContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            else {
                showToast("检查异常，设备重新运行");
                Intent intent = new Intent(appContext, InitDataActivity.class);
                startActivity(intent);
                finish();
            }
        }

        if (mWorkerHandler == null) {
            mWorkerHandler = HandlerThreadHandler.createHandler(TAG);
            mWorkerThreadID = mWorkerHandler.getLooper().getThread().getId();
        }

    }

    public void  useClosePageCountTimer()
    {
        if(closePageCountTimer==null) {
            closePageCountTimer = new ClosePageCountTimer(this, 120);
        }
    }

    public void  useClosePageCountTimer(ClosePageCountTimer.OnPageCountLinster onPageCountLinster,long seconds)
    {
        if(closePageCountTimer==null) {
            closePageCountTimer = new ClosePageCountTimer(this, seconds, onPageCountLinster);
        }
    }

    public void  closePageCountTimerStart() {


        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(closePageCountTimer!=null) {
                    closePageCountTimer.start();
                }
            }
        });
    }

    public void  closePageCountTimerStop() {

        if(closePageCountTimer!=null) {
            closePageCountTimer.cancel();
        }
    }


    public void setHideStatusBar(boolean isshow) {

        Intent intent = new Intent();
        intent.setAction("android.intent.action.hidenavigation");
        intent.putExtra("enable", isshow);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            //获取触摸动作，如果ACTION_UP，计时开始。
            case MotionEvent.ACTION_UP:
                closePageCountTimerStart();
                break;
            //否则其他动作计时取消
            default:
                closePageCountTimerStop();
                break;
        }
        return super.dispatchTouchEvent(ev);
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
        closePageCountTimerStart();
        AppManager.getAppManager().setCurrentActivity(this);
        HeartbeatService.sendHeartbeatBag();
        //TcStatInterface.recordPageStart(BaseFragmentActivity.this);
    }

    /**
     * Activity被覆盖到下面或者锁屏时被调用
     */
    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
        closePageCountTimerStop();
        //TcStatInterface.recordPageEnd();
    }


    /**
     * 退出当前Activity或者跳转到新Activity时被调用
     */
    @Override
    protected void onStop() {
        isForeground = false;
        super.onStop();
        closePageCountTimerStop();
    }

    /**
     * 退出当前Activity时被调用,调用之后Activity就结束了
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mWorkerHandler != null) {
            try {
                mWorkerHandler.getLooper().quit();
            } catch (final Exception e) {
                //
            }
            mWorkerHandler = null;
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mJpush_MessageReceiver);
        AppManager.getAppManager().finishActivity(this);
        closePageCountTimerStop();
        TcStatInterface.recordAppEnd();
    }

    @Override
    public void finish() {
        closePageCountTimerStop();
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
                if (isShowLoading) {
                    if(customDialogLoading!=null&&customDialogLoading.isShowing()) {
                        customDialogLoading.cancelDialog();
                    }
                }
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
                if (isShowLoading) {
                    if(customDialogLoading!=null&&customDialogLoading.isShowing()) {
                        customDialogLoading.cancelDialog();
                    }
                }
                handler.onFailure(msg, e);
            }
        });
    }

    public void postByMy(String url, Map<String, Object> params, Map<String, String> filePaths, final Boolean isShowLoading, final String loadingMsg, final HttpResponseHandler handler) {

        HttpClient.postByAppSecret(BuildConfig.APPKEY, BuildConfig.APPSECRET, url, params, filePaths, new HttpResponseHandler() {

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
                if (isShowLoading) {
                    if(customDialogLoading!=null&&customDialogLoading.isShowing()) {
                        customDialogLoading.cancelDialog();
                    }
                }
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
                if (isShowLoading) {
                    if(customDialogLoading!=null&&customDialogLoading.isShowing()) {
                        customDialogLoading.cancelDialog();
                    }
                }
                handler.onFailure(msg, e);
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

    public void eventNotify(int type, JSONObject content){

        MachineBean machine = AppCacheManager.getMachine();

        Map<String, Object> params = new HashMap<>();
        params.put("appId", BuildConfig.APPLICATION_ID);
        params.put("deviceId", getAppContext().getDeviceId());
        params.put("machineId", machine.getId() + "");
        params.put("type", type);
        params.put("content", content);

        postByMy(Config.URL.machine_EventNotify, params, null, false, "", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {

            }
        });
    }

    public boolean isApkInDebug() {
        try {
            ApplicationInfo info =getAppContext().getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    public final synchronized void queueEvent(final Runnable task, final long delayMillis) {
        if ((task == null) || (mWorkerHandler == null)) return;
        try {
            mWorkerHandler.removeCallbacks(task);
            if (delayMillis > 0) {
                mWorkerHandler.postDelayed(task, delayMillis);
            } else if (mWorkerThreadID == Thread.currentThread().getId()) {
                task.run();
            } else {
                mWorkerHandler.post(task);
            }
        } catch (final Exception e) {
            // ignore
        }
    }

    // 動的パーミッション要求時の要求コード
    protected static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 0x12345;
    protected static final int REQUEST_PERMISSION_AUDIO_RECORDING = 0x234567;
    protected static final int REQUEST_PERMISSION_NETWORK = 0x345678;
    protected static final int REQUEST_PERMISSION_CAMERA = 0x537642;

    /**
     * 外部ストレージへの書き込みパーミッションが有るかどうかをチェック
     * なければ説明ダイアログを表示する
     * @return true 外部ストレージへの書き込みパーミッションが有る
     */
    protected boolean checkPermissionWriteExternalStorage() {
        if (!PermissionCheck.hasWriteExternalStorage(this)) {
            MessageDialogFragmentV4.showDialog(this, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE,
                    com.serenegiant.common.R.string.permission_title, com.serenegiant.common.R.string.permission_ext_storage_request,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
            return false;
        }
        return true;
    }

    /**
     * 録音のパーミッションが有るかどうかをチェック
     * なければ説明ダイアログを表示する
     * @return true 録音のパーミッションが有る
     */
    protected boolean checkPermissionAudio() {
        if (!PermissionCheck.hasAudio(this)) {
            MessageDialogFragmentV4.showDialog(this, REQUEST_PERMISSION_AUDIO_RECORDING,
                    com.serenegiant.common.R.string.permission_title, com.serenegiant.common.R.string.permission_audio_recording_request,
                    new String[]{Manifest.permission.RECORD_AUDIO});
            return false;
        }
        return true;
    }

    /**
     * ネットワークアクセスのパーミッションが有るかどうかをチェック
     * なければ説明ダイアログを表示する
     * @return true ネットワークアクセスのパーミッションが有る
     */
    protected boolean checkPermissionNetwork() {
        if (!PermissionCheck.hasNetwork(this)) {
            MessageDialogFragmentV4.showDialog(this, REQUEST_PERMISSION_NETWORK,
                    com.serenegiant.common.R.string.permission_title, com.serenegiant.common.R.string.permission_network_request,
                    new String[]{Manifest.permission.INTERNET});
            return false;
        }
        return true;
    }

    /**
     * カメラアクセスのパーミッションがあるかどうかをチェック
     * なければ説明ダイアログを表示する
     * @return true カメラアクセスのパーミッションが有る
     */
    protected boolean checkPermissionCamera() {
        if (!PermissionCheck.hasCamera(this)) {
            MessageDialogFragmentV4.showDialog(this, REQUEST_PERMISSION_CAMERA,
                    com.serenegiant.common.R.string.permission_title, com.serenegiant.common.R.string.permission_camera_request,
                    new String[]{Manifest.permission.CAMERA});
            return false;
        }
        return true;
    }

}
