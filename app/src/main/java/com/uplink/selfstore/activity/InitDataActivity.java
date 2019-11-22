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
import com.uplink.selfstore.deviceCtrl.MachineCtrl;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.R;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.GlobalDataSetBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.service.CameraSnapService;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.ui.LoadingView;
import com.uplink.selfstore.utils.serialport.ChangeToolUtils;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

public class InitDataActivity extends BaseFragmentActivity implements View.OnClickListener {
    private String TAG = "InitDataActivity";

    private Handler handler_msg;
    private Button btn_retry;
    private ProgressBar loading_bar;
    private LoadingView loading_ani;
    private TextView loading_msg;
    private TextView txt_machineId;

    private MachineCtrl machineCtrl=new MachineCtrl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initdata);

        setHideStatusBar(false);
        initView();
        initEvent();
        initData();

        Intent intent = new Intent(InitDataActivity.this, CameraSnapService.class);
        startService(intent);

//        Intent intent3 = new Intent();
//        intent3.setAction("android.intent.action.CameraSnapService");
//        intent3.putExtra("cmd", "cmd2");
//        intent3.putExtra("value", "value2");
//        sendBroadcast(intent3);

    }

    protected void initView() {
        btn_retry = (Button) findViewById(R.id.btn_retry);
        loading_ani = (LoadingView) findViewById(R.id.loading_ani);
        loading_bar = (ProgressBar) findViewById(R.id.loading_bar);
        loading_msg = (TextView) findViewById(R.id.loading_msg);
        txt_machineId=(TextView) findViewById(R.id.txt_machineId);
    }


    private void initEvent() {
        btn_retry.setOnClickListener(this);
        loading_ani.start();

        handler_msg = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                loading_msg.setText(msg.obj.toString());

                switch (msg.what) {
                    case 0x0001:
                        break;
                    case 0x0002:
                        btn_retry.setVisibility(View.VISIBLE);
                        break;
                    case 0x0003:
                        loading_ani.stop();
                        Intent intent = new Intent(getAppContext(), MainActivity.class);
                        SystemClock.sleep(2000);
                        startActivity(intent);
                        finish();
                    case 0x0004:
                        btn_retry.setVisibility(View.INVISIBLE);
                        break;
                }

                return  false;
            }
        });
    }

    private void initData() {

        txt_machineId.setText(getAppContext().getDeviceId());

        setMachineInitData();
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
                break;
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
        params.put("machineId", getAppContext().getDeviceId());
        params.put("jPushRegId", JPushInterface.getRegistrationID(getAppContext()));
        params.put("appVersionCode", BuildConfig.VERSION_CODE);
        params.put("appVersionName", BuildConfig.VERSION_NAME);
        params.put("ctrlSdkVersionCode", machineCtrl.vesion());
        params.put("macAddress", "");

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
