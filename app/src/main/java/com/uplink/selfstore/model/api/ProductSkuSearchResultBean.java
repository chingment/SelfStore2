package com.uplink.selfstore.model.api;

import java.util.List;

public class ProductSkuSearchResultBean {
    private List<SearchProductSkuBean> productSkus;

    public List<SearchProductSkuBean> getProductSkus() {
        return productSkus;
    }

    public void setProductSkus(List<SearchProductSkuBean> productSkus) {
        this.productSkus = productSkus;
    }
}
