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
import com.uplink.selfstore.activity.adapter.ExHandleItemAdapter;
import com.uplink.selfstore.activity.adapter.ExHandleReasonAdapter;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.ExHandleItemBean;
import com.uplink.selfstore.model.api.ExHandleUniqueBean;
import com.uplink.selfstore.model.api.ExHandleReasonBean;
import com.uplink.selfstore.model.api.DeviceGetRunExHandleItemsResultBean;
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
    private List<ExHandleItemBean> exItems;
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

                if (dialog_ConfrmHandle != null) {
                    dialog_ConfrmHandle.hide();
                }


                Map<String, Object> params = new HashMap<>();
                params.put("deviceId", getDevice().getDeviceId() + "");

                JSONArray json_items = new JSONArray();
                JSONArray json_reasons = new JSONArray();
                try {

                    for (int i=0;i<exItems.size();i++) {
                        JSONObject json_item = new JSONObject();
                        json_item.put("itemId", exItems.get(i).getItemId());
                        JSONArray json_uniques = new JSONArray();
                        List<ExHandleUniqueBean> uniques = exItems.get(i).getUniques();
                        for (int j = 0; j < uniques.size(); j++) {
                            ExHandleUniqueBean unique = uniques.get(j);
                            if(unique.isCanHandle()) {
                                JSONObject json_unique = new JSONObject();
                                json_unique.put("uniqueId", unique.getUniqueId());
                                json_unique.put("signStatus", unique.getSignStatus());
                                json_uniques.put(json_unique);
                            }
                        }

                        json_item.put("uniques",json_uniques);

                        json_items.put(json_item);
                    }


                    for (int i=0;i<exReasons.size();i++) {
                      ExHandleReasonBean exReason=exReasons.get(i);
                        if(exReason.isChecked()){
                            JSONObject json_reason = new JSONObject();
                            json_reason.put("reasonId",exReason.getReasonId());
                            json_reason.put("title",exReason.getTitle());
                            json_reasons.put(json_reason);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                params.put("exItems", json_items);
                params.put("exReasons", json_reasons);
                postByMy(SmRunExHandleActivity.this, Config.URL.device_HandleRunExItems, params, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
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


    public void loadExItems() {
        if (exItems != null) {

            if(exItems.size()>0) {
                ExHandleItemAdapter exHandleItemAdapter = new ExHandleItemAdapter(SmRunExHandleActivity.this, exItems);
                list_exorders.setAdapter(exHandleItemAdapter);
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

        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", getDevice().getDeviceId());

        postByMy(SmRunExHandleActivity.this, Config.URL.device_GetRunExHandleItems, params,null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<DeviceGetRunExHandleItemsResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<DeviceGetRunExHandleItemsResultBean>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {
                    exItems=rt.getData().getExItems();
                    exReasons=rt.getData().getExReasons();
                    loadExItems();
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

        if(exItems!=null) {
            for (int i = 0; i < exItems.size(); i++) {
                List<ExHandleUniqueBean> uniques = exItems.get(i).getUniques();
                for (int j = 0; j < uniques.size(); j++) {
                    ExHandleUniqueBean detailItem = uniques.get(j);
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
