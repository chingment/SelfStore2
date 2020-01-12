package com.lgh.uvccamera.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.config.CameraConfig;
import com.lgh.uvccamera.utils.LogUtil;
import com.serenegiant.usb.UVCCamera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 描述：usb插拔监听、连接工具类
 * 作者：liugh
 * 日期：2018/9/17
 * 版本：v2.0.0
 */
public class UsbMonitor implements IMonitor {
    private static final String ACTION_USB_DEVICE_PERMISSION = "ACTION_USB_DEVICE_PERMISSION";
    private Context mContext;
    private UsbManager mUsbManager;
    private USBReceiver mUsbReceiver;
    private UsbController mUsbController;
    private Handler mMesssageHandler;
    private CameraConfig mConfig;
    private UVCCamera mUVCCamera;

    public UsbMonitor(Context context, CameraConfig config) {
        this.mContext = context;
        this.mConfig = config;
        this.mUsbManager = (UsbManager) context.getSystemService(context.USB_SERVICE);
    }

    public void  setUVCCamera(UVCCamera uvcCamera){
        mUVCCamera=uvcCamera;
    }

    /**
     * 注册usb插拔监听广播
     */
    @Override
    public void registerReceiver() {
        LogUtil.i("registerReceiver");
        if (mUsbReceiver == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            filter.addAction(ACTION_USB_DEVICE_PERMISSION);
            mUsbReceiver = new USBReceiver();
            mContext.registerReceiver(mUsbReceiver, filter);
        }
    }

    /**
     * 注销usb插拔监听广播
     */
    @Override
    public void unregisterReceiver() {
        LogUtil.i("unregisterReceiver");
        if (mUsbReceiver != null) {
            mContext.unregisterReceiver(mUsbReceiver);
            mUsbReceiver = null;
        }
    }

    @Override
    public void checkDevice() {
        LogUtil.i("checkDevice");
       // UsbDevice usbDevice = getUsbCameraDevice();
//        if (isTargetDevice(usbDevice) && mConnectCallback != null) {
//            mConnectCallback.onAttached(usbDevice);
//        }
    }

    @Override
    public void openDevice(UsbDevice usbDevice) {
        LogUtil.i("Camera->判断是否有权限访问USB");
        if (mUsbManager.hasPermission(usbDevice)) {
            LogUtil.i("Camera->已授权");
            processConnect(usbDevice);
        } else {
            LogUtil.i("Camera->请求授权");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_DEVICE_PERMISSION), 0);
            mUsbManager.requestPermission(usbDevice, pendingIntent);
        }
    }


    private void processConnect(UsbDevice usbDevice) {

        Message msg = new Message();

        mUsbController = new UsbController(mUsbManager, usbDevice);
        mUsbController.open();



        if(mUVCCamera==null){
            LogUtil.i("Camera->设备连接失败,mUVCCamera为空");
            msg.what = UVCCamera.CAMERA_CONNECTFUAILURE;
            Bundle data = new Bundle();
            data.putString("message", "连接失败");
            msg.setData(data);
        }
        else {

            mUVCCamera.open(mUsbController);

            UsbDeviceConnection usbDeviceConnection = mUsbManager.openDevice(usbDevice);
            if (usbDeviceConnection == null) {
                LogUtil.i("Camera->设备连接失败");
                msg.what = UVCCamera.CAMERA_CONNECTFUAILURE;
                Bundle data = new Bundle();
                data.putString("message", "连接失败");
                msg.setData(data);
            } else {
                LogUtil.i("Camera->设备连接成功");
                msg.what = UVCCamera.CAMERA_CONNECTSUCCESS;
                Bundle data = new Bundle();
                data.putString("message", "连接成功");
                msg.setData(data);
            }
        }

        if(mMesssageHandler!=null) {
            mMesssageHandler.sendMessage(msg);
        }

    }

    @Override
    public void closeDevice() {
        LogUtil.i("closeDevice");
        if (mUsbController != null) {
            mUsbController.close();
            mUsbController = null;
        }
    }


    public void setMesssageHandler(Handler messsageHandler ) {
        this.mMesssageHandler = messsageHandler;
    }

    /**
     * 判断某usb设备是否摄像头，usb摄像头的大小类是239-2
     *
     * @param usbDevice
     * @return
     */
    public boolean isUsbCamera(UsbDevice usbDevice) {
        return usbDevice != null && 239 == usbDevice.getDeviceClass() && 2 == usbDevice.getDeviceSubclass();
    }


    public UsbDevice getUsbDevice(int productId, int vendorId){
        if(mUsbManager!=null) {
            HashMap<String, UsbDevice> deviceMap = mUsbManager.getDeviceList();
            if (deviceMap != null) {
                for (UsbDevice usbDevice : deviceMap.values()) {
                    if (isUsbCamera(usbDevice)) {
                        if (usbDevice.getVendorId() == vendorId && usbDevice.getProductId() == productId) {
                            return usbDevice;
                        }
                    }
                }
            }
        }
        return  null;
    }


    /**
     * usb插拔广播监听类
     */
    private class USBReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
           // LogUtil.i("usbDevice-->" + usbDevice);
            if (mMesssageHandler == null) {
                return;
            }
            switch (intent.getAction()) {
                case ACTION_USB_DEVICE_PERMISSION:
                    LogUtil.i("camera-->授权成功");
                    processConnect(usbDevice);
                    break;
                default:
                    break;
            }
        }
    }

}
