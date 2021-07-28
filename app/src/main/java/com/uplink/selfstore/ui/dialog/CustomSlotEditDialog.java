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
import com.uplink.selfstore.model.api.SkuSearchResultBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.SearchSkuBean;
import com.uplink.selfstore.model.api.SlotBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppLogcatManager;
import com.uplink.selfstore.own.Config;
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

public class CustomSlotEditDialog extends Dialog {
    private static final String TAG = "CustomSlotEditDialog";
    private View mLayoutRes;// 布局文件
    private SmDeviceStockActivity mContext;
    private Dialog mThis;

    private View btn_close;
    private TextView txt_searchKey;
    private ImageView img_SkuImg;
    private TextView txt_SlotId;
    private TextView txt_StockId;
    private TextView txt_SlotName;
    private TextView txt_Version;
    private TextView txt_SkuId;
    private TextView txt_SkuCumCode;
    private TextView txt_SkuName;
    private TextView txt_SkuSpecDes;
    private TextView txt_SellQty;
    private TextView txt_LockQty;
    private TextView txt_SumQty;
    private TextView txt_MaxQty;
    private TextView txt_WrnQty;
    private ImageButton btn_keydelete;
    private Button btn_delete;
    private Button btn_fill;
    private Button btn_save;
    private View btn_decrease;
    private View btn_increase;
    private View btn_pick_test;
    private View btn_decreasebymax;
    private View btn_increasebymax;
    private View btn_decreasebywrn;
    private View btn_increasebywrn;

    private ListView list_search_skus;
    private SlotBean slot;
    private CabinetBean cabinet;
    //private ScannerCtrl scannerCtrl;
    private CabinetCtrlByDS cabinetCtrlByDS;
    private CabinetCtrlByZS cabinetCtrlByZS;
    private CustomLoadingDialog dialog_Running;
    //private ScanKeyManager scanKeyManager;
    private boolean isHappneException=false;
    private String exceptionMessage="";

    public CustomSlotEditDialog(final Context context) {
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


                String slotId = String.valueOf(txt_SlotId.getText());
                String skuId = String.valueOf(txt_SkuId.getText());

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
                        LogUtil.e(TAG,"开始拍照->出货口");
                        CameraWindow.takeCameraPicByChk(pickupActionResult.getImgId());
                    }

