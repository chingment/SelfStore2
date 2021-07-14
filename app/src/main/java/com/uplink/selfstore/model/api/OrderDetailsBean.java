package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chingment on 2019/2/28.
 */

public class OrderDetailsBean implements Serializable {

    private String orderId;
    private int status;
    private int payStatus;
    private List<OrderDetailsSkuBean> Skus;


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public int getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(int payStatus) {
        this.payStatus = payStatus;
    }

    public List<OrderDetailsSkuBean> getSkus() {
        return Skus;
    }

    public void setSkus(List<OrderDetailsSkuBean> skus) {
        Skus = skus;
    }
}
