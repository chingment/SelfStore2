package com.uplink.selfstore.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.uplink.selfstore.R;
import com.uplink.selfstore.ostCtrl.OstCtrlBySx;
import com.uplink.selfstore.ostCtrl.OstCtrlByYs;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

public class SmRescueToolActivity extends SwipeBackActivity implements View.OnClickListener {

    private Button btn_ShowNavBySx;
    private Button btn_ShowNavByYs;
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
        btn_ShowNavBySx=(Button) findViewById(R.id.btn_ShowNavBySx);
        btn_ShowNavByYs=(Button)findViewById(R.id.btn_ShowNavByYs);
        btn_AppExit=(Button)findViewById(R.id.btn_AppExit);
    }

    private void initEvent() {
        btn_ShowNavBySx.setOnClickListener(this);
        btn_ShowNavByYs.setOnClickListener(this);
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
                case R.id.btn_ShowNavBySx:
                    OstCtrlBySx ostCtrlBySx=new OstCtrlBySx();
                    ostCtrlBySx.setHideStatusBar(SmRescueToolActivity.this,false);
                    break;
                case R.id.btn_ShowNavByYs:
                    OstCtrlByYs ostCtrlByYs=new OstCtrlByYs();
                    ostCtrlByYs.setHideStatusBar(SmRescueToolActivity.this,false);
                    break;
                case R.id.btn_AppExit:
                    AppManager.getAppManager().AppExit(SmRescueToolActivity.this);
                    break;
            }
        }
    }

}
