package com.uplink.selfstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.ReplenishPlanAdapter;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.RetReplenishGetPlans;
import com.uplink.selfstore.model.api.ReplenishPlanBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.ReqUrl;
import com.uplink.selfstore.ui.my.MyListView;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.util.HashMap;
import java.util.Map;


public class SmReplenishPlanActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "SmReplenishPlanActivity";

    private MyListView lv_Plans;
    private LinearLayout ll_Plans_Empty;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smreplenishplan);

        setNavTtile(this.getResources().getString(R.string.aty_smreplenishplan_navtitle));

        setNavGoBackBtnVisible(true);

        initView();
        initEvent();
        initData();
    }

    private void initView() {
        lv_Plans = (MyListView) findViewById(R.id.lv_Plans);
        ll_Plans_Empty = (LinearLayout) findViewById(R.id.ll_Plans_Empty);
    }

    private void initEvent() {

    }

    private void initData() {
        //getPlans();
    }

    private void getPlans(){

        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", getDevice().getDeviceId() + "");
        params.put("page",0);
        params.put("limit",10);

        postByMy(SmReplenishPlanActivity.this, ReqUrl.replenish_GetPlans, params, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {

                ApiResultBean<RetReplenishGetPlans> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<RetReplenishGetPlans>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {

                    RetReplenishGetPlans d = rt.getData();

                    ReplenishPlanAdapter adapter=new ReplenishPlanAdapter(SmReplenishPlanActivity.this,d.getItems());

                    adapter.setOnClickListener(new ReplenishPlanAdapter.OnClickListener() {
                        @Override
                        public void onClick(ReplenishPlanBean v) {
                            Intent intent = new Intent(getAppContext(), SmReplenishPlanDetailActivity.class);
                            intent.putExtra("replenishPlan",v);
                            startActivity(intent);
                        }
                    });

                    lv_Plans.setAdapter(adapter);

                    if(d.getTotal()==0){
                        ll_Plans_Empty.setVisibility(View.VISIBLE);
                    }
                    else {
                        ll_Plans_Empty.setVisibility(View.GONE);
                    }

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
    protected void onResume() {
        super.onResume();
        getPlans();//刷新数据
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
