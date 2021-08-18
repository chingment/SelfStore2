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
import com.uplink.selfstore.db.DbManager;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.CustomDataByVendingBean;
import com.uplink.selfstore.model.api.DeviceBean;
import com.uplink.selfstore.model.api.OrderDetailsBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.RetDeviceEventNotify;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppContext;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.http.HttpClient;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ostCtrl.OstCtrlInterface;
import com.uplink.selfstore.service.UsbService;
import com.uplink.selfstore.ui.dialog.CustomDialogLoading;
import com.uplink.selfstore.ui.dialog.CustomDialogSystemWarn;
import com.uplink.selfstore.utils.LocationUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;
import com.uplink.selfstore.utils.ToastUtil;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by chingment on 2017/8/23.
 */

public  class BaseFragmentActivity extends FragmentActivity implements View.OnClickListener {
    private static final String TAG = "BaseFragmentActivity";
    private AppContext appContext;
    public static boolean isForeground = false;
    private CustomDialogLoading dialog_Loading;
    private CustomDialogSystemWarn dialog_SystemWarn;
    private ClosePageCountTimer closePageCountTimer;
    private CustomDataByVendingBean customDataByVending;
    private DeviceBean device;
    private Handler laodingUIHandler;
    public LocationUtil locationUtil;
    public  Toast TOAST;
    private Map<String,Boolean> orderSearchByPickupCode=new HashMap<String, Boolean>();

    public CustomDataByVendingBean getCustomDataByVending() {

        if (customDataByVending == null) {
            customDataByVending = AppCacheManager.getCustomDataByVending();
        }

        return customDataByVending;
    }

    public DeviceBean getDevice() {

        if (device == null) {
            device = AppCacheManager.getDevice();
        }

        return device;
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

    public CustomDialogSystemWarn getDialogBySystemWarn(){
        if(dialog_SystemWarn==null){
            dialog_SystemWarn = new CustomDialogSystemWarn(this);
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
        DeviceBean device=getDevice();
        if(device!=null) {
            if(!device.getDeviceId().equals("")) {
                if(device.isExIsHas()) {
                        getDialogBySystemWarn().setCloseVisibility(View.GONE);
                        getDialogBySystemWarn().show();
                }
                else {
                    getDialogBySystemWarn().hide();
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

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
//        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            Window window = getWindow();
//            // Translucent status bar
//            window.setFlags(
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }

        locationUtil = LocationUtil.getInstance(this); //阻碍线程线程读取

        appContext = (AppContext) getApplication();
        dialog_Loading = new CustomDialogLoading(this);
        dialog_SystemWarn = new CustomDialogSystemWarn(this);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        AppManager.getAppManager().addActivity(this);


        if (StringUtil.isEmptyNotNull(AppCacheManager.getDevice().getDeviceId())) {
            Activity activity = AppManager.getAppManager().currentActivity();
            if(activity!=null){
                if (activity instanceof InitDataActivity||activity instanceof SmRescueToolActivity) {

                } else {
                    showToast("检查异常，设备重新运行");
                    Intent intent = new Intent(appContext, InitDataActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        } else {
            DeviceBean device = getDevice();
            dialog_SystemWarn.setCsrPhoneNumber(device.getConsult().getCsrPhoneNumber());
            dialog_SystemWarn.setCsrQrcode(device.getConsult().getCsrQrCode());
            dialog_SystemWarn.setCsrHelpTip(device.getConsult().getCsrHelpTip());
        }


        laodingUIHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if(msg.obj!=null) {
                            if(dialog_Loading==null) {
                                dialog_Loading = new CustomDialogLoading((Context) msg.obj);
                            }
                            if(!dialog_Loading.isShowing()) {
                                dialog_Loading.setTipsText("正在处理中");
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

    public void showDeviceId() {
        RelativeLayout layout_deviceid = findViewById(R.id.layout_deviceid);
        if (layout_deviceid != null) {
            layout_deviceid.getBackground().setAlpha(50);
            layout_deviceid.setVisibility(View.VISIBLE);
            TextView tv_deviceId_title =findViewById(R.id.tv_deviceId_title);
            TextView tv_deviceId_value =findViewById(R.id.tv_deviceId_value);
            if(tv_deviceId_value!=null){

                tv_deviceId_value.setText(getDevice().getDeviceId());
                tv_deviceId_value.setTextColor(Color.argb(255, 0, 255, 0));
                tv_deviceId_title.setTextColor(Color.argb(255, 0, 255, 0));
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


    public void showLoading(Context context) {
        Message m = new Message();
        m.what = 1;
        m.obj = context;
        laodingUIHandler.sendMessage(m);
    }

    public void hideLoading(Context context){
        Message m = new Message();
        m.what=2;
        m.obj=context;
        laodingUIHandler.sendMessage(m);
    }


    public void postByMy(Context context,String url, Map<String, Object> params,final Boolean isShowLoading, final String loadingMsg, final HttpResponseHandler handler) {

        HttpClient.postByMy(url, params,new HttpResponseHandler() {

            @Override
            public void onBeforeSend() {
                if (isShowLoading) {
                    showLoading(context);
                }
            }

            @Override
            public void onSuccess(String response) {
                if (isShowLoading) {
                   hideLoading(context);
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
                    hideLoading(context);
                }
                handler.onFailure(msg, e);
            }
        });
    }

    public static void eventNotify(String eventCode,String eventRemark, JSONObject content) {


        DeviceBean device = AppCacheManager.getDevice();

        Map<String, Object> params = new HashMap<>();
        params.put("appId", BuildConfig.APPLICATION_ID);
        params.put("deviceId", device.getDeviceId());
        params.put("lat", LocationUtil.LAT);
        params.put("lng", LocationUtil.LNG);
        params.put("eventCode", eventCode);
        params.put("eventRemark", eventRemark);
        if (content != null) {
            params.put("content", content);
        }

        JSONObject json = new JSONObject();
        try {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                json.put(entry.getKey(), entry.getValue());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String content2 = json.toString();

        int msg_id = DbManager.getInstance().saveTripMsg(Config.URL.device_EventNotify, content2);

        params.put("msgId", msg_id);
        params.put("msgMode", "normal");
        HttpClient.postByMy(Config.URL.device_EventNotify, params, new HttpResponseHandler() {

            @Override
            public void onBeforeSend() {


            }

            @Override
            public void onSuccess(String response) {

                ApiResultBean<RetDeviceEventNotify> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<RetDeviceEventNotify>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {
                    RetDeviceEventNotify ret = rt.getData();
                    DbManager.getInstance().deleteTripMsg(ret.getMsgId());
                }
            }

            @Override
            public void onFailure(String msg, Exception e) {

            }
        });
    }

    public void orderSearchByPickupCode(Context context, String pickCode) {

        Map<String, Object> params = new HashMap<>();

        params.put("deviceId", this.getDevice().getDeviceId());
        params.put("pickupCode", pickCode);

        postByMy(context,Config.URL.order_SearchByPickupCode, params, true, "正在寻找订单", new HttpResponseHandler() {
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
