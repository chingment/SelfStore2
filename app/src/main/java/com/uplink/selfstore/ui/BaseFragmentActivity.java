package com.uplink.selfstore.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.InitDataActivity;
import com.uplink.selfstore.activity.OrderDetailsActivity;
import com.uplink.selfstore.activity.SmRescueToolActivity;
import com.uplink.selfstore.deviceCtrl.ScannerCtrl;
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
import com.uplink.selfstore.ostCtrl.OstCtrlInterface;
import com.uplink.selfstore.service.UsbService;
import com.uplink.selfstore.ui.dialog.CustomLoadingDialog;
import com.uplink.selfstore.ui.dialog.CustomSystemWarnDialog;
import com.uplink.selfstore.utils.LocationUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;
import com.uplink.selfstore.utils.ToastUtil;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chingment on 2017/8/23.
 */

public class BaseFragmentActivity extends FragmentActivity implements View.OnClickListener {
    private static final String TAG = "BaseFragmentActivity";
    private AppContext appContext;
    public static boolean isForeground = false;
    private CustomLoadingDialog dialog_Loading;
    private CustomSystemWarnDialog dialog_SystemWarn;
    private ClosePageCountTimer closePageCountTimer;
    private GlobalDataSetBean globalDataSet;
    //private ScannerCtrl scannerCtrl;
    private Handler laodingUIHandler;
    public LocationUtil locationUtil;

    private Map<String,Boolean> orderSearchByPickupCode=new HashMap<String, Boolean>();

    public GlobalDataSetBean getGlobalDataSet() {

        if (globalDataSet == null) {
            globalDataSet = AppCacheManager.getGlobalDataSet();
        }

        return globalDataSet;
    }

