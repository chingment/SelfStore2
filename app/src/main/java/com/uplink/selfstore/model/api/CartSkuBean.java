package com.uplink.selfstore.model.api;

import java.io.Serializable;

/**
 * Created by chingment on 2018/6/14.
 */

public class CartSkuBean implements Serializable {

    private String id;
    private int quantity;
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
}
