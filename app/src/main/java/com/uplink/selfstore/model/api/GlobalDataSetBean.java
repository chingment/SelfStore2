package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chingment on 2018/6/5.
 */

public class GlobalDataSetBean implements Serializable {


    private MachineBean machine;
    private List<ProductKindBean> productKinds;
    private HashMap<String, ProductSkuBean> productSkus;
    private HashMap<String, AdBean> ads;

    public HashMap<String, ProductSkuBean> getProductSkus() {
        return productSkus;
    }

    public void setProductSkus(HashMap<String, ProductSkuBean> productSkus) {
        this.productSkus = productSkus;
    }

    public MachineBean getMachine() {
        return machine;
    }

    public void setMachine(MachineBean machine) {
        this.machine = machine;
    }


    public HashMap<String, AdBean> getAds() {
        return ads;
    }

    public void setAds(HashMap<String, AdBean> ads) {
        this.ads = ads;
    }

    public List<ProductKindBean> getProductKinds() {
        return productKinds;
    }

    public void setProductKinds(List<ProductKindBean> productKinds) {
        this.productKinds = productKinds;
    }

}
