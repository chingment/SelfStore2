package com.uplink.selfstore.own;


import android.app.Activity;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.activity.CartActivity;
import com.uplink.selfstore.activity.MainActivity;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.model.api.AdBean;
import com.uplink.selfstore.model.api.GlobalDataSetBean;
import com.uplink.selfstore.model.api.OrderPayStatusQueryResultBean;
import com.uplink.selfstore.model.api.SkuBean;
import com.uplink.selfstore.model.push.UpdateHomeLogoBean;
import com.uplink.selfstore.model.push.UpdateSkuStockBean;
import com.uplink.selfstore.model.push.*;
import com.uplink.selfstore.ostCtrl.OstCtrlInterface;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.util.HashMap;
import java.util.List;

/**
 * Created by chingment on 2019/3/20.
 */

public class PushUpdateUtil {

    private static final String TAG = "PushUpdateUtil";

    public static void receive(String type, String content) {

        LogUtil.i(TAG, "type:" + type + ",content:" + content);

        switch (type) {
            case "MCmdSysReboot":
                sysReboot();//重启系统
                break;
            case "MCmdSysShutdown":
                sysShutdown();//关闭系统
                break;
            case "MCmdSysSetStatus":
                sysSetStatus(content);
                break;
            case "MCmdUpdateHomeLogo":
                LogUtil.d("进入update:HomeLogo");
                updateHomeLogo(content);//更新设备logo
                break;
            case "MCmdUpdateAds":
                LogUtil.d("进入update:Ads");
                updateAds(content);//更新设备banner
                break;
            case "MCmdUpdateSkuStock":
                LogUtil.d("进入update:SkuStock");
                //updateSkuStock(content);//更新设备种类
                break;
            case "MCmdPaySuccess":
                LogUtil.d("进入paySuccess");
                //paySuccess(content);//支付成功
                break;
            case "MCmdDsx01OpenPickupDoor":
                LogUtil.d("进入openPickupDoor");
                dsx01openPickupDoor();//打开取货门
                break;
        }

        BaseFragmentActivity.eventNotify(type,"接收命令成功",null);

    }

    private static void sysReboot() {
        OstCtrlInterface.getInstance().reboot(AppContext.getInstance().getApplicationContext());
    }

    private static void sysShutdown() {
        OstCtrlInterface.getInstance().shutdown(AppContext.getInstance().getApplicationContext());
    }

    private static void sysSetStatus(String content) {

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

    private static void updateHomeLogo(String content) {

        UpdateHomeLogoBean updateHomeLogo = JSON.parseObject(content, new TypeReference<UpdateHomeLogoBean>() {
        });


        GlobalDataSetBean globalDataSet = AppCacheManager.getGlobalDataSet();

        globalDataSet.getDevice().setLogoImgUrl(updateHomeLogo.getUrl());

        List<Activity> acts = AppManager.getAppManager().getActivityStack();
        if (acts != null) {
            if (acts.size() > 0) {
                for (Activity act : acts) {

                    if (act instanceof MainActivity) {

                        MainActivity act_Main = (MainActivity) act;
                        act_Main.loadLogo(updateHomeLogo.getUrl());
                        break;
                    }
                }
            }
        }
    }

    private static void updateAds(String content) {

        HashMap<String, AdBean> ads = JSON.parseObject(content, new TypeReference<HashMap<String, AdBean>>() {
        });

        GlobalDataSetBean globalDataSet = AppCacheManager.getGlobalDataSet();

        globalDataSet.setAds(ads);

        AppCacheManager.setGlobalDataSet(globalDataSet);

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

    private static void updateSkuStock(String content) {
        try {

            UpdateSkuStockBean updateSkuStock = JSON.parseObject(content, new TypeReference<UpdateSkuStockBean>() {
            });


            GlobalDataSetBean globalDataSet = AppCacheManager.getGlobalDataSet();

            HashMap<String, SkuBean> skus = globalDataSet.getSkus();

            String skuId = updateSkuStock.getSkuId();

            if (!StringUtil.isEmpty(skuId)) {
                if (skus.containsKey(skuId)) {

                    SkuBean sku = skus.get(skuId);

                    sku.setSellQuantity(updateSkuStock.getSellQuantity());
                    sku.setSalePrice(updateSkuStock.getSalePrice());
                    sku.setSalePriceByVip(updateSkuStock.getSalePriceByVip());
                    sku.setOffSell(updateSkuStock.isOffSell());

                    skus.put(skuId, sku);

                    globalDataSet.setSkus(skus);
                }
            }

        } catch (Exception ex) {
            LogUtil.e(TAG, ex);
        }

    }

    private static void paySuccess(String content) {

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

    private static void dsx01openPickupDoor() {
        CabinetCtrlByDS cabinetCtrlByDS = CabinetCtrlByDS.getInstance();
        cabinetCtrlByDS.connect();
        cabinetCtrlByDS.openPickupDoor();
    }
}
