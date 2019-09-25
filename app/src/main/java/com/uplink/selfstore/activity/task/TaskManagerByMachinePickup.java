package com.uplink.selfstore.activity.task;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chingment on 2019/4/10.
 */

public class TaskManagerByMachinePickup {

    private PriorityBlockingQueue<TaskByMachinePickup> mQueue;
    private TaskDispatcherByMachinePickup[] mTaskDispatchers;
    private volatile static TaskManagerByMachinePickup ourInstance;
    private final static int MAX_POOL_SIZE = 3;
    private AtomicInteger mSequenceGenerator = new AtomicInteger();

    private TaskManagerByMachinePickup() {
        mTaskDispatchers = new TaskDispatcherByMachinePickup[MAX_POOL_SIZE];
        startTask();
    }

    public static TaskManagerByMachinePickup getInstance() {
        if (ourInstance == null) {
            synchronized (TaskManagerByMachinePickup.class) {
                if (ourInstance == null) {
                    ourInstance = new TaskManagerByMachinePickup();

                }
            }
        }

        return ourInstance;
    }

    public void stop() {
        if (mTaskDispatchers != null) {
            int len = mTaskDispatchers.length;
            TaskDispatcherByMachinePickup dispatcher;
            for (int i = 0; i < len; i++) {
                dispatcher = mTaskDispatchers[i];
                if (dispatcher != null) {
                    dispatcher.quit();
                }

            }
        }
    }

    public void startTask() {
        stop();
        for (int i = 0; i < mTaskDispatchers.length; i++) {
            TaskDispatcherByMachinePickup dispatcher = new TaskDispatcherByMachinePickup(mQueue);
            mTaskDispatchers[i] = dispatcher;
            dispatcher.start();
        }
    }

    public void cancelAll() {
        stop();
        mQueue.clear();
    }


    public void addTask(TaskByMachinePickup task) {
        //task.setSequence(getSequenceNumber());
        mQueue.add(task);
    }
}
