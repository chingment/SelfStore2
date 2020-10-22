package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chingment on 2019/2/28.
 */

public class OrderDetailsSkuBean implements Serializable {

    private String productSkuId;
    private String name;
    private String mainImgUrl;
    private int quantity;
    private int quantityBySuccess;
    private int quantityByException;

    public int getQuantityByException() {
        return quantityByException;
    }

    public void setQuantityByException(int quantityByException) {
        this.quantityByException = quantityByException;
    }

    private List<PickupSlotBean> slots;

    public List<PickupSlotBean> getSlots() {
        return slots;
    }

    public void setSlots(List<PickupSlotBean> slots) {
        this.slots = slots;
    }


    public String getProductSkuId() {
        return productSkuId;
    }

    public void setProductSkuId(String productSkuId) {
        this.productSkuId = productSkuId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getMainImgUrl() {
        return mainImgUrl;
    }

    public void setMainImgUrl(String mainImgUrl) {
        this.mainImgUrl = mainImgUrl;
    }

    public void setQuantityBySuccess(int quantityBySuccess) {
        this.quantityBySuccess = quantityBySuccess;
    }
}
