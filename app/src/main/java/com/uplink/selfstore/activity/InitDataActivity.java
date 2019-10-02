package com.uplink.selfstore.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.R;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.GlobalDataSetBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.ui.LoadingView;
import com.uplink.selfstore.utils.LogUtil;

import java.util.HashMap;
import java.util.Map;

public class InitDataActivity extends BaseFragmentActivity implements View.OnClickListener {
    private String TAG = "InitDataActivity";

    private Handler handler_msg;
    private Button btn_retry;
    private ProgressBar loading_bar;
    private LoadingView loading_ani;
    private TextView loading_msg;
    private TextView txt_machineId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initdata);

         setShowStatuBar(true);

        if (AppCacheManager.getGlobalDataSet() != null) {
            Intent intent = new Intent(getAppContext(), MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        initView();
        initEvent();
        initData();

    }

    protected void initView() {
        btn_retry = (Button) findViewById(R.id.btn_retry);
        loading_ani = (LoadingView) findViewById(R.id.loading_ani);
        loading_bar = (ProgressBar) findViewById(R.id.loading_bar);
        loading_msg = (TextView) findViewById(R.id.loading_msg);
        txt_machineId=(TextView) findViewById(R.id.txt_machineId);
    }

    @SuppressLint("HandlerLeak")
    private void initEvent() {
        btn_retry.setOnClickListener(this);
        loading_ani.start();

        handler_msg = new Handler() {
            @Override
            public void handleMessage(Message msg) {
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
            }
        };
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
        Map<String, String> params = new HashMap<>();
        params.put("machineId", getAppContext().getDeviceId());
        params.put("datetime", AppCacheManager.getLastUpdateTime());

        getByMy(Config.URL.machine_InitData, params, false, "", new HttpResponseHandler() {
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
