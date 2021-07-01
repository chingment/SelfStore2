package com.uplink.selfstore.own;

import com.uplink.selfstore.model.api.*;
import com.uplink.selfstore.utils.ACache;
import com.uplink.selfstore.utils.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by chingment on 2018/1/16.
 */

public class AppCacheManager {

    private static String Cache_Key_OpUserInfo = "Cache_Key_OpUserInfo";
    private static String Cache_Key_LastUserName = "Cache_LastUserName";
    private static String Cache_Key_LastUpdateTime = "Cache_LastUpdateTime";
    private static String Cache_Key_Device = "Cache_Device";
    private static String Cache_Key_CustomDataByVending = "Cache_CustomDataByVending";
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


    public static DeviceBean getDevice() {

        DeviceBean device = (DeviceBean) AppCacheManager.getCache().getAsObject(Cache_Key_Device);

        if(device==null) {
            device=new DeviceBean();
            device.setDeviceId("");
            return device;
        }

        if(device.getScanner()==null){
            ScannerBean scanner=new ScannerBean();
            scanner.setUse(false);
            device.setScanner(scanner);
        }

        if(device.getFingerVeinner()==null){
            FingerVeinnerBean fingerVeinner=new FingerVeinnerBean();
            fingerVeinner.setUse(false);
            device.setFingerVeinner(fingerVeinner);
        }

        if(device.getCabinets()==null){
            HashMap<String, CabinetBean> cabinets=new HashMap<String, CabinetBean>();
            device.setCabinets(cabinets);
        }

        if(device.getPayOptions()==null){
            List<TerminalPayOptionBean> payOptions=new ArrayList<>();
            device.setPayOptions(payOptions);
        }

        return device;

    }

    public static void setDevice(DeviceBean bean) {
        AppCacheManager.getCache().remove(Cache_Key_Device);
        AppCacheManager.getCache().put(Cache_Key_Device, bean);
    }


    public static void setCustomDataByVending(CustomDataByVendingBean bean) {
        AppCacheManager.getCache().remove(Cache_Key_CustomDataByVending);
        AppCacheManager.getCache().put(Cache_Key_CustomDataByVending, bean);
    }


    public static CustomDataByVendingBean getCustomDataByVending() {
        CustomDataByVendingBean customDataByVending = (CustomDataByVendingBean) AppCacheManager.getCache().getAsObject(Cache_Key_CustomDataByVending);
        return customDataByVending;
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



        return cartSkusByCache;

    }

    public static SkuBean getSku(String skuId) {
        SkuBean bean = null;

        CustomDataByVendingBean customDataByVending = getCustomDataByVending();

        if (customDataByVending != null) {
            if (customDataByVending.getSkus() != null) {

                HashMap<String, SkuBean> skus = customDataByVending.getSkus();

                bean = skus.get(skuId);

            }
        }

        return bean;
    }



}
