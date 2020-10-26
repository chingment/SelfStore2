package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class SlotBean implements Serializable {
    private  String slotId;
    private  String stockId;
    private  String cabinetId;
    private  String productSkuId;
    private  String cumCode;
    private  String name;
    private  String mainImgUrl;
    private  String specDes;
    private  int sumQuantity;
    private  int lockQuantity;
    private  int sellQuantity;
    private  int warnQuantity;
    private  int holdQuantity;
    private  boolean isOffSell;
    private  int maxQuantity;
    private  Boolean isCanAlterMaxQuantity;
    private int version;


    public String getStockId() {
        return stockId;
    }

    public void setStockId(String stockId) {
        this.stockId = stockId;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public String getProductSkuId() {
        return productSkuId;
    }

    public void setProductSkuId(String productSkuId) {
        this.productSkuId = productSkuId;
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(int maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public String getCabinetId() {
        return cabinetId;
    }

    public void setCabinetId(String cabinetId) {
        this.cabinetId = cabinetId;
    }

    public int getWarnQuantity() {
        return warnQuantity;
    }

    public void setWarnQuantity(int warnQuantity) {
        this.warnQuantity = warnQuantity;
    }

    public int getHoldQuantity() {
        return holdQuantity;
    }

    public void setHoldQuantity(int holdQuantity) {
        this.holdQuantity = holdQuantity;
    }

    public Boolean getCanAlterMaxQuantity() {
        return isCanAlterMaxQuantity;
    }

    public void setCanAlterMaxQuantity(Boolean canAlterMaxQuantity) {
        isCanAlterMaxQuantity = canAlterMaxQuantity;
    }

    public String getCumCode() {
        return cumCode;
    }

    public void setCumCode(String cumCode) {
        this.cumCode = cumCode;
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

    public String getSpecDes() {
        return specDes;
    }

    public void setSpecDes(String specDes) {
        this.specDes = specDes;
    }
}
