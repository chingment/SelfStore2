package com.uplink.selfstore.activity.task;

import java.util.UUID;

/**
 * Created by chingment on 2019/4/10.
 */

public abstract class TaskByMachinePickup implements Runnable, Comparable<TaskByMachinePickup> {
    private Integer mSequence;
    private String uuid;

    private boolean isRunning = false;

    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }

    public void Task() {
        uuid = UUID.randomUUID().toString();
    }

    public Priority getPriority() {
        return Priority.NORMAL;
    }

    public void setSequence(Integer sequence) {
        this.mSequence = sequence;
    }

    @Override
    public int compareTo(TaskByMachinePickup o) {
        if (o == null) {
            return this.mSequence;
        }
        if (this.mSequence == null) {
            this.mSequence = new Integer(0);
        }
        if (o.mSequence == null) {
            o.mSequence = new Integer(0);
        }
        Priority left = this.getPriority();
        Priority right = o.getPriority();
        return left == right ?
                this.mSequence - o.mSequence : right.ordinal() - left.ordinal();
    }

    @Override
    public void run() {
        isRunning = true;

    }

    public boolean isRunning() {

        return isRunning;
    }

    public void cancel() {

    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
