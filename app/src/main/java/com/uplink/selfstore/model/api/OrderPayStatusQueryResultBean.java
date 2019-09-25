package com.uplink.selfstore.model.api;

import java.io.Serializable;

/**
 * Created by chingment on 2019/2/28.
 */

public class OrderPayStatusQueryResultBean implements Serializable {

    private String orderId;
    private String orderSn;
    private int status;
    private OrderDetailsBean orderDetails;

    public OrderDetailsBean getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(OrderDetailsBean orderDetails) {
        this.orderDetails = orderDetails;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderSn() {
        return orderSn;
    }

    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
