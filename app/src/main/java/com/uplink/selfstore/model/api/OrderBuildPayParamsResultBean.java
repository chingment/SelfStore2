package com.uplink.selfstore.model.api;

public class OrderBuildPayParamsResultBean {
    private String chargeAmount;

    public String getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(String chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public String getPayUrl() {
        return payUrl;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    private String payUrl;
}
