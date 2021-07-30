package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class SlotBean implements Serializable {
    private  String slotId;
    private  String slotName;
    private  String stockId;
    private  String cabinetId;
    private  String skuId;
    private  String skuCumCode;
    private  String skuName;
    private  String skuMainImgUrl;
    private  String skuSpecDes;
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

    public String getSlotName() {
        return slotName;
    }

    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public String getSkuCumCode() {
        return skuCumCode;
    }

    public void setSkuCumCode(String skuCumCode) {
        this.skuCumCode = skuCumCode;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public String getSkuMainImgUrl() {
        return skuMainImgUrl;
    }

    public void setSkuMainImgUrl(String skuMainImgUrl) {
        this.skuMainImgUrl = skuMainImgUrl;
    }

    public String getSkuSpecDes() {
        return skuSpecDes;
    }

    public void setSkuSpecDes(String skuSpecDes) {
        this.skuSpecDes = skuSpecDes;
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
}
