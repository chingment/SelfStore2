package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class CabinetBean implements Serializable {
    private String cabinetId;
    private String name;
    private String rowColLayout;
    private int priority;
    private String comId;
    private int comBaud;


    public String getComId() {
        return comId;
    }

    public void setComId(String comId) {
        this.comId = comId;
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

    public String getCabinetId() {
        return cabinetId;
    }

    public void setCabinetId(String cabinetId) {
        this.cabinetId = cabinetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModelNo() {
        String modelNo="";
        if(cabinetId==null){
            return modelNo;
        }

        if(cabinetId.length()!=8){
            return modelNo;
        }

        modelNo=cabinetId.substring(0,5);

        return modelNo;
    }

    public int getCodeNo() {
        int codeNo=-1;
        if(cabinetId==null){
            return codeNo;
        }

        if(cabinetId.length()!=8){
            return codeNo;
        }


        codeNo=Integer.valueOf(cabinetId.substring(6,8));

        return codeNo;
    }

    public int getComBaud() {
        return comBaud;
    }

    public void setComBaud(int comBaud) {
        this.comBaud = comBaud;
    }
}
