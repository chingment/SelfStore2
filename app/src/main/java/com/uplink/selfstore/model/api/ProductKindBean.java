package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.net.PortUnreachableException;
import java.util.List;

/**
 * Created by chingment on 2018/6/7.
 */

public class ProductKindBean implements Serializable {
    private String kindId;
    private String name;
    private List<String> childs;

    public String getKindId() {
        return kindId;
    }

    public void setKindId(String kindId) {
        this.kindId = kindId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<String> getChilds() {
        return childs;
    }

    public void setChilds(List<String> childs) {
        this.childs = childs;
    }

    public ProductKindBean() {

    }

    public ProductKindBean(String kindId, String name) {
        this.kindId = kindId;
        this.name = name;
    }
}
