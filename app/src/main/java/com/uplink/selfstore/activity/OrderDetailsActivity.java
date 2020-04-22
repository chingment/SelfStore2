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
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByZS;
import com.uplink.selfstore.model.DSCabRowColLayoutBean;
import com.uplink.selfstore.model.PickupResult;
import com.uplink.selfstore.model.DSCabSlotNRC;
import com.uplink.selfstore.model.api.CabinetBean;
import com.uplink.selfstore.model.api.OrderDetailsBean;
import com.uplink.selfstore.model.api.OrderDetailsSkuBean;
import com.uplink.selfstore.model.api.PickupSkuBean;
import com.uplink.selfstore.model.api.PickupSlotBean;
import com.uplink.selfstore.ui.ClosePageCountTimer;
import com.uplink.selfstore.ui.dialog.CustomConfirmDialog;
import com.uplink.selfstore.ui.my.MyListView;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;
import com.uplink.selfstore.utils.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class OrderDetailsActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "OrderDetailsActivity";

    private TextView txt_OrderId;
    private MyListView list_Skus;
    private View btn_PickupCompeled;
    private View btn_ContactKefu;

    private CustomConfirmDialog dialog_PickupCompelte;

    private PickupSkuBean curPickupSku=null;
    private ImageView curPickupSku_Img_Mainimg;
    private TextView curPickupSku_Tv_Tip1;
    private TextView curPickupSku_Tv_Tip2;

    private OrderDetailsBean orderDetails;
    private CabinetCtrlByDS cabinetCtrlByDS=null;
    private CabinetCtrlByZS cabinetCtrlByZS=null;


    private boolean isHappneException=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderdetails);
        setNavTtile(this.getResources().getString(R.string.aty_orderdetails_navtitle));

        cabinetCtrlByDS = CabinetCtrlByDS.getInstance();
        cabinetCtrlByZS = CabinetCtrlByZS.getInstance();

        orderDetails = (OrderDetailsBean) getIntent().getSerializableExtra("dataBean");

        initView();
        initEvent();
        initData();

        cabinetCtrlByDS.connect();
        cabinetCtrlByZS.connect();

        cabinetCtrlByDS.setPickupHandler(new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {

                        Bundle bundle = msg.getData();
                        int status = bundle.getInt("status");
                        String message = bundle.getString("message");
                        PickupResult pickupResult = null;
                        if (bundle.getSerializable("result") != null) {
                            pickupResult = (PickupResult) bundle.getSerializable("result");
                        }

                        if (!StringUtil.isEmptyNotNull(message)) {
                            LogUtil.i("取货消息：" + message);
                        }

                        String componentName =getTopComponentName();
                        if(componentName==null){
                          isHappneException=true;
                        }
                        else if(!componentName.toLowerCase().contains("orderdetailsactivity")) {
                            isHappneException = true;
                        }

                        if (isHappneException) {
                            cabinetCtrlByDS.emgStop();
                            pickupEventNotify(curPickupSku, 6000, "取货失败，不在当前取货界面", pickupResult);
                        }
                        else {
                            switch (status) {
                                case 1: //消息提示
                                    showToast(message);
                                    break;
                                case 2://取货就绪成功
                                    curPickupSku_Tv_Tip2.setText(message);
                                    break;
                                case 3://取货中
                                    curPickupSku_Tv_Tip2.setText("正在取货中..请稍等");
                                    pickupEventNotify(curPickupSku, 3012, "取货中", pickupResult);
                                    break;
                                case 4://取货成功
                                    curPickupSku_Tv_Tip2.setText("取货完成");
                                    pickupEventNotify(curPickupSku, 4000, "取货完成", pickupResult);
                                    break;
                                case 5://取货失败，机器异常
                                    LogUtil.e("取货失败,机器异常");
                                    curPickupSku_Tv_Tip2.setText("取货失败,机器发生异常:"+message);
                                    isHappneException = true;
                                    pickupEventNotify(curPickupSku, 6000, "取货失败,机器发生异常，" + message, pickupResult);
                                    break;
                                case 6://取货失败，程序异常
                                    LogUtil.e("取货失败,程序异常");
                                    curPickupSku_Tv_Tip2.setText("取货失败，程序发生异常");
                                    isHappneException = true;
                                    pickupEventNotify(curPickupSku, 6000, "取货失败，程序发生异常", pickupResult);
                                    break;
                                default:
                                    LogUtil.e("取货失败,未知状态");
                                    curPickupSku_Tv_Tip2.setText("取货失败，未知状态");
                                    isHappneException = true;
                                    pickupEventNotify(curPickupSku, 6000, "取货失败，未知状态", pickupResult);
                                    break;
                            }
                        }

                        return false;
                    }
                })
        );

        cabinetCtrlByZS.setHandler(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                switch (msg.what) {
                    case CabinetCtrlByZS.MESSAGE_WHAT_ONEUNLOCK:
                        Bundle bundle = msg.getData();
                        int status = bundle.getInt("status");
                        String message = bundle.getString("message");
                        switch (status) {
                            case 1://消息提示
                                showToast(message);
                                break;
                            case 2://启动就绪成功
                                curPickupSku_Tv_Tip2.setText("取货就绪成功..请稍等");
                                break;
//                            case 3://取货中
//                                curPickupSku_Tv_Tip2.setText("正在取货中..请稍等");
//                                pickupEventNotify(curPickupSku,3012, "取货中", null);
//                                break;
                            case 3:
                            case 4://反馈成功
                                //todo 暂时默认发送命令既成功
                                PickupResult pickupResult = new PickupResult();
                                pickupResult.setPickupComplete(true);
                                curPickupSku_Tv_Tip2.setText("取货完成");
                                pickupEventNotify(curPickupSku, 4000, "取货完成", pickupResult);

//                                CabinetCtrlByZS.ZSCabBoxStatusResult result = (CabinetCtrlByZS.ZSCabBoxStatusResult) bundle.getSerializable("result");
//                                if (result != null) {
//                                    if (result.getCabBoxs() != null) {
//                                        ZSCabBoxBean zsCabBoxBean = result.getCabBoxs().get(Integer.valueOf(currentPickupSku.getSlotId()));
//                                        if (zsCabBoxBean != null) {
//                                            if (zsCabBoxBean.isOpen()) {
//                                                PickupResult pickupResult = new PickupResult();
//                                                pickupResult.setPickupComplete(true);
//                                                curpickupsku_tip2.setText("取货完成");
//                                                pickupEventNotify(currentPickupSku.getId(), currentPickupSku.getCabinetId(), currentPickupSku.getSlotId(), currentPickupSku.getUniqueId(), 4000, "取货完成", pickupResult);
//                                            }
//                                        }
//                                    }
//                                }
                                break;
                            case 5://取货超时
                                isHappneException=true;
                                curPickupSku_Tv_Tip2.setText("取货发生异常..");
                                pickupEventNotify(curPickupSku, 6000, "取货失败，取货超时," + message, null);
                                break;
                            case 6://取货失败
                                isHappneException=true;
                                curPickupSku_Tv_Tip2.setText("取货发生异常...");
                                pickupEventNotify(curPickupSku, 6000, "取货失败,程序异常", null);
                                break;
                        }
                        break;
                }
                return false;
            }
        }));


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                curPickupSku = getCurrentPickupProductSku();
                if (curPickupSku != null) {
                    setSendPickup(curPickupSku);
                } else {
                    setPickupCompleteDrawTips();
                }
            }
        }, 2000);//2秒后执行Runnable中的run方法


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
                if(slot.isAllowPickup()) {
                    if(slot.getStatus()==3010) {
                        pickSku = new PickupSkuBean();
                        pickSku.setId(sku.getId());
                        pickSku.setName(sku.getName());
                        pickSku.setMainImgUrl(sku.getMainImgUrl());
                        pickSku.setSlotId(slot.getSlotId());
                        pickSku.setCabinetId(slot.getCabinetId());
                        pickSku.setUniqueId(slot.getUniqueId());
                        pickSku.setStatus(slot.getStatus());
                        isHas = true;
                        break;
                    }
                }
            }
        }

        return pickSku;
    }

    private boolean isPickupCompelte(){

        boolean isCompelte=true;

        List<OrderDetailsSkuBean> productSkus =orderDetails.getProductSkus();

        for (int i = 0; i < productSkus.size(); i++) {

            OrderDetailsSkuBean sku = productSkus.get(i);
            List<PickupSlotBean> slots = sku.getSlots();

            if(!isCompelte) {
                break;
            }

            for (int j = 0; j < slots.size(); j++) {
                PickupSlotBean slot = slots.get(j);
                if (slot.getStatus() != 4000) {
                    isCompelte = false;
                    break;
                }
            }
        }


        return  isCompelte;
    }

    public void  setPickupCompleteDrawTips() {
        curPickupSku_Img_Mainimg.setImageResource(R.drawable.icon_pickupcomplete);
        curPickupSku_Tv_Tip1.setText("出货完成");
        curPickupSku_Tv_Tip2.setText("欢迎再次购买......");
        useClosePageCountTimer(new ClosePageCountTimer.OnPageCountLinster() {
            @Override
            public void onTick(long seconds) {

            }
        }, 30);
        closePageCountTimerStart();
    }

    private void initView() {

        txt_OrderId = (TextView) findViewById(R.id.txt_OrderId);
        btn_PickupCompeled = (View) findViewById(R.id.btn_PickupCompeled);
        btn_ContactKefu = (View) findViewById(R.id.btn_ContactKefu);
        list_Skus = (MyListView) findViewById(R.id.list_skus);
        list_Skus.setFocusable(false);
        list_Skus.setClickable(false);
        list_Skus.setPressed(false);
        list_Skus.setEnabled(false);

        dialog_PickupCompelte = new CustomConfirmDialog(OrderDetailsActivity.this, getAppContext().getString(R.string.aty_orderdetails_confirmtips_pickup), true);
        dialog_PickupCompelte.getTipsImage().setImageDrawable(ContextCompat.getDrawable(OrderDetailsActivity.this, (R.drawable.dialog_icon_warn)));
        dialog_PickupCompelte.getBtnSure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (dialog_PickupCompelte != null && dialog_PickupCompelte.isShowing()) {
                    dialog_PickupCompelte.cancel();
                }

                Intent intent = new Intent(OrderDetailsActivity.this, ProductKindActivity.class);
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


        curPickupSku_Img_Mainimg = (ImageView) findViewById(R.id.curpickupsku_img_main);
        curPickupSku_Tv_Tip1 = (TextView) findViewById(R.id.curpickupsku_tip1);
        curPickupSku_Tv_Tip2 = (TextView) findViewById(R.id.curpickupsku_tip2);

    }

    private void initEvent() {
        btn_PickupCompeled.setOnClickListener(this);
        btn_ContactKefu.setOnClickListener(this);
    }

    private void initData() {

        if(orderDetails==null) {
            LogUtil.i("bean为空");
            return;
        }

        txt_OrderId.setText(orderDetails.getId());


        OrderDetailsSkuAdapter orderDetailsSkuAdapter = new OrderDetailsSkuAdapter(OrderDetailsActivity.this, orderDetails.getProductSkus());
        list_Skus.setAdapter(orderDetailsSkuAdapter);
    }

    //设置商品卡槽去货中
    private void setSendPickup(PickupSkuBean pickupSku) {
        if (pickupSku != null) {
            LogUtil.d("当前取货:" + pickupSku.getName() + ",productSkuId:" + pickupSku.getId() + ",slotId:" + pickupSku.getSlotId() + ",uniqueId:" + pickupSku.getUniqueId());
            CommonUtil.loadImageFromUrl(OrderDetailsActivity.this, curPickupSku_Img_Mainimg, pickupSku.getMainImgUrl());
            curPickupSku_Tv_Tip1.setText(pickupSku.getName());
            curPickupSku_Tv_Tip2.setText("准备出货......");
            pickupEventNotify(pickupSku, 3011, "发起取货", null);
        }
    }

    public void pickupEventNotify(final PickupSkuBean pickupSku, final int status, String remark, PickupResult pickupResult) {

        try {
            JSONObject content = new JSONObject();
            content.put("orderId", orderDetails.getId());
            content.put("uniqueId", pickupSku.getUniqueId());
            content.put("productSkuId", pickupSku.getId());
            content.put("cabinetId", pickupSku.getCabinetId());
            content.put("slotId", pickupSku.getSlotId());
            content.put("status", status);
            content.put("isTest", false);
            if (pickupResult != null) {
                content.put("actionId", pickupResult.getCurrentActionId());
                content.put("actionName", pickupResult.getCurrentActionName());
                content.put("actionStatusCode", pickupResult.getCurrentActionStatusCode());
                content.put("actionStatusName", pickupResult.getCurrentActionStatusName());
                content.put("pickupUseTime", pickupResult.getPickupUseTime());
                content.put("isPickupComplete", pickupResult.isPickupComplete());
                content.put("imgId", pickupResult.getImgId());
            }
            content.put("remark", remark);
            LogUtil.d("status:" + status);
            eventNotify("Pickup", content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(isHappneException) {
            if (!OrderDetailsActivity.this.isFinishing()) {
                if (!getDialogBySystemWarn().isShowing()) {
                    getDialogBySystemWarn().setWarnTile("系统维护中..");
                    getDialogBySystemWarn().setBtnCloseVisibility(View.GONE);
                    getDialogBySystemWarn().show();
                }
            }
            cabinetCtrlByDS.emgStop();
            curPickupSku_Tv_Tip2.setText("取货失败，程序发生异常");
        }
        else {
            CabinetBean cabinet = getMachine().getCabinets().get(pickupSku.getCabinetId());

            switch (status) {
                case 3011:
                    switch (cabinet.getModelNo()) {
                        case "dsx01":
                            DSCabSlotNRC dsCabSlotNRC = DSCabSlotNRC.GetSlotNRC(pickupSku.getCabinetId(), pickupSku.getSlotId());
                            if (dsCabSlotNRC == null) {
                                curPickupSku_Tv_Tip2.setText("准备出货异常......货道编号解释错误");
                                return;
                            }

                            DSCabRowColLayoutBean dSCabRowColLayout = JSON.parseObject(cabinet.getRowColLayout(), new TypeReference<DSCabRowColLayoutBean>() {
                            });
                            cabinetCtrlByDS.pickUp(dsCabSlotNRC.getRow(), dsCabSlotNRC.getCol(), dSCabRowColLayout.getPendantRows());
                            break;
                        case "zsx01":
                            cabinetCtrlByZS.unLock(cabinet.getCodeNo(), Integer.valueOf(pickupSku.getSlotId()));
                            break;
                    }
                    break;
                case 4000:
                    List<OrderDetailsSkuBean> productSkus = orderDetails.getProductSkus();

                    for (int i = 0; i < productSkus.size(); i++) {
                        if (productSkus.get(i).getId().equals(pickupSku.getId())) {
                            int quantityBySuccess = productSkus.get(i).getQuantityBySuccess();
                            int quantityByException = productSkus.get(i).getQuantityByException();
                            int quantity = productSkus.get(i).getQuantity();
                            if ((quantityBySuccess + quantityByException) < quantity) {
                                productSkus.get(i).setQuantityBySuccess(quantityBySuccess + 1);
                            }
                            for (int j = 0; j < productSkus.get(i).getSlots().size(); j++) {
                                if (productSkus.get(i).getSlots().get(j).getSlotId().equals(pickupSku.getSlotId()) && productSkus.get(i).getSlots().get(j).getUniqueId().equals(pickupSku.getUniqueId())) {
                                    productSkus.get(i).getSlots().get(j).setStatus(4000);
                                }
                            }
                        }
                    }

                    orderDetails.setProductSkus(productSkus);

                    OrderDetailsSkuAdapter skuAdapter = new OrderDetailsSkuAdapter(OrderDetailsActivity.this, productSkus);
                    list_Skus.setAdapter(skuAdapter);
                    curPickupSku = getCurrentPickupProductSku();
                    if (curPickupSku != null) {
                        setSendPickup(curPickupSku);
                    } else {
                        setPickupCompleteDrawTips();
                    }
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.btn_PickupCompeled:
                    if(!isPickupCompelte()){
                        showToast(getAppContext().getString(R.string.tips_notpickupcompelte));
                        return;
                    }
                    dialog_PickupCompelte.show();
                    break;
                case R.id.btn_ContactKefu:
                    getDialogBySystemWarn().setWarnTile("您好，需要提供帮助吗？");
                    getDialogBySystemWarn().setBtnCloseVisibility(View.VISIBLE);
                    getDialogBySystemWarn().show();
                    break;
            }
        }
    }


    @Override
    public void onStop() {
        super.onStop();

        if (cabinetCtrlByDS != null) {
            cabinetCtrlByDS.disConnect();
            cabinetCtrlByDS = null;
        }

        if (cabinetCtrlByZS != null) {
            cabinetCtrlByZS.disConnect();
            cabinetCtrlByZS = null;
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        if(cabinetCtrlByDS!=null){
            cabinetCtrlByDS.disConnect();
            cabinetCtrlByDS = null;
        }

        if (cabinetCtrlByZS != null) {
            cabinetCtrlByZS.disConnect();
            cabinetCtrlByZS = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog_PickupCompelte != null && dialog_PickupCompelte.isShowing()) {
            dialog_PickupCompelte.cancel();
        }


        if (cabinetCtrlByDS != null) {
            cabinetCtrlByDS.disConnect();
        }

        if (cabinetCtrlByZS != null) {
            cabinetCtrlByZS.disConnect();
            cabinetCtrlByZS = null;
        }

        closePageCountTimerStop();
    }


//    public  void  saveCaptureStill(byte[] data,String saveDir,String fileName) {
//        try {
//            if (data == null)
//                return;
//            if (saveDir == null)
//                return;
//            if (fileName == null)
//                return;
//
//            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, mCameraPreviewWidth, mCameraPreviewHeight, null);
//            ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
//            yuvImage.compressToJpeg(new Rect(0, 0, mCameraPreviewWidth, mCameraPreviewHeight), 100, bos);
//            byte[] buffer = bos.toByteArray();
//
//            Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
//
//            String mSaveDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + saveDir;
//
//            File pathFile = new File(mSaveDir);
//            if (!pathFile.exists()) {
//                pathFile.mkdirs();
//            }
//
//            String filePath = mSaveDir + "/" + fileName + ".jpg";
//            File outputFile = new File(filePath);
//            final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
//            os.flush();
//            os.close();
//
//            //上传到服务器
//            List<String> filePaths = new ArrayList<>();
//            filePaths.add(filePath);
//            Map<String, String> params = new HashMap<>();
//            params.put("fileName", fileName);
//            params.put("folder", "pickup");
//            HttpClient.postFile(Config.URL.uploadfile, params, filePaths, null);
//
//            LogUtil.i(TAG,"拍照保存成功");
//
//        } catch (Exception e) {
//            Log.e(TAG, e.toString());
//        }
//    }
}