    public MachineBean getMachine() {
        return AppCacheManager.getMachine();
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
        if(dialog_SystemWarn==null){
            dialog_SystemWarn = new CustomSystemWarnDialog(this);
        }

        return dialog_SystemWarn;
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

//    public void setScannerCtrl(Context context) {
//        if(getMachine().getScanner().getUse()) {
//            scannerCtrl = ScannerCtrl.getInstance();
//            scannerCtrl.connect();
//
//            if(!scannerCtrl.isConnect()) {
//                LogUtil.e(TAG, "扫描器连接失败");
//            }
//
//            scannerCtrl.setScanHandler(new Handler(new Handler.Callback() {
//                        @Override
//                        public boolean handleMessage(Message msg) {
//                            Bundle bundle;
//                            bundle = msg.getData();
//                            String scanResult = bundle.getString("result");
//                            if (scanResult != null) {
//                                if (scanResult.contains("pickupcode")) {
//                                    LogUtil.e("pickupcode:" + scanResult);
//                                    orderSearchByPickupCode(context,scanResult);
//                                }
//                            }
//                            return false;
//                        }
//                    })
//            );
//        }
//    }

    public void useClosePageCountTimer() {
        if(closePageCountTimer==null) {
            closePageCountTimer = new ClosePageCountTimer(this, 120);
        }
    }

    public void useClosePageCountTimer(ClosePageCountTimer.OnPageCountLinster onPageCountLinster,long seconds) {
        if(closePageCountTimer==null) {
            closePageCountTimer = new ClosePageCountTimer(this, seconds, onPageCountLinster);
        }
    }

    public void closePageCountTimerStart() {

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if(closePageCountTimer!=null) {
                    closePageCountTimer.start();
                }
            }
        });
    }

    public void closePageCountTimerStop() {

        if(closePageCountTimer!=null) {
            closePageCountTimer.cancel();
        }
    }

    public void checkIsHasExHappen() {
        MachineBean machine=getMachine();
        if(machine!=null) {
            if(!machine.getMachineId().equals("")) {
                if(machine.isExIsHas()) {
                        getDialogBySystemWarn().setBtnCloseVisibility(View.GONE);
                        getDialogBySystemWarn().show();
                }
            }
        }
    }

    public void showToast(String txt) {
        if (!StringUtil.isEmpty(txt)) {
            ToastUtil.showMessage(BaseFragmentActivity.this, txt, Toast.LENGTH_LONG);
        }
    }

    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    LogUtil.d(TAG,"USB Ready");
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    LogUtil.d(TAG,"USB Permission not granted");
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    LogUtil.d(TAG,"No USB connected");
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    LogUtil.d(TAG,"USB disconnected");
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    LogUtil.d(TAG,"USB device not supported");
                    break;
            }
        }
    };
    private UsbService usbService;
    private Handler mScanCtrlHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mScanCtrlHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        locationUtil = LocationUtil.getInstance(this); //阻碍线程线程读取

        appContext = (AppContext) getApplication();
        dialog_Loading = new CustomLoadingDialog(this);
        dialog_SystemWarn = new CustomSystemWarnDialog(this);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        AppManager.getAppManager().addActivity(this);


        if (StringUtil.isEmptyNotNull(AppCacheManager.getMachine().getMachineId()) || this.getGlobalDataSet() == null) {
            Activity activity = AppManager.getAppManager().currentActivity();
            if (activity instanceof InitDataActivity||activity instanceof SmRescueToolActivity) {

            } else {
                showToast("检查异常，设备重新运行");
                Intent intent = new Intent(appContext, InitDataActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            dialog_SystemWarn.setCsrPhoneNumber(getMachine().getCsrPhoneNumber());
            dialog_SystemWarn.setCsrQrcode(getMachine().getCsrQrCode());
            dialog_SystemWarn.setCsrHelpTip(getMachine().getCsrHelpTip());
        }


        laodingUIHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if(msg.obj!=null) {
                            dialog_Loading = new CustomLoadingDialog((Context) msg.obj);
                            dialog_Loading.setProgressText("正在处理中");
                            dialog_Loading.show();
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    if (dialog_Loading != null) {
                                        if (dialog_Loading.isShowing()) {
                                            laodingUIHandler.sendEmptyMessage(2);
                                        }
                                    }
                                }
                            }, 6000);
                        }
                        break;
                    case 2:
                        if (dialog_Loading != null) {
                            dialog_Loading.cancel();
                            dialog_Loading=null;
                        }
                        break;
                }
                return false;
            }
        });
    }

    public void showMachineId() {
        RelativeLayout layout_machineid = findViewById(R.id.layout_machineid);
        if (layout_machineid != null) {
            layout_machineid.getBackground().setAlpha(50);
            layout_machineid.setVisibility(View.VISIBLE);
            TextView tv_machineId_title =findViewById(R.id.tv_machineId_title);
            TextView tv_machineId_value =findViewById(R.id.tv_machineId_value);
            if(tv_machineId_value!=null){

                tv_machineId_value.setText(getMachine().getMachineId());
                tv_machineId_value.setTextColor(Color.argb(255, 0, 255, 0));
                tv_machineId_title.setTextColor(Color.argb(255, 0, 255, 0));
            }
        }
    }


    public  void  setScanCtrlHandler(Handler handler) {
        this.mScanCtrlHandler = handler;
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


        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null);


        //HeartbeatService.sendHeartbeatBag();

        //if(scannerCtrl!=null) {
        //    scannerCtrl.connect();
        //}
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
        closePageCountTimerStop();

        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);

        //if(scannerCtrl!=null) {
        //    scannerCtrl.disConnect();
        //}
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

        AppManager.getAppManager().finishActivity(this);

        closePageCountTimerStop();

        if (dialog_Loading != null) {

            dialog_Loading.cancel();
        }

        if(dialog_SystemWarn!=null){
            dialog_SystemWarn.cancel();
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

    public void getByMy(Context context, String url, Map<String, String> params, final Boolean isShowLoading, final String loadingMsg, final HttpResponseHandler handler) {

        HttpClient.getByAppSecret(BuildConfig.APPKEY, BuildConfig.APPSECRET, url, params, new HttpResponseHandler() {

            @Override
            public void onBeforeSend() {

                if (isShowLoading) {
                    if (!StringUtil.isEmptyNotNull(loadingMsg)) {

                        Message m = new Message();
                        m.what=1;
                        m.obj=context;
                        laodingUIHandler.sendMessage(m);
                    }
                }
            }

            @Override
            public void onSuccess(String response) {
                if (isShowLoading) {
                    Message m = new Message();
                    m.what=2;
                    m.obj=context;
                    laodingUIHandler.sendMessage(m);
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
                    Message m = new Message();
                    m.what=2;
                    m.obj=context;
                    laodingUIHandler.sendMessage(m);
                }
                handler.onFailure(msg, e);
            }
        });
    }

    public void postByMy(Context context,String url, Map<String, Object> params, Map<String, String> filePaths, final Boolean isShowLoading, final String loadingMsg, final HttpResponseHandler handler) {

        HttpClient.postByAppSecret(BuildConfig.APPKEY, BuildConfig.APPSECRET, url, params, filePaths, new HttpResponseHandler() {

            @Override
            public void onBeforeSend() {
                if (isShowLoading) {
                    Message m = new Message();
                    m.what=1;
                    m.obj=context;
                    laodingUIHandler.sendMessage(m);
                }
            }

            @Override
            public void onSuccess(String response) {
                if (isShowLoading) {
                    Message m = new Message();
                    m.what=2;
                    m.obj=context;
                    laodingUIHandler.sendMessage(m);
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
                    Message m = new Message();
                    m.what=2;
                    m.obj=context;
                    laodingUIHandler.sendMessage(m);
                }
                handler.onFailure(msg, e);
            }
        });
    }

    public void orderCancle(Context context, String orderId,int type, String reason) {

        MachineBean machine = AppCacheManager.getMachine();

        Map<String, Object> params = new HashMap<>();
        params.put("machineId", machine.getMachineId() + "");
        params.put("orderId", orderId);
        params.put("type", type);
        params.put("reason", reason);

        postByMy(context,Config.URL.order_Cancle, params, null, true, "", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
            }
        });
    }

    public static void eventNotify(String eventCode,String eventRemark, JSONObject content){

        MachineBean machine = AppCacheManager.getMachine();

        Map<String, Object> params = new HashMap<>();
        params.put("appId", BuildConfig.APPLICATION_ID);
        params.put("deviceId", AppContext.getInstance().getDeviceId());
        params.put("machineId", machine.getMachineId() + "");
        params.put("lat", LocationUtil.LAT);
        params.put("lng", LocationUtil.LNG);
        params.put("eventCode", eventCode);
        params.put("eventRemark", eventRemark);
        if(content!=null) {
            params.put("content", content);
        }

        HttpClient.postByAppSecret(BuildConfig.APPKEY, BuildConfig.APPSECRET, Config.URL.machine_EventNotify, params, null, new HttpResponseHandler() {

            @Override
            public void onBeforeSend() {

            }

            @Override
            public void onSuccess(String response) {

            }

            @Override
            public void onFailure(String msg, Exception e) {
            }
        });
    }

    public void orderSearchByPickupCode(Context context, String pickCode) {

        Map<String, String> params = new HashMap<>();

        params.put("machineId", this.getMachine().getMachineId());
        params.put("pickupCode", pickCode);

        getByMy(context,Config.URL.order_SearchByPickupCode, params, false, "正在寻找订单", new HttpResponseHandler() {
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
