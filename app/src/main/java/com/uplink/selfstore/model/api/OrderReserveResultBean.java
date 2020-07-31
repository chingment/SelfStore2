package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chingment on 2018/10/8.
 */

public class OrderReserveResultBean implements Serializable {

    private  String orderId;
    private  String chargeAmount;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(String chargeAmount) {
        this.chargeAmount = chargeAmount;
    }
}
