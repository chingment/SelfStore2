package com.uplink.selfstore.activity;

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
import com.uplink.selfstore.deviceCtrl.MachineCtrl;
import com.uplink.selfstore.http.HttpResponseHandler;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDetailsActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "OrderDetailsActivity";
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
    private Handler handler_UpdateUI;
    private PickupSkuBean currentPickupSku=null;
    private Boolean isPicking=false;

    private MachineCtrl machineCtrl=new MachineCtrl();
    private final int MESSAGE_WHAT_PICKUP=2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderdetails);
        setNavTtile(this.getResources().getString(R.string.activity_orderdetails_navtitle));

        orderDetails = (OrderDetailsBean) getIntent().getSerializableExtra("dataBean");

        initView();
        initEvent();
        initData();

        currentPickupSku = getCurrentPickupProductSku();
        if (currentPickupSku != null) {
            setPickuping(currentPickupSku.getId(), currentPickupSku.getSlotId(), currentPickupSku.getUniqueId());
        } else {
            curpickupsku_img_main.setImageResource(R.drawable.icon_pickupcomplete);
            curpickupsku_tip1.setText("出货完成");
            curpickupsku_tip2.setText("欢迎再次购买......");
        }

        machineCtrl.setPickupHandler(new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {

                        Bundle bundle = msg.getData();
                        int status = bundle.getInt("status");
                        String message = bundle.getString("message");
                        MachineCtrl.PickupResult pickupResult = null;
                        if (bundle.getSerializable("result") != null) {
                            pickupResult = (MachineCtrl.PickupResult) bundle.getSerializable("result");
                        }

                        switch (msg.what) {
                            case 1: //消息提示
                                showToast(message);
                                break;
                            case 2://取货中


                                if (pickupResult != null) {
                                    LogUtil.i(pickupResult.getCurrentActionName() + "," + pickupResult.getCurrentActionStatusName()+"," + pickupResult.getCurrentActionStatusName());

                                    curpickupsku_tip2.setText("正在取货中..请稍等");

                                    if(pickupResult.isPickupComplete()) {
                                        curpickupsku_tip2.setText("取货完成");
                                        setPickupSuccess(currentPickupSku.getId(), currentPickupSku.getSlotId(), currentPickupSku.getUniqueId());
                                    }
                                }



                            default:
                                break;
                        }

                        return false;
                    }
                })
        );

        currentPickupSku = getCurrentPickupProductSku();
        if (currentPickupSku != null) {
            LogUtil.d("currentPickupSku不为空");
            CommonUtil.loadImageFromUrl(OrderDetailsActivity.this, curpickupsku_img_main, currentPickupSku.getMainImgUrl());
            curpickupsku_tip1.setText(currentPickupSku.getName());
            curpickupsku_tip2.setText("准备出货......");
        } else {
            LogUtil.d("currentPickupSku为空");
            curpickupsku_img_main.setImageResource(R.drawable.icon_pickupcomplete);
            curpickupsku_tip1.setText("出货完成");
            curpickupsku_tip2.setText("欢迎再次购买......");
        }

        // 3010 待取货 3011 已发送取货命令 3012 取货中 4000 已完成 6000 异常
        // pickupQueryStatus("ba0ebe970a2840adaf0b5e59c9522317");
        // pickupEventNotify("ba0ebe970a2840adaf0b5e59c9522317",3011,"已发送取货命令");
        //setProductSkuPickupSuccess("a9565c8c71aa42b49bc263c143b9574c","n1r8c5");
        //setProductSkuPickupSuccess("a9565c8c71aa42b49bc263c143b9574c","n1r8c5");
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
        dialog_PickupCompelte.getTipsImage().setImageDrawable(ContextCompat.getDrawable(OrderDetailsActivity.this, (R.drawable.dialog_icon_warn)));
        dialog_PickupCompelte.getBtnSure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_PickupCompelte.dismiss();

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
        LogUtil.d("当前取货:" + currentPickupSku.getName() + ",productSkuId:"+productSkuId+",slotId:" + currentPickupSku.getSlotId()+",uniqueId:"+uniqueId);
        CommonUtil.loadImageFromUrl(OrderDetailsActivity.this, curpickupsku_img_main, currentPickupSku.getMainImgUrl());
        curpickupsku_tip1.setText(currentPickupSku.getName());
        curpickupsku_tip2.setText("准备出货......");

        SlotNRC slotNRC = SlotNRC.GetSlotNRC(slotId);
        if (slotNRC == null) {
            showToast("货道编号解释错误");
            return;
        }

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
                              SlotNRC slotNRC = SlotNRC.GetSlotNRC(currentPickupSku.getSlotId());
                              if (slotNRC != null) {
                                  machineCtrl.pickUp(slotNRC.getRow(),slotNRC.getCol());
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
                                      currentPickupSku = getCurrentPickupProductSku();
                                      if (currentPickupSku != null) {
                                          setPickuping(currentPickupSku.getId(), currentPickupSku.getSlotId(), currentPickupSku.getUniqueId());
                                      } else {
                                          curpickupsku_img_main.setImageResource(R.drawable.icon_pickupcomplete);
                                          curpickupsku_tip1.setText("出货完成");
                                          curpickupsku_tip2.setText("欢迎再次购买......");
                                      }

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
    }

}
