package com.uplink.selfstore.model.api;

import java.io.Serializable;

/**
 * Created by chingment on 2018/10/8.
 */

public class OrderReserveResultBean implements Serializable {

    private String orderId;
    private String orderSn;
    private String chargeAmount;

    public String getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(String chargeAmount) {
        this.chargeAmount = chargeAmount;
    }


    public String getOrderSn() {
        return orderSn;
    }

    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    public String getOrderId() {
        return orderId;

    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
