package com.uplink.selfstore.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.uplink.selfstore.R;
import com.uplink.selfstore.ostCtrl.OstCtrlBySx;
import com.uplink.selfstore.ostCtrl.OstCtrlByYs;
import com.uplink.selfstore.ostCtrl.OstCtrlInterface;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

public class SmRescueToolActivity extends SwipeBackActivity implements View.OnClickListener {

    private Button btn_ShowNav;
    private Button btn_AppExit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smrescuetool);
        setNavTtile("设置检查");
        setNavGoBackBtnVisible(true);

        initView();
        initEvent();
    }

    private void initView() {
        btn_ShowNav=(Button) findViewById(R.id.btn_ShowNav);
        btn_AppExit=(Button)findViewById(R.id.btn_AppExit);
    }

    private void initEvent() {
        btn_ShowNav.setOnClickListener(this);
        btn_AppExit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.nav_back:
                    finish();
                    break;
                case R.id.btn_ShowNav:
                    OstCtrlInterface.getInstance().setHideStatusBar(SmRescueToolActivity.this,false);
                    break;
                case R.id.btn_AppExit:
                    OstCtrlInterface.getInstance().setHideStatusBar(SmRescueToolActivity.this,false);
                    AppManager.getAppManager().AppExit(SmRescueToolActivity.this);
                    break;
            }
        }
    }

}
