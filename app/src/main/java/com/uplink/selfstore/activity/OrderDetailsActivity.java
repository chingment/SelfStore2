package com.uplink.selfstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.OrderDetailsSkuAdapter;
import com.uplink.selfstore.activity.task.PickTask;
import com.uplink.selfstore.activity.task.blockTask.TaskPriority;
import com.uplink.selfstore.activity.task.blockTask.TaskScheduler;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.OrderDetailsBean;
import com.uplink.selfstore.model.api.OrderDetailsSkuBean;
import com.uplink.selfstore.model.api.OrderPickupStatusQueryResultBean;
import com.uplink.selfstore.model.api.PickupSkuBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.SlotBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.own.MachinePickupWorkManager;
import com.uplink.selfstore.ui.dialog.CustomConfirmDialog;
import com.uplink.selfstore.ui.my.MyListView;
import com.uplink.selfstore.ui.my.MyTimeTask;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.BitmapUtil;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.DateUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

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
    private OrderDetailsBean orderDetails;
    private MyTimeTask taskByGetPickupStatus;
    private MachinePickupWorkManager workManagerByPickup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderdetails);
        setNavTtile(this.getResources().getString(R.string.activity_orderdetails_navtitle));

        orderDetails = (OrderDetailsBean) getIntent().getSerializableExtra("dataBean");


        initView();
        initEvent();

        setView(orderDetails);

//        workManagerByPickup=new MachinePickupWorkManager();
//        workManagerByPickup.run(orderDetails.getProductSkus(),new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//
//
//                PickupSkuBean sku=(PickupSkuBean)msg.obj;
//                LogUtil.d("取货流程消息通知:" + sku.getName()+","+sku.getMainImgUrl());
//                CommonUtil.loadImageFromUrl(OrderDetailsActivity.this, curpickupsku_img_main, sku.getMainImgUrl());
//                curpickupsku_tip1.setText(sku.getName()+",正在出货中");
//                curpickupsku_tip2.setText("");
//
//
//            }
//        });

       final Handler hd=  new Handler() {
            @Override
            public void handleMessage(Message msg) {


                PickupSkuBean sku = (PickupSkuBean) msg.obj;
                LogUtil.d("取货流程消息通知:" + sku.getName() + "," + sku.getMainImgUrl());
                CommonUtil.loadImageFromUrl(OrderDetailsActivity.this, curpickupsku_img_main, sku.getMainImgUrl());
                curpickupsku_tip1.setText(sku.getName() + ",正在出货中");
                curpickupsku_tip2.setText("");


            }
        };

        Runnable runnable = new Runnable() {
            public void run() {

                List<PickupSkuBean> pickUps=getAllPickupProductSkus(orderDetails.getProductSkus());

                for(int i=0;i<pickUps.size();i++) {
                    PickTask pickTask=new PickTask(pickUps.get(i));
                    pickTask.setPriority(TaskPriority.DEFAULT); //设置优先级，默认是DEFAULT
                    pickTask.setHandlerMsg(hd);
                    TaskScheduler.getInstance().enqueue(pickTask);
                }
            }
        };
        Thread currentThread = new Thread(runnable);
        currentThread.start();

        taskByGetPickupStatus =new MyTimeTask(1000, new TimerTask() {
            @Override
            public void run() {
                getPickupStatus();
            }
        });
        //taskByGetPickupStatus.start();
    }

    private List<PickupSkuBean> getAllPickupProductSkus(List<OrderDetailsSkuBean> skus) {

        List<PickupSkuBean> p1 = new ArrayList<>();

        for (int i = 0; i < skus.size(); i++) {
            OrderDetailsSkuBean sku = skus.get(i);
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

                p1.add(pickSku);
            }
        }

        return p1;
    }


    protected void initView() {

        txt_OrderSn = (TextView) findViewById(R.id.txt_OrderSn);
        btn_PickupCompeled = (View) findViewById(R.id.btn_PickupCompeled);
        btn_ContactKefu = (View) findViewById(R.id.btn_ContactKefu);
        list_skus = (MyListView) findViewById(R.id.list_skus);
        list_skus.setFocusable(false);
        list_skus.setClickable(false);
        list_skus.setPressed(false);
        list_skus.setEnabled(false);

        dialog_PickupCompelte = new CustomConfirmDialog(OrderDetailsActivity.this, getAppContext().getString(R.string.activity_orderdetails_tips_outpickup_confirm), true);
        dialog_PickupCompelte.getTipsImage().setImageDrawable(getResources().getDrawable((R.drawable.dialog_icon_warn)));
        dialog_PickupCompelte.getBtnSure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_PickupCompelte.dismiss();
                if(taskByGetPickupStatus!=null)
                {
                    taskByGetPickupStatus.stop();
                }

                if(workManagerByPickup!=null)
                {
                    workManagerByPickup.stop();
                }
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
        dialog_ContactKefu.getBtnArea().setVisibility(View.GONE);

        curpickupsku_img_main = (ImageView) findViewById(R.id.curpickupsku_img_main);
        curpickupsku_tip1 = (TextView) findViewById(R.id.curpickupsku_tip1);
        curpickupsku_tip2 = (TextView) findViewById(R.id.curpickupsku_tip2);
    }

    private void initEvent() {
        btn_PickupCompeled.setOnClickListener(this);
        btn_ContactKefu.setOnClickListener(this);
    }

    public void setView(OrderDetailsBean bean) {

        MachineBean machine = AppCacheManager.getMachine();

        if(bean==null) {
            LogUtil.i("bean为空");
            return;
        }

        txt_OrderSn.setText(bean.getOrderSn()+"");

        if(machine.getCsrQrCode()!=null) {
            dialog_ContactKefu.getTipsImage().setImageBitmap(BitmapUtil.createQrCodeBitmap(machine.getCsrQrCode()));
        }

        OrderDetailsSkuAdapter cartSkuAdapter = new OrderDetailsSkuAdapter(OrderDetailsActivity.this, bean.getProductSkus());
        list_skus.setAdapter(cartSkuAdapter);
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

    public void getPickupStatus() {

        Map<String, String> params = new HashMap<>();

        MachineBean machine = AppCacheManager.getMachine();

        params.put("machineId", machine.getId());
        params.put("orderId", orderDetails.getOrderId());


        getByMy(Config.URL.order_PickupStatusQuery, params, false,"", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<OrderPickupStatusQueryResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<OrderPickupStatusQueryResultBean>>() {
                });


                if (rt.getResult() == Result.SUCCESS) {

                    OrderPickupStatusQueryResultBean data=rt.getData();

                    OrderDetailsSkuAdapter cartSkuAdapter = new OrderDetailsSkuAdapter(OrderDetailsActivity.this, data.getProductSkus());
                    list_skus.setAdapter(cartSkuAdapter);
                    //workManagerByPickup.reSetAllPickupProductSkus(data.getProductSkus());
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

        if(taskByGetPickupStatus!=null)
        {
            taskByGetPickupStatus.stop();
        }

        if(workManagerByPickup!=null)
        {
            workManagerByPickup.stop();
        }
    }
}
