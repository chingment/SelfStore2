package com.uplink.selfstore.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.OrderDetailsSkuAdapter;
import com.uplink.selfstore.activity.task.PickTask;
import com.uplink.selfstore.activity.task.blockTask.TaskScheduler;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.machineCtrl.DeShangMidCtrl;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.OrderDetailsBean;
import com.uplink.selfstore.model.api.OrderDetailsSkuBean;
import com.uplink.selfstore.model.api.OrderPickupStatusQueryResultBean;
import com.uplink.selfstore.model.api.PickupSkuBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.PickupSlotBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.dialog.CustomConfirmDialog;
import com.uplink.selfstore.ui.my.MyListView;
import com.uplink.selfstore.ui.my.MyTimeTask;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.BitmapUtil;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import org.apache.commons.logging.Log;

import java.util.ArrayList;
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
    private MyTimeTask taskByPickup;
    private DeShangMidCtrl midCtrl;
    private Handler midCtrlHandler;
    private PickupSkuBean currentPickupSku=null;
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderdetails);
        setNavTtile(this.getResources().getString(R.string.activity_orderdetails_navtitle));

        orderDetails = (OrderDetailsBean) getIntent().getSerializableExtra("dataBean");

        initView();
        initEvent();
        initData();

        // 3010 待取货 3011 已发送取货命令 3012 取货中 4000 已完成 6000 异常
        // pickupQueryStatus("ba0ebe970a2840adaf0b5e59c9522317");
        // pickupEventNotify("ba0ebe970a2840adaf0b5e59c9522317",3011,"已发送取货命令");
        //setProductSkuPickupSuccess("a9565c8c71aa42b49bc263c143b9574c","n1r8c5");
        //setProductSkuPickupSuccess("a9565c8c71aa42b49bc263c143b9574c","n1r8c5");

        midCtrlHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case 0x0001:
                        LogUtil.d("取货流程消息通知:" + currentPickupSku.getName() + ",准备就绪");
                        CommonUtil.loadImageFromUrl(OrderDetailsActivity.this, curpickupsku_img_main, currentPickupSku.getMainImgUrl());
                        curpickupsku_tip1.setText(currentPickupSku.getName());
                        curpickupsku_tip2.setText("出货就绪......");
                        break;
                    case 0x0002:
                        LogUtil.d("取货流程消息通知:" + currentPickupSku.getName() + ",出货开始");
                        curpickupsku_tip2.setText("出货开始......");
                        break;
                    case 0x0003:
                        LogUtil.d("取货流程消息通知:" + currentPickupSku.getName() + ",出货中");
                        curpickupsku_tip2.setText("出货中......");
                        break;
                    case 0x0004:
                        LogUtil.d("取货流程消息通知:" + currentPickupSku.getName() + ",出货完成");
                        curpickupsku_tip2.setText("出货完成......");
                        break;
                    case 0x0005:
                        LogUtil.d("取货流程消息通知:" + currentPickupSku.getName() + ",出货异常");
                        curpickupsku_tip2.setText("出货异常......");
                        break;
                    case 0x0006:
                        LogUtil.d("取货流程消息通知:" + currentPickupSku.getName() + "");
                        curpickupsku_tip2.setText((String)msg.obj);
                        break;

                }
            }
        };

        try {

            //串口，波特率
            midCtrl = new DeShangMidCtrl(2, 9600);
            //x轴上面有多少货物
            midCtrl.setMaxRow((byte)0x7);
            //y轴上面有多少货物
            midCtrl.setMaxCol((byte) 0x7);
            //串口数据监听事件
            midCtrl.setOnSendUIReport(new DeShangMidCtrl.OnSendUIReport() {
                @Override
                public void OnSendUI(int status, String message) {
                    LogUtil.d("status:"+status+",message:"+message);
                    sendMidCtrlHandlerMsg(0x0006,message);
                }

            });
        }
        catch (Exception ex)
        {
            showToast("设备驱动发生异常");
        }


        taskByPickup = new MyTimeTask(1000, new TimerTask() {
            @Override
            public void run() {
                LogUtil.d("监控取货进度");

                List<OrderDetailsSkuBean> skus =orderDetails.getProductSkus();
                for (int i = 0; i < skus.size(); i++) {
                    OrderDetailsSkuBean sku = skus.get(i);
                    List<PickupSlotBean> slots = sku.getSlots();
                    for (int j = 0; j < slots.size(); j++) {
                        PickupSlotBean slot = slots.get(j);

                        if(slot.getStatus()==3010&&currentPickupSku==null) {

                            //取y轴上面第几个货物
                            midCtrl.setMacCol((byte) 0x0);
                            //取x轴上面第几个货物
                            midCtrl.setMacRow((byte) 0x0);
                            //取货
                            midCtrl.setMacRunning();

                            slot.setStatus(3011);

                            PickupSkuBean pickSku = new PickupSkuBean();
                            pickSku.setId(sku.getId());
                            pickSku.setName(sku.getName());
                            pickSku.setMainImgUrl(sku.getMainImgUrl());
                            pickSku.setSlotId(slot.getSlotId());
                            pickSku.setUniqueId(slot.getUniqueId());
                            pickSku.setStatus(slot.getStatus());

                            currentPickupSku = pickSku;

                            sendMidCtrlHandlerMsg(0x0001,"正在取货中");
                        }


                    }
                }

            }
        });
        taskByPickup.start();
    }

    private void sendMidCtrlHandlerMsg(int what, String msg) {
        final Message m = new Message();
        m.what = what;
        m.obj = msg;
        midCtrlHandler.sendMessage(m);
    }

    private List<PickupSkuBean> getAllPickupProductSkus(List<OrderDetailsSkuBean> skus) {

        List<PickupSkuBean> p1 = new ArrayList<>();

        for (int i = 0; i < skus.size(); i++) {
            OrderDetailsSkuBean sku = skus.get(i);
            List<PickupSlotBean> slots = sku.getSlots();
            for (int j = 0; j < slots.size(); j++) {
                PickupSlotBean slot = slots.get(j);
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




    private void initView() {

        txt_OrderSn = (TextView) findViewById(R.id.txt_OrderSn);
        btn_PickupCompeled = (View) findViewById(R.id.btn_PickupCompeled);
        btn_ContactKefu = (View) findViewById(R.id.btn_ContactKefu);
        list_skus = (MyListView) findViewById(R.id.list_skus);
        list_skus.setFocusable(false);
        list_skus.setClickable(false);
        list_skus.setPressed(false);
        list_skus.setEnabled(false);

        dialog_PickupCompelte = new CustomConfirmDialog(OrderDetailsActivity.this, getAppContext().getString(R.string.activity_orderdetails_tips_outpickup_confirm), true);
        dialog_PickupCompelte.getTipsImage().setImageDrawable(ContextCompat.getDrawable(OrderDetailsActivity.this,(R.drawable.dialog_icon_warn)));
        dialog_PickupCompelte.getBtnSure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_PickupCompelte.dismiss();
                if(taskByPickup!=null)
                {
                    taskByPickup.stop();
                }

                TaskScheduler.getInstance().clearExecutor();

                Intent intent = new Intent(getAppContext(), ProductKindActivity.class);
                startActivity(intent);
                finish();
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

    private void initData() {

        MachineBean machine = AppCacheManager.getMachine();

        if(orderDetails==null) {
            LogUtil.i("bean为空");
            return;
        }

        txt_OrderSn.setText(orderDetails.getOrderSn()+"");

        if(machine.getCsrQrCode()!=null) {
            dialog_ContactKefu.getTipsImage().setImageBitmap(BitmapUtil.createQrCodeBitmap(machine.getCsrQrCode()));
        }

        OrderDetailsSkuAdapter cartSkuAdapter = new OrderDetailsSkuAdapter(OrderDetailsActivity.this, orderDetails.getProductSkus());
        list_skus.setAdapter(cartSkuAdapter);
    }

    private void setProductSkuPickupSuccess(String productSkuId,String slotId) {
        List<OrderDetailsSkuBean> productSkus =orderDetails.getProductSkus();

        for(int i=0;i<productSkus.size();i++) {
            if(productSkus.get(i).getId().equals(productSkuId)) {
                int quantityBySuccess=productSkus.get(i).getQuantityBySuccess();
                int quantityByException=productSkus.get(i).getQuantityByException();
                int quantity=productSkus.get(i).getQuantity();
                if((quantityBySuccess+quantityByException)<quantity) {
                    productSkus.get(i).setQuantityBySuccess(quantityBySuccess + 1);

                }
            }
        }

        OrderDetailsSkuAdapter skuAdapter = new OrderDetailsSkuAdapter(OrderDetailsActivity.this, productSkus);
        list_skus.setAdapter(skuAdapter);
    }

    private void setProductSkuPickupException(String productSkuId,String slotId) {
        List<OrderDetailsSkuBean> productSkus =orderDetails.getProductSkus();

        for(int i=0;i<productSkus.size();i++) {
            if(productSkus.get(i).getId().equals(productSkuId)) {
                int quantityBySuccess=productSkus.get(i).getQuantityBySuccess();
                int quantityByException=productSkus.get(i).getQuantityByException();
                int quantity=productSkus.get(i).getQuantity();
                if((quantityBySuccess+quantityByException)<quantity) {
                    productSkus.get(i).setQuantityByException(quantityByException + 1);
                }
            }
        }

        OrderDetailsSkuAdapter skuAdapter = new OrderDetailsSkuAdapter(OrderDetailsActivity.this, productSkus);
        list_skus.setAdapter(skuAdapter);
    }

    public void pickupQueryStatus(String uniqueId) {

        Map<String, String> params = new HashMap<>();

        MachineBean machine = AppCacheManager.getMachine();

        params.put("machineId", machine.getId());
        params.put("uniqueId", uniqueId);


        getByMy(Config.URL.order_PickupStatusQuery, params, false,"", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<OrderPickupStatusQueryResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<OrderPickupStatusQueryResultBean>>() {
                });


                if (rt.getResult() == Result.SUCCESS) {


                }
            }
        });
    }

    public void pickupEventNotify(String uniqueId,int status,String remark) {

        Map<String, Object> params = new HashMap<>();

        MachineBean machine = AppCacheManager.getMachine();

        params.put("machineId", machine.getId());
        params.put("uniqueId", uniqueId);
        params.put("status", status);
        params.put("remark", remark);

        postByMy(Config.URL.order_PickupEventNotify, params, null,false,"", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<Object> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<Object>>() {
                });


                if (rt.getResult() == Result.SUCCESS) {


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

        if(taskByPickup!=null)
        {
            taskByPickup.stop();
        }

        if(midCtrl!=null)
        {
            midCtrl.stop();
        }

       TaskScheduler.getInstance().clearExecutor();
    }
}
