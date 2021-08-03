package com.uplink.selfstore.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.AdContentAdapter;
import com.uplink.selfstore.activity.adapter.BannerAdapter;
import com.uplink.selfstore.model.DSCabSlotNRC;
import com.uplink.selfstore.model.api.AdBean;
import com.uplink.selfstore.model.api.AdContentBean;
import com.uplink.selfstore.taskexecutor.onebyone.BaseSyncTask;
import com.uplink.selfstore.taskexecutor.onebyone.TinySyncExecutor;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.ui.CameraWindow;
import com.uplink.selfstore.ui.dialog.CustomDialogNumKey;
import com.uplink.selfstore.ui.loopviewpager.AutoLoopViewPager;
import com.uplink.selfstore.ui.viewpagerindicator.CirclePageIndicator;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.LongClickUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;
//import com.uplink.selfstore.utils.ScanKeyManager;
import com.uplink.selfstore.utils.StringUtil;
import com.uplink.selfstore.utils.runtimepermissions.PermissionsManager;
import com.uplink.selfstore.utils.runtimepermissions.PermissionsResultAction;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseFragmentActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private RelativeLayout layout_header;
    private AutoLoopViewPager banner_pager;//banner 页面
    private CirclePageIndicator banner_indicator;//banner 底部小图标
    private ImageView img_logo;
    private ImageButton btn_buy;
    private ImageButton btn_pick;
    private CustomDialogNumKey dialog_NumKey;

    //private ScanKeyManager scanKeyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //DSCabSlotNRC dsCabSlotNRC = DSCabSlotNRC.GetSlotNRC(pickupSku.getCabinetId(), pickupSku.getSlotId());

        setHideStatusBar(true);
        //setScannerCtrl(MainActivity.this);

        initView();
        initEvent();
        initData();

        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 1);
            } else {
                //TODO 做你需要的事情
                CameraWindow.show(this);
            }
        }
        else {
            CameraWindow.show(this);
        }

        /**
         * 请求所有必要的权限----原理就是获取清单文件中申请的权限
         */
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
//              Toast.makeText(MainActivity.this, "All permissions have been granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDenied(String permission) {
                //Toast.makeText(MainActivity.this, "Permission " + permission + " has been denied", Toast.LENGTH_SHORT).show();
            }
        });

        showDeviceId();

        //拦截扫码器回调,获取扫码内容
//        scanKeyManager = new ScanKeyManager(new ScanKeyManager.OnScanValueListener() {
//            @Override
//            public void onScanValue(String value) {
//                LogUtil.e("ScanValue", value);
//            }
//        });
    }

    @Override
    public void onResume(){
        super.onResume();
        checkIsHasExHappen();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog_NumKey != null) {
            dialog_NumKey.cancel();
        }
    }

    private void initView() {
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

        dialog_NumKey = new CustomDialogNumKey(MainActivity.this);

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

        dialog_NumKey.setOnSureListener(new CustomDialogNumKey.OnSureListener() {
            @Override
            public void onClick(View v, String number) {
                LogUtil.e("pickupcode:" + number);
                orderSearchByPickupCode(MainActivity.this, "pickupcode@v1:"+number);
            }
        });

    }

    private void initData() {
        loadAds(this.getCustomDataByVending().getAds());
    }

    public void loadAds(HashMap<String, AdBean> ads) {

        if(ads==null)
            return;

        //100 是首页中部轮播广告
        if(ads.containsKey("100")) {
            AdBean ad = ads.get("100");
            if(ad!=null) {
                List<AdContentBean> ad_Contents = ad.getContents();
                if (ad_Contents != null) {
                    AdContentAdapter adContent_adapter = new AdContentAdapter(getAppContext(), ad_Contents, ImageView.ScaleType.FIT_XY);
                    banner_pager.setAdapter(adContent_adapter);
                    banner_indicator.setViewPager(banner_pager);
                }
            }
        }

        //101 是首页头部LOGO
        if(ads.containsKey("101")){
            AdBean ad = ads.get("101");
            if(ad!=null) {
                List<AdContentBean> ad_Contents = ad.getContents();
                if (ad_Contents != null&&ad_Contents.size()>0) {
                    CommonUtil.loadImageFromUrl(getAppContext(), img_logo, ad_Contents.get(0).getDataUrl());
                }
                else {
                    //img_logo.setImageDrawable(0);
                    Bitmap bitmap = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.logo_empty);
                    img_logo.setImageBitmap(bitmap);
                }
            }
        }


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

//    /*监听键盘事件,除了返回事件都将它拦截,使用我们自定义的拦截器处理该事件*/
//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//
//        if (event.getKeyCode() != KeyEvent.KEYCODE_BACK) {
//            scanKeyManager.analysisKeyEvent(event);
//            return true;
//        }
//        return super.dispatchKeyEvent(event);
//    }

}
