package com.uplink.selfstore.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tamic.statinterface.stats.core.TcStatInterface;
import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.InitDataActivity;
import com.uplink.selfstore.activity.OrderDetailsActivity;
import com.uplink.selfstore.deviceCtrl.ScannerCtrl;
import com.uplink.selfstore.jpush.LocalBroadcastManager;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.GlobalDataSetBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.OrderDetailsBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppContext;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.http.HttpClient;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.service.HeartbeatService;
import com.uplink.selfstore.ostCtrl.OstCtrlInterface;
import com.uplink.selfstore.ui.dialog.CustomDialogLoading;
import com.uplink.selfstore.ui.dialog.CustomSystemWarnDialog;
import com.uplink.selfstore.utils.LocationUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;
import com.uplink.selfstore.utils.ToastUtil;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

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
    private ScannerCtrl scannerCtrl;

    public LocationUtil locationUtil;

    private Map<String,Boolean> orderSearchByPickupCode=new HashMap<String, Boolean>();

    public GlobalDataSetBean getGlobalDataSet() {

        if (globalDataSet == null) {
            globalDataSet = AppCacheManager.getGlobalDataSet();
        }

        return globalDataSet;
    }

    public MachineBean getMachine() {
        machine = AppCacheManager.getMachine();
        return machine;
    }

    public AppContext getAppContext() {
        return appContext;
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

    public CustomSystemWarnDialog getDialogBySystemWarn(){
        if(dialogBySystemWarn==null){
            dialogBySystemWarn = new CustomSystemWarnDialog(this);
        }

        return dialogBySystemWarn;
    }

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
    public void setHideStatusBar(boolean ishidden) {
        OstCtrlInterface.getInstance().setHideStatusBar(appContext, ishidden);
    }

    public void setScannerCtrl() {
        if(getMachine().getScanner().getUse()) {
            scannerCtrl = ScannerCtrl.getInstance();
            scannerCtrl.connect();

            if(!scannerCtrl.isConnect()) {
                LogUtil.e(TAG, "扫描器连接失败");
            }

            scannerCtrl.setScanHandler(new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            Bundle bundle;
                            bundle = msg.getData();
                            String scanResult = bundle.getString("result");
                            if (scanResult != null) {
                                if (scanResult.contains("pickupcode")) {
                                    LogUtil.e("pickupcode:" + scanResult);
                                    orderSearchByPickupCode(scanResult);
                                }
                            }
                            return false;
                        }
                    })
            );
        }
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

    public void checkIsHasExHappen() {
        MachineBean machine=getMachine();
        if(machine!=null) {
            if(!machine.getId().equals("")) {
                if(machine.isExIsHas()) {
                    if(!getDialogBySystemWarn().isShowing()) {
                        getDialogBySystemWarn().setBtnCloseVisibility(View.GONE);
                        getDialogBySystemWarn().show();
                    }
                }
            }
        }
    }

    public void showToast(String txt) {
        if (!StringUtil.isEmpty(txt)) {
            ToastUtil.showMessage(BaseFragmentActivity.this, txt, Toast.LENGTH_LONG);
        }
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

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        isForeground = true;
        super.onResume();
        closePageCountTimerStart();
        AppManager.getAppManager().setCurrentActivity(this);
        HeartbeatService.sendHeartbeatBag();

        if(scannerCtrl!=null) {
            scannerCtrl.connect();
        }
        //TcStatInterface.recordPageStart(BaseFragmentActivity.this);
    }

    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
        closePageCountTimerStop();

        if(scannerCtrl!=null) {
            scannerCtrl.disConnect();
        }
        //TcStatInterface.recordPageEnd();
    }

    @Override
    protected void onStop() {
        isForeground = false;
        super.onStop();
        closePageCountTimerStop();
    }

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

    @Override
    public void onClick(View v) {

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

    public void orderSearchByPickupCode(String pickCode) {

        Map<String, String> params = new HashMap<>();

        params.put("machineId", this.getMachine().getId());
        params.put("pickupCode", pickCode);

        getByMy(Config.URL.order_SearchByPickupCode, params, true, "正在寻找订单", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<OrderDetailsBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<OrderDetailsBean>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {
                    synchronized(BaseFragmentActivity.class) {
                        if (!orderSearchByPickupCode.containsKey(pickCode)) {
                            orderSearchByPickupCode.put(pickCode, true);
                            OrderDetailsBean d = rt.getData();
                            Intent intent = new Intent(getAppContext(), OrderDetailsActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("dataBean", d);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            finish();
                        }
                    }
                } else {
                    showToast(rt.getMessage());
                }
            }

            @Override
            public void onFailure(String msg, Exception e) {
                showToast(msg);
            }
        });
    }

}
