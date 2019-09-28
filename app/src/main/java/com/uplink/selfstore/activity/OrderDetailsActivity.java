package com.uplink.selfstore.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
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
import com.uplink.selfstore.activity.adapter.OrderDetailsSkuAdapter;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.OrderDetailsBean;
import com.uplink.selfstore.model.api.OrderDetailsSkuBean;
import com.uplink.selfstore.model.api.OrderPayStatusQueryResultBean;
import com.uplink.selfstore.model.api.PickupSkuBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.SlotBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.own.MachinePickupWorkManager;
import com.uplink.selfstore.ui.dialog.CustomConfirmDialog;
import com.uplink.selfstore.ui.my.MyListView;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.BitmapUtil;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.DateUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDetailsActivity extends SwipeBackActivity implements View.OnClickListener {

    private TextView txt_OrderSn;
    private MyListView list_skus;


    private View btn_PickupCompeled;
    private View btn_ContactKefu;

    private CustomConfirmDialog dialog_PickupCompelte;
    private CustomConfirmDialog dialog_ContactKefu;
    private  ImageView curpickupsku_img_main;
    private TextView curpickupsku_tip1;
    private TextView curpickupsku_tip2;

   // private PickupSkuBean curPickupSku = null;
    private List<PickupSkuBean> pickupSkus = new ArrayList<>();

   // private Handler taskByPickup_HandlerMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderdetails);
        setNavTtile(this.getResources().getString(R.string.activity_orderdetails_navtitle));

        OrderDetailsBean orderDetails = (OrderDetailsBean) getIntent().getSerializableExtra("dataBean");


        initView();
        initEvent();

        setView(orderDetails);


        for (int i = 0; i < orderDetails.getSkus().size(); i++) {
            OrderDetailsSkuBean sku = orderDetails.getSkus().get(i);
            List<SlotBean> slots = sku.getSlots();
            for (int j = 0; j < slots.size(); j++) {
                SlotBean slot = slots.get(j);
                PickupSkuBean pickSku = new PickupSkuBean();

                pickSku.setId(sku.getId());
                pickSku.setName(sku.getName());
                pickSku.setMainImgUrl(sku.getMainImgUrl());
                pickSku.setSlotId(slot.getSlotId());
                pickSku.setUniqueId(slot.getUniqueId());
                pickSku.setStatus(slot.getStatus());

                pickupSkus.add(pickSku);
            }
        }


//        taskByPickup_HandlerMsg = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                PickupSkuBean sku=(PickupSkuBean)msg.obj;
//
//                LogUtil.d("取货流程消息通知:" + sku.getName());
//                CommonUtil.loadImageFromUrl(OrderDetailsActivity.this, curpickupsku_img_main, sku.getMainImgUrl());
//                curpickupsku_tip1.setText(sku.getName()+",正在出货中");
//                curpickupsku_tip2.setText("");
//            }
//        };

        MachinePickupWorkManager  workManager=new MachinePickupWorkManager();
        workManager.run(pickupSkus,new Handler() {
            @Override
            public void handleMessage(Message msg) {
                PickupSkuBean sku=(PickupSkuBean)msg.obj;
                LogUtil.d("取货流程消息通知:" + sku.getName());
                CommonUtil.loadImageFromUrl(OrderDetailsActivity.this, curpickupsku_img_main, sku.getMainImgUrl());
                curpickupsku_tip1.setText(sku.getName()+",正在出货中");
                curpickupsku_tip2.setText("");
            }
        });
//
//
//        Runnable runnable = new Runnable() {
//            public void run() {
//                while (true) {
//                    System.out.println("execute task");
//                    try {
//
//                        if (curPickupSku == null) {
//                            for (int i = 0; i < pickupSkus.size(); i++) {
//                                if (pickupSkus.get(i).getStatus() == 2000) {
//                                    pickupSkus.get(i).setStatus(2001);
//                                    pickupSkus.get(i).setStartTime(DateUtil.getNowDate());
//                                    curPickupSku = pickupSkus.get(i);
//                                    setTips(0x0001,curPickupSku);
//                                    break;
//                                }
//                            }
//                        } else {
//                            LogUtil.d("取货流程:" + curPickupSku.getName() + ",正在取货中");
//                            Calendar c = Calendar.getInstance();
//                            c.setTime(curPickupSku.getStartTime());
//                            c.add(Calendar.MINUTE, 1);
//
//                            Date pickupTimeout = c.getTime();
//
//                            if(pickupTimeout.getTime()<DateUtil.getNowDate().getTime()) {
//                                curPickupSku=null;
//                            }
//                        }
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        };
//        Thread thread = new Thread(runnable);
//        thread.start();
    }

