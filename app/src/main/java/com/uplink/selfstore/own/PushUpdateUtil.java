package com.uplink.selfstore.own;


import android.app.Activity;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.activity.CartActivity;
import com.uplink.selfstore.activity.MainActivity;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.model.api.GlobalDataSetBean;
import com.uplink.selfstore.model.api.ImgSetBean;
import com.uplink.selfstore.model.api.OrderPayStatusQueryResultBean;
import com.uplink.selfstore.model.api.ProductSkuBean;
import com.uplink.selfstore.model.push.UpdateHomeLogoBean;
import com.uplink.selfstore.model.push.UpdateProductSkuStockBean;
import com.uplink.selfstore.model.push.*;
import com.uplink.selfstore.ostCtrl.OstCtrlInterface;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.utils.LogUtil;

import java.util.HashMap;
import java.util.List;

/**
 * Created by chingment on 2019/3/20.
 */

public class PushUpdateUtil {

    private static final String TAG = "PushUpdateUtil";

    public static void receive(String cmd, String content) {

        LogUtil.i(TAG, "cmd:" + cmd + ",content:" + content);

        switch (cmd) {
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
                updateHomeLogo(content);//更新机器logo
                break;
            case "MCmdUpdateHomeBanners":
                LogUtil.d("进入update:HomeBanners");
                updateHomeBanners(content);//更新机器banner
                break;
            case "MCmdUpdateProductSkuStock":
                LogUtil.d("进入update:ProductSkuStock");
                //updateProductSkuStock(content);//更新机器种类
                break;
            case "MCmdPaySuccess":
                LogUtil.d("进入paySuccess");
                //paySuccess(content);//支付成功
                break;
            case "MCmdDsx01OpenPickupDoor":
                LogUtil.d("进入openPickupDoor");
                dsx01openPickupDoor();//支付成功
                break;
        }

        BaseFragmentActivity.eventNotify(cmd,"接收命令成功",null);

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
            if (currentActivity.getDialogBySystemWarn().isShowing()) {
                currentActivity.getDialogBySystemWarn().setBtnCloseVisibility(View.GONE);
                currentActivity.getDialogBySystemWarn().dismiss();
            }
        } else if (sysSetStatusBean.getStatus() == 2) {
            if (!currentActivity.getDialogBySystemWarn().isShowing()) {
                currentActivity.getDialogBySystemWarn().setBtnCloseVisibility(View.GONE);
                currentActivity.getDialogBySystemWarn().show();
            }
        }

    }

    private static void updateHomeLogo(String content) {

        UpdateHomeLogoBean updateHomeLogo = JSON.parseObject(content, new TypeReference<UpdateHomeLogoBean>() {
        });


        GlobalDataSetBean globalDataSet = AppCacheManager.getGlobalDataSet();

        globalDataSet.getMachine().setLogoImgUrl(updateHomeLogo.getUrl());

        List<Activity> acts = AppManager.getAppManager().getActivityStack();
        if (acts != null) {
            if (acts.size() > 0) {
                for (Activity act : acts) {

                    if (act instanceof MainActivity) {

                        MainActivity act_Main = (MainActivity) act;
                        act_Main.loadLogo();
                        break;
                    }
                }
            }
        }
    }

    private static void updateHomeBanners(String content) {

        List<ImgSetBean> banners = JSON.parseObject(content, new TypeReference<List<ImgSetBean>>() {
        });

        GlobalDataSetBean globalDataSet = AppCacheManager.getGlobalDataSet();

        globalDataSet.setBanners(banners);

        AppCacheManager.setGlobalDataSet(globalDataSet);

        List<Activity> acts = AppManager.getAppManager().getActivityStack();
        if (acts != null) {
            if (acts.size() > 0) {
                for (Activity act : acts) {
                    if (act instanceof MainActivity) {
                        MainActivity act_MainActivity = (MainActivity) act;
                        act_MainActivity.loadBanner();
                        break;
                    }
                }
            }
        }
    }

    private static void updateProductSkuStock(String content) {
        try {

            UpdateProductSkuStockBean updateProductSkuStock = JSON.parseObject(content, new TypeReference<UpdateProductSkuStockBean>() {
            });


            GlobalDataSetBean globalDataSet = AppCacheManager.getGlobalDataSet();

            HashMap<String, ProductSkuBean> productSkus = globalDataSet.getProductSkus();


            if (productSkus.get(updateProductSkuStock.getId()) != null) {
                productSkus.get(updateProductSkuStock.getId()).setSellQuantity(updateProductSkuStock.getSellQuantity());
                productSkus.get(updateProductSkuStock.getId()).setSalePrice(updateProductSkuStock.getSalePrice());
                productSkus.get(updateProductSkuStock.getId()).setSalePriceByVip(updateProductSkuStock.getSalePriceByVip());
                productSkus.get(updateProductSkuStock.getId()).setOffSell(updateProductSkuStock.isOffSell());
            }

            globalDataSet.setProductSkus(productSkus);

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
                            if (CartActivity.LAST_ORDERID.equals(payResult.getId())) {
                                CartActivity act_CartActivity = (CartActivity) act;
                                act_CartActivity.doPaySuccess(payResult);
                                break;
                            }
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
