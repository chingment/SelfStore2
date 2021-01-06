package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.PickupAutoTestSlotAdapter;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.model.DSCabRowColLayoutBean;
import com.uplink.selfstore.model.DSCabSlotNRC;
import com.uplink.selfstore.model.PickupActionResult;
import com.uplink.selfstore.model.api.CabinetBean;
import com.uplink.selfstore.model.api.PickupSkuBean;
import com.uplink.selfstore.own.AppLogcatManager;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.ui.CameraWindow;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.ui.my.MyListView;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

public class CustomPickupAutoTestDialog extends Dialog {
    private static final String TAG = "CustomPickupAutoTestDialog";
    private View mLayoutRes;// 布局文件
    private BaseFragmentActivity mContext;
    private Dialog mThis;
    private View btn_close;


    private View btn_start;
    private View btn_stop;
    private View btn_exit;


    private TextView txt_sumQuantity;
    private TextView txt_waitPickupQuantity;
    private TextView txt_pickupedQuantity;
    private TextView txt_exQuantity;
    private TextView txt_pickupingQuantity;


    private MyListView list_Skus;
    private PickupSkuBean curPickupSku=null;
    private int curPickupSku_idx=-1;

    private ImageView curPickupSku_Img_Mainimg;
    private TextView curPickupSku_Tv_Tip1;
    private TextView curPickupSku_Tv_Tip2;

    private CabinetCtrlByDS cabinetCtrlByDS=null;

    private CabinetBean cabinet;
    private List<PickupSkuBean> pickupSkus;

    private boolean isHappneException=false;
    private boolean isExit=false;
    private String exceptionMessage="";

    public CustomPickupAutoTestDialog(final Context context) {
        super(context, R.style.dialog_style);
        mThis = this;
        mContext =  (BaseFragmentActivity) context;
        mLayoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_pickupautotest, null);

        initView();
        initEvent();
        initData();

        cabinetCtrlByDS = CabinetCtrlByDS.getInstance();
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
                        if (CommonUtil.isWifi(context)) {
                            isTakePic = true;
                        }

                        if (isTakePic) {
                            if (pickupActionResult == null) {
                                pickupActionResult = new PickupActionResult();
                            }

                            if (CameraWindow.cameraIsRunningByChk()) {
                                pickupActionResult.setImgId(UUID.randomUUID().toString());

                                LogUtil.e(TAG, "开始拍照");
                                CameraWindow.takeCameraPicByChk(pickupActionResult.getImgId());
                            }

                            if (CameraWindow.cameraIsRunningByJg()) {
                                pickupActionResult.setImgId2(UUID.randomUUID().toString());
                                CameraWindow.takeCameraPicByJg(pickupActionResult.getImgId2());
                            }
                        }

