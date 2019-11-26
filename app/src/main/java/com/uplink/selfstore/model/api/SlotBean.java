package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class SlotBean implements Serializable {
    private  String id;
    private  String productSkuId;
    private  String productSkuName;
    private  String productSkuMainImgUrl;
    private  int sumQuantity;
    private  int lockQuantity;
    private  int sellQuantity;
    private  boolean isOffSell;
    private  int maxLimitSumQuantity;
    private int version;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductSkuId() {
        return productSkuId;
    }

    public void setProductSkuId(String productSkuId) {
        this.productSkuId = productSkuId;
    }

    public String getProductSkuName() {
        return productSkuName;
    }

    public void setProductSkuName(String productSkuName) {
        this.productSkuName = productSkuName;
    }

    public String getProductSkuMainImgUrl() {
        return productSkuMainImgUrl;
    }

    public void setProductSkuMainImgUrl(String productSkuMainImgUrl) {
        this.productSkuMainImgUrl = productSkuMainImgUrl;
    }


    public int getSumQuantity() {
        return sumQuantity;
    }

    public void setSumQuantity(int sumQuantity) {
        this.sumQuantity = sumQuantity;
    }

    public int getLockQuantity() {
        return lockQuantity;
    }

    public void setLockQuantity(int lockQuantity) {
        this.lockQuantity = lockQuantity;
    }

    public int getSellQuantity() {
        return sellQuantity;
    }

    public void setSellQuantity(int sellQuantity) {
        this.sellQuantity = sellQuantity;
    }

    public boolean isOffSell() {
        return isOffSell;
    }

    public void setOffSell(boolean offSell) {
        isOffSell = offSell;
    }

    public int getMaxLimitSumQuantity() {
        return maxLimitSumQuantity;
    }

    public void setMaxLimitSumQuantity(int maxLimitSumQuantity) {
        this.maxLimitSumQuantity = maxLimitSumQuantity;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
