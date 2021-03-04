package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class SearchSkuBean implements Serializable {
    private String skuId;
    private String cumCode;
    private String barCode;
    private String name;
    private String mainImgUrl;
    private String specDes;


    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMainImgUrl() {
        return mainImgUrl;
    }

    public void setMainImgUrl(String mainImgUrl) {
        this.mainImgUrl = mainImgUrl;
    }

    public String getSpecDes() {
        return specDes;
    }

    public void setSpecDes(String specDes) {
        this.specDes = specDes;
    }

    public String getCumCode() {
        return cumCode;
    }

    public void setCumCode(String cumCode) {
        this.cumCode = cumCode;
    }
}
