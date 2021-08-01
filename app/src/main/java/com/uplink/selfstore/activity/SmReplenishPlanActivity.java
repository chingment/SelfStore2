package com.uplink.selfstore.activity;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.CabinetAdapter;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByZS;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.model.DSCabRowColLayoutBean;
import com.uplink.selfstore.model.ScanSlotResult;
import com.uplink.selfstore.model.ZSCabRowColLayoutBean;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.CabinetBean;
import com.uplink.selfstore.model.api.DeviceSlotsResultBean;
import com.uplink.selfstore.model.api.PickupSkuBean;
import com.uplink.selfstore.model.api.ReplenishGetPlansResultBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.SlotBean;
import com.uplink.selfstore.own.AppLogcatManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.service.UsbService;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.ui.dialog.CustomConfirmDialog;
import com.uplink.selfstore.ui.dialog.CustomLoadingDialog;
import com.uplink.selfstore.ui.dialog.CustomPickupAutoTestDialog;
import com.uplink.selfstore.ui.my.MyListView;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.InterUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;
import com.uplink.selfstore.utils.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class SmReplenishPlanActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "SmReplenishPlanActivity";

    private MyListView lv_Plans;

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
        lv_Plans= (MyListView) findViewById(R.id.lv_Plans);
    }

    private void initEvent() {

    }

    private void initData() {
        getPlans();
    }

    private void getPlans(){

        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", getDevice().getDeviceId() + "");
        params.put("page",0);
        params.put("limit",10);

        postByMy(SmReplenishPlanActivity.this,Config.URL.replenish_GetPlans, params, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {

                ApiResultBean<ReplenishGetPlansResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<ReplenishGetPlansResultBean>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {

                    ReplenishGetPlansResultBean d = rt.getData();
//
//                    ImSeatAdapter imSeatAdapter = new ImSeatAdapter(CartActivity.this, d.getSeats());
//                    imSeatAdapter.setOnLinster(new ImSeatAdapter.OnItemListener() {
//                        @Override
//                        public void call(ImSeatBean v) {
//
//                            ImBean im= getDevice().getIm();
//
//                            EMClient.getInstance().login(im.getUserName(), im.getPassword(), new EMCallBack() {
//
//                                @Override
//                                public void onSuccess() {
//                                    Log.d(TAG, "EMClient->login: onSuccess");
//
//                                    currentSvcConsulterId=v.getUserId();
//
//                                    JSONObject jsonExMessage = new JSONObject();
//
//                                    try {
//
//                                        jsonExMessage.put("type", "buyinfo");
//
//                                        JSONObject jsonExMessageContent = new JSONObject();
//                                        jsonExMessageContent.put("deviceId", getDevice().getDeviceId());
//                                        jsonExMessageContent.put("storeName", getDevice().getStoreName());
//                                        JSONArray json_Skus = new JSONArray();
//                                        for (String key : cartSkus.keySet()) {
//                                            CartSkuBean bean = cartSkus.get(key);
//                                            JSONObject json_Sku = new JSONObject();
//                                            json_Sku.put("skuId", bean.getSkuId());
//                                            json_Sku.put("name", bean.getName());
//                                            json_Sku.put("mainImgUrl", bean.getMainImgUrl());
//                                            json_Sku.put("quantity", bean.getQuantity());
//                                            json_Skus.put(json_Sku);
//                                        }
//                                        jsonExMessageContent.put("skus", json_Skus);
//                                        jsonExMessage.put("content", jsonExMessageContent);
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                    LogUtil.i(TAG, "jsonExMessage:" + jsonExMessage.toString());
//                                    Intent intent = new Intent(CartActivity.this, EmVideoCallActivity.class);
//                                    intent.putExtra("username", v.getImUserName());
//                                    intent.putExtra("isComingCall", false);
//                                    intent.putExtra("ex_nickName", v.getNickName());
//                                    intent.putExtra("ex_message", jsonExMessage.toString());
//
//                                    startActivityForResult(intent, 0x002);
//
//                                }
//
//                                @Override
//                                public void onProgress(int progress, String status) {
//                                    Log.d(TAG, "EMClient->login: onProgress");
//                                }
//
//                                @Override
//                                public void onError(final int code, final String message) {
//                                    Log.d(TAG, "EMClient->login: onError: " + code);
//                                }
//                            });
//                        }
//                    });
//
//                    v.setAdapter(imSeatAdapter);

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
                case R.id.btn_AutoTest:
                    Intent intent = new Intent(getAppContext(), SmReplenishPlanDetailActivity.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    }
}
