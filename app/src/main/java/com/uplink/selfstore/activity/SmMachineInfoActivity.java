package com.uplink.selfstore.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.R;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.LocationUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import cn.jpush.android.api.JPushInterface;

public class SmMachineInfoActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "SmMachineInfoActivity";
    private TextView txt_MerchantName;
    private TextView txt_StoreName;
    private TextView txt_DeviceId;
    private TextView txt_MachineId;
    private TextView txt_Location;
    private TextView txt_JPushRegId;
    private TextView txt_AppVersion;
    private TextView txt_CabinetCtrlSdkVersionByDS;
    private TextView txt_Currency;
    private TextView txt_CurrencySymbol;

    private CabinetCtrlByDS cabinetCtrlByDS=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smmachineinfo);

        cabinetCtrlByDS=CabinetCtrlByDS.getInstance();

        setNavTtile(this.getResources().getString(R.string.aty_smmachineinfo_navtitle));
        setNavGoBackBtnVisible(true);

        initView();
        initEvent();
        initData();
    }

    private void initView() {
        txt_MerchantName = (TextView) findViewById(R.id.txt_MerchantName);
        txt_StoreName = (TextView) findViewById(R.id.txt_StoreName);
        txt_MachineId = (TextView) findViewById(R.id.txt_MachineId);
        txt_DeviceId=(TextView) findViewById(R.id.txt_DeviceId);
        txt_Location = (TextView) findViewById(R.id.txt_Location);
        txt_JPushRegId = (TextView) findViewById(R.id.txt_JPushRegId);
        txt_AppVersion= (TextView) findViewById(R.id.txt_AppVersion);
        txt_CabinetCtrlSdkVersionByDS=(TextView) findViewById(R.id.txt_CabinetCtrlSdkVersionByDS);
        txt_Currency= (TextView) findViewById(R.id.txt_Currency);
        txt_CurrencySymbol= (TextView) findViewById(R.id.txt_CurrencySymbol);
    }

    private void initEvent() {

    }

    private void initData() {

        txt_MerchantName.setText(getMachine().getMerchName());
        txt_StoreName.setText(getMachine().getStoreName());
        txt_MachineId.setText(getMachine().getId());
        txt_DeviceId.setText(getAppContext().getDeviceId());
        txt_JPushRegId.setText(JPushInterface.getRegistrationID(getAppContext()));
        txt_AppVersion.setText(BuildConfig.VERSION_NAME);
        txt_CabinetCtrlSdkVersionByDS.setText(cabinetCtrlByDS.vesion());
        txt_Currency.setText(getMachine().getCurrency());
        txt_CurrencySymbol.setText(getMachine().getCurrencySymbol());
        txt_Location.setText(LocationUtil.LAT+","+ LocationUtil.LNG);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.nav_back:
                    finish();
                    break;
                case R.id.nav_btn:

                    break;
            }
        }
    }
}
