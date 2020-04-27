package com.uplink.selfstore.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.uplink.selfstore.R;
import com.uplink.selfstore.ostCtrl.OstCtrlBySx;
import com.uplink.selfstore.ostCtrl.OstCtrlByYs;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

public class InitToolActivity extends SwipeBackActivity implements View.OnClickListener {

    private Button btn_ShowNavBySx;
    private Button btn_ShowNavByYs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inittool);
        setNavTtile("设置检查");
        setNavGoBackBtnVisible(true);

        initView();
        initEvent();
    }

    protected void initView() {
        btn_ShowNavBySx=(Button) findViewById(R.id.btn_ShowNavBySx);
        btn_ShowNavByYs=(Button)findViewById(R.id.btn_ShowNavByYs);
    }

    private void initEvent() {
        btn_ShowNavBySx.setOnClickListener(this);
        btn_ShowNavByYs.setOnClickListener(this);
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
                    ostCtrlBySx.setHideStatusBar(InitToolActivity.this,false);
                    break;
                case R.id.btn_ShowNavByYs:
                    OstCtrlByYs ostCtrlByYs=new OstCtrlByYs();
                    ostCtrlByYs.setHideStatusBar(InitToolActivity.this,false);
                    break;
            }
        }
    }
}
