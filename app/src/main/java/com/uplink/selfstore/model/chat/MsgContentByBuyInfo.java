package com.uplink.selfstore.model.chat;

import java.util.List;

public class MsgContentByBuyInfo {

    private  String deviceId;
    private  String storeName;
    private  int handleStatus;
    private  String handleDescribe;
    private String operateUserName;

    private List<SkuBean> skus;
    public String getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    public String getStoreName() {
        return storeName;
    }
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
    public List<SkuBean> getSkus() {
        return skus;
    }
    public void setSkus(List<SkuBean> skus) {
        this.skus = skus;
    }

    public int getHandleStatus() {
        return handleStatus;
    }

    public void setHandleStatus(int handleStatus) {
        this.handleStatus = handleStatus;
    }

    public String getOperateUserName() {
        return operateUserName;
    }

    public void setOperateUserName(String operateUserName) {
        this.operateUserName = operateUserName;
    }

    public String getHandleDescribe() {
        return handleDescribe;
    }

    public void setHandleDescribe(String handleDescribe) {
        this.handleDescribe = handleDescribe;
    }
}
