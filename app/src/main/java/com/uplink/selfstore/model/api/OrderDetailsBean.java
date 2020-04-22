package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chingment on 2019/2/28.
 */

public class OrderDetailsBean implements Serializable {

    private String id;
    private int status;
    private List<OrderDetailsSkuBean> productSkus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
