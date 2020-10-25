package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

public class AdBean implements Serializable {

    private String adId;
    private String name;
    private List<AdContentBean> contents;

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AdContentBean> getContents() {
        return contents;
    }

    public void setContents(List<AdContentBean> contents) {
        this.contents = contents;
    }
}
