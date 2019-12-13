package com.uplink.selfstore.model.api;

import java.io.Serializable;

/**
 * Created by chingment on 2019/9/28.
 */

public class PickupSlotBean implements Serializable {
    private String uniqueId;
    private String slotId;
    private int status;
    private boolean isCanPickup;

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

    public boolean isCanPickup() {
        return isCanPickup;
    }

    public void setCanPickup(boolean canPickup) {
        isCanPickup = canPickup;
    }
}
