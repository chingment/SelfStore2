package com.uplink.selfstore.model;

import java.io.Serializable;

public class PickupActionResult implements Serializable {
    private int actionCount;
    private int actionId;
    private String actionName;
    private int actionStatusCode;
    private String actionStatusName;
    private long pickupUseTime;
    private String imgId;
    private String imgId2;
    private String imgId3;

    public int getActionCount() {
        return actionCount;
    }

    public void setActionCount(int actionCount) {
        this.actionCount = actionCount;
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;

        switch (actionId){

            case  0:
                this.actionName="机器停止复位";
                break;
            case  1:
                this.actionName="回到原点";
                break;
            case  2:
                this.actionName="XY移动";
                break;
            case  3:
                this.actionName="接货动作1";
                break;
            case  4:
                this.actionName="接货动作2";
                break;
            case  5:
                this.actionName="Y 轴上移至出货口";
                break;
            case  6:
                this.actionName="Y 轴下移至出货口";
                break;
            case  7:
                this.actionName="货架移动至出货口";
                break;
            case  8:
                this.actionName="提货动作";
                break;
            default:
                this.actionName="未知动作";
                break;
        }
    }

    public String getActionName() {
        return actionName;
    }

    public int getActionStatusCode() {
        return actionStatusCode;
    }

    public void setActionStatusCode(int actionStatusCode) {
        this.actionStatusCode = actionStatusCode;

        switch (actionStatusCode)
        {
            case 0:
                this.actionStatusName="空闲状态，没有执行动作";
                break;
            case 1:
                this.actionStatusName="动作执行中";
                break;
            case 2:
                this.actionStatusName="动作执行完成";
                break;
            case 3:
                this.actionStatusName="动作超时异常";
                break;
            default:
                this.actionStatusName="未知状态";
                break;
        }
    }

    public String getActionStatusName() {
        return actionStatusName;
    }

    public long getPickupUseTime() {
        return pickupUseTime;
    }

    public void setPickupUseTime(long pickupUseTime) {
        this.pickupUseTime = pickupUseTime;
    }

    public String getImgId() {
        return imgId;
    }

    public void setImgId(String imgId) {
        this.imgId = imgId;
    }

    public String getImgId2() {
        return imgId2;
    }

    public void setImgId2(String imgId2) {
        this.imgId2 = imgId2;
    }

    public String getImgId3() {
        return imgId3;
    }

    public void setImgId3(String imgId3) {
        this.imgId3 = imgId3;
    }
}