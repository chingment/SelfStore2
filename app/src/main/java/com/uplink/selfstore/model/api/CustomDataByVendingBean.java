package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class CustomDataByVendingBean implements Serializable {

    private List<KindBean> kinds;
    private HashMap<String, SkuBean> skus;
    private HashMap<String, AdBean> ads;

    private boolean isHiddenKind;
    private int kindRowCellSize;
    private int maxBuyNumber;

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

    public int getMaxBuyNumber() {
        return maxBuyNumber;
    }

    public void setMaxBuyNumber(int maxBuyNumber) {
        this.maxBuyNumber = maxBuyNumber;
    }

    public boolean isHiddenKind() {
        return isHiddenKind;
    }

    public void setHiddenKind(boolean hiddenKind) {
        isHiddenKind = hiddenKind;
    }

    public int getKindRowCellSize() {
        return kindRowCellSize;
    }

    public void setKindRowCellSize(int kindRowCellSize) {
        this.kindRowCellSize = kindRowCellSize;
    }

}
