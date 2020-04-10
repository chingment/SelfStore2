package com.uplink.selfstore.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.ExHandleOrderAdapter;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.ExHandleOrderBean;
import com.uplink.selfstore.model.api.ExHandleOrderDetailItemBean;
import com.uplink.selfstore.model.api.MachineGetRunExHandleItemsResultBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.dialog.CustomConfirmDialog;
import com.uplink.selfstore.ui.my.MyListView;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmRunExHandleActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "SmRunExHandleActivity";

    private MyListView list_exorders;

    private Button btn_GoBack;
    private Button btn_Handle;
    private CustomConfirmDialog dialog_ConfrmHandle;

    private List<ExHandleOrderBean> exOrders;

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

    protected void initView() {

        btn_GoBack = (Button) findViewById(R.id.btn_GoBack);
        btn_Handle = (Button) findViewById(R.id.btn_Handle);
        list_exorders = (MyListView) findViewById(R.id.list_exorders);
        list_exorders.setFocusable(false);
        list_exorders.setClickable(false);
        list_exorders.setPressed(false);
        list_exorders.setEnabled(false);

        dialog_ConfrmHandle = new CustomConfirmDialog(SmRunExHandleActivity.this, "确定要处理异常订单，慎重操作，会影响实际库存？", true);
        dialog_ConfrmHandle.getTipsImage().setImageDrawable(ContextCompat.getDrawable(SmRunExHandleActivity.this, (R.drawable.dialog_icon_warn)));
        dialog_ConfrmHandle.getBtnSure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (dialog_ConfrmHandle != null && dialog_ConfrmHandle.isShowing()) {
                    dialog_ConfrmHandle.cancel();
                }


                Map<String, Object> params = new HashMap<>();
                params.put("machineId", getMachine().getId() + "");

                JSONArray json_Orders = new JSONArray();

                try {

                    for (int i=0;i<exOrders.size();i++) {
                        JSONObject json_Order = new JSONObject();
                        json_Order.put("id", exOrders.get(i).getId());
                        JSONArray json_UniqueItems = new JSONArray();
                        List<ExHandleOrderDetailItemBean> detailItems = exOrders.get(i).getDetailItems();
                        for (int j = 0; j < detailItems.size(); j++) {
                            ExHandleOrderDetailItemBean detailItem = detailItems.get(j);

                            JSONObject json_UniqueItem = new JSONObject();
                            json_UniqueItem.put("uniqueId", detailItem.getUniqueId());
                            json_UniqueItem.put("signStatus", detailItem.getSignStatus());
                            json_UniqueItems.put(json_UniqueItem);

                        }

                        json_Order.put("uniqueItems",json_UniqueItems);

                        json_Orders.put(json_Order);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                params.put("orders", json_Orders);

                postByMy(Config.URL.machine_HandleRunExItems, params, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {

                        ApiResultBean<Object> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<Object>>() {
                        });

                        if (rt.getResult() == Result.SUCCESS) {
                            loadExOrders();
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
                dialog_ConfrmHandle.dismiss();
            }
        });
    }

    protected void initEvent() {

        btn_GoBack.setOnClickListener(this);
        btn_Handle.setOnClickListener(this);
    }


    public void loadExOrders() {
        if (exOrders == null) {
            exOrders=new ArrayList<>();
        }

        ExHandleOrderAdapter exHandleOrderAdapter = new ExHandleOrderAdapter(SmRunExHandleActivity.this, exOrders);
        list_exorders.setAdapter(exHandleOrderAdapter);
        list_exorders.setVisibility(View.VISIBLE);
    }

    protected void initData() {
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
                    loadExOrders();
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

    private void handle(){

        for (int i=0;i<exOrders.size();i++) {
            List<ExHandleOrderDetailItemBean> detailItems = exOrders.get(i).getDetailItems();
            for (int j = 0; j < detailItems.size(); j++) {
                ExHandleOrderDetailItemBean detailItem = detailItems.get(j);
                if (detailItem.getSignStatus() == 0) {
                    showToast("请标记" + detailItem.getName() + "的取货状态");
                    return;
                }
            }
        }

        dialog_ConfrmHandle.show();

    }
}
