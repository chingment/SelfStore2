package com.uplink.selfstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.R;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.DeviceBean;
import com.uplink.selfstore.model.api.OpUserInfoBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.dialog.CustomDialogFingerVein;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.LongClickUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;
import com.uplink.selfstore.utils.StringUtil;


import java.util.HashMap;
import java.util.Map;

public class SmLoginActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "SmLoginActivity";
    private View nav_back;
    private Button btn_LoginByAccount;//账号密码登录按钮
    private EditText tv_UserName;//账户
    private EditText tv_Password;//密码
    private View btn_AppExit;
    private View btn_LoginByVeinLock;//指静脉登录按钮
    private CustomDialogFingerVein dialog_FingerVein;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smlogin);

        setNavTtile(this.getResources().getString(R.string.aty_smlogin_navtitle));
        setNavGoBackBtnVisible(true);
        initView();
        initEvent();
        initData();

        useClosePageCountTimer();
    }

    private void initView() {

        nav_back=this.findViewById(R.id.nav_back);
        btn_LoginByAccount = (Button) this.findViewById(R.id.btn_LoginByAccount);
        btn_LoginByVeinLock= this.findViewById(R.id.btn_LoginByVeinLock);

        tv_UserName = (EditText) this.findViewById(R.id.tv_UserName);
        tv_Password = (EditText) this.findViewById(R.id.tv_Password);
        btn_AppExit=this.findViewById(R.id.btn_AppExit);

        if(getDevice().getFingerVeinner().getUse()) {
            dialog_FingerVein=new CustomDialogFingerVein(SmLoginActivity.this);
            dialog_FingerVein.setCheckLoginHandler(new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            Bundle bundle = msg.getData();
                            int status = bundle.getInt("status");
                            String message = bundle.getString("message");
                            byte[] result;
                            switch (status) {
                                case 1://消息提示
                                    //showToast(message);
                                    LogUtil.d(TAG,"静指脉设备消息提示："+message);
                                    break;
                                case 2://检查到手指
                                    result = bundle.getByteArray("result");
                                    loginByFingerVein(result);
                                    break;
                                case 3://异常信息
                                    LogUtil.d(TAG,"静指脉设备连接异常");
                                    break;
                            }
                            return false;
                        }
                    })
            );
            dialog_FingerVein.startCheckLogin();
        }
    }

    private void initEvent() {
        btn_LoginByAccount.setOnClickListener(this);
        btn_LoginByVeinLock.setOnClickListener(this);
        nav_back.setOnClickListener(this);

        LongClickUtil.setLongClick(new Handler(), btn_AppExit, 500, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(getAppContext(), SmRescueToolActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
        });
    }

    private void initData() {

        String lastUsername = AppCacheManager.getLastUserName();

        if (!StringUtil.isEmptyNotNull(lastUsername)) {
            tv_UserName.setText(lastUsername);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            String userName = tv_UserName.getText() + "";
            if (!StringUtil.isEmptyNotNull(userName)) {
                tv_Password.requestFocus();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.nav_back:
                    finish();
                    break;
                case R.id.btn_LoginByAccount:
                    loginByAccount();
                    break;
                case R.id.btn_LoginByVeinLock:
                    if(dialog_FingerVein!=null) {
                        dialog_FingerVein.startCheckLogin();
                        dialog_FingerVein.show();
                    }
                    break;

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog_FingerVein != null) {
            dialog_FingerVein.cancel();
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
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    public void  loginByAccount(){

        String userName = tv_UserName.getText() + "";
        if (StringUtil.isEmpty(userName)) {
            showToast("用户名为空");
            return;
        }
        String password = tv_Password.getText() + "";
        if (StringUtil.isEmpty(password)) {
            showToast("密码为空");
            return;
        }

        DeviceBean device = AppCacheManager.getDevice();


        Map<String, Object> params = new HashMap<>();
        params.put("userName", userName);
        params.put("password", password);
        params.put("appId", BuildConfig.APPLICATION_ID);
        params.put("loginWay", 5);
        params.put("deviceId", device.getDeviceId() + "");


        postByMy(SmLoginActivity.this, Config.URL.own_LoginByAccount, params, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {

                ApiResultBean<OpUserInfoBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<OpUserInfoBean>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {

                    AppCacheManager.setLastUserName(rt.getData().getUserName());
                    AppCacheManager.setOpUserInfo(rt.getData());

                    Intent intent = new Intent(getAppContext(), SmHomeActivity.class);
                    startActivity(intent);
                    finish();

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

    public void  loginByFingerVein(byte[] veinData){

        DeviceBean device = AppCacheManager.getDevice();

        Map<String, Object> params = new HashMap<>();
        params.put("veinData",  Base64.encodeToString(veinData, Base64.NO_WRAP));
        params.put("appId", BuildConfig.APPLICATION_ID);
        params.put("loginWay", 5);
        params.put("deviceId", device.getDeviceId() + "");


        postByMy(SmLoginActivity.this, Config.URL.own_LoginByFingerVein, params, false, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {

                ApiResultBean<OpUserInfoBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<OpUserInfoBean>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {

                    AppCacheManager.setLastUserName(rt.getData().getUserName());
                    AppCacheManager.setOpUserInfo(rt.getData());

                    Intent intent = new Intent(getAppContext(), SmHomeActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    dialog_FingerVein.resumeCheckLogin();
                    //showToast(rt.getMessage());
                }
            }

            @Override
            public void onFailure(String msg, Exception e) {
                dialog_FingerVein.resumeCheckLogin();
                showToast(msg);
            }
        });

    }


}
