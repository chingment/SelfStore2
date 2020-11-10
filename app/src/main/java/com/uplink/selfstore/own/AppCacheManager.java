package com.uplink.selfstore.own;

import com.uplink.selfstore.model.api.*;
import com.uplink.selfstore.utils.ACache;
import com.uplink.selfstore.utils.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        if(!StringUtil.isEmptyNotNull(userName)) {
            AppCacheManager.getCache().put(Cache_Key_LastUserName, userName);
        }
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
            bean.setMachineId("");
            return bean;
        }

        if(globalDataSet.getMachine()==null){
            bean.setMachineId("");
            return bean;
        }

        bean = globalDataSet.getMachine();

        if(bean.getScanner()==null){
            ScannerBean scanner=new ScannerBean();
            scanner.setUse(false);
            bean.setScanner(scanner);
        }

        if(bean.getFingerVeinner()==null){
            FingerVeinnerBean fingerVeinner=new FingerVeinnerBean();
            fingerVeinner.setUse(false);
            bean.setFingerVeinner(fingerVeinner);
        }

        if(bean.getCabinets()==null){
            HashMap<String, CabinetBean> cabinets=new HashMap<String, CabinetBean>();
            bean.setCabinets(cabinets);
        }

        if(bean.getPayOptions()==null){
            List<TerminalPayOptionBean> payOptions=new ArrayList<>();
            bean.setPayOptions(payOptions);
        }

        return bean;

    }

    public static void setGlobalDataSet(GlobalDataSetBean bean) {
        AppCacheManager.getCache().remove(Cache_Key_GlobalDataSet);
        AppCacheManager.getCache().put(Cache_Key_GlobalDataSet, bean);
    }


    public static GlobalDataSetBean _globalDataSet;

    public static GlobalDataSetBean getGlobalDataSet() {

        //if (_globalDataSet == null) {
            _globalDataSet = (GlobalDataSetBean) AppCacheManager.getCache().getAsObject(Cache_Key_GlobalDataSet);
        //}

        return _globalDataSet;

    }

    public static void setCartSkus(LinkedHashMap<String, CartSkuBean> list) {
        if (list == null) {
            AppCacheManager.getCache().remove(Cache_Key_Cart);
        } else {
            AppCacheManager.getCache().put(Cache_Key_Cart, list);
        }
    }

    public static void clearCartSkus() {
        AppCacheManager.getCache().remove(Cache_Key_Cart);
    }

    public static  LinkedHashMap<String, CartSkuBean> getCartSkus() {
        LinkedHashMap<String, CartSkuBean> cartSkus = new LinkedHashMap<String, CartSkuBean>();


        LinkedHashMap<String, CartSkuBean> cartSkusByCache = (LinkedHashMap<String, CartSkuBean>) AppCacheManager.getCache().getAsObject(Cache_Key_Cart);
        if (cartSkusByCache == null)
            return cartSkus;


//        for(String key : cartSkus.keySet()) {
//            CartSkuBean bean = cartSkus.get(key);
//            sumQuantity += bean.getQuantity();
//            sumSalesPrice += bean.getQuantity() * bean.getSalePrice();
//        }
//
//        for (CartSkuBean bean : cartSkusByCache) {
//            ProductSkuBean productSku = globalDataSet.getProductSkus().get(bean.getId());
//            if (productSku != null) {
//                CartSkuBean cartSku = new CartSkuBean();
//                cartSku.setId(productSku.getId());
//                cartSku.setMainImgUrl(productSku.getMainImgUrl());
//                cartSku.setQuantity(bean.getQuantity());
//                cartSku.setTrgVideoService(productSku.isTrgVideoService());
//                cartSku.setName(productSku.getName());
//                cartSku.setSalePrice(productSku.getSalePrice());
//                cartSkus.add(cartSku);
//            }
//        }

        //AppCacheManager.setCartSkus(cartSkus);

        return cartSkusByCache;

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
