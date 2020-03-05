package com.uplink.selfstore.activity;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.R;
import com.uplink.selfstore.deviceCtrl.MachineCtrl;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.LocationUtil;
import com.uplink.selfstore.utils.LogUtil;
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
    private TextView txt_MachineCtrlSdkVersion;
    private TextView txt_Currency;
    private TextView txt_CurrencySymbol;

    private MachineCtrl machineCtrl=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smmachineinfo);

        machineCtrl=MachineCtrl.getInstance();

        setNavTtile(this.getResources().getString(R.string.aty_smmachineinfo_navtitle));
        setNavGoBackBtnVisible(true);

        initView();
        initEvent();
        initData();
    }

    protected void initView() {
        txt_MerchantName = (TextView) findViewById(R.id.txt_MerchantName);
        txt_StoreName = (TextView) findViewById(R.id.txt_StoreName);
        txt_MachineId = (TextView) findViewById(R.id.txt_MachineId);
        txt_DeviceId=(TextView) findViewById(R.id.txt_DeviceId);
        txt_Location = (TextView) findViewById(R.id.txt_Location);
        txt_JPushRegId = (TextView) findViewById(R.id.txt_JPushRegId);
        txt_AppVersion= (TextView) findViewById(R.id.txt_AppVersion);
        txt_MachineCtrlSdkVersion=(TextView) findViewById(R.id.txt_MachineCtrlSdkVersion);
        txt_Currency= (TextView) findViewById(R.id.txt_Currency);
        txt_CurrencySymbol= (TextView) findViewById(R.id.txt_CurrencySymbol);
    }

    protected void initEvent() {

    }

    protected void initData() {

        MachineBean machine = AppCacheManager.getMachine();


        txt_MerchantName.setText(machine.getMerchName());
        txt_StoreName.setText(machine.getStoreName());
        txt_MachineId.setText(machine.getId());
        txt_DeviceId.setText(getAppContext().getDeviceId());
        txt_JPushRegId.setText(JPushInterface.getRegistrationID(getAppContext()));
        txt_AppVersion.setText(BuildConfig.VERSION_NAME);
        txt_MachineCtrlSdkVersion.setText(machineCtrl.vesion());
        txt_Currency.setText(machine.getCurrency());
        txt_CurrencySymbol.setText(machine.getCurrencySymbol());
//        Location location = LocationUtil.getInstance(SmMachineInfoActivity.this).showLocation();
//        if (location != null) {
//            String address = "纬度：" + location.getLatitude() + "经度：" + location.getLongitude();
//            LogUtil.d("FLY.LocationUtils", address);
//            txt_Location.setText(address);
//        }
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
