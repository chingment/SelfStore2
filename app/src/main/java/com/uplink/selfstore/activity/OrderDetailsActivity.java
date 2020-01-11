package com.uplink.selfstore.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lgh.uvccamera.UVCCameraProxy;
import com.lgh.uvccamera.bean.PicturePath;
import com.lgh.uvccamera.callback.PictureCallback;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.OrderDetailsSkuAdapter;
import com.uplink.selfstore.deviceCtrl.MachineCtrl;
import com.uplink.selfstore.http.HttpClient;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.PickupResult;
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
import com.uplink.selfstore.ui.ClosePageCountTimer;
import com.uplink.selfstore.ui.dialog.CustomConfirmDialog;
import com.uplink.selfstore.ui.dialog.CustomSystemWarnDialog;
import com.uplink.selfstore.ui.my.MyListView;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.BitmapUtil;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderDetailsActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "OrderDetailsActivity";
    private TextView txt_OrderSn;
    private MyListView list_skus;
    private View btn_PickupCompeled;
    private View btn_ContactKefu;
    private CustomConfirmDialog dialog_PickupCompelte;
    private CustomSystemWarnDialog dialog_SystemWarn;
    private ImageView curpickupsku_img_main;
    private TextView curpickupsku_tip1;
    private TextView curpickupsku_tip2;
    private OrderDetailsBean orderDetails;
    private PickupSkuBean currentPickupSku=null;
    private int[] cabinetPendantRows=null;
    private MachineCtrl machineCtrl=null;


    private UVCCameraProxy mUVCCamera;
    private TextureView mCameraTextureView;

    private int mCameraPreviewWidth=640;
    private int mCameraPreviewHeight=480;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderdetails);
        setNavTtile(this.getResources().getString(R.string.aty_orderdetails_navtitle));

        machineCtrl=MachineCtrl.getInstance();
        //cameraCtrl=new CameraCtrl(OrderDetailsActivity.this,mCameraHandler);

        orderDetails = (OrderDetailsBean) getIntent().getSerializableExtra("dataBean");

        MachineBean machine = AppCacheManager.getMachine();
        cabinetPendantRows=machine.getCabinetPendantRows_1();

        initView();
        initEvent();
        initData();
        initUVCCamera();
        //machineCtrl.connect();
        machineCtrl.setPickupHandler(new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {

                        Bundle bundle = msg.getData();
                        int status = bundle.getInt("status");
                        String message = bundle.getString("message");
                        PickupResult pickupResult = null;
                        if (bundle.getSerializable("result") != null) {
                            pickupResult = (PickupResult) bundle.getSerializable("result");
                        }

                        if(!StringUtil.isEmptyNotNull(message)) {
                            LogUtil.i("取货消息：" + message);
                        }
                        switch (status) {
                            case 1: //消息提示
                                showToast(message);
                                break;
                            case 2://取货就绪成功
                                if (pickupResult != null) {
                                    curpickupsku_tip2.setText("取货就绪成功..请稍等");
                                }

                                if(mUVCCamera!=null) {
                                    if (!mUVCCamera.isCameraOpen()) {
                                        mUVCCamera.openCamera(37424, 1443);
                                    }
                                }

                                break;
                            case 3://取货中
                                if (pickupResult != null) {
                                    curpickupsku_tip2.setText("正在取货中..请稍等");

                                    //拍照
                                    if(pickupResult.getCurrentActionId()==8){
                                        if(mUVCCamera!=null) {
                                            LogUtil.i(TAG,"进入拍照流程");
                                            pickupResult.setImgId(UUID.randomUUID().toString());
                                            mUVCCamera.takePicture(pickupResult.getImgId());

//
//                                            final String imgId=pickupResult.getImgId();
//                                            new Handler().postDelayed(new Runnable(){
//                                                public void run(){
//                                                    mUVCCamera.takePicture(pickupResult.getImgId());
//                                                }
//                                            },5000);
//
//
//                                            new Thread (new Runnable(){
//                                                public void run(){
//                                                    try {
//                                                        Thread.sleep(5000);
//                                                    }
//                                                    catch (Exception ex){
//
//                                                    }
//                                                    mUVCCamera.takePicture(imgId);
//                                                }
//                                            });
                                            // Intent cameraSnapService = new Intent();
//                                        cameraSnapService.setAction("android.intent.action.cameraSnapService");
//                                        cameraSnapService.putExtra("cameraId", 0);
//                                        cameraSnapService.putExtra("imgId", pickupResult.getImgId());
//                                        sendBroadcast(cameraSnapService);
                                        }
                                    }

                                    pickupEventNotify(currentPickupSku.getId(),currentPickupSku.getSlotId(),currentPickupSku.getUniqueId(),3012,"取货中",pickupResult);

                                }
                                break;
                            case 4://取货成功
                                if (pickupResult != null) {
                                    if (pickupResult.isPickupComplete()) {
                                        curpickupsku_tip2.setText("取货完成");
                                        pickupEventNotify(currentPickupSku.getId(),currentPickupSku.getSlotId(),currentPickupSku.getUniqueId(),4000,"取货完成",pickupResult);
                                    }
                                }
                                break;
                            case 5://取货超时
                                LogUtil.e("取货失败,取货动作超时");
                                if(pickupResult!=null) {

                                    pickupResult.setImgId(UUID.randomUUID().toString());

                                    pickupEventNotify(currentPickupSku.getId(), currentPickupSku.getSlotId(), currentPickupSku.getUniqueId(), 6000, message, pickupResult);
                                }
                                showToast(message);
                                if(!dialog_SystemWarn.isShowing()) {
                                    dialog_SystemWarn.setWarnTile("系统维护中.");
                                    dialog_SystemWarn.setBtnCloseVisibility(View.GONE);
                                    dialog_SystemWarn.show();
                                }
                                break;
                            case 6://取货失败
                                LogUtil.e("取货失败,程序异常");
                                pickupEventNotify(currentPickupSku.getId(),currentPickupSku.getSlotId(),currentPickupSku.getUniqueId(),6000,"程序异常",pickupResult);
                                showToast(message);
                                if(!dialog_SystemWarn.isShowing()) {
                                    dialog_SystemWarn.setWarnTile("系统维护中..");
                                    dialog_SystemWarn.setBtnCloseVisibility(View.GONE);
                                    dialog_SystemWarn.show();
                                }
                                break;
                            default:
                                break;
                        }

                        return false;
                    }
                })
        );


        currentPickupSku = getCurrentPickupProductSku();
        if (currentPickupSku != null) {
            setSendPickup(currentPickupSku.getId(), currentPickupSku.getSlotId(), currentPickupSku.getUniqueId());
        } else {
            setPickupCompleteDrawTips();
        }
    }


    private void initUVCCamera() {
        //1137 42694  //益力多
        //1443     37424 // 面包

        mUVCCamera = new UVCCameraProxy(this);
        // 已有默认配置，不需要可以不设置
        mUVCCamera.getConfig()
                .isDebug(true)
                .setPicturePath(PicturePath.APPCACHE)
                .setDirName("uvccamera");

        mUVCCamera.setPreviewTexture(mCameraTextureView);
        mUVCCamera.setMessageHandler(new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {

                        Bundle bundle = msg.getData();
                        int status = bundle.getInt("status");
                        String message = bundle.getString("message");
                        Log.e(TAG,"摄像头->"+ message);

                        switch (msg.what) {
                            case 2://连接成功
                                mUVCCamera.setPreviewSize(mCameraPreviewWidth, mCameraPreviewHeight);

                                mUVCCamera.startPreview();
                                break;
                        }
                        return false;
                    }
                })
        );

        mUVCCamera.setPictureTakenCallback(new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data,String fileName) {
                if(data!=null) {
                    //showToast("拍照成功");
                    Log.e(TAG, "拍照获取成功");
                    saveCaptureStill(data,"SelfStore",fileName);
                }
            }
        });
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

    public void  setPickupCompleteDrawTips() {
        curpickupsku_img_main.setImageResource(R.drawable.icon_pickupcomplete);
        curpickupsku_tip1.setText("出货完成");
        curpickupsku_tip2.setText("欢迎再次购买......");
        useClosePageCountTimer(new ClosePageCountTimer.OnPageCountLinster(){
            @Override
            public void onTick(long seconds) {

            }
        },30);
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

        dialog_PickupCompelte = new CustomConfirmDialog(OrderDetailsActivity.this, getAppContext().getString(R.string.aty_orderdetails_confirmtips_pickup), true);
        dialog_PickupCompelte.getTipsImage().setImageDrawable(ContextCompat.getDrawable(OrderDetailsActivity.this, (R.drawable.dialog_icon_warn)));
        dialog_PickupCompelte.getBtnSure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (dialog_PickupCompelte != null && dialog_PickupCompelte.isShowing()) {
                    dialog_PickupCompelte.cancel();
                }

                Thread t = new Thread(new Runnable(){
                    public void run(){
                        machineCtrl.goGoZero();
                    }
                });
                t.start();

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

        dialog_SystemWarn = new CustomSystemWarnDialog(OrderDetailsActivity.this);
        curpickupsku_img_main = (ImageView) findViewById(R.id.curpickupsku_img_main);
        curpickupsku_tip1 = (TextView) findViewById(R.id.curpickupsku_tip1);
        curpickupsku_tip2 = (TextView) findViewById(R.id.curpickupsku_tip2);

        mCameraTextureView =(TextureView)findViewById(R.id.cameraView);

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

        txt_OrderSn.setText(orderDetails.getOrderSn());


        dialog_SystemWarn.setCsrPhoneNumber(machine.getCsrPhoneNumber());
        dialog_SystemWarn.setCsrQrcode(machine.getCsrQrCode());


        OrderDetailsSkuAdapter cartSkuAdapter = new OrderDetailsSkuAdapter(OrderDetailsActivity.this, orderDetails.getProductSkus());
        list_skus.setAdapter(cartSkuAdapter);
    }

    //设置商品卡槽去货中
    private void setSendPickup(String productSkuId,String slotId,String uniqueId) {
        LogUtil.d("当前取货:" + currentPickupSku.getName() + ",productSkuId:"+productSkuId+",slotId:" + currentPickupSku.getSlotId()+",uniqueId:"+uniqueId);
        CommonUtil.loadImageFromUrl(OrderDetailsActivity.this, curpickupsku_img_main, currentPickupSku.getMainImgUrl());
        curpickupsku_tip1.setText(currentPickupSku.getName());
        curpickupsku_tip2.setText("准备出货......");

        SlotNRC slotNRC = SlotNRC.GetSlotNRC(slotId);
        if (slotNRC == null) {
            //showToast("货道编号解释错误");
            curpickupsku_tip2.setText("准备出货异常......货道编号解释错误");
            return;
        }

        pickupEventNotify(productSkuId,slotId,uniqueId,3011,"发起取货",null);

    }

    //设置商品卡槽取货失败
