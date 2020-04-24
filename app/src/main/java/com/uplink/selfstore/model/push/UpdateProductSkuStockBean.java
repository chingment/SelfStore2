package com.uplink.selfstore.model.push;

import java.io.Serializable;
import java.util.List;

public class UpdateProductSkuStockBean implements Serializable {

    private String id;
    private float salePrice;
    private float salePriceByVip;
    private boolean isOffSell;
    private int sumQuantity;
    private int lockQuantity;
    private int sellQuantity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isOffSell() {
        return isOffSell;
    }

    public void setOffSell(boolean offSell) {
        isOffSell = offSell;
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
}
