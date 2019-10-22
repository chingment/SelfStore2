package com.uplink.selfstore.activity.task;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.uplink.selfstore.activity.task.blockTask.BaseTask;
import com.uplink.selfstore.machineCtrl.DeShangMidCtrl;
import com.uplink.selfstore.model.SlotNRC;
import com.uplink.selfstore.model.api.PickupSkuBean;
import com.uplink.selfstore.utils.serialport.ChangeToolUtils;

/**
 * Created by chingment on 2019/10/1.
 */

public class PickTask extends BaseTask {
    private PickupSkuBean pickupSku;
    private Handler handlerMsg;
    private DeShangMidCtrl  midCtrl=new DeShangMidCtrl(2,9600);

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

                    SlotNRC slotNRC=GetSlotNRC(pickupSku.getSlotId());
                    if(slotNRC!=null) {
                        midCtrl.setMacCol(ChangeToolUtils.intToByte(slotNRC.getCol()));
                        midCtrl.setMacRow(ChangeToolUtils.intToByte(slotNRC.getRow()));
                        midCtrl.setMacRunning();
                    }

//                    sendHandlerMsg(0x0002, pickupSku);
                    Thread.sleep(2000);
//                    sendHandlerMsg(0x0003, pickupSku);
//                    Thread.sleep(2000);
//                    sendHandlerMsg(0x0004, pickupSku);
//                    Thread.sleep(2000);



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

    private SlotNRC GetSlotNRC(String slotId)
    {


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
            slotNRC.setRow(Integer.valueOf(str_c));

            return  slotNRC;
        }
        catch (NullPointerException ex)
        {
            return  null;
        }

    }

}
