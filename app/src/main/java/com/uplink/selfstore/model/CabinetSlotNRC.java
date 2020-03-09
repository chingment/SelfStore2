package com.uplink.selfstore.model;

//协议解释
public class CabinetSlotNRC {
    private String ctrl="";
    private String cab="";
    private int row=-1;
    private int col=-1;

    public CabinetSlotNRC()
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


    public static CabinetSlotNRC GetSlotNRC(String cabinetId, String slotId) {

        if (cabinetId == null)
            return null;

        if (cabinetId.length() != 8) {
            return null;
        }

        if (slotId == null)
            return null;

        int r_index = slotId.indexOf('r');
        if (r_index < 0) {
            return null;
        }

        int c_index = slotId.indexOf('c');

        if (c_index < 0) {
            return null;
        }

        CabinetSlotNRC cabinetSlotNRC = new CabinetSlotNRC();

        cabinetSlotNRC.setCtrl(cabinetId.substring(0, 5));
        cabinetSlotNRC.setCab(cabinetId.substring(5, 8));


        String str_r = slotId.substring(r_index + 1, c_index);
        String str_c = slotId.substring(c_index + 1, slotId.length());

        cabinetSlotNRC.setRow(Integer.valueOf(str_r));
        cabinetSlotNRC.setCol(Integer.valueOf(str_c));

        return cabinetSlotNRC;

    }
}
