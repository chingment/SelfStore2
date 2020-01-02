package com.uplink.selfstore.activity;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.serenegiant.usb.UVCCamera;
import com.serenegiant.widget.CameraViewInterface;
import com.uplink.selfstore.R;
import com.uplink.selfstore.deviceCtrl.CameraCtrl;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.LocationUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

public class SmHardwareActivity extends SwipeBackActivity implements View.OnClickListener {

    private static final String TAG = "SmHardwareActivity";

    private CameraViewInterface mCameraViewByChuHuoKou;
    private Button mCameraOpenByChuHuoKou;
    private Button mCameraCloseByChuHuoKou;
    private Button mCameraCaptureStillByChuHuoKou;
    private Button mCameraRecordByChuHuoKou;

    private CameraViewInterface mCameraViewByJiGui;
    private Button mCameraOpenByJiGui;
    private Button mCameraCloseByJiGui;
    private Button mCameraCaptureStillByJiGui;
    private Button mCameraRecordByJiGui;


    private CameraCtrl cameraCtrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smhardware);

        setNavTtile(this.getResources().getString(R.string.aty_smhardware_navtitle));
        setNavBackVisible(true);
        setNavBtnVisible(true);

        mCameraViewByChuHuoKou = (CameraViewInterface) findViewById(R.id.cameraViewByChuHuoKou);
        mCameraViewByChuHuoKou.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        mCameraOpenByChuHuoKou= (Button) findViewById(R.id.cameraOpenByChuHuoKou);
        mCameraOpenByChuHuoKou.setOnClickListener(this);
        mCameraCloseByChuHuoKou= (Button) findViewById(R.id.cameraCloseByChuHuoKou);
        mCameraCloseByChuHuoKou.setOnClickListener(this);
        mCameraCaptureStillByChuHuoKou= (Button) findViewById(R.id.cameraCaptureStillByChuHuoKou);
        mCameraCaptureStillByChuHuoKou.setOnClickListener(this);
        mCameraRecordByChuHuoKou= (Button) findViewById(R.id.cameraRecordByChuHuoKou);
        mCameraRecordByChuHuoKou.setOnClickListener(this);

        mCameraViewByJiGui = (CameraViewInterface) findViewById(R.id.cameraViewByJiGui);
        mCameraViewByJiGui.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        mCameraOpenByJiGui= (Button) findViewById(R.id.cameraOpenByJiGui);
        mCameraOpenByJiGui.setOnClickListener(this);
        mCameraCloseByJiGui= (Button) findViewById(R.id.cameraCloseByJiGui);
        mCameraCloseByJiGui.setOnClickListener(this);
        mCameraCaptureStillByJiGui= (Button) findViewById(R.id.cameraCaptureStillByJiGui);
        mCameraCaptureStillByJiGui.setOnClickListener(this);
        mCameraRecordByJiGui= (Button) findViewById(R.id.cameraRecordByJiGui);
        mCameraRecordByJiGui.setOnClickListener(this);

        cameraCtrl=new CameraCtrl(SmHardwareActivity.this);

        cameraCtrl.setCameraByChuHuoKou(37424,1443);
        cameraCtrl.setCameraByJiGui(42694,1137);

       // MyThread myThread=new MyThread();
       // myThread.start();
    }

    private class MyThread extends Thread {

        @Override
        public void run() {

            cameraCtrl.openCameraByChuHuoKou(mCameraViewByChuHuoKou);

           try {
                Thread.sleep(1300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            cameraCtrl.openCameraByJiGui(mCameraViewByJiGui);

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

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.nav_back:
                    finish();
                    break;
                case R.id.cameraOpenByChuHuoKou:
                    if(cameraCtrl.getCameraByChuHuoKou()==null) {
                        showToast("找不到出口货摄像头设备");
                    }
                    cameraCtrl.openCameraByChuHuoKou(mCameraViewByChuHuoKou);
                    break;
                case R.id.cameraCloseByChuHuoKou:
                    if(cameraCtrl.getCameraByChuHuoKou()==null) {
                        showToast("找不到出口货摄像头设备");
                    }
                    cameraCtrl.closeCameraByChuHuoKou();
                    break;
                case R.id.cameraCaptureStillByChuHuoKou:
                    if(cameraCtrl.getCameraByChuHuoKou()==null) {
                        showToast("找不到出口货摄像头设备");
                    }

                    cameraCtrl.captureStillByChuHuoKou();
                    break;
                case R.id.cameraRecordByChuHuoKou:
                    break;
                case R.id.cameraOpenByJiGui:
                    if(cameraCtrl.getCameraByJiGui()==null) {
                        showToast("找不到机柜摄像头设备");
                    }
                    cameraCtrl.openCameraByJiGui(mCameraViewByJiGui);
                    break;
                case R.id.cameraCloseByJiGui:
                    if(cameraCtrl.getCameraByJiGui()==null) {
                        showToast("找不到机柜摄像头设备");
                    }
                    cameraCtrl.closeCameraByJiGui();
                    break;
                case R.id.cameraCaptureStillByJiGui:
                    if(cameraCtrl.getCameraByJiGui()==null) {
                        showToast("找不到机柜摄像头设备");
                    }
                    cameraCtrl.captureStillByJiGui();
                    break;
                case R.id.cameraRecordByJiGui:
                    break;

            }
        }
    }
}
