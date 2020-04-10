package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class ExHandleReasonBean implements Serializable {
    private   String id;
    private   String title;
    private   boolean isChecked;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