                    if(CameraWindow.cameraIsRunningByJg()) {
                        LogUtil.e(TAG,"开始拍照->机柜");
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
                            dialog_Running.setProgressText(message);
                            dialog_Running.show();
                            break;
                        case 3://取货中
                            dialog_Running.setProgressText("正在取货中..请稍等");
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
                            LogUtil.e(TAG, exceptionMessage);
                            dialog_Running.hide();
                            AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS CustomSlotEditDialog ", "pickuptest");
                            pickupEventNotify(skuId, slotId, 6000, exceptionMessage, pickupActionResult);
                            mContext.showToast(exceptionMessage);
                            break;
                        case 6://取货失败
                            isHappneException = true;
                            exceptionMessage = "取货失败,程序发生异常:" + message;
                            LogUtil.e(TAG, exceptionMessage);
                            dialog_Running.hide();
                            AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS CustomSlotEditDialog ", "pickuptest");
                            pickupEventNotify(skuId, slotId, 6000, exceptionMessage, pickupActionResult);
                            mContext.showToast(exceptionMessage);
                            break;
                        default:
                            isHappneException = true;
                            exceptionMessage = "取货失败，未知状态:" + message;
                            LogUtil.e(TAG, exceptionMessage);
                            dialog_Running.hide();
                            AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS CustomSlotEditDialog ", "pickuptest");
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

                String slotId = String.valueOf(txt_SlotId.getText());
                String skuId = String.valueOf(txt_SkuId.getText());

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
                                dialog_Running.setProgressText("取货就绪成功");
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
                                LogUtil.e(TAG,"取货超时");
                                break;
                            case 6://取货失败
                                if (dialog_Running != null) {
                                    dialog_Running.hide();
                                }
                                mContext.showToast(message);
                                pickupEventNotify(skuId, slotId, 6000, "取货失败[" + message + "]", null);
                                LogUtil.e(TAG,"取货失败");
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
        btn_close = ViewHolder.get(mLayoutRes, R.id.btn_close);
        txt_searchKey = ViewHolder.get(mLayoutRes, R.id.txt_searchKey);

        txt_Version=ViewHolder.get(mLayoutRes, R.id.txt_Version);
        img_SkuImg = ViewHolder.get(mLayoutRes, R.id.img_SkuImg);
        txt_SlotId = ViewHolder.get(mLayoutRes, R.id.txt_SlotId);
        txt_SlotName = ViewHolder.get(mLayoutRes, R.id.txt_SlotName);
        txt_StockId  = ViewHolder.get(mLayoutRes, R.id.txt_StockId);
        txt_SkuCumCode= ViewHolder.get(mLayoutRes, R.id.txt_SkuCumCode);
        txt_SkuId = ViewHolder.get(mLayoutRes, R.id.txt_SkuId);
        txt_SkuName = ViewHolder.get(mLayoutRes, R.id.txt_SkuName);
        txt_SkuSpecDes= ViewHolder.get(mLayoutRes, R.id.txt_SkuSpecDes);
        txt_SellQty = ViewHolder.get(mLayoutRes, R.id.txt_SellQty);
        txt_LockQty = ViewHolder.get(mLayoutRes, R.id.txt_LockQty);
        txt_SumQty = ViewHolder.get(mLayoutRes, R.id.txt_SumQty);
        txt_MaxQty = ViewHolder.get(mLayoutRes, R.id.txt_MaxQty);
        txt_WrnQty= ViewHolder.get(mLayoutRes, R.id.txt_WrnQty);
        list_search_skus = ViewHolder.get(mLayoutRes, R.id.list_search_skus);
        btn_keydelete = ViewHolder.get(mLayoutRes, R.id.btn_keydelete);
        btn_pick_test = ViewHolder.get(mLayoutRes, R.id.btn_pick_test);
        btn_delete = ViewHolder.get(mLayoutRes, R.id.btn_delete);
        btn_fill = ViewHolder.get(mLayoutRes, R.id.btn_fill);
        btn_decrease = ViewHolder.get(mLayoutRes, R.id.btn_decrease);
        btn_increase = ViewHolder.get(mLayoutRes, R.id.btn_increase);
        btn_save = ViewHolder.get(mLayoutRes, R.id.btn_save);
        btn_decreasebymax = ViewHolder.get(mLayoutRes, R.id.btn_decreasebymax);
        btn_increasebymax = ViewHolder.get(mLayoutRes, R.id.btn_increasebymax);
        btn_decreasebywrn = ViewHolder.get(mLayoutRes, R.id.btn_decreasebywrn);
        btn_increasebywrn = ViewHolder.get(mLayoutRes, R.id.btn_increasebywrn);
        dialog_Running = new CustomLoadingDialog(this.mContext);

    }

