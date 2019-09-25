package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.net.PortUnreachableException;
import java.util.List;

/**
 * Created by chingment on 2018/6/7.
 */

public class ProductKindBean implements Serializable {
    private String id;
    private String name;
    private List<String> childs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public ProductKindBean(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
