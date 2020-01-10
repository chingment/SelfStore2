package com.uplink.selfstore.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.lgh.uvccamera.UVCCameraProxy;
import com.lgh.uvccamera.bean.PicturePath;
import com.lgh.uvccamera.callback.PictureCallback;
import com.uplink.selfstore.R;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.BitmapUtil;
import com.uplink.selfstore.utils.LocationUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class SmHardwareActivity extends SwipeBackActivity implements View.OnClickListener {

    private static final String TAG = "SmHardwareActivity";

    private UVCCameraProxy mUVCCamera;
    private Button mCameraOpenByChuHuoKou;
    private Button mCameraOpenByJiGui;
    private Button mCameraClose;
    private Button mCameraCaptureStill;
    private Button mCameraRecord;
    private Button mCameraTest;
    private TextureView mCameraTextureView;

    private int mCameraPreviewWidth=640;
    private int mCameraPreviewHeight=480;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smhardware);

        setNavTtile(this.getResources().getString(R.string.aty_smhardware_navtitle));
        setNavBackVisible(true);
        setNavBtnVisible(true);

        mCameraOpenByChuHuoKou= (Button) findViewById(R.id.cameraOpenByChuHuoKou);
        mCameraOpenByChuHuoKou.setOnClickListener(this);
        mCameraOpenByJiGui= (Button) findViewById(R.id.cameraOpenByJiGui);
        mCameraOpenByJiGui.setOnClickListener(this);
        mCameraClose= (Button) findViewById(R.id.cameraClose);
        mCameraClose.setOnClickListener(this);
        mCameraCaptureStill= (Button) findViewById(R.id.cameraCaptureStill);
        mCameraCaptureStill.setOnClickListener(this);
        mCameraRecord= (Button) findViewById(R.id.cameraRecord);
        mCameraRecord.setOnClickListener(this);

        mCameraTextureView =(TextureView)findViewById(R.id.cameraView);

        if(Config.DEBUG) {
            mCameraTest = (Button) findViewById(R.id.cameraTest);
            mCameraTest.setVisibility(View.VISIBLE);
            mCameraTest.setOnClickListener(this);
        }

        initUVCCamera();

        //cameraCtrl.setCameraByJiGui(37424,1443);
        //cameraCtrl.setCameraByChuHuoKou(42694,1137);
    }

    private void initUVCCamera() {
        //1137 42694  //益力多
        //1443     37424 // 面包

        mUVCCamera = new UVCCameraProxy(this);
        // 已有默认配置，不需要可以不设置
        mUVCCamera.getConfig()
                .isDebug(true)
                .setPicturePath(PicturePath.APPCACHE)
                .setDirName("uvccamera");

        mUVCCamera.setPreviewTexture(mCameraTextureView);
        mUVCCamera.setMessageHandler(new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {

                        Bundle bundle = msg.getData();
                        int status = bundle.getInt("status");
                        String message = bundle.getString("message");
                        Log.e(TAG, message);

                        switch (msg.what) {
                            case 2://连接成功
                                //showAllPreviewSizes();
                                mUVCCamera.setPreviewSize(mCameraPreviewWidth, mCameraPreviewHeight);
                                mUVCCamera.startPreview();
                                // mUVCCamera.startPreview();
                                break;
                        }
                        return false;
                    }
                })
        );

        mUVCCamera.setPictureTakenCallback(new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data,String fileName) {
                if(data!=null) {
                    showToast("拍照成功");
                    Log.e(TAG, "拍照成功:,data.lenght:" + data.length);
                    saveCaptureStill(data,"SelfStore",fileName);
                }
            }
        });
    }

    private class MyThread extends Thread {

        int i=0;
        @Override
        public void run() {
            super.run();

            while (i<100) {

                i++;


                if(!mUVCCamera.isCameraOpen()) {
                    mUVCCamera.openCamera(37424,1443);
                }


                try {
                    Thread.sleep(5000);
                }
                catch (Exception e){

                }

                mUVCCamera.takePicture(UUID.randomUUID().toString());



                try {
                    Thread.sleep(500);
                }
                catch (Exception e){

                }

                if(mUVCCamera.isCameraOpen()) {
                    mUVCCamera.closeCamera();
                }

                try {
                    Thread.sleep(500);
                }
                catch (Exception e){

                }

                LogUtil.e(TAG,"第"+(i+1)+"次完成");
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
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
                    if(mUVCCamera.isCameraOpen()) {
                        showToast("请先关闭");
                        return;
                    }
                    mUVCCamera.openCamera(37424,1443);
                    break;
                case R.id.cameraOpenByJiGui:
                    if(mUVCCamera.isCameraOpen()) {
                        showToast("请先关闭");
                        return;
                    }
                    mUVCCamera.openCamera(321,6257);
                    break;
                case R.id.cameraCaptureStill:
                    if(!mUVCCamera.isCameraOpen()) {
                        showToast("请先打开");
                        return;
                    }
                    mUVCCamera.takePicture(UUID.randomUUID().toString());
                    break;
                case R.id.cameraRecord:
                    break;
                case R.id.cameraClose:
                    if(!mUVCCamera.isCameraOpen()) {
                        showToast("已关闭");
                        return;
                    }

                    mUVCCamera.closeCamera();
                    break;
                case R.id.cameraTest:
                    MyThread myThread=new MyThread();
                    myThread.start();
                    break;
            }
        }
    }

    public  void  saveCaptureStill(byte[] data,String saveDir,String uniqueId) {
        try {
            if (data == null)
                return;
            if (saveDir == null)
                return;
            if (uniqueId == null)
                return;

            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, mCameraPreviewWidth, mCameraPreviewHeight, null);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
            yuvImage.compressToJpeg(new Rect(0, 0, mCameraPreviewWidth, mCameraPreviewHeight), 100, bos);
            byte[] buffer = bos.toByteArray();

            Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);

            String mSaveDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + saveDir;

            File pathFile = new File(mSaveDir);
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }

            String filePath = mSaveDir + "/" + uniqueId + ".jpg";
            File outputFile = new File(filePath);
            final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
