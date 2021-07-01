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

public class SmDeviceInfoActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "SmDeviceInfoActivity";
    private TextView txt_MerchantName;
    private TextView txt_StoreName;
    private TextView txt_ShopName;
    private TextView txt_ShopAddress;
    private TextView txt_DeviceId;
    private TextView txt_Location;
    private TextView txt_JPushRegId;
    private TextView txt_AppVersion;
    private TextView txt_CabinetCtrlSdkVersionByDS;
    private TextView txt_Currency;
    private TextView txt_CurrencySymbol;
    private TextView txt_ComName;
    private CabinetCtrlByDS cabinetCtrlByDS=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smdeviceinfo);

        cabinetCtrlByDS=CabinetCtrlByDS.getInstance();

        setNavTtile(this.getResources().getString(R.string.aty_smdeviceinfo_navtitle));
        setNavGoBackBtnVisible(true);

        initView();
        initEvent();
        initData();
    }

    private void initView() {
        txt_MerchantName = (TextView) findViewById(R.id.txt_MerchantName);
        txt_StoreName = (TextView) findViewById(R.id.txt_StoreName);
        txt_ShopName = (TextView) findViewById(R.id.txt_ShopName);
        txt_ShopAddress = (TextView) findViewById(R.id.txt_ShopAddress);
        txt_DeviceId = (TextView) findViewById(R.id.txt_DeviceId);
        txt_Location = (TextView) findViewById(R.id.txt_Location);
        txt_JPushRegId = (TextView) findViewById(R.id.txt_JPushRegId);
        txt_AppVersion = (TextView) findViewById(R.id.txt_AppVersion);
        txt_CabinetCtrlSdkVersionByDS = (TextView) findViewById(R.id.txt_CabinetCtrlSdkVersionByDS);
        txt_Currency = (TextView) findViewById(R.id.txt_Currency);
        txt_CurrencySymbol = (TextView) findViewById(R.id.txt_CurrencySymbol);
        txt_ComName = (TextView) findViewById(R.id.txt_ComName);
    }

    private void initEvent() {

    }

    private void initData() {

        txt_MerchantName.setText(getDevice().getMerchName());
        txt_StoreName.setText(getDevice().getStoreName());
        txt_ShopName.setText(getDevice().getShopName());
        txt_ShopAddress.setText(getDevice().getShopAddress());
        txt_DeviceId.setText(getAppContext().getDeviceId());
        txt_JPushRegId.setText("");
        txt_AppVersion.setText(BuildConfig.VERSION_NAME);
        txt_CabinetCtrlSdkVersionByDS.setText(cabinetCtrlByDS.vesion());
        txt_Currency.setText("");
        txt_CurrencySymbol.setText("");
        txt_Location.setText(LocationUtil.LAT+","+ LocationUtil.LNG);
        txt_ComName.setText(BuildConfig.COMNAME);
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
