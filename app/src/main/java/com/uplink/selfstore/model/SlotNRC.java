package com.uplink.selfstore.model;

public class SlotNRC {
    private String cabinetId="";
    private int row=-1;
    private int col=-1;

    public SlotNRC()
    {

    }
    public String getCabinetId() {
        return cabinetId;
    }

    public void setCabinetId(String cabinetId) {
        this.cabinetId = cabinetId;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }
}
