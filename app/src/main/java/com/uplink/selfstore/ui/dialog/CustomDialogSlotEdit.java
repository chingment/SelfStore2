package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.SmDeviceStockActivity;
import com.uplink.selfstore.activity.adapter.SlotSkuSearchAdapter;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByZS;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.DSCabRowColLayoutBean;
import com.uplink.selfstore.model.PickupActionResult;
import com.uplink.selfstore.model.DSCabSlotNRC;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.CabinetBean;
import com.uplink.selfstore.model.api.DeviceBean;
import com.uplink.selfstore.model.api.RetSkuSearch;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.SearchSkuBean;
import com.uplink.selfstore.model.api.SlotBean;
import com.uplink.selfstore.app.AppCacheManager;
import com.uplink.selfstore.app.AppLogcatManager;
import com.uplink.selfstore.model.api.ReqUrl;
import com.uplink.selfstore.ui.CameraWindow;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;
//import com.uplink.selfstore.utils.ScanKeyManager;
import com.uplink.selfstore.utils.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomDialogSlotEdit extends Dialog {
    private static final String TAG = "CustomDialogSlotEdit";
    private View mLayoutRes;// 布局文件
    private SmDeviceStockActivity mContext;
    private Dialog mThis;

    private View dlg_Close;
    private ImageView iv_SkuImg;
    private TextView tv_SlotId;
    private TextView tv_StockId;
    private TextView tv_SlotName;
    private TextView tv_Version;
    private TextView tv_SkuId;
    private TextView tv_SkuCumCode;
    private TextView tv_SkuName;
    private TextView tv_SkuSpecDes;
    private TextView tv_SellQty;
    private TextView tv_LockQty;
    private TextView tv_SumQty;
    private TextView tv_MaxQty;
    private TextView tv_WrnQty;
    private Button btn_DeleteSlot;
    private Button btn_FillSlot;
    private Button btn_SaveSlot;
    private View btn_DecSumQty;
    private View btn_IncSumQty;
    private View btn_PickTest;
    private View btn_DecMaxQty;
    private View btn_IncMaxQty;
    private View btn_DecWrnQty;
    private View btn_IncWrnQty;

    private TextView tv_SearchSkuKey;
    private ImageButton btn_DeleteSearchSkuKey;
    private ListView lv_SearchSkus;

    private SlotBean slot;
    private CabinetBean cabinet;
    //private ScannerCtrl scannerCtrl;
    private CabinetCtrlByDS cabinetCtrlByDS;
    private CabinetCtrlByZS cabinetCtrlByZS;
    private CustomDialogLoading dialog_Running;
    //private ScanKeyManager scanKeyManager;
    private boolean isHappneException=false;
    private String exceptionMessage="";

    public CustomDialogSlotEdit(final Context context) {
        super(context, R.style.dialog_style);
        mThis = this;
        mContext = (SmDeviceStockActivity) context;
        mLayoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_slotedit, null);

        initView();
        initEvent();
        initData();

        cabinetCtrlByDS = CabinetCtrlByDS.getInstance();
        cabinetCtrlByDS.setPickupHandler(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                if(!CameraWindow.cameraIsRunningByChk()){
                    CameraWindow.openCameraByChk();
                }

                if(!CameraWindow.cameraIsRunningByJg()){
                    CameraWindow.openCameraByJg();
                }


                String slotId = String.valueOf(tv_SlotId.getText());
                String skuId = String.valueOf(tv_SkuId.getText());

                Bundle bundle = msg.getData();
                int status = bundle.getInt("status");
                String message = bundle.getString("message");
                PickupActionResult pickupActionResult = null;
                if (bundle.getSerializable("result") != null) {
                    pickupActionResult = (PickupActionResult) bundle.getSerializable("result");
                }


                boolean isTakePic=false;

                if(isHappneException) {
                    isTakePic = true;
                }

                if(!isTakePic) {
                    if (status == 5 || status > 6) {
                        isTakePic = true;
                    }
                }

                if(pickupActionResult!=null) {
                    if (pickupActionResult.getActionId() == 8) {
                        isTakePic = true;
                    }
                }

                //判断是使用WIFI网络，则每一步捕捉相片
//                if(CommonUtil.isWifi(context)) {
//                    isTakePic = true;
//                }

                if(isTakePic){
                    if(pickupActionResult==null){
                        pickupActionResult=new PickupActionResult();
                    }

                    if(CameraWindow.cameraIsRunningByChk()) {
                        pickupActionResult.setImgId(UUID.randomUUID().toString());
                        LogUtil.d(TAG,"开始拍照->出货口");
                        CameraWindow.takeCameraPicByChk(pickupActionResult.getImgId());
                    }

                    if(CameraWindow.cameraIsRunningByJg()) {
                        LogUtil.d(TAG,"开始拍照->机柜");
                        pickupActionResult.setImgId2(UUID.randomUUID().toString());
                        CameraWindow.takeCameraPicByJg(pickupActionResult.getImgId2());
                    }
                }


                if (isHappneException) {
                    if (cabinetCtrlByDS != null) {
                        cabinetCtrlByDS.emgStop();
                    }
                    pickupEventNotify(skuId, slotId, 6000, exceptionMessage, pickupActionResult);
                }
                else {
                    switch (status) {
                        case 1://消息提示
                            dialog_Running.hide();
                            mContext.showToast(message);
                            break;
                        case 2://启动就绪成功，弹出窗口，同时默认120秒关闭窗口
                            dialog_Running.setTipsText(message);
                            dialog_Running.show();
                            break;
                        case 3://取货中
                            dialog_Running.setTipsText("正在取货中..请稍等");
                            if (pickupActionResult != null) {
                                pickupEventNotify(skuId, slotId, 3012, "发起取货", pickupActionResult);
                            }
                            break;
                        case 4://取货成功
                            dialog_Running.hide();
                            if (pickupActionResult != null) {
                                mContext.showToast("取货完成");
                                pickupEventNotify(skuId, slotId, 4000, "取货完成", pickupActionResult);

                            }
                            break;
                        case 5://取货超时
                            isHappneException = true;
                            exceptionMessage = "取货失败,设备发生异常:" + message;
                            LogUtil.d(TAG, exceptionMessage);
                            dialog_Running.hide();
                            AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS CustomDialogSlotEdit ", "pickuptest");
                            pickupEventNotify(skuId, slotId, 6000, exceptionMessage, pickupActionResult);
                            mContext.showToast(exceptionMessage);
                            break;
                        case 6://取货失败
                            isHappneException = true;
                            exceptionMessage = "取货失败,程序发生异常:" + message;
                            LogUtil.d(TAG, exceptionMessage);
                            dialog_Running.hide();
                            AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS CustomDialogSlotEdit ", "pickuptest");
                            pickupEventNotify(skuId, slotId, 6000, exceptionMessage, pickupActionResult);
                            mContext.showToast(exceptionMessage);
                            break;
                        default:
                            isHappneException = true;
                            exceptionMessage = "取货失败，未知状态:" + message;
                            LogUtil.d(TAG, exceptionMessage);
                            dialog_Running.hide();
                            AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS CustomDialogSlotEdit ", "pickuptest");
                            pickupEventNotify(skuId, slotId, 6000, exceptionMessage, pickupActionResult);
                            break;
                    }
                }
                return false;
            }
        }));

        cabinetCtrlByZS = CabinetCtrlByZS.getInstance();
        cabinetCtrlByZS.setHandler(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                String slotId = String.valueOf(tv_SlotId.getText());
                String skuId = String.valueOf(tv_SkuId.getText());

                switch (msg.what) {
                    case CabinetCtrlByZS.MESSAGE_WHAT_ONEUNLOCK:
                        Bundle bundle = msg.getData();
                        int status = bundle.getInt("status");
                        String message = bundle.getString("message");
                        switch (status) {
                            case 1://消息提示
                                dialog_Running.hide();
                                mContext.showToast(message);
                                break;
                            case 2://启动就绪成功
                                dialog_Running.setTipsText("取货就绪成功");
                                dialog_Running.show();
                                break;
//                            case 3://取货中
//                                if (customDialogRunning != null) {
//                                    customDialogRunning.setProgressText("正在取货中..请稍等");
//                                    pickupEventNotify(skuId, slotId, 3012, "发起取货", null);
//                                }
//                                mContext.showToast(message);
//                                break;
                            case 3:
                            case 4:
                                dialog_Running.hide();

                                mContext.showToast("取货完成");
                                pickupEventNotify(skuId, slotId, 4000, "取货完成", null);

//                                CabinetCtrlByZS.ZSCabBoxStatusResult result = (CabinetCtrlByZS.ZSCabBoxStatusResult) bundle.getSerializable("result");
//                                if (result != null) {
//                                    if (result.getCabBoxs() != null) {
//                                        ZSCabBoxBean zsCabBoxBean = result.getCabBoxs().get(Integer.valueOf(slotId));
//                                        if (zsCabBoxBean != null) {
//                                            if (zsCabBoxBean.isOpen()) {
//                                                if (customDialogRunning != null && customDialogRunning.isShowing()) {
//                                                    customDialogRunning.cancelDialog();
//                                                }
//                                                mContext.showToast("取货完成");
//                                                pickupEventNotify(skuId, slotId, 4000, "取货完成", null);
//                                            }
//                                        }
//                                    }
//                                }

                                break;
                            case 5://取货超时
                                dialog_Running.hide();
                                mContext.showToast(message);
                                pickupEventNotify(skuId, slotId, 6000, "取货超时", null);
                                LogUtil.d(TAG,"取货超时");
                                break;
                            case 6://取货失败
                                if (dialog_Running != null) {
                                    dialog_Running.hide();
                                }
                                mContext.showToast(message);
                                pickupEventNotify(skuId, slotId, 6000, "取货失败[" + message + "]", null);
                                LogUtil.d(TAG,"取货失败");
                                break;
                        }
                        break;
                }
                return false;
            }
        }));

