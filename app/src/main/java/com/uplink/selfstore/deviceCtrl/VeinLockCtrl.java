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

public class VeinLockCtrl {
    private static String TAG = "VeinLockCtrl";
    private byte[]  mByteDevName;
    private Handler checkLoginHandler = null;
    private boolean cmd_CheckLoginIsStopListener = true;
    private Context mContext;
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

        if (s_devName == "")
            return 1;

        byte[] b_DevName = s_devName.getBytes();

        //初始化设备
        int ret_Init = BioVein.FV_InitDevice(b_DevName);

        if (ret_Init != 0) {
            return 2;
        }

        int fd = __tryGetUsbPermission__(s_devName);

        if(fd==-1){
            return 3;
        }

        //打开设备
        int ret_Open = BioVein.FV_OpenDevice(b_DevName, fd);
        if (ret_Open != 0) {
            return 4;
        }

        mByteDevName=b_DevName;

        return 0;
    }

    public void disConnect() {

        cmd_CheckLoginIsStopListener=true;

        if(mByteDevName!=null) {
            BioVein.FV_CloseDevice(mByteDevName);
            BioVein.FV_RemoveDevice(mByteDevName);
        }
    }
    public void startCheckLogin() {
        CheckLoginListenerThread checkLoginListenerThread = new CheckLoginListenerThread();
        checkLoginListenerThread.start();
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

            cmd_CheckLoginIsStopListener = false;

            while (!cmd_CheckLoginIsStopListener)
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
