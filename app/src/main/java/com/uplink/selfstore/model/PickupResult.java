package com.uplink.selfstore.model;

import java.io.Serializable;

public class PickupResult implements Serializable {
    private int actionCount;
    private int currentActionId;
    private String currentActionName;
    private int currentActionStatusCode;
    private String currentActionStatusName;
    private boolean isPickupComplete;
    private long pickupUseTime;
    private String imgId;
    private String imgId2;

    public int getActionCount() {
        return actionCount;
    }

    public void setActionCount(int actionCount) {
        this.actionCount = actionCount;
    }

    public int getCurrentActionId() {
        return currentActionId;
    }

    public void setCurrentActionId(int currentActionId) {
        this.currentActionId = currentActionId;

        switch (currentActionId){

            case  0:
                this.currentActionName="机器停止复位";
                break;
            case  1:
                this.currentActionName="回到原点";
                break;
            case  2:
                this.currentActionName="XY移动";
                break;
            case  3:
                this.currentActionName="接货动作1";
                break;
            case  4:
                this.currentActionName="接货动作2";
                break;
            case  5:
                this.currentActionName="Y 轴上移至出货口";
                break;
            case  6:
                this.currentActionName="Y 轴下移至出货口";
                break;
            case  7:
                this.currentActionName="货架移动至出货口";
                break;
            case  8:
                this.currentActionName="提货动作";
                break;
            default:
                this.currentActionName="未知动作";
                break;
        }
    }

    public String getCurrentActionName() {
        return currentActionName;
    }

    public int getCurrentActionStatusCode() {
        return currentActionStatusCode;
    }

    public void setCurrentActionStatusCode(int currentActionStatusCode) {
        this.currentActionStatusCode = currentActionStatusCode;

        switch (currentActionStatusCode)
        {
            case 0:
                this.currentActionStatusName="空闲状态，没有执行动作";
                break;
            case 1:
                this.currentActionStatusName="动作执行中";
                break;
            case 2:
                this.currentActionStatusName="动作执行完成";
                break;
            case 3:
                this.currentActionStatusName="动作超时异常";
                break;
            default:
                this.currentActionStatusName="未知状态";
                break;
        }
    }

    public String getCurrentActionStatusName() {
        return currentActionStatusName;
    }

    public boolean isPickupComplete() {
        return isPickupComplete;
    }

    public void setPickupComplete(boolean pickupComplete) {
        isPickupComplete = pickupComplete;
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
}