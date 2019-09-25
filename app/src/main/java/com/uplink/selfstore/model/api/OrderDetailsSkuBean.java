package com.uplink.selfstore.model.api;

import java.io.Serializable;

/**
 * Created by chingment on 2019/2/28.
 */

public class OrderDetailsSkuBean implements Serializable {

    private String id;
    private String name;
    private String imgUrl;
    private int quantity;
    private int quantityBySuccess;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantityBySuccess() {
        return quantityBySuccess;
    }

    public void setQuantityBySuccess(int quantityBySuccess) {
        this.quantityBySuccess = quantityBySuccess;
    }
}
