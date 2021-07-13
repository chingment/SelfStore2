package com.uplink.selfstore.own;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.activity.CartActivity;
import com.uplink.selfstore.activity.MainActivity;
import com.uplink.selfstore.activity.OrderDetailsActivity;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.model.api.AdBean;
import com.uplink.selfstore.model.api.DeviceBean;
import com.uplink.selfstore.model.api.CustomDataByVendingBean;
import com.uplink.selfstore.model.api.OrderDetailsBean;
import com.uplink.selfstore.model.api.OrderPayStatusQueryResultBean;
import com.uplink.selfstore.model.api.SkuBean;
import com.uplink.selfstore.model.push.UpdateHomeLogoBean;
import com.uplink.selfstore.model.push.UpdateSkuStockBean;
import com.uplink.selfstore.model.push.*;
import com.uplink.selfstore.ostCtrl.OstCtrlInterface;
import com.uplink.selfstore.service.MqttService;
import com.uplink.selfstore.taskexecutor.onebyone.BaseSyncTask;
import com.uplink.selfstore.taskexecutor.onebyone.TinySyncExecutor;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by chingment on 2019/3/20.
 */

public class CommandManager {

    private static Lock lock = new ReentrantLock();

    private static final String TAG = "CommandManager";

    public static void Execute(String id,String method, String params) {

        lock.lock();// 得到锁

        LogUtil.i(TAG, "id:" + id + ",method:" + method + ",params:" + params);

        try {

            switch (method) {
                case "reboot_sys":
                    reboot_sys();
                    break;
                case "shutdown_sys":
                    shutdown_sys();
                    break;
                case "set_sys_status":
                    set_sys_status(params);
                    break;
                case "update_ads":
                    update_ads(params);
                    break;
                case "update_stock":
                    update_stock(params);
                    break;
                case "pay_success":
                    pay_success(params);
                    break;
                case "open_pickup_door":
                    open_pickup_door();
                    break;
                case "order_pickup":

                    if (!TinySyncExecutor.getInstance().currentTaskIsNull()) {
                        LogUtil.d(TAG, "已有订单正在执行");
                    } else {

                        DeviceBean device=AppCacheManager.getDevice();

                        if (StringUtil.isEmptyNotNull(device.getDeviceId())) {
                            LogUtil.d(TAG, "设备未配置");
                            return;
                        }

                        String status = "unknow";
                        Activity activity = AppManager.getAppManager().currentActivity();
                        if (activity != null) {
                            String  activityName = activity.getLocalClassName();
                            if (activityName.contains(".Sm")) {
                                status = "setting";
                            } else {
                                if (device.isExIsHas()) {
                                    status = "exception";
                                } else {
                                    status = "running";
                                }
                            }
                        }


                        if(device.isExIsHas()){
                            LogUtil.d(TAG, "设备存在异常");
                            return;
                        }

                        if(status!="running"){
                            LogUtil.d(TAG, "设备正在维护或存在异常");
                            return;
                        }

                        BaseSyncTask task = new BaseSyncTask() {
                            @Override
                            public void doTask() {

                                Intent intent = new Intent(AppContext.getInstance(), OrderDetailsActivity.class);
                                Bundle bundle = new Bundle();
                                OrderDetailsBean orderDetails = new OrderDetailsBean();
                                orderDetails.setOrderId("dadsdsad");
                                orderDetails.setStatus(10000);
                                //orderDetails.setSkus(bean.getSkus());
                                bundle.putSerializable("dataBean", orderDetails);
                                intent.putExtras(bundle);

                                AppContext.getInstance().startActivity(intent);

                            }
                        };


                        TinySyncExecutor.getInstance().enqueue(task);
                    }


                    break;
            }
        } finally {
            lock.unlock();// 释放锁
        }
    }

    private static void reboot_sys() {
        OstCtrlInterface.getInstance().reboot(AppContext.getInstance().getApplicationContext());
    }

    private static void shutdown_sys() {
        OstCtrlInterface.getInstance().shutdown(AppContext.getInstance().getApplicationContext());
    }

    private static void set_sys_status(String content) {

        SysSetStatusBean sysSetStatusBean = JSON.parseObject(content, new TypeReference<SysSetStatusBean>() {
        });

        if (sysSetStatusBean == null)
            return;

        BaseFragmentActivity currentActivity = (BaseFragmentActivity) AppManager.getAppManager().currentActivity();

        if (currentActivity == null)
            return;

        if (currentActivity.getDialogBySystemWarn() == null)
            return;

        if (sysSetStatusBean.getStatus() == 1) {
                currentActivity.getDialogBySystemWarn().setBtnCloseVisibility(View.GONE);
                currentActivity.getDialogBySystemWarn().hide();
        } else if (sysSetStatusBean.getStatus() == 2) {
                currentActivity.getDialogBySystemWarn().setBtnCloseVisibility(View.GONE);
                currentActivity.getDialogBySystemWarn().show();
        }

    }

    private static void update_ads(String content) {

        HashMap<String, AdBean> ads = JSON.parseObject(content, new TypeReference<HashMap<String, AdBean>>() {
        });

        CustomDataByVendingBean customDataByVending = AppCacheManager.getCustomDataByVending();

        customDataByVending.setAds(ads);

        AppCacheManager.setCustomDataByVending(customDataByVending);

        List<Activity> acts = AppManager.getAppManager().getActivityStack();
        if (acts != null) {
            if (acts.size() > 0) {
                for (Activity act : acts) {
                    if (act instanceof MainActivity) {
                        MainActivity act_MainActivity = (MainActivity) act;
                        act_MainActivity.loadAds(ads);
                        break;
                    }
                }
            }
        }
    }

    private static void update_stock(String content) {
        try {

            UpdateSkuStockBean updateSkuStock = JSON.parseObject(content, new TypeReference<UpdateSkuStockBean>() {
            });


            CustomDataByVendingBean customDataByVending = AppCacheManager.getCustomDataByVending();

            HashMap<String, SkuBean> skus = customDataByVending.getSkus();

            String skuId = updateSkuStock.getSkuId();

            if (!StringUtil.isEmpty(skuId)) {
                if (skus.containsKey(skuId)) {

                    SkuBean sku = skus.get(skuId);

                    sku.setSellQuantity(updateSkuStock.getSellQuantity());
                    sku.setSalePrice(updateSkuStock.getSalePrice());
                    sku.setSalePriceByVip(updateSkuStock.getSalePriceByVip());
                    sku.setOffSell(updateSkuStock.isOffSell());

                    skus.put(skuId, sku);

                    customDataByVending.setSkus(skus);
                }
            }

        } catch (Exception ex) {
            LogUtil.e(TAG, ex);
        }

    }

    private static void pay_success(String content) {

        OrderPayStatusQueryResultBean payResult = JSON.parseObject(content, new TypeReference<OrderPayStatusQueryResultBean>() {
        });

        if (payResult != null) {
            List<Activity> acts = AppManager.getAppManager().getActivityStack();
            if (acts != null) {
                if (acts.size() > 0) {
                    for (Activity act : acts) {
                        if (act instanceof CartActivity) {
//                            if (CartActivity.LAST_ORDERID.equals(payResult.getId())) {
//                                CartActivity act_CartActivity = (CartActivity) act;
//                                act_CartActivity.doPaySuccess(payResult);
//                                break;
//                            }
                        }
                    }
                }
            }
        }

    }

    private static void open_pickup_door() {
        CabinetCtrlByDS cabinetCtrlByDS = CabinetCtrlByDS.getInstance();
        cabinetCtrlByDS.connect();
        cabinetCtrlByDS.openPickupDoor();
    }
}
