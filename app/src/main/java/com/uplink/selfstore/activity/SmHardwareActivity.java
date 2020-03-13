package com.uplink.selfstore.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.lgh.uvccamera.UVCCameraProxy;
import com.lgh.uvccamera.callback.PictureCallback;
import com.serenegiant.usb.UVCCamera;
import com.uplink.selfstore.R;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByZS;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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

    private CabinetCtrlByDS cabinetCtrlByDS=null;
    private Button btnMachineGoZero;


    //中顺硬件诊断
    private LinearLayout zs_hd_layout;
    private EditText zs_hd_et_plateid;
    private EditText zs_hd_et_numid;
    private EditText zs_hd_et_ck;
    private Button zs_hd_btn_testopen;
    private Button zs_hd_btn_teststatus;
    private CabinetCtrlByZS zs_CabinetCtrlByZS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smhardware);

        setNavTtile(this.getResources().getString(R.string.aty_smhardware_navtitle));
        setNavGoBackBtnVisible(true);


        cabinetCtrlByDS=CabinetCtrlByDS.getInstance();

        cabinetCtrlByDS.setGoZeroHandler(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                Bundle bundle = msg.getData();
                int status = bundle.getInt("status");
                String message = bundle.getString("message");

                showToast(message);
                return false;
            }
        }));

        initViewByZS();

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
        if(Config.IS_BUILD_DEBUG) {
            mCameraTest = (Button) findViewById(R.id.cameraTest);
            mCameraTest.setVisibility(View.VISIBLE);
            mCameraTest.setOnClickListener(this);
        }

        btnMachineGoZero=(Button) findViewById(R.id.btnMachineGoZero);
        btnMachineGoZero.setOnClickListener(this);

        initUVCCamera();

        //cameraCtrl.setCameraByJiGui(37424,1443);
        //cameraCtrl.setCameraByChuHuoKou(42694,1137);
    }

    private void initUVCCamera() {
        //1137 42694  //益力多
        //1443     37424 // 面包

        mUVCCamera = new UVCCameraProxy(this);

        mUVCCamera.setPreviewTexture(mCameraTextureView);
        mUVCCamera.setMessageHandler(new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {

                        Bundle bundle = msg.getData();
                        String message = bundle.getString("message");
                        Log.e(TAG,message);

                        switch (msg.what) {
                            case UVCCamera.CAMERA_NOFINDDEVICE://找不到设备
                                LogUtil.d(TAG,"找不到设备");
                                break;
                            case UVCCamera.CAMERA_CONNECTSUCCESS://连接成功
                                LogUtil.d(TAG,"连接成功");
                                mUVCCamera.setPreviewSize(mCameraPreviewWidth, mCameraPreviewHeight);
                                mUVCCamera.startPreview();
                                break;
                            case UVCCamera.CAMERA_CONNECTFUAILURE://连接失败
                                mUVCCamera.closeCamera();
                                LogUtil.d(TAG,"连接失败");
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


    private void  initViewByZS(){
        zs_hd_layout= (LinearLayout) findViewById(R.id.zs_hd_layout);
        zs_hd_et_plateid= (EditText) findViewById(R.id.zs_hd_et_plateid);
        zs_hd_et_numid= (EditText) findViewById(R.id.zs_hd_et_numid);
        zs_hd_et_ck= (EditText) findViewById(R.id.zs_hd_et_ck);
        zs_hd_btn_testopen= (Button) findViewById(R.id.zs_hd_btn_testopen);
        zs_hd_btn_teststatus= (Button) findViewById(R.id.zs_hd_btn_teststatus);

        zs_hd_btn_testopen.setOnClickListener(this);
        zs_hd_btn_teststatus.setOnClickListener(this);

        zs_CabinetCtrlByZS=CabinetCtrlByZS.getInstance();

        zs_CabinetCtrlByZS.setHandler(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {


                switch (msg.what){
                    case CabinetCtrlByZS.MESSAGE_WHAT_UNLOCK:
                        Bundle bundle = msg.getData();
                        int status = bundle.getInt("status");
                        String message = bundle.getString("message");
                        showToast(message);
                        switch (status){
                            case 1:
                                break;
                            case  2:
                                break;
                            case  3:
                                break;
                        }


                        break;
                    case CabinetCtrlByZS.MESSAGE_WHAT_QUERYSTATUS:
                        break;
                }
                return false;
            }
        }));

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

        String str_zs_hd_et_ck=zs_hd_et_ck.getText()+"";
        String str_zs_hd_et_plateid=zs_hd_et_plateid.getText()+"";
        String str_zs_hd_et_numid=zs_hd_et_numid.getText()+"";


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
                    //321,6257
                   // 1137 42694
                    mUVCCamera.openCamera(42694,1137);
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
                case R.id.btnMachineGoZero:
                    cabinetCtrlByDS.testGoZero();
                    break;
                case R.id.zs_hd_btn_testopen:
                    if (StringUtil.isEmpty(str_zs_hd_et_ck)) {
                        showToast("请输入串口名称");
                        return;
                    }
                    if (StringUtil.isEmpty(str_zs_hd_et_plateid)) {
                        showToast("请输入锁版ID");
                        return;
                    }
                    if (StringUtil.isEmpty(str_zs_hd_et_numid)) {
                        showToast("请输入箱子ID");
                        return;
                    }
                    zs_CabinetCtrlByZS.setConfig(str_zs_hd_et_ck,115200);
                    zs_CabinetCtrlByZS.unLock(Integer.valueOf(str_zs_hd_et_plateid),Integer.valueOf(str_zs_hd_et_numid));
                    break;
                case R.id.zs_hd_btn_teststatus:
                    if (StringUtil.isEmpty(str_zs_hd_et_ck)) {
                        showToast("请输入串口名称");
                        return;
                    }
                    if (StringUtil.isEmpty(str_zs_hd_et_plateid)) {
                        showToast("请输入锁版ID");
                        return;
                    }
                    if (StringUtil.isEmpty(str_zs_hd_et_numid)) {
                        showToast("请输入箱子ID");
                        return;
                    }
                    zs_CabinetCtrlByZS.setConfig(str_zs_hd_et_ck,115200);
                    zs_CabinetCtrlByZS.queryStatus(Integer.valueOf(str_zs_hd_et_plateid));
                    break;
            }
        }
    }

    public  void saveCaptureStill(byte[] data,String saveDir,String uniqueId) {
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
