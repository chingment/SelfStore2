package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

public class RetImServiceSeats implements Serializable {

    private List<ImSeatBean> seats;

    public List<ImSeatBean> getSeats() {
        return seats;
    }

    public void setSeats(List<ImSeatBean> seats) {
        this.seats = seats;
    }
}
