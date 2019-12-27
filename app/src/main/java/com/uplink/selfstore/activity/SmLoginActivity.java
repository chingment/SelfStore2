package com.uplink.selfstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.OpUserInfoBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.dialog.CustomFingerVeinDialog;
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
    private Button btn_loginByAccount;//账号密码登录按钮
    private EditText txt_username;//账户
    private EditText txt_password;//密码
    private View btn_appexit;
    private View btn_loginByVeinLock;//指静脉登录按钮
    private CustomFingerVeinDialog dialog_FingerVein;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smlogin);

        setNavTtile(this.getResources().getString(R.string.activity_smlogin_navtitle));
        setNavBackVisible(true);
        initView();
        initEvent();
        initData();

        useClosePageCountTimer();
    }

    protected void initView() {

        nav_back=this.findViewById(R.id.nav_back);
        btn_loginByAccount = (Button) this.findViewById(R.id.btn_loginByAccount);
        txt_username = (EditText) this.findViewById(R.id.txt_username);
        txt_password = (EditText) this.findViewById(R.id.txt_password);
        btn_appexit=this.findViewById(R.id.btn_appexit);
        btn_loginByVeinLock= this.findViewById(R.id.btn_loginByVeinLock);
        dialog_FingerVein=new CustomFingerVeinDialog(SmLoginActivity.this);
    }

    protected void initEvent() {
        btn_loginByAccount.setOnClickListener(this);
        btn_loginByVeinLock.setOnClickListener(this);
        nav_back.setOnClickListener(this);

        LongClickUtil.setLongClick(new Handler(), btn_appexit, 500, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LogUtil.e("长按触发");
                setHideStatusBar(false);
                AppManager.getAppManager().AppExit(SmLoginActivity.this);
                return true;
            }
        });
    }

    protected void initData() {

        String lastUsername = AppCacheManager.getLastUserName();

        if (!StringUtil.isEmptyNotNull(lastUsername)) {
            txt_username.setText(lastUsername);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            String userName = txt_username.getText() + "";
            if (!StringUtil.isEmptyNotNull(userName)) {
                txt_password.requestFocus();
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
                case R.id.btn_loginByAccount:


                    String userName = txt_username.getText() + "";
                    if (StringUtil.isEmpty(userName)) {
                        showToast("用户名为空");
                        return;
                    }
                    String password = txt_password.getText() + "";
                    if (StringUtil.isEmpty(password)) {
                        showToast("密码为空");
                        return;
                    }

                    MachineBean machine = AppCacheManager.getMachine();


                    Map<String, Object> params = new HashMap<>();
                    params.put("machineId", machine.getId() + "");
                    params.put("userName", userName);
                    params.put("password", password);


                    postByMy(Config.URL.machine_Login, params, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
                        @Override
                        public void onSuccess(String response) {

                            ApiResultBean<OpUserInfoBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<OpUserInfoBean>>() {
                            });

                            if (rt.getResult() == Result.SUCCESS) {

                                AppCacheManager.setLastUserName(rt.getData().getUserName());
                                AppCacheManager.setOpUserInfo(rt.getData());

                                Intent intent = new Intent(getAppContext(), SmHomeActivity.class);
                                startActivity(intent);


                            } else {
                                showToast(rt.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(String msg, Exception e) {
                            showToast(msg);
                        }
                    });

                    break;
                case R.id.btn_loginByVeinLock:
                    dialog_FingerVein.show();
                    break;

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog_FingerVein != null && dialog_FingerVein.isShowing()) {
            dialog_FingerVein.cancel();
        }
    }


}
