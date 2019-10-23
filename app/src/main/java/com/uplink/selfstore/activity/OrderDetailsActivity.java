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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.OrderDetailsSkuAdapter;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.machineCtrl.DeShangMidCtrl;
import com.uplink.selfstore.model.SlotNRC;
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
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.BitmapUtil;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;
import com.uplink.selfstore.utils.serialport.ChangeToolUtils;

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
    private OrderDetailsBean orderDetails;

    private DeShangMidCtrl midCtrl;
    private Handler midCtrlHandler;
    private PickupSkuBean currentPickupSku=null;
    private Boolean isPicking=false;
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

        currentPickupSku=getCurrentPickupProductSku();
        if(currentPickupSku!=null) {
            CommonUtil.loadImageFromUrl(OrderDetailsActivity.this, curpickupsku_img_main, currentPickupSku.getMainImgUrl());
            curpickupsku_tip1.setText(currentPickupSku.getName());
            curpickupsku_tip2.setText("准备出货......");
        }
        else
        {
            curpickupsku_tip1.setText("出货完成");
            curpickupsku_tip1.setText("");
        }

        // 3010 待取货 3011 已发送取货命令 3012 取货中 4000 已完成 6000 异常
        // pickupQueryStatus("ba0ebe970a2840adaf0b5e59c9522317");
        // pickupEventNotify("ba0ebe970a2840adaf0b5e59c9522317",3011,"已发送取货命令");
        //setProductSkuPickupSuccess("a9565c8c71aa42b49bc263c143b9574c","n1r8c5");
        //setProductSkuPickupSuccess("a9565c8c71aa42b49bc263c143b9574c","n1r8c5");

        midCtrlHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case 0x6: //状态机空闲，检查有无未取货商品
                        currentPickupSku = getCurrentPickupProductSku();
                        if (currentPickupSku != null && isPicking.equals(false)) {
                            LogUtil.d("检查有到有待取货商品");
                            isPicking=true;
                            setPickuping(currentPickupSku.getId(), currentPickupSku.getSlotId(), currentPickupSku.getUniqueId());
                        }
                        break;
                    case 0xa://出货完成
                        LogUtil.d("出货完成");
                        if (currentPickupSku != null&&isPicking.equals(true)) {
                            setPickupSuccess(currentPickupSku.getId(), currentPickupSku.getSlotId(), currentPickupSku.getUniqueId());
                        }
                    default:
                        break;

                }
            }
        };

        try {

            //串口，波特率
            midCtrl = new DeShangMidCtrl(2, 9600);

            //串口数据监听事件
            midCtrl.setOnSendUIReport(new DeShangMidCtrl.OnSendUIReport() {
                @Override
                public void OnSendUI(int status, String message) {
                    LogUtil.d("status:" + status + ",message:" + message);
                    sendMidCtrlHandlerMsg(status, message);
                }
            });

            //x轴上面有多少货物
            midCtrl.setMaxRow((byte) 0x7);
            //y轴上面有多少货物
            midCtrl.setMaxCol((byte) 0x7);

        } catch (Exception ex) {
            showToast("设备驱动发生异常");
        }
    }

    private void sendMidCtrlHandlerMsg(int what, String msg) {
        final Message m = new Message();
        m.what = what;
        m.obj = msg;
        midCtrlHandler.sendMessage(m);
    }

    // 3010 待取货 3011 已发送取货命令 3012 取货中 4000 已完成 6000 异常
    private PickupSkuBean getCurrentPickupProductSku() {

        PickupSkuBean pickSku=null;

        List<OrderDetailsSkuBean> productSkus =orderDetails.getProductSkus();

        boolean isHas=false;
        for (int i = 0; i < productSkus.size(); i++) {

            OrderDetailsSkuBean sku = productSkus.get(i);
            List<PickupSlotBean> slots = sku.getSlots();

            if(isHas) {
                break;
            }

            for (int j = 0; j < slots.size(); j++) {
                PickupSlotBean slot = slots.get(j);
                if(slot.getStatus()!=4000&&slot.getStatus()!=6000) {
                    pickSku = new PickupSkuBean();
                    pickSku.setId(sku.getId());
                    pickSku.setName(sku.getName());
                    pickSku.setMainImgUrl(sku.getMainImgUrl());
                    pickSku.setSlotId(slot.getSlotId());
                    pickSku.setUniqueId(slot.getUniqueId());
                    pickSku.setStatus(slot.getStatus());
                    isHas=true;
                    break;
                }
            }
        }

        return pickSku;
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

                if(midCtrl!=null)
                {
                    midCtrl.stop();
                }
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


    //设置商品卡槽去货中
    private void setPickuping(String productSkuId,String slotId,String uniqueId) {
        LogUtil.d("setPickuping:productSkuId:"+productSkuId);
        LogUtil.d("当前取货" + currentPickupSku.getName() + ",slotId:" + currentPickupSku.getSlotId());
        CommonUtil.loadImageFromUrl(OrderDetailsActivity.this, curpickupsku_img_main, currentPickupSku.getMainImgUrl());
        curpickupsku_tip1.setText(currentPickupSku.getName());
        curpickupsku_tip2.setText("准备出货......");

        pickupEventNotify(productSkuId,slotId,uniqueId,3012,"取货中");

    }

    //设置商品卡槽取货成功
    private void setPickupSuccess(String productSkuId,String slotId,String uniqueId) {
        LogUtil.d("setPickupSuccess:productSkuId:"+productSkuId);
        pickupEventNotify(productSkuId,slotId,uniqueId,4000,"取货完成");
    }

    //设置商品卡槽取货失败
    private void setPickupException(String productSkuId,String slotId,String uniqueId) {


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

    public void pickupEventNotify(final String productSkuId,final String slotId,final String uniqueId, final int status, String remark) {

        Map<String, Object> params = new HashMap<>();

        MachineBean machine = AppCacheManager.getMachine();

        params.put("machineId", machine.getId());
        params.put("uniqueId", uniqueId);
        params.put("status", status);
        params.put("remark", remark);
        LogUtil.d("status:"+status);
        postByMy(Config.URL.order_PickupEventNotify, params, null,false,"", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<Object> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<Object>>() {
                });


                if (rt.getResult() == Result.SUCCESS) {

                      switch (status)
                      {
                          case 3012:
                              SlotNRC slotNRC = GetSlotNRC(currentPickupSku.getSlotId());
                              if (slotNRC != null) {
                                  midCtrl.setMacCol(ChangeToolUtils.intToByte(slotNRC.getCol()));
                                  midCtrl.setMacRow(ChangeToolUtils.intToByte(slotNRC.getRow()));
                                  //取y轴上面第几个货物
                                  //midCtrl.setMacCol((byte) 0x0);
                                  //取x轴上面第几个货物
                                  //midCtrl.setMacRow((byte) 0x0);
                                  //取货
                                  midCtrl.setMacRunning();
                              }
                              break;
                          case 4000:

                              List<OrderDetailsSkuBean> productSkus = orderDetails.getProductSkus();

                                  for (int i = 0; i < productSkus.size(); i++) {
                                      if (productSkus.get(i).getId().equals(productSkuId)) {
                                          int quantityBySuccess = productSkus.get(i).getQuantityBySuccess();
                                          int quantityByException = productSkus.get(i).getQuantityByException();
                                          int quantity = productSkus.get(i).getQuantity();
                                          if ((quantityBySuccess + quantityByException) < quantity) {
                                              productSkus.get(i).setQuantityBySuccess(quantityBySuccess + 1);
                                          }

                                          for (int j = 0; j < productSkus.get(i).getSlots().size(); j++) {
                                              if (productSkus.get(i).getSlots().get(j).getSlotId().equals(slotId) && productSkus.get(i).getSlots().get(j).getUniqueId().equals(uniqueId)) {
                                                  productSkus.get(i).getSlots().get(j).setStatus(4000);
                                              }
                                          }
                                      }
                                      orderDetails.setProductSkus(productSkus);
                                      OrderDetailsSkuAdapter skuAdapter = new OrderDetailsSkuAdapter(OrderDetailsActivity.this, productSkus);
                                      list_skus.setAdapter(skuAdapter);
                                      currentPickupSku = null;
                                      isPicking = false;
                                  }

                              break;
                      }
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

        if(midCtrl!=null)
        {
            midCtrl.stop();
        }

    }

    private SlotNRC GetSlotNRC(String slotId) {


        int n_index=slotId.indexOf('n');

        if(n_index<0)
        {
            return null;
        }

        int r_index=slotId.indexOf('r');
        if(r_index<0)
        {
            return  null;
        }

        int c_index=slotId.indexOf('c');

        if(c_index<0)
        {
            return null;
        }

        try {
            SlotNRC slotNRC=new SlotNRC();

            String str_n = slotId.substring(n_index + 1, r_index - n_index);
            String str_r = slotId.substring(r_index + 1, c_index);
            String str_c = slotId.substring(c_index + 1, slotId.length());

            slotNRC.setCabinetId(str_n);
            slotNRC.setRow(Integer.valueOf(str_r));
            slotNRC.setCol(Integer.valueOf(str_c));

            return  slotNRC;
        }
        catch (NullPointerException ex)
        {
            return  null;
        }

    }
}