//    public void setTips(int what, PickupSkuBean msg) {
//        final Message m = new Message();
//        m.what = what;
//        m.obj = msg;
//        taskByPickup_HandlerMsg.sendMessage(m);
//    }

    private void initView() {

        txt_OrderSn = (TextView) findViewById(R.id.txt_OrderSn);
        btn_PickupCompeled = (View) findViewById(R.id.btn_PickupCompeled);
        btn_ContactKefu = (View) findViewById(R.id.btn_ContactKefu);
        list_skus = (MyListView) findViewById(R.id.list_skus);
        list_skus.setFocusable(false);
        list_skus.setClickable(false);
        list_skus.setPressed(false);
        list_skus.setEnabled(false);

        dialog_PickupCompelte = new CustomConfirmDialog(OrderDetailsActivity.this, getAppContext().getString(R.string.activity_cart_tips_payclose_confirm), true);
        dialog_PickupCompelte.getBtnSure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_PickupCompelte.dismiss();
                Intent intent = new Intent(getAppContext(), ProductKindActivity.class);
                startActivity(intent);
            }
        });
        dialog_PickupCompelte.getBtnCancle().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_PickupCompelte.dismiss();
            }
        });

        dialog_ContactKefu = new CustomConfirmDialog(OrderDetailsActivity.this, getAppContext().getString(R.string.activity_orderdetails_contactkefu_tips), false);
        dialog_ContactKefu.getBtnSure().setVisibility(View.GONE);
        dialog_ContactKefu.getBtnCancle().setVisibility(View.GONE);

        curpickupsku_img_main = (ImageView) findViewById(R.id.curpickupsku_img_main);
        curpickupsku_tip1 = (TextView) findViewById(R.id.curpickupsku_tip1);
        curpickupsku_tip2 = (TextView) findViewById(R.id.curpickupsku_tip2);
    }

    private void initEvent() {
        btn_PickupCompeled.setOnClickListener(this);
        btn_ContactKefu.setOnClickListener(this);
    }

    public void setView(OrderDetailsBean bean) {

        if(bean==null) {
            LogUtil.i("bean为空");
            return;
        }

        txt_OrderSn.setText(bean.getSn()+"");

        if(bean.getCsrQrCode()!=null) {
            dialog_ContactKefu.getTipsImage().setImageBitmap(BitmapUtil.createQrCodeBitmap(bean.getCsrQrCode()));
        }

        OrderDetailsSkuAdapter cartSkuAdapter = new OrderDetailsSkuAdapter(OrderDetailsActivity.this, bean.getSkus());
        list_skus.setAdapter(cartSkuAdapter);
        list_skus.setVisibility(View.VISIBLE);
    }

    private void loadData(String orderId) {


        Map<String, String> params = new HashMap<>();

        MachineBean machine = AppCacheManager.getMachine();

        params.put("machineId", machine.getId());
        params.put("orderId", orderId);


        getByMy(Config.URL.order_Details, params, false,"", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<OrderDetailsBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<OrderDetailsBean>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {
                    setView(rt.getData());
                }
            }

        });

    }


    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.btn_PickupCompeled:
                    dialog_PickupCompelte.show();
                    break;
                case R.id.btn_ContactKefu:
                    dialog_ContactKefu.show();
                    break;
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog_PickupCompelte != null && dialog_PickupCompelte.isShowing()) {
            dialog_PickupCompelte.cancel();
        }

        if (dialog_ContactKefu != null && dialog_ContactKefu.isShowing()) {
            dialog_ContactKefu.cancel();
        }
    }
}
