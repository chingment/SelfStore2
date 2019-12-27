package com.uplink.selfstore.deviceCtrl;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.uplink.selfstore.utils.LogUtil;
import com.wedone.BioVein;

import static java.lang.Thread.sleep;

public class VeinLockCtrl {
    private static String TAG = "VeinLockCtrl";
    private Context mContext;

    private byte[]  mByteDevName;
    private Handler checkLoginHandler = null;
    private boolean checkLoginIsStopListener = true;

    private Handler collectHandler = null;
    private boolean collectIsStopListener = true;

    private  int connect_status=0;
    public  VeinLockCtrl(Context context)
    {
        mContext=context;
    }

    public int connect() {

        byte m_DevList[][] = new byte[10][64];
        BioVein.FV_EnumDevice(m_DevList);

        String s_devName = "";
        for (int i = 0; i < 10; i++) {
            String i_name = new String(m_DevList[i]);
            i_name = i_name.trim();
            if (i_name.length() > 0) {
                //data_list.add(s);
                LogUtil.i("设备名称：" + i_name);

                if (i_name.contains("B02")) {
                    s_devName = i_name;
                    break;
                }
            }

        }

        if (s_devName == "") {
            connect_status=1;
            return connect_status;
        }

        byte[] b_DevName = s_devName.getBytes();

        //初始化设备
        int ret_Init = BioVein.FV_InitDevice(b_DevName);

        if (ret_Init != 0) {
            connect_status=2;
            return connect_status;
        }

        int fd = __tryGetUsbPermission__(s_devName);

        if(fd==-1){
            connect_status=3;
            return connect_status;
        }

        //打开设备
        int ret_Open = BioVein.FV_OpenDevice(b_DevName, fd);
        if (ret_Open != 0) {
            connect_status=4;
            return connect_status;
        }

        mByteDevName=b_DevName;

        connect_status=0;
        return connect_status;
    }

    public int getConnectStatus(){
        return connect_status;
    }

    public void disConnect() {

        checkLoginIsStopListener=true;

        if(mByteDevName!=null) {
            BioVein.FV_CloseDevice(mByteDevName);
            BioVein.FV_RemoveDevice(mByteDevName);
        }
    }

    public void startCheckLogin() {
        CheckLoginListenerThread checkLoginListenerThread = new CheckLoginListenerThread();
        checkLoginListenerThread.start();
    }

    public void stopCheckLogin() {
       checkLoginIsStopListener=true;
    }

    private void sendCheckLoginHandlerMessage(int status, String message) {
        if (checkLoginHandler != null) {
            Message m = new Message();
            m.what = 1;
            Bundle data = new Bundle();
            data.putInt("status", status);
            data.putString("message", message);
            m.setData(data);
            checkLoginHandler.sendMessage(m);
        }
    }

    public void setCheckLoginHandler(Handler checkLoginHandler) {
        this.checkLoginHandler = checkLoginHandler;
    }

    private class CheckLoginListenerThread extends Thread {

        @Override
        public void run() {
            int ret_connect = connect();
            if (ret_connect != 0) {
                String message = "指静脉设备异常";
                switch (ret_connect) {
                    case 1:
                        message = "找不到指静脉设备";
                        break;
                    case 2:
                        message = "初始化设备失败";
                        break;
                    case 3:
                        message = "读取设备权限失败";
                        break;
                    case 4:
                        message = "打开设备失败";
                        break;
                }

                LogUtil.i(TAG, "指静脉登录流程监听：" + message);
                sendCheckLoginHandlerMessage(1, message);
                return;
            }

            LogUtil.i(TAG, "指静脉登录流程监听：检查设备正常");

            checkLoginIsStopListener = false;

            while (!checkLoginIsStopListener)
            {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(mByteDevName!=null) {
                    //先获取一枚特征
                    byte[] featureData = new byte[512];
                    int flag = 0x00;
                    //public static native int FV_GrabFeature(byte[] devId, byte[] featureData, int flag);
                    int ret = BioVein.FV_GrabFeature(mByteDevName, featureData, flag);

                    LogUtil.i(TAG, "指静脉登录流程监听：获取到静脉结果：" + ret);
                }
            }
        }
    }


