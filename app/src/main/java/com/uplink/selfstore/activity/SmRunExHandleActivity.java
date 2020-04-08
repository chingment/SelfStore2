package com.uplink.selfstore.activity;

import android.os.Bundle;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.ExHandleOrderAdapter;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.ExHandleOrderBean;
import com.uplink.selfstore.model.api.MachineGetRunExHandleItemsResultBean;
import com.uplink.selfstore.model.api.MachineSlotsResultBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.my.MyListView;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmRunExHandleActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "SmRunExHandleActivity";

    private MyListView list_exorders;

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

        list_exorders = (MyListView) findViewById(R.id.list_exorders);
        list_exorders.setFocusable(false);
        list_exorders.setClickable(false);
        list_exorders.setPressed(false);
        list_exorders.setEnabled(false);
    }

    protected void initEvent() {

    }


    public void loadExOrders(List<ExHandleOrderBean> exOrders) {
        if (exOrders != null) {
            ExHandleOrderAdapter exHandleOrderAdapter = new ExHandleOrderAdapter(SmRunExHandleActivity.this, exOrders);
            list_exorders.setAdapter(exHandleOrderAdapter);
            list_exorders.setVisibility(View.VISIBLE);
        }
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
                    loadExOrders(rt.getData().getExOrders());
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
                default:
                    break;
            }
        }
    }
}
