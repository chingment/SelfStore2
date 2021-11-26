package com.uplink.selfstore.model;

import java.io.Serializable;

public class LogcatCommand implements Serializable {

    private String command;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