    public void startCollect() {
        CollectListenerThread collectListenerThread = new CollectListenerThread();
        collectListenerThread.start();
    }

    public void  stopCollect(){
        collectIsStopListener=true;
    }

    public void setCollectHandler(Handler collectHandler) {
        this.collectHandler = collectHandler;
    }

    private void sendCollectHandlerMessage(int status, String message) {
        if (collectHandler != null) {
            Message m = new Message();
            m.what = 1;
            Bundle data = new Bundle();
            data.putInt("status", status);
            data.putString("message", message);
            m.setData(data);
            collectHandler.sendMessage(m);
        }
    }

    private class CollectListenerThread extends Thread {

        @Override
        public void run() {

            int status = getConnectStatus();
            if (status != 0) {
                sendCollectHandlerMessage(1, "静指脉设备连接异常");
                return;
            }

            collectIsStopListener=false;
            checkLoginIsStopListener=false;

            byte[] featureData = new byte[512];
            int flag = 0x00, ret = -1;
            boolean isWaitSuccess = true;

            int reg_n = 3;
            byte[] reg_feature = new byte[512 * reg_n];
            boolean is_reg = false;

            for (int i = 0; i < reg_n; i++) {

                if(collectIsStopListener){
                    break;
                }

                //检测到手指放好为止
                try {
                    isWaitSuccess = WaitFingerStatus((byte) 0x03, 100, 500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!isWaitSuccess) {
                    LogUtil.i(TAG, "指静脉采集流程监听：检测不到手指");
                    return;
                }

                //获得单枚特征
                ret = BioVein.FV_GrabFeature(mByteDevName, featureData, flag);
                if (ret != 0) {
                    LogUtil.i(TAG, "指静脉采集流程监听：采集单枚特征失败");
                    return;
                }

                //循环获取单指静脉时，要对是否同一手指进行校验
                if (i > 0) {
                    byte[] featureTmp = new byte[512 * i];
                    System.arraycopy(reg_feature, 0, featureTmp, 0, 512 * i);
                    //public static native int FV_IsSameFinger(byte[] featureDataMatch, byte[] featureDataReg, int RegCnt, int flag);
                    ret = BioVein.FV_IsSameFinger(featureData, featureTmp, i, 0x03);
                    if (ret != 0) {
                        LogUtil.i(TAG, "指静脉采集流程监听：不同一手指");
                        //NoticeMsgPrint("FV_IsSameFinger:" + ret);
                        //MainActivity.toast(MainActivity.this, "Inconsistent fingers!");
                        BioVein.FV_SetLedBeep(mByteDevName, (short) 3, 500, 500);
                        return;
                    }
                }

                //累计特征
                System.arraycopy(featureData, 0, reg_feature, 512 * i, 512);

                //检测至手指移开为止
                try {
                    isWaitSuccess = WaitFingerStatus((byte) 0x00, 100, 500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!isWaitSuccess) {
                    LogUtil.i(TAG, "指静脉采集流程监听：手指未移开");
                    return;
                }
            }


            is_reg = true;


            String textMsg = "采集结果:" + ret + ", 次数:" + reg_feature.length / 512 + ", 数据大小:" + reg_feature.length;
            LogUtil.i(TAG, "指静脉采集流程监听->"+textMsg);

            //String textMsg = "FV_GrabFeature:" + ret + ", Total number:" + reg_feature.length/512 + ", Total size:" + reg_feature.length;
            //MainActivity.toast(MainActivity.this, "Successful registration.");
            //NoticeMsgPrint(textMsg);
        }
    }

    /**
     * Wedone: 等待手指传感器变为某种指定的状态，等待的时间为nInterval*nTimes
     *
     * @参数(IN)  byte bFingerStatus: 等待的状态；0：手指已经移开，3：手指已经放置好。
     * @参数(IN)  int nTimes: 检测的次数，必须大于0。
     * @参数(IN)  int nInterval: 每次检测的间隔，单位为毫秒，建议在500 - 1000毫秒之间。
     * @调用 public
     * @返回 boolean: true=成功的等到了指定的状态：
     *             false=没有等到指定的状态就超时了
     */
    public boolean WaitFingerStatus(byte bFingerStatus, int nTimes, int nInterval) throws InterruptedException {
        if((0 >= nTimes) || (200 >= nInterval)){
            return false;
        }

        byte bFingerSt[] = new byte[1];

        long lRetVal;
        for(int nCnt = 0; nCnt < nTimes; nCnt++){

            if(collectIsStopListener||checkLoginIsStopListener){
                break;
            }

            if(0 == bFingerStatus){
                System.out.println("Move your fingers, please...\n" + nCnt);
            }
            else if(0x03 == bFingerStatus){
                System.out.println("Put your finger down, please...\n" + nCnt);
            }
            else{
                break;
            }
            lRetVal = BioVein.FV_FingerDetect(mByteDevName, bFingerSt);
            if (0 != lRetVal) {
                return false;
            }
            if(bFingerSt[0] == bFingerStatus){
                return true;
            }

            sleep(nInterval, 0);
        }
        return false;
    }


    private static final String ACTION_USB_PERMISSION = "com.template.USB_PERMISSION";//可自定义
    private BroadcastReceiver mUsbPermissionActionReceiver;
    private static UsbManager mUsbManager;
    private int fd = -1;
    private int __tryGetUsbPermission__(final String devname){

        final String TAG = "TryGetUsbPermission";

        mUsbPermissionActionReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    context.unregisterReceiver(this);//解注册
                    synchronized (this) {
                        UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if(null != usbDevice){
                                Log.e(TAG,usbDevice.getDeviceName()+"已获取USB权限.");
                            }
                        }
                        else {
                            //user choose NO for your previously popup window asking for grant perssion for this usb device
                            Log.e(TAG,String.valueOf("USB权限已被拒绝，Permission denied for device" + usbDevice));
                        }
                    }
                }
            }
        };


        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);

        if(mUsbPermissionActionReceiver != null) {
            mContext.registerReceiver(mUsbPermissionActionReceiver, filter);
        }

        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);

        boolean has_idcard_usb = false;
        for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            String devnamestr = usbDevice.getDeviceName();
            if(((usbDevice.getVendorId() == 0x0481 && usbDevice.getProductId() == 0x5641)
                    || (usbDevice.getVendorId() == 0x064B && usbDevice.getProductId() == 0x7823)))
            // && Integer.parseInt(devnamestr.substring(13,16)) == Integer.valueOf(devname.substring(5, 7), 16)
            // && Integer.parseInt(devnamestr.substring(17,20)) == Integer.valueOf(devname.substring(9, 11), 16))//身份证设备USB
            {
                // 获取权限
                has_idcard_usb = true;

                LogUtil.e(TAG,usbDevice.getDeviceName()+"已找到USB");

                if(mUsbManager.hasPermission(usbDevice)){
                    LogUtil.e(TAG,usbDevice.getDeviceName()+"已获取过USB权限");
                }else{
                    LogUtil.e(TAG,usbDevice.getDeviceName()+"请求获取USB权限");
                    mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                }

                // 打开USB，获取FileDescriptor
                UsbDeviceConnection connection = mUsbManager.openDevice(usbDevice);
                if(connection != null){
                    fd = connection.getFileDescriptor();
                    LogUtil.e(TAG, "UsbManager openDevice Success, fd:" + fd +", devname:"+ devname.substring(0, 11));
                } else {
                    LogUtil.e(TAG, "UsbManager openDevice failed");
                }
            }
        }

        if(!has_idcard_usb)
        {
            LogUtil.e(TAG,"未找到身份证USB");
        }
        return fd;
    }
}
