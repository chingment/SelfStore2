package com.uplink.selfstore.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.ExHandleOrderAdapter;
import com.uplink.selfstore.activity.adapter.ExHandleReasonAdapter;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.ExHandleOrderBean;
import com.uplink.selfstore.model.api.ExHandleOrderDetailItemBean;
import com.uplink.selfstore.model.api.ExHandleReasonBean;
import com.uplink.selfstore.model.api.MachineGetRunExHandleItemsResultBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.dialog.CustomConfirmDialog;
import com.uplink.selfstore.ui.my.MyGridView;
import com.uplink.selfstore.ui.my.MyListView;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmRunExHandleActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "SmRunExHandleActivity";

    private MyListView list_exorders;
    private MyGridView list_reasons;
    private Button btn_GoBack;
    private Button btn_Handle;
    private CustomConfirmDialog dialog_ConfrmHandle;
    private CustomConfirmDialog dialog_HandleComplete;
    private List<ExHandleOrderBean> exOrders;
    private List<ExHandleReasonBean> exReasons;

    private LinearLayout layout_ex;
    private LinearLayout layout_exorders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smrunexhandle);
        setNavTtile(this.getResources().getString(R.string.aty_smrunexhandle_navtitle));
        setNavGoBackBtnVisible(true);

        initView();
        initEvent();
        initData();
    }

    private void initView() {

        btn_GoBack = (Button) findViewById(R.id.btn_GoBack);
        btn_Handle = (Button) findViewById(R.id.btn_Handle);
        list_exorders = (MyListView) findViewById(R.id.list_exorders);
        list_reasons = (MyGridView) findViewById(R.id.list_reasons);

        list_exorders.setFocusable(false);
        list_exorders.setClickable(false);
        list_exorders.setPressed(false);
        list_exorders.setEnabled(false);

        layout_ex=(LinearLayout) findViewById(R.id.layout_ex);
        layout_exorders=(LinearLayout) findViewById(R.id.layout_exorders);
        dialog_ConfrmHandle = new CustomConfirmDialog(SmRunExHandleActivity.this, "确定要处理异常，影响实际库存，慎重操作？", true);
        dialog_ConfrmHandle.getTipsImage().setImageDrawable(ContextCompat.getDrawable(SmRunExHandleActivity.this, (R.drawable.dialog_icon_warn)));
        dialog_ConfrmHandle.getBtnSure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (dialog_ConfrmHandle != null && dialog_ConfrmHandle.isShowing()) {
                    dialog_ConfrmHandle.cancel();
                }


                Map<String, Object> params = new HashMap<>();
                params.put("machineId", getMachine().getId() + "");

                JSONArray json_ExOrders = new JSONArray();
                JSONArray json_ExReasons = new JSONArray();
                try {

                    for (int i=0;i<exOrders.size();i++) {
                        JSONObject json_Order = new JSONObject();
                        json_Order.put("id", exOrders.get(i).getId());
                        JSONArray json_UniqueItems = new JSONArray();
                        List<ExHandleOrderDetailItemBean> detailItems = exOrders.get(i).getDetailItems();
                        for (int j = 0; j < detailItems.size(); j++) {
                            ExHandleOrderDetailItemBean detailItem = detailItems.get(j);
                            if(detailItem.isCanHandle()) {
                                JSONObject json_UniqueItem = new JSONObject();
                                json_UniqueItem.put("uniqueId", detailItem.getUniqueId());
                                json_UniqueItem.put("signStatus", detailItem.getSignStatus());
                                json_UniqueItems.put(json_UniqueItem);
                            }
                        }

                        json_Order.put("uniqueItems",json_UniqueItems);

                        json_ExOrders.put(json_Order);
                    }


                    for (int i=0;i<exReasons.size();i++) {
                      ExHandleReasonBean exReason=exReasons.get(i);
                        if(exReason.isChecked()){
                            JSONObject json_ExReason = new JSONObject();
                            json_ExReason.put("id",exReason.getId());
                            json_ExReason.put("title",exReason.getTitle());
                            json_ExReasons.put(json_ExReason);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                params.put("exOrders", json_ExOrders);
                params.put("exReasons", json_ExReasons);
                postByMy(Config.URL.machine_HandleRunExItems, params, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {

                        ApiResultBean<Object> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<Object>>() {
                        });

                        if (rt.getResult() == Result.SUCCESS) {

                            dialog_HandleComplete.show();

                        } else {
                            showToast(rt.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(String msg, Exception e) {
                        showToast(msg);
                    }
                });
            }
        });

        dialog_ConfrmHandle.getBtnCancle().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_ConfrmHandle.hide();
            }
        });

        dialog_HandleComplete = new CustomConfirmDialog(SmRunExHandleActivity.this, "处理完成，返回主界面", false);
        dialog_HandleComplete.getTipsImage().setImageDrawable(ContextCompat.getDrawable(SmRunExHandleActivity.this, (R.drawable.dialog_icon_success)));
        dialog_HandleComplete.setBtnCloseVisibility(View.GONE);
        dialog_HandleComplete.getBtnSure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_ConfrmHandle.hide();
                finish();
            }
        });

    }

    protected void initEvent() {

        btn_GoBack.setOnClickListener(this);
        btn_Handle.setOnClickListener(this);
    }


    public void loadExOrders() {
        if (exOrders != null) {

            if(exOrders.size()>0) {
                ExHandleOrderAdapter exHandleOrderAdapter = new ExHandleOrderAdapter(SmRunExHandleActivity.this, exOrders);
                list_exorders.setAdapter(exHandleOrderAdapter);
                layout_exorders.setVisibility(View.VISIBLE);
            }
        }
    }

    public void loadExReasons() {
        if (exReasons == null) {
            exReasons=new ArrayList<>();
        }

        ExHandleReasonAdapter exHandleReasonAdapter = new ExHandleReasonAdapter(SmRunExHandleActivity.this, exReasons);
        list_reasons.setAdapter(exHandleReasonAdapter);
        list_reasons.setVisibility(View.VISIBLE);
    }

    private void initData() {
        getRunExHandleItems();
    }


    private void getRunExHandleItems() {

        Map<String, String> params = new HashMap<>();
        params.put("machineId", getMachine().getId());

        getByMy(Config.URL.machine_GetRunExHandleItems, params, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<MachineGetRunExHandleItemsResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<MachineGetRunExHandleItemsResultBean>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {
                    exOrders=rt.getData().getExOrders();
                    exReasons=rt.getData().getExReasons();
                    loadExOrders();
                    loadExReasons();
                } else {
                    showToast(rt.getMessage());
                }
            }

            @Override
            public void onFailure(String msg, Exception e) {
                showToast(msg);
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.nav_back:
                case R.id.btn_GoBack:
                    finish();
                    break;
                case R.id.btn_Handle:
                    handle();
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(dialog_ConfrmHandle!=null){
            dialog_ConfrmHandle.cancel();
        }

        if(dialog_HandleComplete!=null){
            dialog_HandleComplete.cancel();
        }

    }

    private void handle(){

        boolean isHasExReason=false;

        for (int i=0;i<exReasons.size();i++) {
            ExHandleReasonBean exReason = exReasons.get(i);
            if(exReason.isChecked()){
                isHasExReason=true;
                break;
            }
        }

        if(!isHasExReason){
            showToast("至少选择一个异常原因");
            return;
        }

        if(exReasons!=null) {
            for (int i = 0; i < exOrders.size(); i++) {
                List<ExHandleOrderDetailItemBean> detailItems = exOrders.get(i).getDetailItems();
                for (int j = 0; j < detailItems.size(); j++) {
                    ExHandleOrderDetailItemBean detailItem = detailItems.get(j);
                    if(detailItem.isCanHandle()) {
                        if (detailItem.getSignStatus() == 0) {
                            showToast("请标记" + detailItem.getName() + "的取货状态");
                            return;
                        }
                    }
                }
            }
        }

        dialog_ConfrmHandle.show();

    }
}
