package com.uplink.selfstore.deviceCtrl;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.view.Surface;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.utils.LogUtil;

import java.util.List;

public class CameraCtrl {
    private static final String TAG = "CameraCtrl";
    private USBMonitor mUSBMonitor;
    private BaseFragmentActivity mContext;
    private static final float[] BANDWIDTH_FACTORS = {0.5f, 0.5f};
    private UsbDevice mCameraByChuHuoKou;//出货口相机
    private CameraViewInterface mCameraViewInterfaceByChuHuoKou;
    private UVCCameraHandler mCameraHandlerByChuHuoKou;
    private Surface mCameraPreviewSurfaceByChuHuoKou;

    private UsbDevice mCameraByJiGui;//机柜相机
    private CameraViewInterface mCameraViewInterfaceByJiGui;
    private UVCCameraHandler mCameraHandlerByJiGui;
    private Surface mCameraPreviewSurfaceByJiGui;

    public CameraCtrl(BaseFragmentActivity context) {
        mContext=context;
        mUSBMonitor = new USBMonitor(context, mOnDeviceConnectListener);
        mUSBMonitor.register();
    }

    public void setCameraByChuHuoKou(int productId,int vendorId){
        mCameraByChuHuoKou=getUsbDevice(productId,vendorId);
    }

    public UsbDevice getCameraByChuHuoKou(){
      return   mCameraByChuHuoKou;
    }

    public void setCameraByJiGui(int productId,int vendorId){
        mCameraByJiGui=getUsbDevice(productId,vendorId);
    }

    public UsbDevice getCameraByJiGui() {
        return mCameraByJiGui;
    }

    private UsbDevice getUsbDevice(int productId, int vendorId){
        List<UsbDevice> deviceMap = mUSBMonitor.getDeviceList();
        if (deviceMap != null) {
            for (UsbDevice usbDevice : deviceMap) {
                if (isUsbCamera(usbDevice)) {
                    if(productId==0) {
                        return usbDevice;
                    }
                    else if(vendorId==0){
                        return usbDevice;
                    }
                    else if(usbDevice.getVendorId()==vendorId&&usbDevice.getProductId()==productId)
                    {
                        return usbDevice;
                    }
                }
            }
        }
        return  null;
    }

    private boolean isUsbCamera(UsbDevice usbDevice) {
        return usbDevice != null && 239 == usbDevice.getDeviceClass() && 2 == usbDevice.getDeviceSubclass();
    }


    public void openCameraByChuHuoKou(CameraViewInterface cameraViewInterface) {
        mCameraViewInterfaceByChuHuoKou=cameraViewInterface;
        mCameraHandlerByChuHuoKou = UVCCameraHandler.createHandler(mContext, cameraViewInterface, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT,BANDWIDTH_FACTORS[1]);
        if(mCameraByChuHuoKou!=null) {
            mUSBMonitor.requestPermission(mCameraByChuHuoKou);
        }
    }

    public void closeCameraByChuHuoKou() {
        if(mCameraByChuHuoKou!=null) {
            mCameraHandlerByChuHuoKou.close();
        }
    }

    public void captureStillByChuHuoKou() {
        if(mCameraByChuHuoKou!=null) {
           if(mCameraHandlerByChuHuoKou.isOpened())
           {
               mCameraHandlerByChuHuoKou.captureStill();
           }
        }
    }

    public void startRecordByChuHuoKou() {
        if(mCameraByChuHuoKou!=null) {
            if(mCameraHandlerByChuHuoKou.isOpened())
            {
                if (!mCameraHandlerByChuHuoKou.isRecording()) {
                    mCameraHandlerByChuHuoKou.startRecording();
                }
            }
        }
    }

    public void stopRecordByChuHuoKou() {
        if(mCameraByChuHuoKou!=null) {
            if(mCameraHandlerByChuHuoKou.isOpened())
            {
                if (mCameraHandlerByChuHuoKou.isRecording()) {
                    mCameraHandlerByChuHuoKou.stopRecording();
                }
            }
        }
    }

    public boolean isOpenCameraByChuHuoKou(){
        if(mCameraByChuHuoKou==null)
            return false;

        if(mCameraHandlerByChuHuoKou==null)
            return false;

        return  mCameraHandlerByChuHuoKou.isOpened();
    }

    public void  openCameraByJiGui(CameraViewInterface cameraViewInterface) {
        mCameraViewInterfaceByJiGui=cameraViewInterface;
        mCameraViewInterfaceByJiGui.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        mCameraHandlerByJiGui=UVCCameraHandler.createHandler(mContext, mCameraViewInterfaceByJiGui, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[1]);
        if(mCameraByJiGui!=null) {
            mUSBMonitor.requestPermission(mCameraByJiGui);
        }
    }

