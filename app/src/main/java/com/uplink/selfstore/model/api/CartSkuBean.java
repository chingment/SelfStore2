package com.uplink.selfstore.model.api;

import java.io.Serializable;

/**
 * Created by chingment on 2018/6/14.
 */

public class CartSkuBean implements Serializable {

    private String id;
    private int quantity;
    private boolean isTrgVideoService;
    private String name;
    private String mainImgUrl;
    private float salePrice;
    private String currencySymbol;

    public CartSkuBean() {

    }

    public CartSkuBean(String id,  int quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isTrgVideoService() {
        return isTrgVideoService;
    }

    public void setTrgVideoService(boolean trgVideoService) {
        isTrgVideoService = trgVideoService;
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

    public float getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(float salePrice) {
        this.salePrice = salePrice;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }
}