    protected void initEvent() {


        final Dialog _this = this;

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                _this.dismiss();
            }
        });

        btn_keydelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String val = txt_searchKey.getText().toString();
                if (val.length() >= 1) {
                    val = val.substring(0, val.length() - 1);
                }
                txt_searchKey.setText(val);
                searchSkus(val);
            }
        });

        btn_pick_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String slotId = String.valueOf(txt_SlotId.getText());

                String skuId=String.valueOf(txt_SkuId.getText());

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

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StringUtil.isEmptyNotNull(txt_SkuId.getText() + "")) {
                    mContext.showToast("没有可删除的商品");
                    return;
                }

                txt_StockId.setText("");
                txt_SkuId.setText("");
                txt_SkuName.setText("暂无设置");
                txt_SkuSpecDes.setText("");
                txt_SkuCumCode.setText("");
                txt_SellQty.setText("0");
                txt_LockQty.setText("0");
                txt_SumQty.setText("0");
                img_SkuImg.setImageResource(R.drawable.default_image);

            }
        });

        btn_fill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtil.isEmptyNotNull(txt_SkuId.getText() + "")) {
                    mContext.showToast("请先设置商品");
                    return;
                }
                int sumQty = Integer.valueOf(txt_MaxQty.getText() + "");
                int lockQty = Integer.valueOf(txt_LockQty.getText() + "");
                int sellQty = sumQty - lockQty;
                txt_SellQty.setText(String.valueOf(sellQty));
                txt_SumQty.setText(String.valueOf(sumQty));

            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DeviceBean device = AppCacheManager.getDevice();

                String stockId = String.valueOf(txt_StockId.getText());
                String slotId = String.valueOf(txt_SlotId.getText());
                String skuId = String.valueOf(txt_SkuId.getText());
                int version=Integer.valueOf(txt_Version.getText()+"");
                int sellQuantity = Integer.valueOf(txt_SellQty.getText() + "");
                int sumQuantity = Integer.valueOf(txt_SumQty.getText() + "");
                int maxQuantity = Integer.valueOf(txt_MaxQty.getText() + "");
                int warnQuantity = Integer.valueOf(txt_WrnQty.getText() + "");
                if(maxQuantity<sumQuantity){

                    mContext.showToast("保存失败，最大数量不能小于实际数据");
                    return;
                }

                if(warnQuantity>sumQuantity){
                    mContext.showToast("保存失败，报警数量不能大于实际数据");
                    return;
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

                mContext.postByMy(mContext,Config.URL.stockSetting_SaveCabinetSlot, params, null, true, mContext.getString(R.string.tips_hanlding), new HttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {

                        ApiResultBean<SlotBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<SlotBean>>() {
                        });

                        mContext.showToast(rt.getMessage());

                        if (rt.getResult() == Result.SUCCESS) {
                            _this.dismiss();
                            mContext.setSlot(rt.getData());
                        }
                    }

                    @Override
                    public void onFailure(String msg, Exception e) {

                    }
                });
            }
        });

        //点击减去
        btn_decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StringUtil.isEmptyNotNull(txt_SkuId.getText() + "")) {
                    mContext.showToast("请先设置商品");
                    return;
                }

                int maxQty = Integer.valueOf(txt_MaxQty.getText() + "");
                int sumQty = Integer.valueOf(txt_SumQty.getText() + "");
                int lockQty = Integer.valueOf(txt_LockQty.getText() + "");

                if (sumQty > lockQty) {
                    sumQty -= 1;
                    int sellQty = sumQty - lockQty;
                    txt_SellQty.setText(String.valueOf(sellQty));
                    txt_SumQty.setText(String.valueOf(sumQty));
            }
            }
        });

        //点击添加
        btn_increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StringUtil.isEmptyNotNull(txt_SkuId.getText() + "")) {
                    mContext.showToast("请先设置商品");
                    return;
                }


                int maxQty = Integer.valueOf(txt_MaxQty.getText() + "");
                int sumQty = Integer.valueOf(txt_SumQty.getText() + "");
                int lockQty = Integer.valueOf(txt_LockQty.getText() + "");
                sumQty += 1;



                int sellQty = sumQty - lockQty;
                txt_SellQty.setText(String.valueOf(sellQty));
                txt_SumQty.setText(String.valueOf(sumQty));

                if(sumQty>maxQty){
                    txt_MaxQty.setText(String.valueOf(sumQty));
                }
            }
        });

        //点击减去
        btn_decreasebymax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StringUtil.isEmptyNotNull(txt_SkuId.getText() + "")) {
                    mContext.showToast("请先设置商品");
                    return;
                }


                int sumQty = Integer.valueOf(txt_SumQty.getText() + "");
                int maxQty = Integer.valueOf(txt_MaxQty.getText() + "");


                if (maxQty > 0) {
                    maxQty = maxQty - 1;
                }

                if(maxQty<sumQty){
                    mContext.showToast("最大数量不能小于实际数量");
                    return;
                }

                txt_MaxQty.setText(String.valueOf(maxQty));
            }
        });

        //点击添加
        btn_increasebymax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StringUtil.isEmptyNotNull(txt_SkuId.getText() + "")) {
                    mContext.showToast("请先设置商品");
                    return;
                }

                int maxQty = Integer.valueOf(txt_MaxQty.getText() + "");

                maxQty = maxQty + 1;


                txt_MaxQty.setText(String.valueOf(maxQty));
            }
        });


        btn_decreasebywrn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StringUtil.isEmptyNotNull(txt_SkuId.getText() + "")) {
                    mContext.showToast("请先设置商品");
                    return;
                }


                int sumQty = Integer.valueOf(txt_SumQty.getText() + "");
                int wrnQty = Integer.valueOf(txt_WrnQty.getText() + "");


                if (wrnQty > 0) {
                    wrnQty = wrnQty - 1;
                }

                if(wrnQty<sumQty){
                    mContext.showToast("报警数据不能小于实际数量");
                    return;
                }

                txt_WrnQty.setText(String.valueOf(wrnQty));
            }
        });

        //点击添加
        btn_increasebywrn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StringUtil.isEmptyNotNull(txt_SkuId.getText() + "")) {
                    mContext.showToast("请先设置商品");
                    return;
                }

                int wrnQty = Integer.valueOf(txt_WrnQty.getText() + "");

                wrnQty = wrnQty + 1;


                txt_WrnQty.setText(String.valueOf(wrnQty));
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

    public void setSlot(SlotBean slot) {

        this.slot = slot;

        txt_SlotId.setText(slot.getSlotId());

        if (StringUtil.isEmptyNotNull(slot.getSkuId())) {
            txt_Version.setText(String.valueOf(slot.getVersion()));
            txt_StockId.setText("");
            txt_SkuId.setText("");
            txt_SkuCumCode.setText("");
            txt_SkuName.setText("暂无设置");
            txt_SkuSpecDes.setText("");
            txt_SellQty.setText("0");
            txt_LockQty.setText("0");
            txt_SumQty.setText("0");
            txt_MaxQty.setText("0");
            txt_WrnQty.setText("0");
            img_SkuImg.setImageResource(R.drawable.default_image);
        } else {
            txt_Version.setText(String.valueOf(slot.getVersion()));
            txt_StockId.setText(slot.getStockId());
            txt_SkuId.setText(slot.getSkuId());
            txt_SkuCumCode.setText(slot.getSkuCumCode());
            txt_SkuName.setText(slot.getSkuName());
            txt_SkuSpecDes.setText(slot.getSkuSpecDes());
            txt_SellQty.setText(String.valueOf(slot.getSellQuantity()));
            txt_LockQty.setText(String.valueOf(slot.getLockQuantity()));
            txt_SumQty.setText(String.valueOf(slot.getSumQuantity()));
            txt_MaxQty.setText(String.valueOf(slot.getMaxQuantity()));
            txt_WrnQty.setText(String.valueOf(slot.getWarnQuantity()));
            if(slot.getCanAlterMaxQuantity()!=null) {
                if (slot.getCanAlterMaxQuantity()) {
                    btn_decreasebymax.setVisibility(View.VISIBLE);
                    btn_increasebymax.setVisibility(View.VISIBLE);
                } else {
                    btn_decreasebymax.setVisibility(View.INVISIBLE);
                    btn_increasebymax.setVisibility(View.INVISIBLE);
                }
            }

            CommonUtil.loadImageFromUrl(mContext, img_SkuImg, slot.getSkuMainImgUrl());
        }
    }

    public void setCabinet(CabinetBean cabinet) {

        this.cabinet = cabinet;
    }

    private void getKey(String key) {
        String val = txt_searchKey.getText() + key;
        txt_searchKey.setText(val);
        searchSkus(val);
    }

    public void searchSkus(String key) {
        txt_searchKey.setText(key);
        Map<String, Object> params = new HashMap<>();

        DeviceBean device = AppCacheManager.getDevice();

        params.put("deviceId", device.getDeviceId());
        params.put("key", key);

        mContext.postByMy(mContext,Config.URL.product_SearchSku, params, null, false, "正在寻找", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);


                ApiResultBean<SkuSearchResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<SkuSearchResultBean>>() {
                });


                if (rt.getResult() == Result.SUCCESS) {
                    SkuSearchResultBean d = rt.getData();

                    SlotSkuSearchAdapter slotSkuSearchAdapter = new SlotSkuSearchAdapter(mContext, d.getSkus());
                    slotSkuSearchAdapter.setCallBackListener(new SlotSkuSearchAdapter.CallBackListener() {
                        @Override
                        public void setSlot(SearchSkuBean sku) {

                            txt_SkuId.setText(sku.getSkuId());
                            txt_SkuCumCode.setText(sku.getCumCode());
                            txt_SkuName.setText(sku.getName());
                            txt_SkuSpecDes.setText(sku.getSpecDes());
                            txt_SellQty.setText("0");
                            txt_LockQty.setText("0");
                            txt_SumQty.setText("0");
                            txt_WrnQty.setText("0");
                            CommonUtil.loadImageFromUrl(mContext, img_SkuImg, sku.getMainImgUrl());

                        }
                    });
                    list_search_skus.setAdapter(slotSkuSearchAdapter);

                    if (d.getSkus() != null) {
                        if (d.getSkus().size() == 1) {
                            SearchSkuBean sku = d.getSkus().get(0);
                            txt_SkuId.setText(sku.getSkuId());
                            txt_SkuName.setText(sku.getName());
                            txt_SkuCumCode.setText(sku.getCumCode());
                            txt_SkuSpecDes.setText(sku.getSpecDes());
                            CommonUtil.loadImageFromUrl(mContext, img_SkuImg, sku.getMainImgUrl());
                        }
                    }
                }
            }
        });
    }

    public void clearSearch() {
        txt_searchKey.setText("");
        SlotSkuSearchAdapter slotSkuSearchAdapter = new SlotSkuSearchAdapter(mContext, new ArrayList<SearchSkuBean>());
        list_search_skus.setAdapter(slotSkuSearchAdapter);
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
