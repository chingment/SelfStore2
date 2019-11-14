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

    public static SlotNRC  GetSlotNRC(String slotId) {


        int n_index=slotId.indexOf('n');

        if(n_index<0)
        {
            return null;
        }

        int r_index=slotId.indexOf('r');
        if(r_index<0)
        {
            return  null;
        }

        int c_index=slotId.indexOf('c');

        if(c_index<0)
        {
            return null;
        }

        try {
            SlotNRC slotNRC=new SlotNRC();

            String str_n = slotId.substring(n_index + 1, r_index - n_index);
            String str_r = slotId.substring(r_index + 1, c_index);
            String str_c = slotId.substring(c_index + 1, slotId.length());

            slotNRC.setCabinetId(str_n);
            slotNRC.setRow(Integer.valueOf(str_r));
            slotNRC.setCol(Integer.valueOf(str_c));

            return  slotNRC;
        }
        catch (NullPointerException ex)
        {
            return  null;
        }

    }
}
