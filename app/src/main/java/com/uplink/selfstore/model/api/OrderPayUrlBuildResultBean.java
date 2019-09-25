package com.uplink.selfstore.model.api;

import java.io.Serializable;

/**
 * Created by chingment on 2019/3/28.
 */

public class OrderPayUrlBuildResultBean implements Serializable {
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPayUrl() {
        return payUrl;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    private String orderId;
    private String payUrl;
}
