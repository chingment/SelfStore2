package com.uplink.selfstore.model.api;

public class OrderBuildPayParamsResultBean {
    private String chargeAmount;
    private String paramType;
    private String paramData;
    private String payTransId;
    private String orderId;


    public String getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(String chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public String getPayTransId() {
        return payTransId;
    }

    public void setPayTransId(String payTransId) {
        this.payTransId = payTransId;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getParamData() {
        return paramData;
    }

    public void setParamData(String paramData) {
        this.paramData = paramData;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
