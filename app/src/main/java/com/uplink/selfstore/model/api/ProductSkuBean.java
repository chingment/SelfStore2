package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chingment on 2018/6/13.
 */

public class ProductSkuBean implements Serializable {

    private String id;
    private String productId;
    private String name;
    private String mainImgUrl;
    private List<ImgSetBean> displayImgUrls;
    private String briefDes;
    private String detailsDes;
    private float salePrice;
    private float salePriceByVip;
    private boolean isShowPrice;
    private float showPrice;
    private String specDes;
    private boolean isOffSell;
    private int sumQuantity;
    private int lockQuantity;
    private int sellQuantity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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

    public List<ImgSetBean> getDisplayImgUrls() {
        return displayImgUrls;
    }

    public void setDisplayImgUrls(List<ImgSetBean> displayImgUrls) {
        this.displayImgUrls = displayImgUrls;
    }

    public String getBriefDes() {
        return briefDes;
    }

    public void setBriefDes(String briefDes) {
        this.briefDes = briefDes;
    }

    public String getDetailsDes() {
        return detailsDes;
    }

    public void setDetailsDes(String detailsDes) {
        this.detailsDes = detailsDes;
    }

    public float getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(float salePrice) {
        this.salePrice = salePrice;
    }

    public float getSalePriceByVip() {
        return salePriceByVip;
    }

    public void setSalePriceByVip(float salePriceByVip) {
        this.salePriceByVip = salePriceByVip;
    }

    public boolean isShowPrice() {
        return isShowPrice;
    }

    public void setShowPrice(boolean showPrice) {
        isShowPrice = showPrice;
    }

    public float getShowPrice() {
        return showPrice;
    }

    public void setShowPrice(float showPrice) {
        this.showPrice = showPrice;
    }

    public String getSpecDes() {
        return specDes;
    }

    public void setSpecDes(String specDes) {
        this.specDes = specDes;
    }

    public boolean isOffSell() {
        return isOffSell;
    }

    public void setOffSell(boolean offSell) {
        isOffSell = offSell;
    }

    public int getSumQuantity() {
        return sumQuantity;
    }

    public void setSumQuantity(int sumQuantity) {
        this.sumQuantity = sumQuantity;
    }

    public int getLockQuantity() {
        return lockQuantity;
    }

    public void setLockQuantity(int lockQuantity) {
        this.lockQuantity = lockQuantity;
    }

    public int getSellQuantity() {
        return sellQuantity;
    }

    public void setSellQuantity(int sellQuantity) {
        this.sellQuantity = sellQuantity;
    }
}