//    private void setPickupException(String productSkuId,String slotId,String uniqueId) {
//
//
//        List<OrderDetailsSkuBean> productSkus =orderDetails.getProductSkus();
//
//        for(int i=0;i<productSkus.size();i++) {
//            if(productSkus.get(i).getId().equals(productSkuId)) {
//                int quantityBySuccess=productSkus.get(i).getQuantityBySuccess();
//                int quantityByException=productSkus.get(i).getQuantityByException();
//                int quantity=productSkus.get(i).getQuantity();
//                if((quantityBySuccess+quantityByException)<quantity) {
//                    productSkus.get(i).setQuantityByException(quantityByException + 1);
//                }
//            }
//        }
//
//        OrderDetailsSkuAdapter skuAdapter = new OrderDetailsSkuAdapter(OrderDetailsActivity.this, productSkus);
//        list_skus.setAdapter(skuAdapter);
//    }

//    public void pickupQueryStatus(String uniqueId) {
//
//        Map<String, String> params = new HashMap<>();
//
//        MachineBean machine = AppCacheManager.getMachine();
//
//        params.put("machineId", machine.getId());
//        params.put("uniqueId", uniqueId);
//
//
//        getByMy(Config.URL.order_PickupStatusQuery, params, false,"", new HttpResponseHandler() {
//            @Override
//            public void onSuccess(String response) {
//                super.onSuccess(response);
//
//                ApiResultBean<OrderPickupStatusQueryResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<OrderPickupStatusQueryResultBean>>() {
//                });
//
//
//                if (rt.getResult() == Result.SUCCESS) {
//
//
//                }
//            }
//        });
//    }

    public void pickupEventNotify(final String productSkuId, final String slotId, final String uniqueId, final int status, String remark, PickupResult pickupResult) {

        Map<String, Object> params = new HashMap<>();

        MachineBean machine = AppCacheManager.getMachine();

        params.put("machineId", machine.getId());
        params.put("uniqueId", uniqueId);
        params.put("status", status);
        if(pickupResult!=null) {
            params.put("actionId", pickupResult.getCurrentActionId());
            params.put("actionName", pickupResult.getCurrentActionName());
            params.put("actionStatusCode", pickupResult.getCurrentActionStatusCode());
            params.put("actionStatusName", pickupResult.getCurrentActionStatusName());
            params.put("pickupUseTime", pickupResult.getPickupUseTime());
            params.put("isPickupComplete", pickupResult.isPickupComplete());
            params.put("imgId", pickupResult.getImgId());
        }
        params.put("remark", remark);
        LogUtil.d("status:" + status);
        postByMy(Config.URL.order_PickupEventNotify, params, null, false, "", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<Object> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<Object>>() {
                });


                if (rt.getResult() == Result.SUCCESS) {

                    switch (status) {
                        case 3011:
                            SlotNRC slotNRC = SlotNRC.GetSlotNRC(currentPickupSku.getSlotId());
                            if (slotNRC != null) {

                                int mode=0;
                                if (cabinetPendantRows != null) {
                                    for (int z = 0; z < cabinetPendantRows.length; z++) {
                                        if (cabinetPendantRows[z] == slotNRC.getRow()) {
                                            mode =1;
                                            break;
                                        }
                                    }
                                }
                                machineCtrl.pickUp(mode,slotNRC.getRow(), slotNRC.getCol());
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
                            }

                            orderDetails.setProductSkus(productSkus);

                            OrderDetailsSkuAdapter skuAdapter = new OrderDetailsSkuAdapter(OrderDetailsActivity.this, productSkus);
                            list_skus.setAdapter(skuAdapter);
                            currentPickupSku = getCurrentPickupProductSku();
                            if (currentPickupSku != null) {
                                setSendPickup(currentPickupSku.getId(), currentPickupSku.getSlotId(), currentPickupSku.getUniqueId());
                            } else {
                                setPickupCompleteDrawTips();

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
                    dialog_SystemWarn.setWarnTile("您好，需要提供帮助吗？");
                    dialog_SystemWarn.setBtnCloseVisibility(View.VISIBLE);
                    dialog_SystemWarn.show();
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

        if (dialog_SystemWarn != null && dialog_SystemWarn.isShowing()) {
            dialog_SystemWarn.cancel();
        }

        if(mUVCCamera!=null){
            mUVCCamera.closeCamera();
            mUVCCamera.unregisterReceiver();
            mUVCCamera=null;
        }
    }


    public  void  saveCaptureStill(byte[] data,String saveDir,String fileName) {
        try {
            if (data == null)
                return;
            if (saveDir == null)
                return;
            if (fileName == null)
                return;

            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, mCameraPreviewWidth, mCameraPreviewHeight, null);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
            yuvImage.compressToJpeg(new Rect(0, 0, mCameraPreviewWidth, mCameraPreviewHeight), 100, bos);
            byte[] buffer = bos.toByteArray();

            Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);

            String mSaveDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + saveDir;

            File pathFile = new File(mSaveDir);
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }

            String filePath = mSaveDir + "/" + fileName + ".jpg";
            File outputFile = new File(filePath);
            final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();

            //上传到服务器
            List<String> filePaths = new ArrayList<>();
            filePaths.add(filePath);
            Map<String, String> params = new HashMap<>();
            params.put("fileName", fileName);
            params.put("folder", "pickup");
            HttpClient.postFile(Config.URL.uploadfile, params, filePaths, null);

            LogUtil.i(TAG,"拍照保存成功");

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
