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
    private HashMap<String,ProductBean> products;
    private List<ImgSetBean> banners;

    public HashMap<String, ProductBean> getProducts() {
        return products;
    }

    public void setProducts(HashMap<String, ProductBean> products) {
        this.products = products;
    }

    public MachineBean getMachine() {
        return machine;
    }

    public void setMachine(MachineBean machine) {
        this.machine = machine;
    }

    public List<ImgSetBean> getBanners() {
        return banners;
    }

    public void setBanners(List<ImgSetBean> banners) {
        this.banners = banners;
    }

    public List<ProductKindBean> getProductKinds() {
        return productKinds;
    }

    public void setProductKinds(List<ProductKindBean> productKinds) {
        this.productKinds = productKinds;
    }

}
