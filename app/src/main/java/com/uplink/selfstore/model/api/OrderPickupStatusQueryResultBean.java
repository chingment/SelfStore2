package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chingment on 2019/9/29.
 */

public class OrderPickupStatusQueryResultBean implements Serializable {
    private  String uniqueId;
    private  String productSkuId;
    private  String slotId;
    private  int status;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getProductSkuId() {
        return productSkuId;
    }

    public void setProductSkuId(String productSkuId) {
        this.productSkuId = productSkuId;
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
}
