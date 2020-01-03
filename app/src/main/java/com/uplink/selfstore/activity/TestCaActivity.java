package com.uplink.selfstore.activity;

import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.uplink.selfstore.R;
import com.uplink.selfstore.utils.LogUtil;

import java.util.List;

public class TestCaActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    TextureView mTexture, mTexture2, mTexture3, mTexture4;
    USBMonitor usbMonitor;
    UVCCamera uvcCamera,uvcCamera2,uvcCamera3,uvcCamera4;


    int time;
    Handler handler = new Handler();


    Button open1;
    Button open2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_ca);
        mTexture = findViewById(R.id.mTexture);
        mTexture2 = findViewById(R.id.mTexture2);
        mTexture3 = findViewById(R.id.mTexture3);
        mTexture4 = findViewById(R.id.mTexture4);

        open1 = findViewById(R.id.open1);
        open1.setOnClickListener(this);
        open2 = findViewById(R.id.open2);
        open2.setOnClickListener(this);

        mTexture.setRotation(-90);
        mTexture2.setRotation(-90);
        mTexture3.setRotation(-90);
        mTexture4.setRotation(-90);

        usbMonitor = new USBMonitor(this, new USBMonitor.OnDeviceConnectListener() {
            @Override
            public void onAttach(final UsbDevice device) {
//                LogUtil.d(TAG, "onAttach: ==" + device.getDeviceName());
//                if (device.getDeviceClass() == 239 && device.getDeviceSubclass() == 2) {//根据相机信息选择选需要打开的相机
//                    if (device.getVendorId()==1137|| device.getVendorId()==1443) {
//
//                        //此处请求权限，需点击确定，有系统权限可忽略
//                        //每个注册权限增加延时，如果4个摄像同时注册权限 可能权限弹窗只会显示一个，导致其他的相机权限未确定，
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                LogUtil.d(TAG, "run: 发送 dev=" + device.getDeviceName());
//                                usbMonitor.requestPermission(device);
//                            }
//                        }, time++ * 2000);
//                    }
//
//                }
            }

            @Override
            public void onDettach(UsbDevice usbDevice) {

            }

            @Override
            public void onConnect(UsbDevice usbDevice, USBMonitor.UsbControlBlock ctrlBlock, boolean b) {
                new Thread(new myRunnable(ctrlBlock)).start();
            }

            @Override
            public void onDisconnect(UsbDevice usbDevice, USBMonitor.UsbControlBlock usbControlBlock) {
                // TODO: 2018/12/1 根据pid vid 或name判断哪个相机断开连接
            }

            @Override
            public void onCancel(UsbDevice usbDevice) {

            }
        });
        usbMonitor.register();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            usbMonitor.unregister();
            uvcCamera.stopPreview();
            uvcCamera.close();
// uvcCamera2.close();
// uvcCamera3.close();
        } catch (Exception e) {

        }

    }

    class myRunnable implements Runnable {
        USBMonitor.UsbControlBlock usbControlBlock;

        public myRunnable(USBMonitor.UsbControlBlock usbControlBlock) {
            this.usbControlBlock = usbControlBlock;
        }

        @Override
        public void run() {
            final UVCCamera camera = new UVCCamera();
            try {
                camera.open(usbControlBlock);
            } catch (Exception e) {
                LogUtil.d(TAG, "开启相机错误！！！！" + camera.getDeviceName());
                return;
            }

            List<Size> supportedSizeList = camera.getSupportedSizeList();
            if (supportedSizeList != null) {
                for (Size size : supportedSizeList) {
                    LogUtil.d(TAG, "run: size=" + size.width + "---" + size.height);
                }
            }

            setCameraParameter(camera);
            //根据不同相机接入name  或者根据pid vid 指定相机在那个view显示
            if (usbControlBlock.getVenderId()==1443) {
                final SurfaceTexture st = mTexture.getSurfaceTexture();
                camera.setPreviewTexture(st);
            } else if (usbControlBlock.getVenderId()==1137) {
                final SurfaceTexture st = mTexture2.getSurfaceTexture();
                camera.setPreviewTexture(st);
            }

            camera.startPreview();
        }
    }

    private void setCameraParameter(UVCCamera camera) {
        try {
            //设置预览尺寸 根据设备自行设置
            camera.setPreviewSize(640,
                    480,
                    1,
                    12,
                    UVCCamera.FRAME_FORMAT_YUYV,
// UVCCamera.FRAME_FORMAT_MJPEG,//此格式设置15帧生效
                    0.4f);
            LogUtil.d(TAG, "**设置参数成功=" + camera.getDeviceName());
        } catch (final IllegalArgumentException e) {
            LogUtil.d(TAG, "**设置参数失败=" + camera.getDeviceName());
            return;
        }
    }

    private UsbDevice getUsbDevice(int productId, int vendorId) {
        List<UsbDevice> deviceMap = usbMonitor.getDeviceList();
        if (deviceMap != null) {
            for (UsbDevice usbDevice : deviceMap) {
                if (usbDevice.getVendorId() == vendorId && usbDevice.getProductId() == productId) {
                    return usbDevice;
                }

            }
        }
        return null;
    }

    @Override
    public void onClick(View v) {


        switch (v.getId())
        {

            case R.id.open1:
                UsbDevice device1=getUsbDevice(37424,1443);
                usbMonitor.requestPermission(device1);
                break;
            case  R.id.open2:
                UsbDevice device2=getUsbDevice(42694,1137);
                usbMonitor.requestPermission(device2);
                break;
        }
    }
}
