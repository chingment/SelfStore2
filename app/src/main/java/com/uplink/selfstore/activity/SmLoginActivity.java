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
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.LongClickUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class SmLoginActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "SmLoginActivity";
    View nav_back;
    Button btn_login;//登录按钮
    EditText txt_username;//账户
    EditText txt_password;//密码
    View btn_appexit;
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
        btn_login = (Button) this.findViewById(R.id.btn_login);
        txt_username = (EditText) this.findViewById(R.id.txt_username);
        txt_password = (EditText) this.findViewById(R.id.txt_password);
        btn_appexit= (View) this.findViewById(R.id.btn_appexit);
    }

    protected void initEvent() {
        btn_login.setOnClickListener(this);
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
                case R.id.btn_login:


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
            }
        }
    }

}
