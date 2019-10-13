package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class SlotProductSkuBean implements Serializable {
    private  String id;
    private  String slotId;
    private  String name;
    private  String mainImgUrl;
    private  String sumQuantity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMainImgUrl() {
        return mainImgUrl;
    }

    public void setMainImgUrl(String mainImgUrl) {
        this.mainImgUrl = mainImgUrl;
    }

    public String getSumQuantity() {
        return sumQuantity;
    }

    public void setSumQuantity(String sumQuantity) {
        this.sumQuantity = sumQuantity;
    }

    public String getLockQuantity() {
        return lockQuantity;
    }

    public void setLockQuantity(String lockQuantity) {
        this.lockQuantity = lockQuantity;
    }

    public String getSellQuantity() {
        return sellQuantity;
    }

    public void setSellQuantity(String sellQuantity) {
        this.sellQuantity = sellQuantity;
    }

    public boolean isOffSell() {
        return isOffSell;
    }

    public void setOffSell(boolean offSell) {
        isOffSell = offSell;
    }

    private  String lockQuantity;
    private  String sellQuantity;
    private  boolean isOffSell;
}
