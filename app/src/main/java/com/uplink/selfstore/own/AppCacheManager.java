package com.uplink.selfstore.own;

import com.uplink.selfstore.model.api.*;
import com.uplink.selfstore.utils.ACache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chingment on 2018/1/16.
 */

public class AppCacheManager {

    private static String Cache_Key_OpUserInfo = "Cache_Key_OpUserInfo";
    private static String Cache_Key_LastUserName = "Cache_LastUserName";
    private static String Cache_Key_LastUpdateTime = "Cache_LastUpdateTime";
    private static String Cache_Key_Machine = "Cache_Machine";
    private static String Cache_Key_GlobalDataSet = "Cache_Key_GlobalDataSet";


    private static String Cache_Key_Cart = "Cache_Key_Cart";


    private static ACache getCache() {

        return ACache.get(AppContext.getInstance());
    }

    public static void setLastUserName(String userName) {
        AppCacheManager.getCache().put(Cache_Key_LastUserName, userName);
    }

    public static String getLastUserName() {

        String userName = AppCacheManager.getCache().getAsString(Cache_Key_LastUserName);

        return userName;

    }

    public static void setOpUserInfo(OpUserInfoBean opUserInfoBean) {
        AppCacheManager.getCache().put(Cache_Key_OpUserInfo, opUserInfoBean);
    }

    public static OpUserInfoBean getOpUserInfo() {

        OpUserInfoBean opUserInfoBean = (OpUserInfoBean)AppCacheManager.getCache().getAsObject(Cache_Key_OpUserInfo);

        return opUserInfoBean;

    }

    public static void setLastUpdateTime(String lastUpdateTime) {

        if (lastUpdateTime == null) {
            lastUpdateTime = "";
        }

        AppCacheManager.getCache().put(Cache_Key_LastUpdateTime, lastUpdateTime);

    }

    public static String getLastUpdateTime() {

        String lastUpdateTime = AppCacheManager.getCache().getAsString(Cache_Key_LastUpdateTime);

        if (lastUpdateTime == null)
            return "";
        return lastUpdateTime;

    }


    public static MachineBean getMachine() {

        MachineBean bean=new MachineBean();

        GlobalDataSetBean globalDataSet = getGlobalDataSet();
        if(globalDataSet==null) {
            bean.setId("");
            return bean;
        }

        bean = globalDataSet.getMachine();

        return bean;

    }

    public static void setGlobalDataSet(GlobalDataSetBean bean) {

        _globalDataSet = bean;
        //AppCacheManager.getCache().put(Cache_Key_GlobalDataSet, bean);


    }


    public static GlobalDataSetBean _globalDataSet;

    public static GlobalDataSetBean getGlobalDataSet() {

//        if (_globalDataSet == null) {
//            _globalDataSet = (GlobalDataSetBean) AppCacheManager.getCache().getAsObject(Cache_Key_GlobalDataSet);
//        }

        return _globalDataSet;

    }

    public static void setCartSkus(List<CartSkuBean> list) {
        if(list==null)
        {
            AppCacheManager.getCache().remove(Cache_Key_Cart);
        }
        else
        {
            ArrayList<CartSkuBean>   bean= (ArrayList<CartSkuBean>) list;
            AppCacheManager.getCache().put(Cache_Key_Cart, bean);
        }
    }

    public static List<CartSkuBean> getCartSkus() {

        ArrayList<CartSkuBean> bean = (ArrayList<CartSkuBean>) AppCacheManager.getCache().getAsObject(Cache_Key_Cart);

        if (bean == null) {
            bean = new ArrayList<CartSkuBean>();
        }

        return bean;

    }

    public static ProductSkuBean getProductSku(String skuId) {
        ProductSkuBean bean = null;

        GlobalDataSetBean globalDataSet = getGlobalDataSet();

        if (globalDataSet != null) {
            if (globalDataSet.getProductSkus() != null) {

                HashMap<String, ProductSkuBean> skus = globalDataSet.getProductSkus();

                bean = skus.get(skuId);

            }
        }

        return bean;
    }



}
