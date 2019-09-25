package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chingment on 2019/9/25.
 */

public class RefSkuBean implements Serializable {
    private String id;
    private int receptionMode;
    private int sumQuantity;
    private int lockQuantity;
    private int sellQuantity;
    private  boolean isOffSell;
    private  float salePrice;
    private  float salePriceByVip;
    private  float showPrice;
    private String specDes;
    private  boolean isShowPrice;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getReceptionMode() {
        return receptionMode;
    }

    public void setReceptionMode(int receptionMode) {
        this.receptionMode = receptionMode;
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

    public float getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(float salePrice) {
        this.salePrice = salePrice;
    }

    public float getSalePriceByVip() {
        return salePriceByVip;
    }

    public void setSalePriceByVip(float salePriceByVip) {
        this.salePriceByVip = salePriceByVip;
    }

    public float getShowPrice() {
        return showPrice;
    }

    public void setShowPrice(float showPrice) {
        this.showPrice = showPrice;
    }

    public String getSpecDes() {
        return specDes;
    }

    public void setSpecDes(String specDes) {
        this.specDes = specDes;
    }

    public boolean isShowPrice() {
        return isShowPrice;
    }

    public void setShowPrice(boolean showPrice) {
        isShowPrice = showPrice;
    }
}
