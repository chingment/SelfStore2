package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class ExHandleReasonBean implements Serializable {
    private   String reasonId;
    private   String title;
    private   boolean isChecked;


    public String getReasonId() {
        return reasonId;
    }

    public void setReasonId(String reasonId) {
        this.reasonId = reasonId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
