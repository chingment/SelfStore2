package com.uplink.selfstore.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.OrderPayUrlBuildResultBean;
import com.uplink.selfstore.model.api.OrderReserveResultBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.util.HashMap;
import java.util.Map;

public class PayWayActivity extends SwipeBackActivity implements View.OnClickListener {

    private View btn_back;
    private View btn_payway_wechat;
    private View btn_payway_zhifubao;
    private OrderReserveResultBean orderReserveResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payway);

        setNavTtile(this.getResources().getString(R.string.activity_payway_navtitle));

        orderReserveResult = (OrderReserveResultBean) getIntent().getSerializableExtra("dataBean");

        initView();
        initEvent();
        initData();
    }

    protected void initView() {
        btn_back = findViewById(R.id.btn_back);
        btn_payway_wechat = findViewById(R.id.btn_payway_wechat);
        btn_payway_zhifubao = findViewById(R.id.btn_payway_zhifubao);
    }

    private void initEvent() {

        btn_back.setOnClickListener(this);
        btn_payway_wechat.setOnClickListener(this);
        btn_payway_zhifubao.setOnClickListener(this);
    }

    private void initData() {

    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.btn_back:
                    orderCancle(orderReserveResult.getOrderId(),"返回上一页，取消订单");
                    finish();
                    break;
                case R.id.btn_payway_wechat:
                    orderPayUrlBuild(1);
                    break;
                case R.id.btn_payway_zhifubao:
                    orderPayUrlBuild(2);
                    break;
            }
        }
    }


    public void orderPayUrlBuild(int payWay) {

        MachineBean machine = AppCacheManager.getMachine();

        Map<String, Object> params = new HashMap<>();
        params.put("machineId", machine.getId() + "");
        params.put("orderId", orderReserveResult.getOrderId());
        params.put("payWay", payWay);


        postByMy(Config.URL.order_PayUrlBuild, params, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {

                ApiResultBean<OrderPayUrlBuildResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<OrderPayUrlBuildResultBean>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {
                    Intent intent2 = new Intent(getAppContext(), PayActivity.class);
                    Bundle b = new Bundle();
                    b.putSerializable("dataBean", rt.getData());
                    intent2.putExtras(b);
                    startActivity(intent2);

                } else {
                    showToast(rt.getMessage());
                }
            }
        });
    }
}
