package com.uplink.selfstore.activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tamic.statinterface.stats.core.TcStatInterface;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.BannerAdapter;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.OrderDetailsBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.ui.CameraWindow;
import com.uplink.selfstore.ui.dialog.CustomNumKeyDialog;
import com.uplink.selfstore.ui.loopviewpager.AutoLoopViewPager;
import com.uplink.selfstore.ui.viewpagerindicator.CirclePageIndicator;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.LongClickUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends BaseFragmentActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private RelativeLayout layout_header;
    private AutoLoopViewPager banner_pager;//banner 页面
    private CirclePageIndicator banner_indicator;//banner 底部小图标
    private ImageView img_logo;
    private ImageButton btn_buy;
    private ImageButton btn_pick;
    private CustomNumKeyDialog dialog_NumKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    }

    @Override
    protected void onStart() {
        super.onStart();
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
    protected void onDestroy() {
        super.onDestroy();
        if (dialog_NumKey != null && dialog_NumKey.isShowing()) {
            dialog_NumKey.cancel();
        }
    }

    private void search(String pickCode) {

        Map<String, String> params = new HashMap<>();

        params.put("machineId", this.getMachine().getId());
        params.put("pickupCode", pickCode);

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

            @Override
            public void onFailure(String msg, Exception e) {
                showToast(msg);
            }
        });
    }
}
