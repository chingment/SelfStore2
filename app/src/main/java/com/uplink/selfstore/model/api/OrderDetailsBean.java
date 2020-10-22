package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chingment on 2019/2/28.
 */

public class OrderDetailsBean implements Serializable {

    private String orderId;
    private int status;
    private List<OrderDetailsSkuBean> productSkus;


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

    public List<OrderDetailsSkuBean> getProductSkus() {
        return productSkus;
    }

    public void setProductSkus(List<OrderDetailsSkuBean> productSkus) {
        this.productSkus = productSkus;
    }
}