    public void closeCameraByJiGui() {
        if(mCameraByJiGui!=null) {
            mCameraHandlerByJiGui.close();
        }
    }

    public boolean isOpenCameraByJiGui(){
        if(mCameraByJiGui==null)
            return false;

        if(mCameraHandlerByJiGui==null)
            return false;

        return  mCameraHandlerByJiGui.isOpened();
    }

    public void captureStillByJiGui() {
        if(mCameraByJiGui!=null) {
            if(mCameraHandlerByJiGui.isOpened())
            {
                mCameraHandlerByJiGui.captureStill();
            }
        }
    }

    public void startRecordByJiGui() {
        if(mCameraByJiGui!=null) {
            if(mCameraHandlerByJiGui.isOpened())
            {
                if (!mCameraHandlerByJiGui.isRecording()) {
                    mCameraHandlerByJiGui.startRecording();
                }
            }
        }
    }

    public void stopRecordByJiGui() {
        if(mCameraByJiGui!=null) {
            if(mCameraHandlerByJiGui.isOpened())
            {
                if (mCameraHandlerByJiGui.isRecording()) {
                    mCameraHandlerByJiGui.stopRecording();
                }
            }
        }
    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            LogUtil.i(TAG, "USB_DEVICE_ATTACHED->pid:" + device.getProductId() + ",vid:" + device.getVendorId() + ",class:" + device.getDeviceClass());
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            LogUtil.i(TAG, "USB_DEVICE_CONNECT->pid:" + device.getProductId() + ",vid:" + device.getVendorId() + ",class:" + device.getDeviceClass());


            if(getCameraByChuHuoKou()!=null) {
                if (device.getVendorId() == getCameraByChuHuoKou().getVendorId()) {
                    if (!mCameraHandlerByChuHuoKou.isOpened()) {
                        mCameraHandlerByChuHuoKou.open(ctrlBlock);
                        SurfaceTexture st = mCameraViewInterfaceByChuHuoKou.getSurfaceTexture();
                        mCameraPreviewSurfaceByChuHuoKou=new Surface(st);
                        mCameraHandlerByChuHuoKou.startPreview(mCameraPreviewSurfaceByChuHuoKou);
                    }
                }
            }

            if(getCameraByJiGui()!=null) {
                if (device.getVendorId() == getCameraByJiGui().getVendorId()) {
                    if (!mCameraHandlerByJiGui.isOpened()) {
                        mCameraHandlerByJiGui.open(ctrlBlock);
                        SurfaceTexture st = mCameraViewInterfaceByJiGui.getSurfaceTexture();
                        mCameraPreviewSurfaceByJiGui= new Surface(st);
                        mCameraHandlerByJiGui.startPreview(mCameraPreviewSurfaceByJiGui);
                    }
                }
            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            LogUtil.i(TAG, "USB_DEVICE_DISCONNECT->pid:" + device.getProductId() + ",vid:" + device.getVendorId() + ",class:" + device.getDeviceClass());

            if(getCameraByChuHuoKou()!=null) {
                if (device.getVendorId() == getCameraByChuHuoKou().getVendorId()) {
                    mContext.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            mCameraHandlerByChuHuoKou.close();
                            if (mCameraPreviewSurfaceByChuHuoKou != null) {
                                mCameraPreviewSurfaceByChuHuoKou.release();
                                mCameraViewInterfaceByChuHuoKou = null;
                            }
                        }
                    }, 0);
                }
            }

            if(getCameraByJiGui()!=null) {
                if (device.getVendorId() == getCameraByJiGui().getVendorId()) {
                    mContext.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            mCameraHandlerByJiGui.close();
                            if (mCameraPreviewSurfaceByJiGui != null) {
                                mCameraPreviewSurfaceByJiGui.release();
                                mCameraPreviewSurfaceByJiGui = null;
                            }
                        }
                    }, 0);
                }
            }

        }

        @Override
        public void onDettach(final UsbDevice device) {
            LogUtil.i(TAG, "USB_DEVICE_DETACHED->pid:" + device.getProductId() + ",vid:" + device.getVendorId() + ",class:" + device.getDeviceClass());
        }

        @Override
        public void onCancel(final UsbDevice device) {
            LogUtil.i(TAG, "USB_DEVICE_CANCEL->pid:" + device.getProductId() + ",vid:" + device.getVendorId() + ",class:" + device.getDeviceClass());
        }
    };
}
