package com.uplink.selfstore.model.api;

/**
 * Created by chingment on 2018/6/20.
 */

public class CartStatisticsBean {
    private int sumQuantity;

    public int getSumQuantity() {
        return sumQuantity;
    }

    public void setSumQuantity(int sumQuantity) {
        this.sumQuantity = sumQuantity;
    }

    public float getSumSalesPrice() {
        return sumSalesPrice;
    }

    public void setSumSalesPrice(float sumSalesPrice) {
        this.sumSalesPrice = sumSalesPrice;
    }

    private float sumSalesPrice;
}
