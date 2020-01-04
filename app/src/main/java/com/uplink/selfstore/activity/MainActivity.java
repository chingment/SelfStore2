package com.uplink.selfstore.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
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
import com.uplink.selfstore.http.HttpClient;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.SlotNRC;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.OrderDetailsBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.service.UpdateAppService;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.ui.CameraWindow;
import com.uplink.selfstore.ui.dialog.CustomDialogLoading;
import com.uplink.selfstore.ui.dialog.CustomNumKeyDialog;
import com.uplink.selfstore.ui.loopviewpager.AutoLoopViewPager;
import com.uplink.selfstore.ui.viewpagerindicator.CirclePageIndicator;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LocationUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.LongClickUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


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

    public LocationUtil locationUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationUtil = LocationUtil.getInstance(getAppContext());

//        String slot="n10r99c100m44";
//        SlotNRC nrc=SlotNRC.GetSlotNRC(slot);

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
        sendRunStatus("running");
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
                    TcStatInterface.onEvent("btn_buy", null);
                    Intent intent = new Intent(getAppContext(), ProductKindActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_pick:
//                    Intent intent3 = new Intent();
//                    intent3.setAction("android.intent.action.cameraSnapService");
//                    intent3.putExtra("cameraId", 0);
//                    intent3.putExtra("uniqueId", "value2");
//                    sendBroadcast(intent3);
//                    Map<String, String> params = new HashMap<>();
//                    params.put("machineId", "A");
//                    HttpClient.postFile("http://upload.17fanju.com/api/upload",params,null,null);
//                    try {
//                        OkHttpClient client=new OkHttpClient();
//
//                        /**
//                         * 上传文件格式
//                         */
////            File file=new File("");
////            RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), file);//将file转换成RequestBody文件
////            RequestBody requestBody=new MultipartBody.Builder()
////                    .addFormDataPart("name","filename",fileBody)
////                    .addFormDataPart("name","value")
////                    .build();
//
//                        RequestBody requestBody=new FormBody.Builder()
//                                .add("a1","value")
//                                .add("a2","value")
//                                .build();
//                        Request request=new Request.Builder()
//                                .url("http://upload.17fanju.com/api/upload")
//                                .post(requestBody)
//                                .build();
//                        Response response=client.newCall(request).execute();
//                        String responseBody=response.body().string();
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
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

            @Override
            public void onFailure(String msg, Exception e) {
                showToast(msg);
            }
        });
    }



//    public static void removeFileByTime(String dirPath) {
//        //获取目录下所有文件
//        List<File> allFile = getDirAllFile(new File(dirPath));
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//        //获取当前时间
//        Date end = new Date(System.currentTimeMillis());
//        try {
//            end = dateFormat.parse(dateFormat.format(new Date(System.currentTimeMillis())));
//        } catch (Exception e){
//            Log.d(TAG, "dataformat exeption e " + e.toString());
//        }
//        Log.d(TAG, "getNeedRemoveFile  dirPath = "  +dirPath);
//        for (File file : allFile) {//ComDef
//            try {
//                //文件时间减去当前时间
//                Date start = dateFormat.parse(dateFormat.format(new Date(file.lastModified())));
//                long diff = end.getTime() - start.getTime();//这样得到的差值是微秒级别
//                long days = diff / (1000 * 60 * 60 * 24);
//                if(ComDef.LOGMAXKEEPTIME <= days){
//                    deleteFile(file);
//                }
//
//            } catch (Exception e){
//                Log.d(TAG, "dataformat exeption e " + e.toString());
//            }
//        }
//    }

}
