package com.uplink.selfstore.activity.task;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.uplink.selfstore.activity.task.blockTask.BaseTask;
import com.uplink.selfstore.model.api.PickupSkuBean;

/**
 * Created by chingment on 2019/10/1.
 */

public class PickTask extends BaseTask {
    PickupSkuBean pickupSku;
    Handler handlerMsg;

    public PickTask(PickupSkuBean pickupSku) {
        this.pickupSku = pickupSku;
    }

    public void  setHandlerMsg(Handler handlerMsg) {
        this.handlerMsg = handlerMsg;
    }

    private void sendHandlerMsg(int what, PickupSkuBean msg) {
        final Message m = new Message();
        m.what = what;
        m.obj = msg;
        handlerMsg.sendMessage(m);
    }

    //执行任务方法，在这里实现你的任务具体内容
    @Override
    public void doTask() {
        super.doTask();
        Log.i("LogTask", "--doTask-");

        sendHandlerMsg(0x0001, pickupSku);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendHandlerMsg(0x0002, pickupSku);
                    Thread.sleep(2000);
                    sendHandlerMsg(0x0003, pickupSku);
                    Thread.sleep(2000);
                    sendHandlerMsg(0x0004, pickupSku);
                    Thread.sleep(2000);
                    unLockBlock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    //任务执行完的回调，在这里你可以做些释放资源或者埋点之类的操作
    @Override
    public void finishTask() {
        super.finishTask();
        Log.i("LogTask", "--finishTask-");
    }

}
