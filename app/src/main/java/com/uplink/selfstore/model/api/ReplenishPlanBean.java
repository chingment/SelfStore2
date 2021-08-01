package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class ReplenishPlanBean implements Serializable {
    private String id;
    private String planId;
    private String planCumCode;
    private StatusBean status;
    private String rshTime;
    private String rsherName;
    private String makerName;
    private String makeTime;
    private String createTime;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getPlanCumCode() {
        return planCumCode;
    }

    public void setPlanCumCode(String planCumCode) {
        this.planCumCode = planCumCode;
    }

    public StatusBean getStatus() {
        return status;
    }

    public void setStatus(StatusBean status) {
        this.status = status;
    }

    public String getRshTime() {
        return rshTime;
    }

    public void setRshTime(String rshTime) {
        this.rshTime = rshTime;
    }

    public String getRsherName() {
        return rsherName;
    }

    public void setRsherName(String rsherName) {
        this.rsherName = rsherName;
    }

    public String getMakerName() {
        return makerName;
    }

    public void setMakerName(String makerName) {
        this.makerName = makerName;
    }

    public String getMakeTime() {
        return makeTime;
    }

    public void setMakeTime(String makeTime) {
        this.makeTime = makeTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
