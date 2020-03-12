package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.uplink.selfstore.activity.SmMachineStockActivity;
import com.uplink.selfstore.activity.adapter.SlotSkuSearchAdapter;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.deviceCtrl.ScanMidCtrl;
import com.uplink.selfstore.model.DSCabRowColLayoutBean;
import com.uplink.selfstore.model.PickupResult;
import com.uplink.selfstore.model.DSCabSlotNRC;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.CabinetBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.ProductSkuSearchResultBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.SearchProductSkuBean;
import com.uplink.selfstore.model.api.SlotBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomSlotEditDialog extends Dialog {

    private View layoutRes;// 布局文件
    private SmMachineStockActivity mContext;
    private View btn_close;
    private TextView txt_searchKey;
    private ImageView img_SkuImg;
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
    private ImageButton btn_keydelete;
    private Button btn_delete;
    private Button btn_fill;
    private Button btn_save;
    private View btn_decrease;
    private View btn_increase;
    private View btn_pick_test;
    private View btn_decreasebymax;
    private View btn_increasebymax;
    private ListView list_search_skus;
    private SlotBean slot;
    private CabinetBean cabinet;
    private ScanMidCtrl scanMidCtrl;
    private CabinetCtrlByDS cabinetCtrlByDS;
    private CustomDialogLoading customDialogRunning;

    public CustomSlotEditDialog(final Context context) {
        super(context, R.style.dialog_style);
        this.mContext = (SmMachineStockActivity) context;
        this.layoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_slotedit, null);


        cabinetCtrlByDS=CabinetCtrlByDS.getInstance();
        cabinetCtrlByDS.setPickupHandler(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                String slotId = String.valueOf(txt_SlotName.getText());
                String productSkuId=String.valueOf(txt_SkuId.getText());

                Bundle bundle = msg.getData();
                int status = bundle.getInt("status");
                String message = bundle.getString("message");
                PickupResult pickupResult = null;
                if (bundle.getSerializable("result") != null) {
                    pickupResult = (PickupResult) bundle.getSerializable("result");
                }
                switch (status) {
                    case 1://消息提示
                        if(customDialogRunning!=null&&customDialogRunning.isShowing()) {
                            customDialogRunning.cancelDialog();
                        }
                        mContext.showToast(message);
                        break;
                    case 2://启动就绪成功，弹出窗口，同时默认120秒关闭窗口
                        if (!customDialogRunning.isShowing()) {
                            customDialogRunning.showDialog();
                            customDialogRunning.setProgressText("取货就绪成功");
                        }
                        break;
                    case 3://取货中
                        if (pickupResult != null) {
                            if(customDialogRunning!=null) {
                                customDialogRunning.setProgressText("正在取货中..请稍等");
                                pickupEventNotify(productSkuId,slotId,3012,"发起取货",pickupResult);
                            }
                        }
                        break;
                    case 4://取货成功
                        if (pickupResult != null) {
                            if (pickupResult.isPickupComplete()) {
                                if(customDialogRunning!=null&&customDialogRunning.isShowing()) {
                                    customDialogRunning.cancelDialog();
                                }
                                mContext.showToast("取货完成");
                                pickupEventNotify(productSkuId,slotId,4000,"取货完成",pickupResult);
                            }
                        }
                        break;
                    case 5://取货超时
                        if(customDialogRunning!=null&&customDialogRunning.isShowing()) {
                            customDialogRunning.cancelDialog();
                        }
                        mContext.showToast(message);
                        LogUtil.e("取货超时");
                        break;
                    case 6://取货失败
                        if(customDialogRunning!=null&&customDialogRunning.isShowing()) {
                            customDialogRunning.cancelDialog();
                        }
                        mContext.showToast(message);
                        LogUtil.e("取货失败");
                        break;
                }
                return false;
            }
        }));

        scanMidCtrl = ScanMidCtrl.getInstance();

        scanMidCtrl.setScanHandler(new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {

                        Bundle bundle;
                        bundle = msg.getData();
                        String scanResult = bundle.getString("result");
                        txt_searchKey.setText(scanResult);
                        searchSkus(scanResult);

                        return false;
                    }
                })
        );


        initView();
        initEvent();
        initData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(layoutRes);
    }

    protected void initView() {
        btn_close = ViewHolder.get(this.layoutRes, R.id.btn_close);
        txt_searchKey = ViewHolder.get(this.layoutRes, R.id.txt_searchKey);

        txt_Version=ViewHolder.get(this.layoutRes, R.id.txt_Version);
        img_SkuImg = ViewHolder.get(this.layoutRes, R.id.img_SkuImg);
        txt_SlotName = ViewHolder.get(this.layoutRes, R.id.txt_SlotName);
        txt_SkuCumCode= ViewHolder.get(this.layoutRes, R.id.txt_SkuCumCode);
        txt_SkuId = ViewHolder.get(this.layoutRes, R.id.txt_SkuId);
        txt_SkuName = ViewHolder.get(this.layoutRes, R.id.txt_SkuName);
        txt_SkuSpecDes= ViewHolder.get(this.layoutRes, R.id.txt_SkuSpecDes);
        txt_SellQty = ViewHolder.get(this.layoutRes, R.id.txt_SellQty);
        txt_LockQty = ViewHolder.get(this.layoutRes, R.id.txt_LockQty);
        txt_SumQty = ViewHolder.get(this.layoutRes, R.id.txt_SumQty);
        txt_MaxQty = ViewHolder.get(this.layoutRes, R.id.txt_MaxQty);
        list_search_skus = ViewHolder.get(this.layoutRes, R.id.list_search_skus);
        btn_keydelete = ViewHolder.get(this.layoutRes, R.id.btn_keydelete);

        btn_pick_test = ViewHolder.get(this.layoutRes, R.id.btn_pick_test);
        btn_delete = ViewHolder.get(this.layoutRes, R.id.btn_delete);
        btn_fill = ViewHolder.get(this.layoutRes, R.id.btn_fill);
        btn_decrease = ViewHolder.get(this.layoutRes, R.id.btn_decrease);
        btn_increase = ViewHolder.get(this.layoutRes, R.id.btn_increase);
        btn_save = ViewHolder.get(this.layoutRes, R.id.btn_save);
        btn_decreasebymax = ViewHolder.get(this.layoutRes, R.id.btn_decreasebymax);
        btn_increasebymax = ViewHolder.get(this.layoutRes, R.id.btn_increasebymax);

        customDialogRunning = new CustomDialogLoading(this.mContext);

    }

    protected void initEvent() {


        final Dialog _this = this;

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _this.dismiss();
                cabinetCtrlByDS.disConnect();
                scanMidCtrl.disconnect();
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

                String slotId = String.valueOf(txt_SlotName.getText());


                String productSkuId=String.valueOf(txt_SkuId.getText());

                pickupEventNotify(productSkuId,slotId,3011,"发起取货",null);


                switch (cabinet.getId()){
                    case "dsx01n01":
                        DSCabSlotNRC dsCabSlotNRC = DSCabSlotNRC.GetSlotNRC(cabinet.getId(), slotId);
                        if (dsCabSlotNRC == null) {
                            mContext.showToast("货道编号解释错误");
                            return;
                        }
                        if (!cabinetCtrlByDS.isConnect()) {
                            cabinetCtrlByDS.connect();
                        }

                        if (!cabinetCtrlByDS.isConnect()) {
                            mContext.showToast("机器连接失败");
                            return;
                        }

                        if (!cabinetCtrlByDS.isNormarl()) {
                            mContext.showToast("机器状态异常");
                            return;
                        }

                        if(!cabinetCtrlByDS.isIdle()) {
                            mContext.showToast("机器不在空闲状态");
                            return;
                        }

                        DSCabRowColLayoutBean dSCabRowColLayout= JSON.parseObject(cabinet.getRowColLayout(), new TypeReference<DSCabRowColLayoutBean>() {});
                        cabinetCtrlByDS.pickUp(dsCabSlotNRC.getRow(), dsCabSlotNRC.getCol(),dSCabRowColLayout.getPendantRows());
                        break;
                    case "zsx01n01":



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

                txt_SkuId.setText("");
                txt_SkuName.setText("暂无设置");
                txt_SkuSpecDes.setText("");
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

                MachineBean machine = AppCacheManager.getMachine();

                String id = String.valueOf(txt_SlotName.getText());
                String productSkuId = String.valueOf(txt_SkuId.getText());
                int version=Integer.valueOf(txt_Version.getText()+"");
                int sellQuantity = Integer.valueOf(txt_SellQty.getText() + "");
                int sumQuantity = Integer.valueOf(txt_SumQty.getText() + "");
                int maxQuantity = Integer.valueOf(txt_MaxQty.getText() + "");

                if(maxQuantity<sumQuantity){

                    mContext.showToast("保存失败，最大数量不能小于实际数据");
                    return;
                }
                Map<String, Object> params = new HashMap<>();
                params.put("id", id);
                params.put("machineId", machine.getId());
                params.put("cabinetId", cabinet.getId());
                params.put("productSkuId", productSkuId);
                params.put("sumQuantity", sumQuantity);
                params.put("maxQuantity", maxQuantity);
                params.put("version", version);

                mContext.postByMy(Config.URL.stockSetting_SaveCabinetSlot, params, null, true, mContext.getString(R.string.tips_hanlding), new HttpResponseHandler() {
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

                int maxQty = Integer.valueOf(txt_MaxQty.getText() + "");
                if (maxQty > 0) {
                    maxQty = maxQty - 1;
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

        LinearLayout all_key = ViewHolder.get(this.layoutRes, R.id.all_key);
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

        txt_SlotName.setText(slot.getId());

        if (StringUtil.isEmptyNotNull(slot.getProductSkuId())) {
            txt_Version.setText(String.valueOf(slot.getVersion()));
            txt_SkuId.setText("");
            txt_SkuCumCode.setText("");
            txt_SkuName.setText("暂无设置");
            txt_SkuSpecDes.setText("");
            txt_SellQty.setText("0");
            txt_LockQty.setText("0");
            txt_SumQty.setText("0");
            txt_MaxQty.setText("0");
            img_SkuImg.setImageResource(R.drawable.default_image);
        } else {
            txt_Version.setText(String.valueOf(slot.getVersion()));
            txt_SkuId.setText(slot.getProductSkuId());
            txt_SkuCumCode.setText(slot.getProductSkuCumCode());
            txt_SkuName.setText(slot.getProductSkuName());
            txt_SkuSpecDes.setText(slot.getProductSkuSpecDes());
            txt_SellQty.setText(String.valueOf(slot.getSellQuantity()));
            txt_LockQty.setText(String.valueOf(slot.getLockQuantity()));
            txt_SumQty.setText(String.valueOf(slot.getSumQuantity()));
            txt_MaxQty.setText(String.valueOf(slot.getMaxQuantity()));
            CommonUtil.loadImageFromUrl(mContext, img_SkuImg, slot.getProductSkuMainImgUrl());
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

    private void searchSkus(String key) {
        Map<String, String> params = new HashMap<>();

        MachineBean machine = AppCacheManager.getMachine();

        params.put("machineId", machine.getId());
        params.put("key", key);

        mContext.getByMy(Config.URL.productSku_Search, params, false, "正在寻找", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);


                ApiResultBean<ProductSkuSearchResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<ProductSkuSearchResultBean>>() {
                });


                if (rt.getResult() == Result.SUCCESS) {
                    ProductSkuSearchResultBean d = rt.getData();

                    SlotSkuSearchAdapter slotSkuSearchAdapter = new SlotSkuSearchAdapter(mContext, d.getProductSkus());
                    slotSkuSearchAdapter.setCallBackListener(new SlotSkuSearchAdapter.CallBackListener() {
                        @Override
                        public void setSlot(SearchProductSkuBean sku) {

                            txt_SkuId.setText(sku.getId());
                            txt_SkuCumCode.setText(sku.getCumCode());
                            txt_SkuName.setText(sku.getName());
                            txt_SkuSpecDes.setText(sku.getSpecDes());
                            txt_SellQty.setText("0");
                            txt_LockQty.setText("0");
                            txt_SumQty.setText("0");

                            CommonUtil.loadImageFromUrl(mContext, img_SkuImg, sku.getMainImgUrl());

                        }
                    });
                    list_search_skus.setAdapter(slotSkuSearchAdapter);

                    if (d.getProductSkus() != null) {
                        if (d.getProductSkus().size() == 1) {
                            SearchProductSkuBean sku = d.getProductSkus().get(0);
                            txt_SkuId.setText(sku.getId());
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
        SlotSkuSearchAdapter slotSkuSearchAdapter = new SlotSkuSearchAdapter(mContext, new ArrayList<SearchProductSkuBean>());
        list_search_skus.setAdapter(slotSkuSearchAdapter);
    }

    private void pickupEventNotify(final String productSkuId, final String slotId,final int status, String remark,PickupResult pickupResult) {

        try {
            JSONObject content = new JSONObject();

            content.put("productSkuId", productSkuId);
            content.put("cabinetId", cabinet.getId());
            content.put("slotId", slotId);
            content.put("status", status);
            content.put("remark", remark);
            content.put("isTest", true);
            if (pickupResult != null) {
                content.put("actionId", pickupResult.getCurrentActionId());
                content.put("actionName", pickupResult.getCurrentActionName());
                content.put("actionStatusCode", pickupResult.getCurrentActionStatusCode());
                content.put("actionStatusName", pickupResult.getCurrentActionStatusName());
                content.put("pickupUseTime", pickupResult.getPickupUseTime());
                content.put("isPickupComplete", pickupResult.isPickupComplete());
            }
            mContext.eventNotify(2, content);
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        super.show();


        scanMidCtrl.connect();

        if(!scanMidCtrl.isConnect()){
            mContext.showToast("扫描器连接失败");
        }
    }
}
