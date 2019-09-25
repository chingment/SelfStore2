package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chingment on 2019/2/28.
 */

public class OrderDetailsBean implements Serializable {

    private String sn;
    private List<OrderDetailsSkuBean> skus;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public List<OrderDetailsSkuBean> getSkus() {
        return skus;
    }

    public void setSkus(List<OrderDetailsSkuBean> skus) {
        this.skus = skus;
    }

}
