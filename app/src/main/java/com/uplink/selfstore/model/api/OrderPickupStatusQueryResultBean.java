package com.uplink.selfstore.model.api;

import java.util.List;

/**
 * Created by chingment on 2019/9/29.
 */

public class OrderPickupStatusQueryResultBean {
    private List<OrderDetailsSkuBean> productSkus;

    public List<OrderDetailsSkuBean> getProductSkus() {
        return productSkus;
    }

    public void setProductSkus(List<OrderDetailsSkuBean> productSkus) {
        this.productSkus = productSkus;
    }
}
