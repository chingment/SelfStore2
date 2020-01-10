package com.lgh.uvccamera.usb;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.serenegiant.usb.UVCCamera;

/**
 * 描述：usb插拔监听实现接口
 * 作者：liugh
 * 日期：2018/12/27
 * 版本：v2.0.0
 */
public interface IMonitor {
    void registerReceiver();

    void unregisterReceiver();

    void checkDevice();

    void requestPermission(UsbDevice usbDevice, UVCCamera mUVCCamera);

    void connectDevice(UsbDevice usbDevice);

    void closeDevice();

    UsbController getUsbController();

    UsbDeviceConnection getConnection();
}
