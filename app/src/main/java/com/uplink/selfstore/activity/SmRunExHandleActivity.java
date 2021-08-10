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
import com.uplink.selfstore.model.api.RetDeviceGetRunExHandleItems;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.dialog.CustomDialogConfirm;
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

    private MyListView lv_ExOrders;
    private MyGridView gv_Reasons;
    private Button btn_GoBack;
    private Button btn_Handle;
    private CustomDialogConfirm dialog_Confirm;
    private List<ExHandleItemBean> exItems;
    private List<ExHandleReasonBean> exReasons;

    private LinearLayout ll_Ex;
    private LinearLayout ll_ExOrders;

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
        lv_ExOrders = (MyListView) findViewById(R.id.lv_ExOrders);
        gv_Reasons = (MyGridView) findViewById(R.id.gv_Reasons);

        lv_ExOrders.setFocusable(false);
        lv_ExOrders.setClickable(false);
        lv_ExOrders.setPressed(false);
        lv_ExOrders.setEnabled(false);

        ll_Ex=(LinearLayout) findViewById(R.id.ll_Ex);
        ll_ExOrders=(LinearLayout) findViewById(R.id.ll_ExOrders);
        dialog_Confirm = new CustomDialogConfirm(SmRunExHandleActivity.this, "确定要处理异常，影响实际库存，慎重操作？", true);
        dialog_Confirm.setTipsImageDrawable(ContextCompat.getDrawable(SmRunExHandleActivity.this, (R.drawable.dialog_icon_warn)));
        dialog_Confirm.setOnClickListener(new CustomDialogConfirm.OnClickListener() {
            @Override
            public void onSure() {

                if (dialog_Confirm != null) {
                    dialog_Confirm.hide();
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

                            dialog_Confirm.setTipsImageDrawable(ContextCompat.getDrawable(SmRunExHandleActivity.this, (R.drawable.dialog_icon_success)));
                            dialog_Confirm.setCloseVisibility(View.GONE);
                            dialog_Confirm.setTipsText("处理完成，返回主界面");
                            dialog_Confirm.setCancleVisibility(View.GONE);
                            dialog_Confirm.setOnClickListener(new CustomDialogConfirm.OnClickListener() {
                                @Override
                                public void onSure() {
                                    dialog_Confirm.hide();
                                    finish();
                                }

                                @Override
                                public void onCancle() {

                                }
                            });
                            dialog_Confirm.show();

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
            public void onCancle() {
                dialog_Confirm.hide();
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
                lv_ExOrders.setAdapter(exHandleItemAdapter);
                ll_ExOrders.setVisibility(View.VISIBLE);
            }
        }
    }

    public void loadExReasons() {
        if (exReasons == null) {
            exReasons=new ArrayList<>();
        }

        ExHandleReasonAdapter exHandleReasonAdapter = new ExHandleReasonAdapter(SmRunExHandleActivity.this, exReasons);
        gv_Reasons.setAdapter(exHandleReasonAdapter);
        gv_Reasons.setVisibility(View.VISIBLE);
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

                ApiResultBean<RetDeviceGetRunExHandleItems> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<RetDeviceGetRunExHandleItems>>() {
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

        if(dialog_Confirm!=null){
            dialog_Confirm.cancel();
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

        dialog_Confirm.show();

    }
}
