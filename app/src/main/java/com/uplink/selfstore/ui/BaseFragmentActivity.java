package com.uplink.selfstore.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.tamic.statinterface.stats.core.TcStatInterface;
import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.InitDataActivity;
import com.uplink.selfstore.activity.MainActivity;
import com.uplink.selfstore.activity.OrderDetailsActivity;
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
import com.uplink.selfstore.ui.dialog.CustomSystemWarnDialog;
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
    public static boolean isForeground = false;
    private MessageReceiver mJpush_MessageReceiver;
    public static final String mJpush_MESSAGE_RECEIVED_ACTION = "com.uplink.selfstore.MESSAGE_RECEIVED_ACTION";
    public static final String mJpush_KEY_TITLE = "title";
    public static final String mJpush_KEY_MESSAGE = "message";
    public static final String mJpush_KEY_EXTRAS = "extras";
    private CustomDialogLoading dialogByLoading;
    private CustomSystemWarnDialog dialogBySystemWarn;
    private ClosePageCountTimer closePageCountTimer;
    private GlobalDataSetBean globalDataSet;
    private MachineBean machine;
    public LocationUtil locationUtil;

    public void setNavTtile(String title) {
        TextView nav_title = (TextView) findViewById(R.id.nav_title);
        if (nav_title != null) {
            nav_title.setText(title);
        }
    }

    public void setNavGoBackBtnVisible(boolean isVisible) {
        View nav_back = findViewById(R.id.nav_back);
        if (isVisible) {
            nav_back.setVisibility(View.VISIBLE);
            nav_back.setOnClickListener(this);
        } else {
            nav_back.setVisibility(View.GONE);
        }
    }

    public GlobalDataSetBean getGlobalDataSet() {

        if (globalDataSet == null) {
            globalDataSet = AppCacheManager.getGlobalDataSet();
        }

        return globalDataSet;
    }

    public MachineBean getMachine() {
        if (machine == null) {
            machine = AppCacheManager.getMachine();
        }
        return machine;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //locationUtil = LocationUtil.getInstance(this); //阻碍线程线程读取

        appContext = (AppContext) getApplication();
        dialogByLoading = new CustomDialogLoading(this);
        dialogBySystemWarn = new CustomSystemWarnDialog(this);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        AppManager.getAppManager().addActivity(this);


        if (StringUtil.isEmptyNotNull(AppCacheManager.getMachine().getId()) || this.getGlobalDataSet() == null) {
            Activity activity = AppManager.getAppManager().currentActivity();
            if (activity instanceof InitDataActivity) {

            } else {
                showToast("检查异常，设备重新运行");
                Intent intent = new Intent(appContext, InitDataActivity.class);
                startActivity(intent);
                finish();
            }
        }
        else {
            dialogBySystemWarn.setCsrPhoneNumber(getMachine().getCsrPhoneNumber());
            dialogBySystemWarn.setCsrQrcode(getMachine().getCsrQrCode());
            dialogBySystemWarn.setCsrHelpTip(getMachine().getCsrHelpTip());
        }

    }

    public CustomSystemWarnDialog getDialogBySystemWarn(){
        if(dialogBySystemWarn==null){
            dialogBySystemWarn = new CustomSystemWarnDialog(this);
        }

        return dialogBySystemWarn;
    }

    public void  useClosePageCountTimer() {
        if(closePageCountTimer==null) {
            closePageCountTimer = new ClosePageCountTimer(this, 120);
        }
    }

    public void  useClosePageCountTimer(ClosePageCountTimer.OnPageCountLinster onPageCountLinster,long seconds) {
        if(closePageCountTimer==null) {
            closePageCountTimer = new ClosePageCountTimer(this, seconds, onPageCountLinster);
        }
    }

    public void  closePageCountTimerStart() {

        new Handler().post(new Runnable() {
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

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mJpush_MessageReceiver);
        AppManager.getAppManager().finishActivity(this);
        closePageCountTimerStop();
        TcStatInterface.recordAppEnd();

        if (dialogBySystemWarn != null && dialogBySystemWarn.isShowing()) {
            dialogBySystemWarn.cancel();
        }

        if (dialogByLoading != null && dialogByLoading.isShowing()) {
            dialogByLoading.cancel();
        }
    }

    @Override
    public void finish() {
        closePageCountTimerStop();
        super.finish();
    }

    protected void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public void checkIsHasExHappen() {
        //getDialogBySystemWarn().setBtnCloseVisibility(View.GONE);
        //getDialogBySystemWarn().show();
    }

    public void getByMy(String url, Map<String, String> params, final Boolean isShowLoading, final String loadingMsg, final HttpResponseHandler handler) {

        HttpClient.getByAppSecret(BuildConfig.APPKEY, BuildConfig.APPSECRET, url, params, new HttpResponseHandler() {

            @Override
            public void onBeforeSend() {

                if (isShowLoading) {
                    if (!StringUtil.isEmptyNotNull(loadingMsg)) {
                        if(!dialogByLoading.isShowing()) {
                            dialogByLoading.setProgressText(loadingMsg);
                            dialogByLoading.showDialog();

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    if (dialogByLoading != null && dialogByLoading.isShowing()) {
                                        dialogByLoading.cancelDialog();
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
                    if(dialogByLoading!=null&&dialogByLoading.isShowing()) {
                        dialogByLoading.cancelDialog();
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
                    if(dialogByLoading!=null&&dialogByLoading.isShowing()) {
                        dialogByLoading.cancelDialog();
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
                        if(!dialogByLoading.isShowing()) {
                            dialogByLoading.setProgressText(loadingMsg);
                            dialogByLoading.showDialog();

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    if (dialogByLoading != null && dialogByLoading.isShowing()) {
                                        dialogByLoading.cancelDialog();
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
                    if(dialogByLoading!=null&&dialogByLoading.isShowing()) {
                        dialogByLoading.cancelDialog();
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
                    if(dialogByLoading!=null&&dialogByLoading.isShowing()) {
                        dialogByLoading.cancelDialog();
                    }
                }
                handler.onFailure(msg, e);
            }
        });
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

    public void eventNotify(String eventCode, JSONObject content){

        MachineBean machine = AppCacheManager.getMachine();

        Map<String, Object> params = new HashMap<>();
        params.put("appId", BuildConfig.APPLICATION_ID);
        params.put("deviceId", getAppContext().getDeviceId());
        params.put("machineId", machine.getId() + "");
        params.put("lat", LocationUtil.LAT);
        params.put("lng", LocationUtil.LNG);
        params.put("eventCode", eventCode);
        params.put("content", content);

        postByMy(Config.URL.machine_EventNotify, params, null, false, "", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {

            }
        });
    }

    public String getTopComponentName(){
        String name=null;
        try {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            if(am!=null) {
                if(am.getRunningTasks(1)!=null) {
                    if(am.getRunningTasks(1).get(0)!=null) {
                        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                        name = cn.getClassName();
                    }
                }
            }
        }
        catch (Exception ex){

        }


        return  name;
    }

}