//        if (mContext.getDevice().getScanner().getUse()) {
//            scannerCtrl = ScannerCtrl.getInstance();
//            scannerCtrl.setScanHandler(new Handler(new Handler.Callback() {
//                        @Override
//                        public boolean handleMessage(Message msg) {
//                            Bundle bundle;
//                            bundle = msg.getData();
//                            String scanResult = bundle.getString("result");
//                            txt_searchKey.setText(scanResult);
//                            searchSkus(scanResult);
//                            return false;
//                        }
//                    })
//            );
//        }

        setCanceledOnTouchOutside(false);
//        scanKeyManager = new ScanKeyManager(new ScanKeyManager.OnScanValueListener() {
//            @Override
//            public void onScanValue(String value) {
//                LogUtil.e(TAG, value);
//                if(!StringUtil.isEmptyNotNull(value)){
//                    txt_searchKey.setText(value);
//                    searchSkus(value);
//                }
//            }
//        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(mLayoutRes);
    }

    protected void initView() {
        dlg_Close = ViewHolder.get(mLayoutRes, R.id.dlg_Close);
        tv_SearchSkuKey = ViewHolder.get(mLayoutRes, R.id.tv_SearchSkuKey);
        lv_SearchSkus = ViewHolder.get(mLayoutRes, R.id.lv_SearchSkus);
        btn_DeleteSearchSkuKey = ViewHolder.get(mLayoutRes, R.id.btn_DeleteSearchSkuKey);
        tv_Version=ViewHolder.get(mLayoutRes, R.id.tv_Version);
        iv_SkuImg = ViewHolder.get(mLayoutRes, R.id.iv_SkuImg);
        tv_SlotId = ViewHolder.get(mLayoutRes, R.id.tv_SlotId);
        tv_SlotName = ViewHolder.get(mLayoutRes, R.id.tv_SlotName);
        tv_StockId  = ViewHolder.get(mLayoutRes, R.id.tv_StockId);
        tv_SkuCumCode= ViewHolder.get(mLayoutRes, R.id.tv_SkuCumCode);
        tv_SkuId = ViewHolder.get(mLayoutRes, R.id.tv_SkuId);
        tv_SkuName = ViewHolder.get(mLayoutRes, R.id.tv_SkuName);
        tv_SkuSpecDes= ViewHolder.get(mLayoutRes, R.id.tv_SkuSpecDes);
        tv_SellQty = ViewHolder.get(mLayoutRes, R.id.tv_SellQty);
        tv_LockQty = ViewHolder.get(mLayoutRes, R.id.tv_LockQty);
        tv_SumQty = ViewHolder.get(mLayoutRes, R.id.tv_SumQty);
        tv_MaxQty = ViewHolder.get(mLayoutRes, R.id.tv_MaxQty);
        tv_WrnQty= ViewHolder.get(mLayoutRes, R.id.tv_WrnQty);
        btn_PickTest = ViewHolder.get(mLayoutRes, R.id.btn_PickTest);
        btn_DeleteSlot = ViewHolder.get(mLayoutRes, R.id.btn_DeleteSlot);
        btn_FillSlot = ViewHolder.get(mLayoutRes, R.id.btn_FillSlot);
        btn_SaveSlot = ViewHolder.get(mLayoutRes, R.id.btn_SaveSlot);
        btn_DecSumQty = ViewHolder.get(mLayoutRes, R.id.btn_DecSumQty);
        btn_IncSumQty = ViewHolder.get(mLayoutRes, R.id.btn_IncSumQty);
        btn_DecMaxQty = ViewHolder.get(mLayoutRes, R.id.btn_DecMaxQty);
        btn_IncMaxQty = ViewHolder.get(mLayoutRes, R.id.btn_IncMaxQty);
        btn_DecWrnQty = ViewHolder.get(mLayoutRes, R.id.btn_DecWrnQty);
        btn_IncWrnQty = ViewHolder.get(mLayoutRes, R.id.btn_IncWrnQty);
        dialog_Running = new CustomDialogLoading(this.mContext);

    }

    protected void initEvent() {

        dlg_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThis.dismiss();
            }
        });

        btn_DeleteSearchSkuKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String val = tv_SearchSkuKey.getText().toString();
                if (val.length() >= 1) {
                    val = val.substring(0, val.length() - 1);
                }
                tv_SearchSkuKey.setText(val);
                searchSkus(val);
            }
        });

        btn_PickTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String slotId = String.valueOf(tv_SlotId.getText());

                String skuId=String.valueOf(tv_SkuId.getText());

                pickupEventNotify(skuId,slotId,3011,"发起取货",null);

                switch (cabinet.getModelNo()){
                    case "dsx01":
                        cabinetCtrlByDS=CabinetCtrlByDS.getInstance();

                        if(cabinet.getCabinetId()==null){
                            mContext.showToast("准备出货异常......机柜编号为空");
                            return;
                        }

                        DSCabSlotNRC dsCabSlotNRC = DSCabSlotNRC.GetSlotNRC(cabinet.getCabinetId(), slotId);
                        if (dsCabSlotNRC == null) {
                            mContext.showToast("准备出货异常......机柜（" + cabinet.getCabinetId() + "）货道编号（" + slotId+ "）解释错误，");
                            return;
                        }

                        if (!cabinetCtrlByDS.isConnect()) {
                            mContext.showToast("设备连接失败");
                            return;
                        }

                        if (!cabinetCtrlByDS.isNormarl()) {
                            mContext.showToast("设备状态异常");
                            return;
                        }

                        DSCabRowColLayoutBean dSCabRowColLayout= JSON.parseObject(cabinet.getRowColLayout(), new TypeReference<DSCabRowColLayoutBean>() {});
                        cabinetCtrlByDS.startPickUp(true, dsCabSlotNRC.getRow(), dsCabSlotNRC.getCol(),dSCabRowColLayout.getPendantRows());
                        break;
                    case "zsx01":
                        String[] parms=slotId.split("-");
                        cabinetCtrlByZS.unLock(Integer.valueOf(parms[1]),Integer.valueOf(parms[0]));
                        break;
                }
            }
        });

        btn_DeleteSlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StringUtil.isEmptyNotNull(tv_SkuId.getText() + "")) {
                    mContext.showToast("没有可删除的商品");
                    return;
                }

                tv_StockId.setText("");
                tv_SkuId.setText("");
                tv_SkuName.setText("暂无设置");
                tv_SkuSpecDes.setText("");
                tv_SkuCumCode.setText("");
                tv_SellQty.setText("0");
                tv_LockQty.setText("0");
                tv_SumQty.setText("0");
                iv_SkuImg.setImageResource(R.drawable.default_image);

            }
        });

        btn_FillSlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtil.isEmptyNotNull(tv_SkuId.getText() + "")) {
                    mContext.showToast("请先设置商品");
                    return;
                }
                int sumQty = Integer.valueOf(tv_MaxQty.getText() + "");
                int lockQty = Integer.valueOf(tv_LockQty.getText() + "");
                int sellQty = sumQty - lockQty;
                tv_SellQty.setText(String.valueOf(sellQty));
                tv_SumQty.setText(String.valueOf(sumQty));

            }
        });

        btn_SaveSlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DeviceBean device = AppCacheManager.getDevice();

                String stockId = String.valueOf(tv_StockId.getText());
                String slotId = String.valueOf(tv_SlotId.getText());
                String skuId = String.valueOf(tv_SkuId.getText());
                int version=Integer.valueOf(tv_Version.getText()+"");
                int sellQuantity = Integer.valueOf(tv_SellQty.getText() + "");
                int sumQuantity = Integer.valueOf(tv_SumQty.getText() + "");
                int maxQuantity = Integer.valueOf(tv_MaxQty.getText() + "");
                int warnQuantity = Integer.valueOf(tv_WrnQty.getText() + "");

                if(!StringUtil.isEmptyNotNull(skuId)) {
                    if (maxQuantity < sumQuantity) {
                        mContext.showToast("保存失败，最大数量不能小于实际数据");
                        return;
                    }

                    if (warnQuantity > maxQuantity) {
                        mContext.showToast("保存失败，报警数量不能大于最大数量");
                        return;
                    }
                }

                Map<String, Object> params = new HashMap<>();
                params.put("slotId", slotId);
                params.put("stockId", stockId);
                params.put("deviceId", device.getDeviceId());
                params.put("cabinetId", cabinet.getCabinetId());
                params.put("skuId", skuId);
                params.put("sumQuantity", sumQuantity);
                params.put("holdQuantity", 0);
                params.put("warnQuantity", warnQuantity);
                params.put("maxQuantity", maxQuantity);

                params.put("version", version);

                mContext.postByMy(mContext, ReqUrl.stockSetting_SaveCabinetSlot, params, true, mContext.getString(R.string.tips_hanlding), new HttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {

                        ApiResultBean<SlotBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<SlotBean>>() {
                        });

                        mContext.showToast(rt.getMessage());

                        if (rt.getResult() == Result.SUCCESS) {
                            mThis.dismiss();
                            mContext.setSlot(rt.getData());
                        }
                    }

                    @Override
                    public void onFailure(String msg, Exception e) {

                    }
                });
            }
        });

        btn_DecSumQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StringUtil.isEmptyNotNull(tv_SkuId.getText() + "")) {
                    mContext.showToast("请先设置商品");
                    return;
                }

                int maxQty = Integer.valueOf(tv_MaxQty.getText() + "");
                int sumQty = Integer.valueOf(tv_SumQty.getText() + "");
                int lockQty = Integer.valueOf(tv_LockQty.getText() + "");

                if (sumQty > lockQty) {
                    sumQty -= 1;
                    int sellQty = sumQty - lockQty;
                    tv_SellQty.setText(String.valueOf(sellQty));
                    tv_SumQty.setText(String.valueOf(sumQty));
            }
            }
        });

        btn_IncSumQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StringUtil.isEmptyNotNull(tv_SkuId.getText() + "")) {
                    mContext.showToast("请先设置商品");
                    return;
                }


                int maxQty = Integer.valueOf(tv_MaxQty.getText() + "");
                int sumQty = Integer.valueOf(tv_SumQty.getText() + "");
                int lockQty = Integer.valueOf(tv_LockQty.getText() + "");
                sumQty += 1;



                int sellQty = sumQty - lockQty;
                tv_SellQty.setText(String.valueOf(sellQty));
                tv_SumQty.setText(String.valueOf(sumQty));

                if(sumQty>maxQty){
                    tv_MaxQty.setText(String.valueOf(sumQty));
                }
            }
        });

        btn_DecMaxQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StringUtil.isEmptyNotNull(tv_SkuId.getText() + "")) {
                    mContext.showToast("请先设置商品");
                    return;
                }


                int sumQty = Integer.valueOf(tv_SumQty.getText() + "");
                int maxQty = Integer.valueOf(tv_MaxQty.getText() + "");


                if (maxQty > 0) {
                    maxQty = maxQty - 1;
                }

                if(maxQty<sumQty){
                    mContext.showToast("最大数量不能小于实际数量");
                    return;
                }

                tv_MaxQty.setText(String.valueOf(maxQty));
            }
        });

        btn_IncMaxQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StringUtil.isEmptyNotNull(tv_SkuId.getText() + "")) {
                    mContext.showToast("请先设置商品");
                    return;
                }

                int maxQty = Integer.valueOf(tv_MaxQty.getText() + "");

                maxQty = maxQty + 1;


                tv_MaxQty.setText(String.valueOf(maxQty));
            }
        });

        btn_DecWrnQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StringUtil.isEmptyNotNull(tv_SkuId.getText() + "")) {
                    mContext.showToast("请先设置商品");
                    return;
                }


                int sumQty = Integer.valueOf(tv_SumQty.getText() + "");
                int wrnQty = Integer.valueOf(tv_WrnQty.getText() + "");


                if (wrnQty > 0) {
                    wrnQty = wrnQty - 1;
                }

                if(wrnQty<sumQty){
                    mContext.showToast("报警数据不能小于实际数量");
                    return;
                }

                tv_WrnQty.setText(String.valueOf(wrnQty));
            }
        });

        btn_IncWrnQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StringUtil.isEmptyNotNull(tv_SkuId.getText() + "")) {
                    mContext.showToast("请先设置商品");
                    return;
                }

                int wrnQty = Integer.valueOf(tv_WrnQty.getText() + "");

                wrnQty = wrnQty + 1;


                tv_WrnQty.setText(String.valueOf(wrnQty));
            }
        });


        LinearLayout all_key = ViewHolder.get(mLayoutRes, R.id.all_key);
        for (int i = 0; i < all_key.getChildCount(); i++) {
            LinearLayout viewchild = (LinearLayout) all_key.getChildAt(i);

            for (int j = 0; j < viewchild.getChildCount(); j++) {

                Button key_btn = (Button) viewchild.getChildAt(j);

                key_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String key = v.getTag().toString();
                        getKey(key);
                    }
                });
            }

        }
    }

    protected void initData() {


    }

    public void setData(CabinetBean cabinet,SlotBean slot) {
        this.cabinet = cabinet;
        this.slot = slot;

        tv_SlotId.setText(slot.getSlotId());
        tv_SlotName.setText(slot.getSlotName());
        if (StringUtil.isEmptyNotNull(slot.getSkuId())) {
            tv_Version.setText(String.valueOf(slot.getVersion()));
            tv_StockId.setText("");
            tv_SkuId.setText("");
            tv_SkuCumCode.setText("");
            tv_SkuName.setText("暂无设置");
            tv_SkuSpecDes.setText("");
            tv_SellQty.setText("0");
            tv_LockQty.setText("0");
            tv_SumQty.setText("0");
            tv_MaxQty.setText("0");
            tv_WrnQty.setText("0");
            iv_SkuImg.setImageResource(R.drawable.default_image);
        } else {
            tv_Version.setText(String.valueOf(slot.getVersion()));
            tv_StockId.setText(slot.getStockId());
            tv_SkuId.setText(slot.getSkuId());
            tv_SkuCumCode.setText(slot.getSkuCumCode());
            tv_SkuName.setText(slot.getSkuName());
            tv_SkuSpecDes.setText(slot.getSkuSpecDes());
            tv_SellQty.setText(String.valueOf(slot.getSellQuantity()));
            tv_LockQty.setText(String.valueOf(slot.getLockQuantity()));
            tv_SumQty.setText(String.valueOf(slot.getSumQuantity()));
            tv_MaxQty.setText(String.valueOf(slot.getMaxQuantity()));
            tv_WrnQty.setText(String.valueOf(slot.getWarnQuantity()));
            if(slot.getCanAlterMaxQuantity()!=null) {
                if (slot.getCanAlterMaxQuantity()) {
                    btn_DecMaxQty.setVisibility(View.VISIBLE);
                    btn_IncMaxQty.setVisibility(View.VISIBLE);
                } else {
                    btn_DecMaxQty.setVisibility(View.INVISIBLE);
                    btn_IncMaxQty.setVisibility(View.INVISIBLE);
                }
            }

            CommonUtil.loadImageFromUrl(mContext, iv_SkuImg, slot.getSkuMainImgUrl());
        }
    }

    private void getKey(String key) {
        String val = tv_SearchSkuKey.getText() + key;
        tv_SearchSkuKey.setText(val);
        searchSkus(val);
    }

    public void searchSkus(String key) {
        tv_SearchSkuKey.setText(key);
        Map<String, Object> params = new HashMap<>();

        DeviceBean device = AppCacheManager.getDevice();

        params.put("deviceId", device.getDeviceId());
        params.put("key", key);

        mContext.postByMy(mContext, ReqUrl.product_SearchSku, params, false, "正在寻找", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);


                ApiResultBean<RetSkuSearch> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<RetSkuSearch>>() {
                });


                if (rt.getResult() == Result.SUCCESS) {
                    RetSkuSearch d = rt.getData();

                    SlotSkuSearchAdapter slotSkuSearchAdapter = new SlotSkuSearchAdapter(mContext, d.getSkus());
                    slotSkuSearchAdapter.setCallBackListener(new SlotSkuSearchAdapter.CallBackListener() {
                        @Override
                        public void setSlot(SearchSkuBean sku) {

                            tv_SkuId.setText(sku.getSkuId());
                            tv_SkuCumCode.setText(sku.getCumCode());
                            tv_SkuName.setText(sku.getName());
                            tv_SkuSpecDes.setText(sku.getSpecDes());
                            tv_SellQty.setText("0");
                            tv_LockQty.setText("0");
                            tv_SumQty.setText("0");
                            tv_WrnQty.setText("0");
                            CommonUtil.loadImageFromUrl(mContext, iv_SkuImg, sku.getMainImgUrl());

                        }
                    });

                    lv_SearchSkus.setAdapter(slotSkuSearchAdapter);

                    if (d.getSkus() != null) {
                        if (d.getSkus().size() == 1) {
                            SearchSkuBean sku = d.getSkus().get(0);
                            tv_SkuId.setText(sku.getSkuId());
                            tv_SkuName.setText(sku.getName());
                            tv_SkuCumCode.setText(sku.getCumCode());
                            tv_SkuSpecDes.setText(sku.getSpecDes());
                            CommonUtil.loadImageFromUrl(mContext, iv_SkuImg, sku.getMainImgUrl());
                        }
                    }
                }
            }
        });
    }

    public void clearSearch() {
        tv_SearchSkuKey.setText("");
        SlotSkuSearchAdapter slotSkuSearchAdapter = new SlotSkuSearchAdapter(mContext, new ArrayList<SearchSkuBean>());
        lv_SearchSkus.setAdapter(slotSkuSearchAdapter);
    }

    private void pickupEventNotify(final String skuId, final String slotId,final int pickupStatus, String remark,PickupActionResult actionResult) {

        try {
            JSONObject content = new JSONObject();

            content.put("skuId", skuId);
            content.put("cabinetId", cabinet.getCabinetId());
            content.put("slotId", slotId);
            content.put("pickupStatus", pickupStatus);
            content.put("remark", remark);
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

            mContext.eventNotify("vending_pickup_test","商品测试取货", content);
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
        super.cancel();


        if (dialog_Running != null) {
            dialog_Running.cancel();
        }

        CameraWindow.releaseCameraByChk();
        CameraWindow.releaseCameraByJg();
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        //scanKeyManager.analysisKeyEvent(event);
        return true;
    }

    @Override
    public void show() {
        super.show();
        isHappneException = false;
        exceptionMessage="";
    }
}
