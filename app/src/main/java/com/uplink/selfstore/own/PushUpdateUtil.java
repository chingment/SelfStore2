package com.uplink.selfstore.own;

import android.app.Activity;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.activity.MainActivity;
import com.uplink.selfstore.activity.ProductKindActivity;
import com.uplink.selfstore.model.api.GlobalDataSetBean;
import com.uplink.selfstore.model.api.ImgSetBean;
import com.uplink.selfstore.model.api.ProductSkuBean;
import com.uplink.selfstore.model.api.UpdateHomeLogoBean;
import com.uplink.selfstore.model.api.UpdateProductSkuStockBean;
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
            case "reboot":
                reboot();//重启系统
                break;
            case "update_baseparams":
                update_baseparams(content);//更新机器基础参数
                break;
            case "update:HomeLogo":
                LogUtil.d("进入update:HomeLogo");
                updateHomeLogo(content);//更新机器logo
                break;
            case "update:HomeBanners":
                LogUtil.d("进入update:HomeBanners");
                updateHomeBanners(content);//更新机器banner
                break;
            case "update:ProductSkuStock":
                LogUtil.d("进入update:ProductSkuStock");
                updateProductSkuStock(content);//更新机器种类
                break;
        }
    }

    private static void reboot() {

        Intent it = new Intent();
        it.setAction("com.fourfaith.reboot");
        it.putExtra("mode", "0");//0 重启 1 关机
        Activity act = AppManager.getAppManager().currentActivity();
        act.sendBroadcast(it);
    }

    private static void update_baseparams(String content) {

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

//    private static void updateStockSlots(String content) {
//
//        List<ProductKindBean> productKinds = JSON.parseObject(content, new TypeReference<List<ProductKindBean>>() {
//        });
//
//        GlobalDataSetBean globalDataSet = AppCacheManager.getGlobalDataSet();
//
//        globalDataSet.setProductKinds(productKinds);
//
//        AppCacheManager.setGlobalDataSet(globalDataSet);
//
//        List<Activity> acts = AppManager.getAppManager().getActivityStack();
//        if (acts != null) {
//            if (acts.size() > 0) {
//                for (Activity act : acts) {
//
//                    if (act instanceof ProductKindActivity) {
//
//                        ProductKindActivity act_ProductKind = (ProductKindActivity) act;
//
//                        act_ProductKind.loadKindData();
//                        break;
//                    }
//                }
//            }
//        }
//    }

    private  static void updateProductSkuStock(String content) {
        try {

            UpdateProductSkuStockBean updateProductSkuStock = JSON.parseObject(content, new TypeReference<UpdateProductSkuStockBean>() {
            });


            GlobalDataSetBean globalDataSet = AppCacheManager.getGlobalDataSet();

            HashMap<String, ProductSkuBean> productSkus = globalDataSet.getProductSkus();


            if (productSkus.get(updateProductSkuStock.getId()) != null) {

                productSkus.get(updateProductSkuStock.getId()).setLockQuantity(updateProductSkuStock.getLockQuantity());
                productSkus.get(updateProductSkuStock.getId()).setSellQuantity(updateProductSkuStock.getSellQuantity());
                productSkus.get(updateProductSkuStock.getId()).setSellQuantity(updateProductSkuStock.getSumQuantity());
                productSkus.get(updateProductSkuStock.getId()).setSalePrice(updateProductSkuStock.getSalePrice());
                productSkus.get(updateProductSkuStock.getId()).setSalePriceByVip(updateProductSkuStock.getSalePriceByVip());
                productSkus.get(updateProductSkuStock.getId()).setOffSell(updateProductSkuStock.isOffSell());
            }


            globalDataSet.setProductSkus(productSkus);

            List<Activity> acts = AppManager.getAppManager().getActivityStack();
            if (acts != null) {
                if (acts.size() > 0) {
                    for (Activity act : acts) {

                        if (act instanceof ProductKindActivity) {

                            ProductKindActivity act_ProductKind = (ProductKindActivity) act;

                            act_ProductKind.loadKindData();
                            break;
                        }
                    }
                }
            }

        } catch (Exception ex) {
            LogUtil.e(TAG, ex);
        }


    }
}
