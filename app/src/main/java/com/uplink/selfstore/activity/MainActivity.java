package com.uplink.selfstore.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tamic.statinterface.stats.core.TcStatInterface;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.BannerAdapter;
import com.uplink.selfstore.deviceCtrl.ScanMidCtrl;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.ui.dialog.CustomNumKeyDialog;
import com.uplink.selfstore.ui.loopviewpager.AutoLoopViewPager;
import com.uplink.selfstore.ui.viewpagerindicator.CirclePageIndicator;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.LongClickUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

public class MainActivity extends BaseFragmentActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private RelativeLayout layout_header;
    private AutoLoopViewPager banner_pager;//banner 页面
    private CirclePageIndicator banner_indicator;//banner 底部小图标
    private ImageView img_logo;
    private ImageButton btn_buy;
    private ImageButton btn_pick;
    private CustomNumKeyDialog dialog_NumKey;

    private ScanMidCtrl scanMidCtrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanMidCtrl = ScanMidCtrl.getInstance();
        scanMidCtrl.connect();
        scanMidCtrl.setScanHandler(new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        Bundle bundle;
                        bundle = msg.getData();
                        String scanResult = bundle.getString("result");
                        if(scanResult!=null){
                            if(scanResult.contains("pickupcode")){
                                LogUtil.e("pickupcode:" + scanResult);
                                orderSearchByPickupCode(scanResult);
                            }
                        }
                        return false;
                    }
                })
        );

        initView();
        initEvent();
        initData();
        checkIsHasExHappen();

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
                LogUtil.e("pickupcode:" + number);
                orderSearchByPickupCode("pickupcode@v1:"+number);
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

        BannerAdapter banner_adapter = new BannerAdapter(getAppContext(), this.getGlobalDataSet().getBanners(), ImageView.ScaleType.FIT_XY);
        banner_pager.setAdapter(banner_adapter);
        banner_indicator.setViewPager(banner_pager);
    }

    @Override
    public void onClick(View v) {

        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.btn_buy:
                    TcStatInterface.onEvent("btn_buy", null);
                    Intent intent = new Intent(getAppContext(), ProductKindActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_pick:
                      //CrashReport.testJavaCrash();
//                    Intent cameraSnapService2 = new Intent();
//                    cameraSnapService2.setAction("android.intent.action.cameraSnapService");
//                    cameraSnapService2.putExtra("cameraId", 0);
//                    cameraSnapService2.putExtra("imgId", "dasd");
//                    sendBroadcast(cameraSnapService2);
                    TcStatInterface.onEvent("btn_pick", null);
                    dialog_NumKey.show();
                    break;
            }
        }
    }


    @Override
    public void onResume(){
        super.onResume();

        if(scanMidCtrl==null) {
            scanMidCtrl = ScanMidCtrl.getInstance();
        }

        scanMidCtrl.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        if(scanMidCtrl!=null){
            scanMidCtrl.disConnect();
            scanMidCtrl = null;
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        if(scanMidCtrl!=null){
            scanMidCtrl.disConnect();
            scanMidCtrl = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog_NumKey != null && dialog_NumKey.isShowing()) {
            dialog_NumKey.cancel();
        }

        if(scanMidCtrl!=null){
            scanMidCtrl.disConnect();
            scanMidCtrl = null;
        }
    }
}