                        if (isExit) {

                            if (isHappneException) {
                                pickupEventNotify(curPickupSku, 6000, exceptionMessage, pickupActionResult);
                            }

                            if (cabinetCtrlByDS != null) {
                                cabinetCtrlByDS.emgStop();
                            }

                        } else {


                            if (isHappneException) {
                                if (cabinetCtrlByDS != null) {
                                    cabinetCtrlByDS.emgStop();
                                }
                                pickupEventNotify(curPickupSku, 6000, exceptionMessage, pickupActionResult);
                            } else {
                                switch (status) {
                                    case 1: //消息提示
                                        //context.showToast(message);
                                        break;
                                    case 2://取货就绪成功
                                        curPickupSku_Tv_Tip2.setText(message);
                                        break;
                                    case 3://取货中
                                        curPickupSku_Tv_Tip2.setText("正在取货中..请稍等");
                                        pickupEventNotify(curPickupSku, 3012, "取货中", pickupActionResult);
                                        break;
                                    case 4://取货成功
                                        curPickupSku_Tv_Tip2.setText("取货完成");
                                        pickupEventNotify(curPickupSku, 4000, "取货完成", pickupActionResult);
                                        break;
                                    case 5://取货失败，机器异常
                                        cabinetCtrlByDS.stopPickup();
                                        isHappneException = true;
                                        exceptionMessage = "取货失败,机器发生异常:" + message;
                                        LogUtil.e(TAG, exceptionMessage);
                                        curPickupSku_Tv_Tip2.setText(exceptionMessage);
                                        AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS CustomPickupAutoTestDialog ", "pickuptest");
                                        pickupEventNotify(curPickupSku, 6000, exceptionMessage, pickupActionResult);
                                        break;
                                    case 6://取货失败，程序异常
                                        cabinetCtrlByDS.stopPickup();
                                        isHappneException = true;
                                        exceptionMessage = "取货失败,程序异常:" + message;
                                        LogUtil.e(TAG, exceptionMessage);
                                        curPickupSku_Tv_Tip2.setText(exceptionMessage);
                                        AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS CustomPickupAutoTestDialog ", "pickuptest");
                                        pickupEventNotify(curPickupSku, 6000, exceptionMessage, pickupActionResult);
                                        break;
                                    default:
                                        cabinetCtrlByDS.stopPickup();
                                        isHappneException = true;
                                        exceptionMessage = "取货失败，未知状态:" + message;
                                        LogUtil.e(TAG, exceptionMessage);
                                        curPickupSku_Tv_Tip2.setText(exceptionMessage);
                                        AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS CustomPickupAutoTestDialog ", "pickuptest");
                                        pickupEventNotify(curPickupSku, 6000, exceptionMessage, pickupActionResult);
                                        break;
                                }
                            }

                        }

