package com.uplink.selfstore.model.api;

import java.util.List;

public class SkuSearchResultBean {
    private List<SearchSkuBean> skus;


    public List<SearchSkuBean> getSkus() {
        return skus;
    }

    public void setSkus(List<SearchSkuBean> skus) {
        this.skus = skus;
    }
}
