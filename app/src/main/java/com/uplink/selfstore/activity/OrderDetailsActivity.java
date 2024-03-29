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
import com.uplink.selfstore.model.PickupActionResult;
import com.uplink.selfstore.model.DSCabSlotNRC;
import com.uplink.selfstore.model.api.CabinetBean;
import com.uplink.selfstore.model.api.DeviceBean;
import com.uplink.selfstore.model.api.OrderDetailsBean;
import com.uplink.selfstore.model.api.OrderDetailsSkuBean;
import com.uplink.selfstore.model.api.PickupSkuBean;
import com.uplink.selfstore.model.api.PickupSlotBean;
import com.uplink.selfstore.app.AppLogcatManager;
import com.uplink.selfstore.utils.tinytaskonebyone.TinySyncExecutor;
import com.uplink.selfstore.ui.CameraWindow;
import com.uplink.selfstore.ui.ClosePageCountTimer;
import com.uplink.selfstore.ui.dialog.CustomDialogConfirm;
import com.uplink.selfstore.ui.my.MyListView;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.IdWorker;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;
import com.uplink.selfstore.utils.StringUtil;

import org.json.JSONObject;

import java.util.List;
import java.util.UUID;


public class OrderDetailsActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "OrderDetailsActivity";

    private TextView txt_OrderId;
    private MyListView list_Skus;
    private View btn_PickupCompeled;
    private View btn_ContactKefu;

    private CustomDialogConfirm dialog_Confirm;

    private PickupSkuBean curPickupSku=null;
    private ImageView curPickupSku_Img_Mainimg;
    private TextView curPickupSku_Tv_Tip1;
    private TextView curPickupSku_Tv_Tip2;

    private OrderDetailsBean orderDetails;
    private CabinetCtrlByDS cabinetCtrlByDS=null;
    private CabinetCtrlByZS cabinetCtrlByZS=null;


    private boolean isHappneException=false;
    private String exceptionMessage="";
    private boolean isGoZero=true;

    private DeviceBean device;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderdetails);
        setNavTtile(this.getResources().getString(R.string.aty_orderdetails_navtitle));

        cabinetCtrlByDS = CabinetCtrlByDS.getInstance();
        cabinetCtrlByZS = CabinetCtrlByZS.getInstance();
        device = getDevice();
        CameraWindow.clearWaitAction();

        orderDetails = (OrderDetailsBean) getIntent().getSerializableExtra("dataBean");

        initView();
        initEvent();
        initData();

        cabinetCtrlByDS.connect();
        cabinetCtrlByZS.connect();

        cabinetCtrlByDS.setPickupHandler(new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {

                        if (!CameraWindow.cameraIsRunningByChk()) {
                            CameraWindow.openCameraByChk();
                        }

                        if (!CameraWindow.cameraIsRunningByJg()) {
                            CameraWindow.openCameraByJg();
                        }

                        Bundle bundle = msg.getData();
                        int status = bundle.getInt("status");
                        String message = bundle.getString("message");
                        PickupActionResult pickupActionResult = null;
                        if (bundle.getSerializable("result") != null) {
                            pickupActionResult = (PickupActionResult) bundle.getSerializable("result");
                        }

                        if (!StringUtil.isEmptyNotNull(message)) {
                            LogUtil.i(TAG, "取货消息：" + message);
                        }

//                        String componentName = getTopComponentName();
//                        if (componentName == null) {
//                            isHappneException = true;
//                            exceptionMessage = "取货异常，取货不在当前界面";
//                        } else if (!componentName.toLowerCase().contains("orderdetailsactivity")) {
//                            isHappneException = true;
//                            exceptionMessage = "取货异常，取货不在当前界面";
//                        }

                        boolean isTakePic = false;

                        if (isHappneException) {
                            isTakePic = true;
                        }

                        if (!isTakePic) {
                            if (status == 5 || status > 6) {
                                isTakePic = true;
                            }
                        }

                        if (pickupActionResult != null) {
                            if (pickupActionResult.getActionId() == 8) {
                                isTakePic = true;
                            }
                        }

                        //判断是使用WIFI网络，则每一步捕捉相片
                        if (CommonUtil.isWifi(OrderDetailsActivity.this)) {
                            isTakePic = true;
                        }

                        if (isTakePic) {

                            if (pickupActionResult == null) {
                                pickupActionResult = new PickupActionResult();
                            }

                            if (CameraWindow.cameraIsRunningByChk()) {
                                pickupActionResult.setImgId(UUID.randomUUID().toString());
                                LogUtil.d(TAG, "开始拍照->出货口");
                                CameraWindow.takeCameraPicByChk(pickupActionResult.getImgId());

//                                final String imgId=pickupResult.getImgId();
//                                if(isDelayTakeCameraPicByChk){
//                                    Handler handler = new Handler();
//                                    handler.postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            CameraWindow.takeCameraPicByChk(imgId);
//                                        }
//                                    }, 5000);
//                                }
//                                else
//                                {
//                                    CameraWindow.takeCameraPicByChk(pickupResult.getImgId());
//                                }
                            }

                            if (CameraWindow.cameraIsRunningByJg()) {
                                LogUtil.d(TAG, "开始拍照->机柜");
                                pickupActionResult.setImgId2(UUID.randomUUID().toString());
                                CameraWindow.takeCameraPicByJg(pickupActionResult.getImgId2());
                            }
                        }

                        if (isHappneException) {
                            if (cabinetCtrlByDS != null) {
                                cabinetCtrlByDS.emgStop();
                            }
                            pickupEventNotify(curPickupSku, 6000, exceptionMessage, pickupActionResult);
                            setPickupException(curPickupSku,exceptionMessage);
                        } else {
                            switch (status) {
                                case 1: //消息提示
                                    showToast(message);
                                    break;
                                case 2://取货就绪成功
                                    curPickupSku_Tv_Tip2.setText(message);
                                    break;
                                case 3://取货中
                                    curPickupSku_Tv_Tip2.setText("正在取货中..请稍等");
                                    pickupEventNotify(curPickupSku, 3012, "取货中", pickupActionResult);
                                    break;
                                case 4://取货成功
                                    isGoZero = false;
                                    curPickupSku_Tv_Tip2.setText("取货完成");
                                    pickupEventNotify(curPickupSku, 4000, "取货完成", pickupActionResult);
                                    setCurPickupSkuComplete(curPickupSku);
                                    break;
                                case 5://取货失败，设备异常
                                    isHappneException = true;
                                    exceptionMessage = "取货失败,设备发生异常:" + message;
                                    LogUtil.d(TAG, exceptionMessage);
                                    curPickupSku_Tv_Tip2.setText(exceptionMessage);
                                    pickupEventNotify(curPickupSku, 6000, exceptionMessage, pickupActionResult);
                                    setPickupException(curPickupSku,exceptionMessage);
                                    break;
                                case 6://取货失败，程序异常
                                    isHappneException = true;
                                    exceptionMessage = "取货失败，程序发生异常:" + message;
                                    LogUtil.d(TAG, exceptionMessage);
                                    curPickupSku_Tv_Tip2.setText(exceptionMessage);
                                    pickupEventNotify(curPickupSku, 6000, exceptionMessage, pickupActionResult);
                                    setPickupException(curPickupSku,exceptionMessage);
                                    break;
                                default:
                                    isHappneException = true;
                                    exceptionMessage = "取货失败，未知状态:" + message;
                                    LogUtil.d(TAG, exceptionMessage);
                                    curPickupSku_Tv_Tip2.setText(exceptionMessage);
                                    pickupEventNotify(curPickupSku, 6000, exceptionMessage, pickupActionResult);
                                    setPickupException(curPickupSku,exceptionMessage);
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
                                PickupActionResult pickupActionResult = new PickupActionResult();

                                curPickupSku_Tv_Tip2.setText("取货完成");
                                pickupEventNotify(curPickupSku, 4000, "取货完成", pickupActionResult);
                                setCurPickupSkuComplete(curPickupSku);
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
                                isHappneException = true;
                                pickupEventNotify(curPickupSku, 6000, "取货失败，取货超时," + message, null);
                                setPickupException(curPickupSku,"取货发生异常..");
                                break;
                            case 6://取货失败
                                isHappneException = true;
                                pickupEventNotify(curPickupSku, 6000, "取货失败,程序异常", null);
                                setPickupException(curPickupSku,"取货失败,程序异常..");
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

                curPickupSku = getCurrentPickupSku();
                if (curPickupSku != null) {
                    setCurPickupSku(curPickupSku);
                } else {
                    setAllPickupComplete();
                }
            }
        }, 2000);//2秒后执行Runnable中的run方法


    }

    // 3010 待取货 3011 已发送取货命令 3012 取货中 4000 已完成 6000 异常
    private PickupSkuBean getCurrentPickupSku() {

        PickupSkuBean pickSku=null;


        List<OrderDetailsSkuBean> skus =orderDetails.getSkus();

        if(skus==null)
            return  pickSku;

        boolean isHas=false;
        for (int i = 0; i < skus.size(); i++) {

            OrderDetailsSkuBean sku = skus.get(i);
            List<PickupSlotBean> slots = sku.getSlots();

            if(isHas) {
                break;
            }

            for (int j = 0; j < slots.size(); j++) {
                PickupSlotBean slot = slots.get(j);
                if(slot.isAllowPickup()) {
                    if(slot.getStatus()==3010) {
                        pickSku = new PickupSkuBean();
                        pickSku.setSkuId(sku.getSkuId());
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

        List<OrderDetailsSkuBean> skus =orderDetails.getSkus();

        if(skus==null)
            return isCompelte;

        for (int i = 0; i < skus.size(); i++) {

            OrderDetailsSkuBean sku = skus.get(i);
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

    public void  setAllPickupComplete() {
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

        dialog_Confirm = new CustomDialogConfirm(OrderDetailsActivity.this, getAppContext().getString(R.string.aty_orderdetails_confirmtips_pickup), true);
        dialog_Confirm.setTipsImageDrawable(ContextCompat.getDrawable(OrderDetailsActivity.this, (R.drawable.dialog_icon_warn)));
        dialog_Confirm.setOnClickListener(new CustomDialogConfirm.OnClickListener() {
            @Override
            public void onSure() {

                if (dialog_Confirm != null) {
                    dialog_Confirm.hide();
                }

                Intent intent = new Intent(OrderDetailsActivity.this, ProductKindActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancle() {
                dialog_Confirm.hide();
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
            LogUtil.i(TAG,"bean为空");
            return;
        }

        txt_OrderId.setText(orderDetails.getOrderId());

        List<OrderDetailsSkuBean> skus= orderDetails.getSkus();

        if(skus!=null) {

            OrderDetailsSkuAdapter orderDetailsSkuAdapter = new OrderDetailsSkuAdapter(OrderDetailsActivity.this, skus);
            list_Skus.setAdapter(orderDetailsSkuAdapter);
        }
    }

    //设置商品卡槽去货中
    private void setCurPickupSku(PickupSkuBean pickupSku) {

        try {
            if (pickupSku != null) {
                LogUtil.d(TAG, "当前取货:" + pickupSku.getName() + ",skuId:" + pickupSku.getSkuId() + ",slotId:" + pickupSku.getSlotId() + ",uniqueId:" + pickupSku.getUniqueId());
                CommonUtil.loadImageFromUrl(OrderDetailsActivity.this, curPickupSku_Img_Mainimg, pickupSku.getMainImgUrl());
                curPickupSku_Tv_Tip1.setText(pickupSku.getName());
                curPickupSku_Tv_Tip2.setText("准备出货......");

                CabinetBean cabinet = getDevice().getCabinets().get(pickupSku.getCabinetId());


                pickupEventNotify(pickupSku, 3011, "发起取货", null);


                switch (cabinet.getModelNo()) {
                    case "dsx01":

                        if (pickupSku.getCabinetId() == null) {
                            setPickupException(pickupSku,"准备出货异常......机柜编号为空");
                            return;
                        }

                        if (pickupSku.getSlotId() == null) {
                            setPickupException(pickupSku,"准备出货异常......货道编号为空");
                            return;
                        }

                        DSCabSlotNRC dsCabSlotNRC = DSCabSlotNRC.GetSlotNRC(pickupSku.getCabinetId(), pickupSku.getSlotId());
                        if (dsCabSlotNRC == null) {
                            setPickupException(pickupSku,"准备出货异常......机柜（" + pickupSku.getCabinetId() + "）货道编号（" + pickupSku.getSlotId() + "）解释错误");
                            return;
                        }

                        DSCabRowColLayoutBean dSCabRowColLayout = JSON.parseObject(cabinet.getRowColLayout(), new TypeReference<DSCabRowColLayoutBean>() {
                        });

                        if (dSCabRowColLayout == null) {
                            setPickupException(pickupSku,"准备出货异常......机柜货道解释异常");
                            return;
                        }

                        if(cabinetCtrlByDS==null) {
                            setPickupException(pickupSku, "准备出货异常......设备解释异常");
                            return;
                        }

                        cabinetCtrlByDS.startPickUp(isGoZero, dsCabSlotNRC.getRow(), dsCabSlotNRC.getCol(), dSCabRowColLayout.getPendantRows());
                        break;
                    case "zsx01":
                        String[] parms = pickupSku.getSlotId().split("-");
                        cabinetCtrlByZS.unLock(Integer.valueOf(parms[1]), Integer.valueOf(parms[0]));
                        break;
                }

            }
        }
        catch (Exception ex){
            LogUtil.e(TAG,ex);
            LogUtil.e(TAG,"准备启动异常："+ex.getMessage());
            setPickupException(pickupSku,"准备启动异常......");
            AppLogcatManager.saveLogcat2Server("logcat -v time  ", "startPickUp");
        }
    }

    private void setCurPickupSkuComplete(PickupSkuBean pickupSku) {

        if(pickupSku==null)
            return;

        List<OrderDetailsSkuBean> skus = orderDetails.getSkus();

        for (int i = 0; i < skus.size(); i++) {
            if (skus.get(i).getSkuId().equals(pickupSku.getSkuId())) {
                int quantityBySuccess = skus.get(i).getQuantityBySuccess();
                int quantityByException = skus.get(i).getQuantityByException();
                int quantity = skus.get(i).getQuantity();
                if ((quantityBySuccess + quantityByException) < quantity) {
                    skus.get(i).setQuantityBySuccess(quantityBySuccess + 1);
                }
                for (int j = 0; j < skus.get(i).getSlots().size(); j++) {
                    if (skus.get(i).getSlots().get(j).getSlotId().equals(pickupSku.getSlotId()) && skus.get(i).getSlots().get(j).getUniqueId().equals(pickupSku.getUniqueId())) {
                        skus.get(i).getSlots().get(j).setStatus(4000);
                    }
                }
            }
        }

        orderDetails.setSkus(skus);

        OrderDetailsSkuAdapter skuAdapter = new OrderDetailsSkuAdapter(OrderDetailsActivity.this, skus);
        list_Skus.setAdapter(skuAdapter);

        try {

            Thread.sleep(300);
        }
        catch (Exception ex){

        }

        curPickupSku = getCurrentPickupSku();
        if (curPickupSku != null) {
            setCurPickupSku(curPickupSku);
        } else {
            setAllPickupComplete();
        }

    }

    private void setPickupException(PickupSkuBean pickupSku,String remark){
        
        if(pickupSku==null)
            return;

        if(isHappneException) {

            if (!OrderDetailsActivity.this.isFinishing()) {
                getDialogBySystemWarn().setWarnTile("系统维护中..");
                getDialogBySystemWarn().setCloseVisibility(View.GONE);
                getDialogBySystemWarn().show();
            }

            if (cabinetCtrlByDS != null) {
                cabinetCtrlByDS.emgStop();
            }
            curPickupSku_Tv_Tip2.setText(remark);

            LogUtil.d(TAG, "取货失败:" + remark);

            AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS OrderDetailsActivity ", "pickup");

        }
    }

    private void pickupEventNotify(PickupSkuBean pickupSku, int pickupStatus, String remark, PickupActionResult actionResult) {

        if(pickupSku==null)
            return;

        if(orderDetails==null)
            return;

        try {

            IdWorker worker = new IdWorker(1,1,1);

            JSONObject content = new JSONObject();
            content.put("signId", worker.nextId());
            content.put("orderId", orderDetails.getOrderId());
            content.put("uniqueId", pickupSku.getUniqueId());
            content.put("skuId", pickupSku.getSkuId() );
            content.put("cabinetId", pickupSku.getCabinetId());
            content.put("slotId", pickupSku.getSlotId());
            content.put("pickupStatus", pickupStatus);
            if (actionResult != null) {
                content.put("actionId", actionResult.getActionId());
                content.put("actionName", actionResult.getActionName());
                content.put("actionStatusCode", actionResult.getActionStatusCode());
                content.put("actionStatusName", actionResult.getActionStatusName());
                content.put("pickupUseTime", actionResult.getPickupUseTime());
                content.put("imgId", actionResult.getImgId());
                content.put("imgId2", actionResult.getImgId2());
            }
            else
            {
                content.put("actionId", -1);
                content.put("actionName", "未知动作");
                content.put("actionStatusCode", 0);
                content.put("actionStatusName", "");
                content.put("pickupUseTime", 0);
                content.put("imgId", "");
                content.put("imgId2", "");
            }
            content.put("remark", remark);

            LogUtil.d(TAG,"pickupStatus1:" + pickupStatus);

            eventNotify("vending_pickup","商品取货", content);

            LogUtil.d(TAG,"pickupStatus2");

        } catch (Exception ex) {
            ex.printStackTrace();
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
                    dialog_Confirm.show();
                    break;
                case R.id.btn_ContactKefu:
                    getDialogBySystemWarn().setWarnTile("您好，需要提供帮助吗？");
                    getDialogBySystemWarn().setCloseVisibility(View.VISIBLE);
                    getDialogBySystemWarn().show();
                    break;
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

//        if (cabinetCtrlByDS != null) {
//            cabinetCtrlByDS.disConnect();
//            cabinetCtrlByDS = null;
//        }
//
//        if (cabinetCtrlByZS != null) {
//            cabinetCtrlByZS.disConnect();
//            cabinetCtrlByZS = null;
//        }
    }

    @Override
    public void onPause(){
        super.onPause();

//        if(cabinetCtrlByDS!=null){
//            cabinetCtrlByDS.disConnect();
//            cabinetCtrlByDS = null;
//        }
//
//        if (cabinetCtrlByZS != null) {
//            cabinetCtrlByZS.disConnect();
//            cabinetCtrlByZS = null;
//        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog_Confirm != null) {
            dialog_Confirm.cancel();
        }

        if (cabinetCtrlByDS != null) {
            cabinetCtrlByDS.disConnect();
        }

        if (cabinetCtrlByZS != null) {
            cabinetCtrlByZS.disConnect();
            cabinetCtrlByZS = null;
        }

        closePageCountTimerStop();

        CameraWindow.releaseCameraByJg();
        CameraWindow.releaseCameraByChk();

        TinySyncExecutor.getInstance().finish();

        new Thread(new Runnable() {
            public void run() {
                if(device!=null) {
                    if(device.isOpenLog()) {
                        AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS OrderDetailsActivity ", "order");
                    }
                }
            }
        }).start();

    }

}
