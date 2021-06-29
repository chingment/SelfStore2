package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chingment on 2018/6/5.
 */

public class GlobalDataSetBean implements Serializable {


    private DeviceBean device;
    private List<KindBean> kinds;
    private HashMap<String, SkuBean> skus;
    private HashMap<String, AdBean> ads;

    public DeviceBean getDevice() {
        return device;
    }

    public void setDevice(DeviceBean device) {
        this.device = device;
    }


    public HashMap<String, AdBean> getAds() {
        return ads;
    }

    public void setAds(HashMap<String, AdBean> ads) {
        this.ads = ads;
    }

    public List<KindBean> getKinds() {
        return kinds;
    }

    public void setKinds(List<KindBean> kinds) {
        this.kinds = kinds;
    }

    public HashMap<String, SkuBean> getSkus() {
        return skus;
    }

    public void setSkus(HashMap<String, SkuBean> skus) {
        this.skus = skus;
    }
}
