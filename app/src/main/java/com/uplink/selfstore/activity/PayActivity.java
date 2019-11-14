package com.uplink.selfstore.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.uplink.selfstore.R;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.OrderPayStatusQueryResultBean;
import com.uplink.selfstore.model.api.OrderPayUrlBuildResultBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.my.MyTimeTask;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class PayActivity extends SwipeBackActivity implements View.OnClickListener {

    private ImageView img_payqrcode;
    private TextView txt_payseconds;
    private TextView btn_gohome;
    private TextView btn_goback;
    private TextView txt_payamount;
    private OrderPayUrlBuildResultBean orderPayUrlBuildResult;

    private int seconds = 120;

    private MyTimeTask taskByCheckPayStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        setNavTtile(this.getResources().getString(R.string.activity_pay_navtitle));
        orderPayUrlBuildResult = (OrderPayUrlBuildResultBean) getIntent().getSerializableExtra("dataBean");
        initView();
        initEvent();
        initData();
    }

    protected void initView() {

        img_payqrcode = (ImageView) findViewById(R.id.img_payqrcode);
        txt_payseconds = (TextView) findViewById(R.id.txt_payseconds);
        txt_payamount = (TextView) findViewById(R.id.txt_payamount);
        btn_gohome = (TextView) findViewById(R.id.btn_gohome);
        btn_goback = (TextView) findViewById(R.id.btn_goback);

        taskByCheckPayStatus =new MyTimeTask(1000, new TimerTask() {
            @Override
            public void run() {
                //LogUtil.i("查询支付状态");
                orderPayStatusQuery();
                //mHandler.sendEmptyMessage(TIMER);
                //或者发广播，启动服务都是可以的

            }
        });
        taskByCheckPayStatus.start();
    }

    private void initEvent() {
        btn_goback.setOnClickListener(this);
        btn_gohome.setOnClickListener(this);
    }

    private void initData() {

        if (orderPayUrlBuildResult != null) {
            Bitmap bitmap = createBitmap(orderPayUrlBuildResult.getPayUrl());
            img_payqrcode.setImageBitmap(bitmap);


            CountDownTimer timer = new CountDownTimer(seconds * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    //LogUtil.e(String.valueOf(millisUntilFinished));
                    long s = (millisUntilFinished / 1000);
                    txt_payseconds.setText(String.valueOf(s) + "'");
                }

                @Override
                public void onFinish() {
                    txt_payseconds.setText("支付超时，请返回重新下单");
                    orderCancle(orderPayUrlBuildResult.getOrderId(),"超过指定支付时间，系统自动取消订单");
                    finish();
                }
            }.start();
        }
    }




    public void orderPayStatusQuery() {

        Map<String, String> params = new HashMap<>();

        MachineBean machine = AppCacheManager.getMachine();

        params.put("machineId", machine.getId());
        params.put("orderId", orderPayUrlBuildResult.getOrderId());


        getByMy(Config.URL.order_PayStatusQuery, params, false,"", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<OrderPayStatusQueryResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<OrderPayStatusQueryResultBean>>() {
                });


                if (rt.getResult() == Result.SUCCESS) {
                    OrderPayStatusQueryResultBean d = rt.getData();
                    //4 为 已完成支付
                    if (d.getStatus() == 3000) {
                        taskByCheckPayStatus.stop();

                        Intent intent= new Intent(PayActivity.this, OrderDetailsActivity.class);
                        Bundle bundle=new Bundle();
                        bundle.putSerializable("dataBean", d.getOrderDetails());
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(taskByCheckPayStatus!=null) {
            taskByCheckPayStatus.stop();
        }
    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.btn_goback:
                    finish();
                    break;
                case R.id.btn_gohome:
                    orderCancle(orderPayUrlBuildResult.getOrderId(),"返回主页，取消订单");
                    Intent l_Intent = new Intent(PayActivity.this, MainActivity.class);
                    startActivity(l_Intent);
                    finish();
                    break;
            }
        }
    }

    public static Bitmap createBitmap(String str) {
        Bitmap bitmap = null;
        BitMatrix result = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            DisplayMetrics dm = new DisplayMetrics();
            int width = dm.widthPixels - 100;
            result = multiFormatWriter.encode(str, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(result);
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException iae) { // ?
            return null;
        }
        return bitmap;
    }

}
