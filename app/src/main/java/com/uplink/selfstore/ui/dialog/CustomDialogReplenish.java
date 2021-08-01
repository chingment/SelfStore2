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
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.ui.CameraWindow;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class CustomDialogReplenish  extends Dialog {

    private static final String TAG = "CustomDialogReplenish";
    private View mLayoutRes;// 布局文件
    private Dialog mThis;

    private BaseFragmentActivity mContext;

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
    private TextView tv_PlanRshQty;
    private TextView tv_RealRshQty;
    private Button btn_Save;

    private View btn_DecRealRshQty;
    private View btn_IncRealRshQty;

    private SlotBean slot;
    private CabinetBean cabinet;

    public CustomDialogReplenish(final Context context) {
        super(context, R.style.dialog_style);

        mThis = this;

        mLayoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_replenish, null);

        initView();
        initEvent();
        initData();

        setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(mLayoutRes);
    }

    protected void initView() {
        dlg_Close = ViewHolder.get(mLayoutRes, R.id.dlg_Close);
        iv_SkuImg = ViewHolder.get(mLayoutRes, R.id.iv_SkuImg);
        tv_Version=ViewHolder.get(mLayoutRes, R.id.tv_Version);
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
        tv_PlanRshQty = ViewHolder.get(mLayoutRes, R.id.tv_PlanRshQty);
        tv_RealRshQty= ViewHolder.get(mLayoutRes, R.id.tv_RealRshQty);
        btn_DecRealRshQty = ViewHolder.get(mLayoutRes, R.id.btn_DecRealRshQty);
        btn_IncRealRshQty = ViewHolder.get(mLayoutRes, R.id.btn_IncRealRshQty);
        btn_Save = ViewHolder.get(mLayoutRes, R.id.btn_Save);
    }

    protected void initEvent() {

        dlg_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThis.dismiss();
            }
        });

        btn_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



            }
        });

        btn_DecRealRshQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int realRshQty = Integer.valueOf(tv_RealRshQty.getText() + "");
                int sellQty = Integer.valueOf(tv_SellQty.getText() + "");
                int sumQty = Integer.valueOf(tv_SumQty.getText() + "");
                if (realRshQty > 0) {
                    realRshQty -=1;
                    sellQty -= 1;
                    sumQty -= 1;
                }

                tv_RealRshQty.setText(String.valueOf(realRshQty));
                tv_SellQty.setText(String.valueOf(sellQty));
                tv_SumQty.setText(String.valueOf(sumQty));
            }
        });

        btn_IncRealRshQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int realRshQty = Integer.valueOf(tv_RealRshQty.getText() + "");
                int sellQty = Integer.valueOf(tv_SellQty.getText() + "");
                int sumQty = Integer.valueOf(tv_SumQty.getText() + "");

                realRshQty += 1;
                sellQty += 1;
                sumQty += 1;

                tv_RealRshQty.setText(String.valueOf(realRshQty));
                tv_SellQty.setText(String.valueOf(sellQty));
                tv_SumQty.setText(String.valueOf(sumQty));
            }
        });

    }

    protected void initData() {


    }

    public void setData(CabinetBean cabinet,SlotBean slot) {

        this.cabinet = cabinet;
        this.slot = slot;

        tv_SlotId.setText(slot.getSlotId());
        tv_SlotName.setText(slot.getSlotName());
        tv_Version.setText(String.valueOf(slot.getVersion()));
        tv_StockId.setText(slot.getStockId());
        tv_SkuId.setText(slot.getSkuId());
        tv_SkuCumCode.setText(slot.getSkuCumCode());
        tv_SkuName.setText(slot.getSkuName());
        tv_SkuSpecDes.setText(slot.getSkuSpecDes());
        tv_SellQty.setText(String.valueOf(slot.getSellQuantity()));
        tv_LockQty.setText(String.valueOf(slot.getLockQuantity()));
        tv_SumQty.setText(String.valueOf(slot.getSumQuantity()));

        CommonUtil.loadImageFromUrl(mContext, iv_SkuImg, slot.getSkuMainImgUrl());

    }

    @Override
    public void cancel() {
        super.cancel();

    }

    @Override
    public void show() {
        super.show();
    }

}
