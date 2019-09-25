package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chingment on 2018/6/13.
 */

public class ProductBean implements Serializable {
    private String id;
    private String name;
    private String mainImgUrl;
    private List<ImgSetBean> displayImgUrls;
    private String briefDes;
    private String detailsDes;

    public String getDetailsDes() {
        return detailsDes;
    }

    public void setDetailsDes(String detailsDes) {
        this.detailsDes = detailsDes;
    }

    private RefSkuBean refSku;

    public RefSkuBean getRefSku() {
        return refSku;
    }

    public void setRefSku(RefSkuBean refSku) {
        this.refSku = refSku;
    }

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

}
