package com.uplink.selfstore.activity.task;

import java.util.concurrent.BlockingQueue;

/**
 * Created by chingment on 2019/4/10.
 */

public class TaskDispatcherByMachinePickup extends Thread {

    private volatile boolean mQuit = false;
    private BlockingQueue<TaskByMachinePickup> mQueue;
    public TaskDispatcherByMachinePickup(BlockingQueue<TaskByMachinePickup> mQueue) {
        this.mQueue = mQueue;
    }
    public void quit() {
        mQuit = true;
        interrupt();
    }

    @Override
    public void run() {
       // android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        TaskByMachinePickup task;
        while (true) {
            try {
                task = mQueue.take();
                if (!task.isRunning()) {
                    task.run();
                }
            } catch (InterruptedException e) {
                if (mQuit) {
                    return;
                }
                continue;
            }

        }
    }
}