                        return false;
                    }


                })
        );

    }

    private void pickupEventNotify(final PickupSkuBean pickupSku, final int pickupStatus, String remark, PickupActionResult actionResult) {

        if (pickupSku == null)
            return;

        try {
            JSONObject content = new JSONObject();
            content.put("uniqueId", pickupSku.getUniqueId());
            content.put("productSkuId", pickupSku.getProductSkuId());
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
            else {
                content.put("actionId", 0);
                content.put("actionName", "未知动作");
                content.put("actionStatusCode", 0);
                content.put("actionStatusName", "");
                content.put("pickupUseTime", 0);
                content.put("imgId", "");
                content.put("imgId2", "");
            }
            content.put("remark", remark);
            LogUtil.d(TAG,"pickupStatus:" + pickupStatus);
            if (mContext != null) {
                mContext.eventNotify("PickupTest", "商品取货", content);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (isHappneException) {
            cabinetCtrlByDS.emgStop();
            curPickupSku_Tv_Tip2.setText(remark);
            pickupSkus.get(curPickupSku_idx).setStatus(6000);
            pickupSkus.get(curPickupSku_idx).setTips("取货异常");
            AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS ", "pickuptest");
        } else {

            switch (pickupStatus) {
                case 3011:
                    pickupSkus.get(curPickupSku_idx).setStatus(3011);
                    pickupSkus.get(curPickupSku_idx).setTips("取货中");

                    switch (cabinet.getModelNo()) {
                        case "dsx01":

                            if(pickupSku.getCabinetId()==null){
                                curPickupSku_Tv_Tip2.setText("准备出货异常......机柜编号为空");
                                return;
                            }

                            if(pickupSku.getSlotId()==null){
                                curPickupSku_Tv_Tip2.setText("准备出货异常......货道编号为空");
                                return;
                            }

                            DSCabSlotNRC dsCabSlotNRC = DSCabSlotNRC.GetSlotNRC(pickupSku.getCabinetId(), pickupSku.getSlotId());
                            if (dsCabSlotNRC == null) {
                                curPickupSku_Tv_Tip2.setText("准备出货异常......机柜（" + pickupSku.getCabinetId() + "）货道编号（" + pickupSku.getSlotId() + "）解释错误");
                                return;
                            }

                            DSCabRowColLayoutBean dSCabRowColLayout = JSON.parseObject(cabinet.getRowColLayout(), new TypeReference<DSCabRowColLayoutBean>() {
                            });
                            cabinetCtrlByDS.startPickUp(dsCabSlotNRC.getRow(), dsCabSlotNRC.getCol(), dSCabRowColLayout.getPendantRows());
                            break;
                    }
                    break;
                case 4000:

                    pickupSkus.get(curPickupSku_idx).setStatus(4000);
                    pickupSkus.get(curPickupSku_idx).setTips("取货成功");

                    curPickupSku = getCurrentPickupProductSku();
                    if (curPickupSku != null) {
                        setPickupNext(curPickupSku);
                    } else {
                        setPickupComplete();
                    }
                    break;
            }
        }

        setSlots(cabinet, pickupSkus);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(mLayoutRes);
    }

    protected void initView() {
        btn_close = ViewHolder.get(mLayoutRes, R.id.btn_close);
        btn_start = ViewHolder.get(mLayoutRes, R.id.btn_start);
        btn_stop = ViewHolder.get(mLayoutRes, R.id.btn_stop);
        btn_exit = ViewHolder.get(mLayoutRes, R.id.btn_exit);
        list_Skus = ViewHolder.get(mLayoutRes, R.id.list_skus);
        list_Skus.setFocusable(false);
        list_Skus.setClickable(false);
        list_Skus.setPressed(false);
        list_Skus.setEnabled(false);


        txt_sumQuantity = ViewHolder.get(mLayoutRes, R.id.txt_sumQuantity);
        txt_waitPickupQuantity = ViewHolder.get(mLayoutRes, R.id.txt_waitPickupQuantity);
        txt_pickupedQuantity = ViewHolder.get(mLayoutRes, R.id.txt_pickupedQuantity);
        txt_exQuantity = ViewHolder.get(mLayoutRes, R.id.txt_exQuantity);
        txt_pickupingQuantity=ViewHolder.get(mLayoutRes, R.id.txt_pickupingQuantity);


        curPickupSku_Img_Mainimg = ViewHolder.get(mLayoutRes, R.id.curpickupsku_img_main);
        curPickupSku_Tv_Tip1 = ViewHolder.get(mLayoutRes, R.id.curpickupsku_tip1);
        curPickupSku_Tv_Tip2 = ViewHolder.get(mLayoutRes, R.id.curpickupsku_tip2);
    }

    protected void initEvent() {


        final Dialog _this = this;

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(cabinetCtrlByDS.isIdle())
                {    isHappneException=false;
                    isExit=true;
                    exceptionMessage="空闲停止机器";
                }
                else {
                    isHappneException=true;
                    isExit=true;
                    exceptionMessage="非空闲停止机器";
                }

                cabinetCtrlByDS.emgStop();
                cabinetCtrlByDS.emgStop();
                _this.dismiss();
            }
        });

        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cabinetCtrlByDS.isIdle())
                {    isHappneException=false;
                    isExit=true;
                    exceptionMessage="空闲停止机器";
                }
                else {
                    isHappneException=true;
                    isExit=true;
                    exceptionMessage="非空闲停止机器";
                }
                cabinetCtrlByDS.emgStop();
                cabinetCtrlByDS.emgStop();
                _this.dismiss();
            }
        });

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isHappneException){
                    mContext.showToast("取货异常，请退出，检查机器");
                    return;
                }
                curPickupSku = getCurrentPickupProductSku();
                if (curPickupSku != null) {
                    setPickupNext(curPickupSku);
                }
            }
        });


        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cabinetCtrlByDS.isIdle())
                {    isHappneException=false;
                    isExit=true;
                    exceptionMessage="空闲停止机器";
                }
                else {
                    isHappneException=true;
                    isExit=true;
                    exceptionMessage="非空闲停止机器";
                }
                cabinetCtrlByDS.emgStop();
            }
        });

    }

    protected void initData() {


    }

    public void  setSlots(CabinetBean cabinet,List<PickupSkuBean> pickupSkus){

        this.cabinet=cabinet;
        this.pickupSkus=pickupSkus;
        PickupAutoTestSlotAdapter orderDetailsSkuAdapter = new PickupAutoTestSlotAdapter(mContext,pickupSkus) ;
        list_Skus.setAdapter(orderDetailsSkuAdapter);

        txt_sumQuantity.setText(pickupSkus.size()+"");

        int waitPickupQuantity=0;
        int pickupedQuantity=0;
        int exQuantity=0;
        int pickupingQuantity=0;
        for (int i=0;i<pickupSkus.size();i++) {
            PickupSkuBean sku = pickupSkus.get(i);
            if (sku.getStatus() == 3010) {
                waitPickupQuantity += 1;
            }
            else if (sku.getStatus() == 3011 || sku.getStatus() == 3012) {
                pickupingQuantity += 1;
            }
            else if (sku.getStatus() == 4000) {
                pickupedQuantity += 1;
            } else if (sku.getStatus() == 6000) {
                exQuantity += 1;
            }
        }

        txt_waitPickupQuantity.setText(waitPickupQuantity+"");
        txt_pickupingQuantity.setText(pickupingQuantity+"");
        txt_pickupedQuantity.setText(pickupedQuantity+"");
        txt_exQuantity.setText(exQuantity+"");

    }

    // 3010 待取货 3011 已发送取货命令 3012 取货中 4000 已完成 6000 异常
    private PickupSkuBean getCurrentPickupProductSku() {
        PickupSkuBean cur_pickupSku=null;
        curPickupSku_idx=-1;
        for (int i = 0; i < pickupSkus.size(); i++) {
            PickupSkuBean l_pickupSku = pickupSkus.get(i);
            if (l_pickupSku.getStatus() == 3010) {
                curPickupSku_idx = i;
                cur_pickupSku = new PickupSkuBean();
                cur_pickupSku.setProductSkuId(l_pickupSku.getProductSkuId());
                cur_pickupSku.setSlotId(l_pickupSku.getSlotId());
                cur_pickupSku.setName(l_pickupSku.getName());
                cur_pickupSku.setMainImgUrl(l_pickupSku.getMainImgUrl());
                cur_pickupSku.setCabinetId(l_pickupSku.getCabinetId());
                cur_pickupSku.setUniqueId(l_pickupSku.getUniqueId());
                cur_pickupSku.setStatus(l_pickupSku.getStatus());
                break;
            }
        }
        return cur_pickupSku;
    }

    private void setPickupNext(PickupSkuBean pickupSku) {
        if (pickupSku != null) {
            LogUtil.d(TAG,"当前取货:" + pickupSku.getName() + ",productSkuId:" + pickupSku.getProductSkuId() + ",slotId:" + pickupSku.getSlotId() + ",uniqueId:" + pickupSku.getUniqueId());
            CommonUtil.loadImageFromUrl(mContext, curPickupSku_Img_Mainimg, pickupSku.getMainImgUrl());
            curPickupSku_Tv_Tip1.setText(pickupSku.getName());
            curPickupSku_Tv_Tip2.setText("准备出货......");
            if(mThis.isShowing()) {
                pickupEventNotify(pickupSku, 3011, "发起取货", null);
            }
        }
    }

    private void setPickupComplete() {
        curPickupSku_Img_Mainimg.setImageResource(R.drawable.icon_pickupcomplete);
        curPickupSku_Tv_Tip1.setText("出货完成");
        curPickupSku_Tv_Tip2.setText("欢迎再次购买......");
    }

    @Override
    public void show(){
        super.show();
        isHappneException=false;
isExit=false;
        exceptionMessage="";
        curPickupSku_Img_Mainimg.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.default_image));
        curPickupSku_Tv_Tip1.setText("请点击开始测试");
        curPickupSku_Tv_Tip2.setText("开始测试前，请确保库存数量与机器实际库存数量一致");
        btn_start.setVisibility(View.VISIBLE);
        btn_stop.setVisibility(View.GONE);
        cabinetCtrlByDS.firstSet();
//        txt_sumQuantity.setText("0");
//        txt_waitPickupQuantity.setText("0");
//        txt_pickupedQuantity.setText("0");
//        txt_exQuantity.setText("0");
    }

    @Override
    public void cancel(){
        super.cancel();

        CameraWindow.releaseCameraByChk();
        CameraWindow.releaseCameraByJg();
    }
}
