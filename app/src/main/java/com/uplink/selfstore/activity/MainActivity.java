package com.uplink.selfstore.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.BannerAdapter;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.SlotNRC;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.OrderDetailsBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.ui.dialog.CustomNumKeyDialog;
import com.uplink.selfstore.ui.loopviewpager.AutoLoopViewPager;
import com.uplink.selfstore.ui.viewpagerindicator.CirclePageIndicator;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.LongClickUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseFragmentActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private RelativeLayout layout_header;
    private BannerAdapter banner_adapter;//banner数据配置
    private AutoLoopViewPager banner_pager;//banner 页面
    private CirclePageIndicator banner_indicator;//banner 底部小图标
    private ImageView img_logo;
    private ImageButton btn_buy;
    private ImageButton btn_pick;
    private CustomNumKeyDialog dialog_NumKey;

    private LocationManager mLocationManager;//位置管理器

    private static final int LOCATION_CODE = 1;
    private String locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GetSlotNRC("n12r34c56");
//        //byte frameHand=(byte)0x24;
//        byte[] data_3= new byte[] {(byte) 0x1};
//        byte[] buffer=Pack((byte)0x82,data_3);
//
//        dataAnalysis(buffer,buffer.length);

        initView();
        initEvent();
        initData();

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);

        for (String provider : providers) {
            LogUtil.i(TAG, "loaction provider:" + provider);
        }

        Location location = null;


        if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            LogUtil.i(TAG, "lGPS模块正常");

        }


        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
            LogUtil.i(TAG, "loaction check provider:" + locationProvider);
            location = locationManager.getLastKnownLocation(locationProvider);
        }


        if (!providers.contains(LocationManager.NETWORK_PROVIDER) && !providers.contains(LocationManager.GPS_PROVIDER)) {
            LogUtil.d(TAG, "loaction check provider: 没有可用的位置提供器");
            return;
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        location = locationManager.getLastKnownLocation(locationProvider);

        updateWithNewLocation(location);

        locationManager.requestLocationUpdates(locationProvider, 2000, 10, locationListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
        sendRunStatus("running");
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
        }

        public void onProviderDisabled(String provider) {
            updateWithNewLocation(null);
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private void updateWithNewLocation(Location location) {
        if (location == null) {
            LogUtil.i("地理位置:location == null");
        } else {
            LogUtil.i("地理位置:" + location.getLatitude() + "，" + location.getLongitude());
        }
    }


    protected void initView() {
        layout_header = (RelativeLayout) findViewById(R.id.layout_header);
        banner_pager = (AutoLoopViewPager) findViewById(R.id.banner_pager);
        banner_indicator = (CirclePageIndicator) findViewById(R.id.banner_indicator);
        img_logo = (ImageView) findViewById(R.id.img_logo);
        btn_buy = (ImageButton) findViewById(R.id.btn_buy);
        btn_pick = (ImageButton) findViewById(R.id.btn_pick);
        banner_pager.setFocusable(true);
        banner_pager.setFocusableInTouchMode(true);
        banner_pager.requestFocus();
        banner_pager.setInterval(5000);


        dialog_NumKey = new CustomNumKeyDialog(MainActivity.this);
    }

    private void initEvent() {

        btn_buy.setOnClickListener(this);
        btn_pick.setOnClickListener(this);

        LongClickUtil.setLongClick(new Handler(), layout_header, 500, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LogUtil.e("长按触发");
                Intent intent = new Intent(getAppContext(), SmLoginActivity.class);
                startActivity(intent);
                return true;
            }
        });

        dialog_NumKey.setOnSureListener(new CustomNumKeyDialog.OnSureListener() {
            @Override
            public void onClick(View v, String number) {
                LogUtil.e("number:" + number);
                search(number);
            }
        });
    }

    private void initData() {

        loadLogo();
        loadBanner();

    }

    public void loadLogo() {


        CommonUtil.loadImageFromUrl(getAppContext(), img_logo, this.getGlobalDataSet().getMachine().getLogoImgUrl());
    }

    public void loadBanner() {

        banner_adapter = new BannerAdapter(getAppContext(), this.getGlobalDataSet().getBanners(), ImageView.ScaleType.FIT_XY);
        banner_pager.setAdapter(banner_adapter);
        banner_indicator.setViewPager(banner_pager);
    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.btn_buy:
                    Intent intent = new Intent(getAppContext(), ProductKindActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_pick:
                    dialog_NumKey.show();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog_NumKey != null && dialog_NumKey.isShowing()) {
            dialog_NumKey.cancel();
        }
    }

    private void search(String pickCode) {

        Map<String, String> params = new HashMap<>();

        MachineBean machine = AppCacheManager.getMachine();

        params.put("machineId", machine.getId());
        params.put("pickCode", pickCode);

        getByMy(Config.URL.order_Search, params, true, "正在寻找订单", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<OrderDetailsBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<OrderDetailsBean>>() {
                });


                if (rt.getResult() == Result.SUCCESS) {
                    OrderDetailsBean d = rt.getData();

                    Intent intent = new Intent(MainActivity.this, OrderDetailsActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("dataBean", d);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();

                } else {
                    showToast(rt.getMessage());
                }
            }
        });
    }


    private void dataAnalysis(byte[] buffer, int length) {
        //处理读逻辑
        Log.d(TAG, "正在处理读逻辑");
        boolean reading = true;
        byte frameHand = (byte) 0x24;
        boolean framePack = true;
        int dataLength = -1;
        byte xorAns = (byte) 0xff;
        byte[] frameEndPack = {(byte) 0x0D, (byte) 0x0A};
        int frameEndLength = 2;
        for (int i = 0; i < length; i++) {

            if ((buffer[i] == (byte) frameHand) && (framePack)) {
                Log.d(TAG, "开始解包！！！出现帧头");
                framePack = false;
                dataLength = -1;
                xorAns = (byte) 0xff;
                continue;
            }

            if ((dataLength == -1) && (!framePack)) {
                dataLength = buffer[i];
                xorAns = (byte) dataLength;

                if (dataLength >= 2) {
                    dataLength--;
                } else {
                    Log.d(TAG, "帧长度错误！！！采用丢包策略！！！");
                    framePack = true;
                    dataLength = -1;
                    xorAns = (byte) 0xff;
                    return;
                }
                Log.d(TAG, "帧长度正常！！！继续解包！！！");
                continue;
            }

            if (i + dataLength + frameEndLength <= length) {
                byte cmd = (byte) buffer[i];
                xorAns = (byte) (xorAns ^ cmd);
                dataLength--;
                i++;
                if (dataLength > 0) {
                    byte[] data = new byte[dataLength];
                    int cnt = 0;

                    while (dataLength > 0) {
                        data[cnt] = buffer[i];
                        xorAns = (byte) (xorAns ^ buffer[i]);
                        dataLength--;
                        i++;
                        cnt++;
                    }

                    if (xorAns != buffer[i]) {
                        Log.d(TAG, "dataLeng>1,xor错误，导致解包失败！！！");
                        framePack = true;
                        dataLength = -1;
                        xorAns = (byte) 0xff;
                        continue;
                    } else {
                        i++;
                        if ((buffer[i] != frameEndPack[0]) || (buffer[i + 1] != frameEndPack[1])) {
                            Log.d(TAG, "dataLeng>1,没有帧尾，导致解包失败！！！");
                            framePack = true;
                            dataLength = -1;
                            xorAns = (byte) 0xff;
                            continue;
                        } else {
                            Log.d(TAG, "unPack(cmd,buffer); 成功解包！！！");
                            //解包
                            //UnPack(cmd,data);

                            //计算新的包;
                            i++;
                            framePack = true;
                            dataLength = -1;
                            xorAns = (byte) 0xff;
                            continue;
                        }
                    }

                } else if (dataLength == 0) {
                    if (xorAns != buffer[i]) {
                        Log.d(TAG, "dataLeng==1,xor错误，导致解包失败！！！");
                        framePack = true;
                        dataLength = -1;
                        xorAns = (byte) 0xff;
                        continue;
                    } else {
                        i++;
                        if ((buffer[i] != frameEndPack[0]) || (buffer[i + 1] != frameEndPack[1])) {
                            Log.d(TAG, "dataLeng==1,没有帧尾，导致解包失败！！！");
                            framePack = true;
                            dataLength = -1;
                            xorAns = (byte) 0xff;
                            return;
                        } else {
                            Log.d(TAG, "unPack(cmd); 成功解包！！！");
                            //解包
                            //UnPack(cmd);
                            //计算新的包;
                            i++;
                            framePack = true;
                            dataLength = -1;
                            xorAns = (byte) 0xff;
                            continue;
                        }
                    }
                }
            } else {
                Log.d(TAG, "现在的偏移量+包长+帧尾 > buffer长度，导致解包错，采用丢包策略！！！");
                framePack = true;
                dataLength = -1;
                xorAns = (byte) 0xff;
                continue;
            }
        }
        //读逻辑处理完
        Log.d(TAG, "读逻辑处理完");
        reading = false;
    }

    private byte [] Pack(byte cmd,byte [] data){
        byte frameHand = (byte) 0x24;
        byte[] frameEndPack = {(byte) 0x0D, (byte) 0x0A};
        byte length = (byte) (2 +data.length);
        int packLength = 1 + 1 +  1 + data.length + 1 + frameEndPack.length;
        byte xorAns = length;
        xorAns = xorAns = (byte) (xorAns ^ cmd);
        byte packData [] = new byte[packLength];

        packData[0] = frameHand;
        packData[1] = length;
        packData[2] = cmd;

        for (int i=0;i<data.length;i++){
            packData[3+i] = data[i];
            xorAns = (byte)(xorAns ^ packData[3+i]);
        }

        packData[3 + data.length] = xorAns;
        packData[4 + data.length] = frameEndPack[0];
        packData[5 + data.length] = frameEndPack[1];

        return packData;
    }

}
