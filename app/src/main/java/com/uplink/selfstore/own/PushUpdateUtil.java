package com.uplink.selfstore.own;

import android.app.Activity;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.activity.MainActivity;
import com.uplink.selfstore.activity.ProductKindActivity;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.GlobalDataSetBean;
import com.uplink.selfstore.model.api.ImgSetBean;
import com.uplink.selfstore.model.api.ProductKindBean;
import com.uplink.selfstore.model.api.ProductSkuBean;
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
            case "update_logo":
                update_logo(content);//更新机器logo
                break;
            case "update_banner":
                update_banner(content);//更新机器banner
                break;
            case "update:productSkuStock":
                LogUtil.d("进入update:productSkuStock");
                update_productSku_Stock(content);//更新机器种类
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

    private static void update_logo(String content) {

    }

    private static void update_banner(String content) {

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

    private static void update_product_kind(String content) {

        List<ProductKindBean> productKinds = JSON.parseObject(content, new TypeReference<List<ProductKindBean>>() {
        });

        GlobalDataSetBean globalDataSet = AppCacheManager.getGlobalDataSet();

        globalDataSet.setProductKinds(productKinds);

        AppCacheManager.setGlobalDataSet(globalDataSet);

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
    }

    public  static void update_productSku_Stock(String content){
        LogUtil.d("进入update:productSkuStock2");
        List<UpdateProductSkuStockBean> updateProductSkuStocks = JSON.parseObject(content, new TypeReference<List<UpdateProductSkuStockBean>>() {
        });


        GlobalDataSetBean globalDataSet = AppCacheManager.getGlobalDataSet();

        HashMap<String, ProductSkuBean> productSkus=globalDataSet.getProductSkus();

        for (UpdateProductSkuStockBean updateStock:
                updateProductSkuStocks     ) {

           if(productSkus.get(updateStock.getId())!=null){

               productSkus.get(updateStock.getId()).setLockQuantity(updateStock.getLockQuantity());
               productSkus.get(updateStock.getId()).setSellQuantity(updateStock.getSellQuantity());
               productSkus.get(updateStock.getId()).setSellQuantity(updateStock.getSumQuantity());
               productSkus.get(updateStock.getId()).setSalePrice(updateStock.getSalePrice());
               productSkus.get(updateStock.getId()).setSalePriceByVip(updateStock.getSalePriceByVip());
               productSkus.get(updateStock.getId()).setOffSell(updateStock.isOffSell());
           }

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

    }
}
