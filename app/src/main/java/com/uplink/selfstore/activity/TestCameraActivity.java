package com.uplink.selfstore.activity;

import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.usbcameracommon.UvcCameraDataCallBack;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;
import com.uplink.selfstore.R;
import com.uplink.selfstore.deviceCtrl.CameraCtrl;
import com.uplink.selfstore.utils.LogUtil;

import java.util.List;

public class TestCameraActivity extends BaseActivity{

    private static final String TAG = "TestCameraActivity";

    private CameraViewInterface mUVCCameraViewByChuHuoKou;
    private CameraViewInterface mUVCCameraViewByByJiGui;

    private CameraCtrl cameraCtrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_camera);


        mUVCCameraViewByChuHuoKou = (CameraViewInterface) findViewById(R.id.camera_view_first);
        mUVCCameraViewByChuHuoKou.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);

        mUVCCameraViewByByJiGui = (CameraViewInterface) findViewById(R.id.camera_view_second);
        mUVCCameraViewByByJiGui.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);


        cameraCtrl=new CameraCtrl(TestCameraActivity.this);

        cameraCtrl.setCameraByChuHuoKou(37424,1443);
        cameraCtrl.setCameraByJiGui(42694,1137);

        MyThread myThread=new MyThread();
        myThread.start();
    }

    private class MyThread extends Thread {

        @Override
        public void run() {

            cameraCtrl.openCameraByChuHuoKou(mUVCCameraViewByChuHuoKou);

           try {
                Thread.sleep(1300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            cameraCtrl.openCameraByJiGui(mUVCCameraViewByByJiGui);

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
