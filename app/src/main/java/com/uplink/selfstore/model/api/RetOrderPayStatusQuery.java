package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chingment on 2019/2/28.
 */

public class RetOrderPayStatusQuery implements Serializable {

    private String orderId;
    private int orderStatus;
    private String payTransId;
    private int payStatus;
    private List<OrderDetailsSkuBean> skus;


    public int getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(int payStatus) {
        this.payStatus = payStatus;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }


    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPayTransId() {
        return payTransId;
    }

    public void setPayTransId(String payTransId) {
        this.payTransId = payTransId;
    }


    public List<OrderDetailsSkuBean> getSkus() {
        return skus;
    }

    public void setSkus(List<OrderDetailsSkuBean> skus) {
        this.skus = skus;
    }
}
