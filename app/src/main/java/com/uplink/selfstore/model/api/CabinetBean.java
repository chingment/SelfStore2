package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class CabinetBean implements Serializable {
    private String id;
    private String name;
    private String rowColLayout;
    private int slotMaxQuantity;
    private int priority;
    private String comId;


    public String getComId() {
        return comId;
    }

    public void setComId(String comId) {
        this.comId = comId;
    }


    public int getSlotMaxQuantity() {
        return slotMaxQuantity;
    }

    public void setSlotMaxQuantity(int slotMaxQuantity) {
        this.slotMaxQuantity = slotMaxQuantity;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getRowColLayout() {
        return rowColLayout;
    }

    public void setRowColLayout(String rowColLayout) {
        this.rowColLayout = rowColLayout;
    }

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

    public String getModelNo() {
        String modelNo="";
        if(id==null){
            return modelNo;
        }

        if(id.length()!=8){
            return modelNo;
        }

        modelNo=id.substring(0,5);

        return modelNo;
    }

    public int getCodeNo() {
        int codeNo=-1;
        if(id==null){
            return codeNo;
        }

        if(id.length()!=8){
            return codeNo;
        }


        codeNo=Integer.valueOf(id.substring(6,8));

        return codeNo;
    }

}
