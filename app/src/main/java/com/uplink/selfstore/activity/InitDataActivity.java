package com.uplink.selfstore.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.activity.adapter.LogAdapter;
import com.uplink.selfstore.deviceCtrl.MachineCtrl;
import com.uplink.selfstore.deviceCtrl.FingerVeinCtrl;
import com.uplink.selfstore.model.LogBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.R;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.GlobalDataSetBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.service.AlarmService;
import com.uplink.selfstore.service.CameraSnapService;
import com.uplink.selfstore.service.UpdateAppService;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.ui.LoadingView;
import com.uplink.selfstore.ui.dialog.CustomFingerVeinDialog;
import com.uplink.selfstore.ui.my.MyListView;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.LongClickUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

public class InitDataActivity extends BaseFragmentActivity implements View.OnClickListener {
    private final String TAG = "InitDataActivity";

    private Handler handler_msg;
    private Button btn_retry;
    private ProgressBar loading_bar;
    private LoadingView loading_ani;
    private TextView loading_msg;
    private TextView txt_deviceId;
    private TextView txt_version;
    private MyListView list_log;
    private View btn_appexit;
    private List<LogBean> logs=new ArrayList<>();
    private MachineCtrl machineCtrl=null;

    private boolean initIsRun=false;
    private Handler initHandler = new Handler();
    private Runnable initRunable = new Runnable() {
        @Override
        public void run() {

            if(!initIsRun) {
                initIsRun=true;
                setMachineInitData();
            }

            initHandler.postDelayed(this, 1000);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initdata);

        setHideStatusBar(false);

        initView();
        initEvent();
        initData();

        machineCtrl=MachineCtrl.getInstance();

        initHandler.postDelayed(initRunable, 1000);

        Intent cameraSnapService = new Intent(this, CameraSnapService.class);
        startService(cameraSnapService);

        Intent updateAppService = new Intent(this, UpdateAppService.class);
        startService(updateAppService);

        Intent alarmService=new Intent(this, AlarmService.class);
        startService(alarmService);

        machineCtrl.goGoZero();

        FingerVeinCtrl.getInstance().tryGetPermission(InitDataActivity.this);

    }

    protected void initView() {
        btn_retry = (Button) findViewById(R.id.btn_retry);
        loading_ani = (LoadingView) findViewById(R.id.loading_ani);
        loading_bar = (ProgressBar) findViewById(R.id.loading_bar);
        loading_msg = (TextView) findViewById(R.id.loading_msg);
        txt_deviceId=(TextView) findViewById(R.id.txt_deviceId);
        txt_version=(TextView) findViewById(R.id.txt_version);
        list_log=(MyListView)findViewById(R.id.list_log);
        btn_appexit=(View)findViewById(R.id.btn_appexit);
    }


    private void initEvent() {

        btn_retry.setOnClickListener(this);

        LongClickUtil.setLongClick(new Handler(), btn_appexit, 500, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LogUtil.e("长按触发");
                setHideStatusBar(false);
                AppManager.getAppManager().AppExit(InitDataActivity.this);
                return true;
            }
        });


        loading_ani.start();

        handler_msg = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String content=msg.obj.toString();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                String dateTime = sdf.format(new Date());
                loading_msg.setText(msg.obj.toString());


                LogBean log=new LogBean();
                log.setDateTime(dateTime);
                log.setContent(content);
                logs.add(log);

                List<LogBean> reverseLogs=new ArrayList<>();
                for (int i=logs.size();i>0;i--) {
                    if(reverseLogs.size()>10) {
                        break;
                    }
                    reverseLogs.add(logs.get(i-1));
                }

                LogAdapter logAdapter = new LogAdapter(InitDataActivity.this,reverseLogs);
                list_log.setAdapter(logAdapter);

                switch (msg.what) {
                    case 1:
                        break;
                    case 2:
                        initIsRun=false;
                        btn_retry.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        loading_ani.stop();
                        Intent intent = new Intent(getAppContext(), MainActivity.class);
                        SystemClock.sleep(2000);
                        startActivity(intent);
                        finish();
                    case 4:
                        btn_retry.setVisibility(View.INVISIBLE);
                        break;
                }

                return  false;
            }
        });
    }

    private void initData() {

        txt_deviceId.setText(getAppContext().getDeviceId());
        txt_version.setText(BuildConfig.VERSION_NAME);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_retry:
                setTips(0x0004, getAppContext().getString(R.string.activity_initdata_tips_retry));
                handler_msg.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setMachineInitData();
                    }
                }, 2000);

                CustomFingerVeinDialog  dialog_FingerVein=new CustomFingerVeinDialog(InitDataActivity.this);
                dialog_FingerVein.setCheckLoginHandler(new Handler(new Handler.Callback() {
                            @Override
                            public boolean handleMessage(Message msg) {
                                Bundle bundle = msg.getData();
                                int status = bundle.getInt("status");
                                String message = bundle.getString("message");
                                byte[] result;
                                switch (status) {
                                    case 1://消息提示
                                        showToast(message);
                                        break;
                                    case 2://检查到手指
                                        result = bundle.getByteArray("result");
                                        //loginByFingerVein(result);
                                        break;
                                }
                                return false;
                            }
                        })
                );

                dialog_FingerVein.startCheckLogin();

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(initHandler!=null&&initRunable!=null) {
            initHandler.removeCallbacks(initRunable);
        }
    }

    public void setTips(int what, String msg) {
        final Message m = new Message();
        m.what = what;
        m.obj = msg;
        handler_msg.sendMessage(m);
    }

    public void setMachineInitData() {

        setTips(0x0001, getAppContext().getString(R.string.activity_initdata_tips_settingmachine));

        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", getAppContext().getDeviceId());
        params.put("imeiId",getAppContext().getImeiId());
        params.put("jPushRegId", JPushInterface.getRegistrationID(getAppContext()));
        params.put("appVersionCode", BuildConfig.VERSION_CODE);
        params.put("appVersionName", BuildConfig.VERSION_NAME);
        params.put("ctrlSdkVersionCode", machineCtrl.vesion());
        params.put("macAddress", getAppContext().getMacAddress());

        postByMy(Config.URL.machine_InitData, params,null, false, "", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                ApiResultBean<GlobalDataSetBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<GlobalDataSetBean>>() {
                });
                if (rt.getResult() == Result.SUCCESS) {
                    AppCacheManager.setGlobalDataSet(rt.getData());
                    setTips(0x0003, getAppContext().getString(R.string.activity_initdata_tips_settingmachinesuccess));
                } else {
                    setTips(0x0002, getAppContext().getString(R.string.activity_initdata_tips_settingmachinefailure) + ":" + rt.getMessage());
                }
            }

            @Override
            public void onFailure(String msg, Exception e) {
                setTips(0x0002, msg);
            }
        });

    }
}
