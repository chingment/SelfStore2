package com.uplink.selfstore.daoBean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class PickupAction {

    @Id(autoincrement = true)
    private long id;
    private String orderId;
    private String uniqueId;
    private String productSkuId;
    private String cabinetId;
    private String slotId;
    private int pickupStatus;
    private String actionId;
    private String actionName;
    private int actionStatusCode;
    private String actionStatusName;
    private long pickupUseTime;
    private String imgId;
    private String imgId2;
    private String remark;
    @Generated(hash = 2005859512)
    public PickupAction(long id, String orderId, String uniqueId,
            String productSkuId, String cabinetId, String slotId, int pickupStatus,
            String actionId, String actionName, int actionStatusCode,
            String actionStatusName, long pickupUseTime, String imgId,
            String imgId2, String remark) {
        this.id = id;
        this.orderId = orderId;
        this.uniqueId = uniqueId;
        this.productSkuId = productSkuId;
        this.cabinetId = cabinetId;
        this.slotId = slotId;
        this.pickupStatus = pickupStatus;
        this.actionId = actionId;
        this.actionName = actionName;
        this.actionStatusCode = actionStatusCode;
        this.actionStatusName = actionStatusName;
        this.pickupUseTime = pickupUseTime;
        this.imgId = imgId;
        this.imgId2 = imgId2;
        this.remark = remark;
    }
    @Generated(hash = 1676745163)
    public PickupAction() {
    }
    public long getId() {
        return this.id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getOrderId() {
        return this.orderId;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    public String getUniqueId() {
        return this.uniqueId;
    }
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
    public String getProductSkuId() {
        return this.productSkuId;
    }
    public void setProductSkuId(String productSkuId) {
        this.productSkuId = productSkuId;
    }
    public String getCabinetId() {
        return this.cabinetId;
    }
    public void setCabinetId(String cabinetId) {
        this.cabinetId = cabinetId;
    }
    public String getSlotId() {
        return this.slotId;
    }
    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }
    public int getPickupStatus() {
        return this.pickupStatus;
    }
    public void setPickupStatus(int pickupStatus) {
        this.pickupStatus = pickupStatus;
    }
    public String getActionId() {
        return this.actionId;
    }
    public void setActionId(String actionId) {
        this.actionId = actionId;
    }
    public String getActionName() {
        return this.actionName;
    }
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    public int getActionStatusCode() {
        return this.actionStatusCode;
    }
    public void setActionStatusCode(int actionStatusCode) {
        this.actionStatusCode = actionStatusCode;
    }
    public String getActionStatusName() {
        return this.actionStatusName;
    }
    public void setActionStatusName(String actionStatusName) {
        this.actionStatusName = actionStatusName;
    }
    public long getPickupUseTime() {
        return this.pickupUseTime;
    }
    public void setPickupUseTime(long pickupUseTime) {
        this.pickupUseTime = pickupUseTime;
    }
    public String getImgId() {
        return this.imgId;
    }
    public void setImgId(String imgId) {
        this.imgId = imgId;
    }
    public String getImgId2() {
        return this.imgId2;
    }
    public void setImgId2(String imgId2) {
        this.imgId2 = imgId2;
    }
    public String getRemark() {
        return this.remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }

}
