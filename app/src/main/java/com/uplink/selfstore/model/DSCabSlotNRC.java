package com.uplink.selfstore.model;

//协议解释
public class DSCabSlotNRC {
    private String ctrl="";
    private String cab="";
    private int row=-1;
    private int col=-1;

    public DSCabSlotNRC()
    {

    }

    public String getCtrl() {
        return ctrl;
    }

    public void setCtrl(String ctrl) {
        this.ctrl = ctrl;
    }

    public String getCab() {
        return cab;
    }

    public void setCab(String cab) {
        this.cab = cab;
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


    public static DSCabSlotNRC GetSlotNRC(String cabinetId, String slotId) {

        if (cabinetId == null)
            return null;

        if (cabinetId.length() != 8) {
            return null;
        }

        if (slotId == null)
            return null;

        String[] prams = slotId.split("-");
        if (prams.length!=3) {
            return null;
        }



        DSCabSlotNRC dsCabSlotNRC = new DSCabSlotNRC();

        dsCabSlotNRC.setCtrl(cabinetId.substring(0, 5));
        dsCabSlotNRC.setCab(cabinetId.substring(5, 8));



        dsCabSlotNRC.setRow(Integer.valueOf(prams[0]));
        dsCabSlotNRC.setCol(Integer.valueOf(prams[1]));

        return dsCabSlotNRC;

    }
}
