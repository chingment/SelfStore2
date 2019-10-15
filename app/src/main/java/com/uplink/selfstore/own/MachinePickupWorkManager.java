package com.uplink.selfstore.own;

import android.os.Handler;
import android.os.Message;

import com.uplink.selfstore.model.api.OrderDetailsSkuBean;
import com.uplink.selfstore.model.api.PickupSkuBean;
import com.uplink.selfstore.model.api.PickupSlotBean;
import com.uplink.selfstore.utils.DateUtil;
import com.uplink.selfstore.utils.LogUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by chingment on 2019/9/28.
 */

public class MachinePickupWorkManager {

    private PickupSkuBean currentPickupProductSku = null;
    private List<PickupSkuBean> allPickupProductSkus = new ArrayList<>();
    private Handler handlerPickupMsg;
    private Thread currentThread;
    private boolean isWorking = true;

    private List<PickupSkuBean> getAllPickupProductSkus(List<OrderDetailsSkuBean> skus) {

        List<PickupSkuBean> p1 = new ArrayList<>();

        for (int i = 0; i < skus.size(); i++) {
            OrderDetailsSkuBean sku = skus.get(i);
            List<PickupSlotBean> slots = sku.getSlots();
            for (int j = 0; j < slots.size(); j++) {
                PickupSlotBean slot = slots.get(j);
                PickupSkuBean pickSku = new PickupSkuBean();

                pickSku.setId(sku.getId());
                pickSku.setName(sku.getName());
                pickSku.setMainImgUrl(sku.getMainImgUrl());
                pickSku.setSlotId(slot.getSlotId());
                pickSku.setUniqueId(slot.getUniqueId());
                pickSku.setStatus(slot.getStatus());

                p1.add(pickSku);
            }
        }

        return p1;
    }

    public void run(List<OrderDetailsSkuBean> _allPickupProductSkus, Handler _handlerPickupMsg) {

        this.allPickupProductSkus = getAllPickupProductSkus(_allPickupProductSkus);
        this.handlerPickupMsg = _handlerPickupMsg;
        this.isWorking = true;

        Runnable runnable = new Runnable() {
            public void run() {
                while (isWorking) {
                    System.out.println("取货流程：" + DateUtil.getNowDate());
                    try {

                        if (currentPickupProductSku == null) {
                            for (int i = 0; i < allPickupProductSkus.size(); i++) {
                                //判断当前状态已支付
                                if (allPickupProductSkus.get(i).getStatus() == 3000) {
                                    //设置当前状态等待取货
                                    allPickupProductSkus.get(i).setStatus(3010);
                                    //机器接收到等待取货，改状态为3011 为取货中,
                                    allPickupProductSkus.get(i).setStartTime(DateUtil.getNowDate());
                                    currentPickupProductSku = allPickupProductSkus.get(i);
                                    setPickupMsg(0x0001, currentPickupProductSku);
                                    break;
                                }
                            }
                        } else {
                            LogUtil.d("取货流程:" + currentPickupProductSku.getName() + ",正在取货中");
                            Calendar c = Calendar.getInstance();
                            c.setTime(currentPickupProductSku.getStartTime());
                            c.add(Calendar.MINUTE, 1);

                            Date pickupTimeout = c.getTime();

                            if (pickupTimeout.getTime() < DateUtil.getNowDate().getTime()) {
                                //设置状态为 6000 代表异常错误
                                currentPickupProductSku.setStatus(6000);

                                setPickupMsg(0x0002, currentPickupProductSku);


                                currentPickupProductSku = null;
                            }
                        }
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        currentThread = new Thread(runnable);
        currentThread.start();
    }

    private void setPickupMsg(int what, PickupSkuBean msg) {
        final Message m = new Message();
        m.what = what;
        m.obj = msg;
        handlerPickupMsg.sendMessage(m);
    }

    public void stop() {
        if (currentThread != null) {
            isWorking = false;
        }
    }

    public void reSetAllPickupProductSkus(List<OrderDetailsSkuBean> _allPickupProductSkus){
        this.allPickupProductSkus=getAllPickupProductSkus(_allPickupProductSkus);
    }
}
