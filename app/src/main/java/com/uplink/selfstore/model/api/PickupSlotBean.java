package com.uplink.selfstore.model.api;

import java.io.Serializable;

/**
 * Created by chingment on 2019/9/28.
 */

public class PickupSlotBean implements Serializable {
    private String uniqueId;
    private String cabinetId;
    private String slotId;
    private int status;
    private boolean isAllowPickup;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isAllowPickup() {
        return isAllowPickup;
    }

    public void setAllowPickup(boolean allowPickup) {
        isAllowPickup = allowPickup;
    }

    public String getCabinetId() {
        return cabinetId;
    }

    public void setCabinetId(String cabinetId) {
        this.cabinetId = cabinetId;
    }
}
