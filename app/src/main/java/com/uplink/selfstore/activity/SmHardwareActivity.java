package com.uplink.selfstore.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByZS;
import com.uplink.selfstore.http.HttpClient;
import com.uplink.selfstore.model.ZSCabBoxBean;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.own.OwnFileUtil;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;


public class SmHardwareActivity extends SwipeBackActivity implements View.OnClickListener {

    private static final String TAG = "SmHardwareActivity";

    //摄像头
    private Camera renlian_camera;
    private boolean renlian_camera_isRunning=false;
    private SurfaceView rennian_camera_surfaceView;
    private Button renlian_camera_btn_open;
    private Button renlian_camera_btn_captureStill;
    private Button renlian_camera_btn_record;
    private SurfaceHolder renlian_camera_surfaceholder;

    private Camera jigui_camera;
    private boolean jigui_camera_isRunning=false;
    private SurfaceView jigui_camera_surfaceView;
    private Button jigui_camera_btn_open;
    private Button jigui_camera_btn_captureStill;
    private Button jigui_camera_btn_record;
    private SurfaceHolder jigui_camera_surfaceholder;

    private Camera chuhuokou_camera;
    private boolean chuhuokou_camera_isRunning=false;
    private SurfaceView chuhuokou_camera_surfaceView;
    private Button chuhuokou_camera_btn_open;
    private Button chuhuokou_camera_btn_captureStill;
    private Button chuhuokou_camera_btn_record;
    private SurfaceHolder chuhuokou_camera_surfaceholder;


    //中顺硬件诊断
    private LinearLayout zs_hd_layout;
    private Button zs_hd_btn_connect;
    private EditText zs_hd_et_plateid;
    private EditText zs_hd_et_numid;
    private EditText zs_hd_et_ck;
    private Button zs_hd_btn_testopen;
    private Button zs_hd_btn_teststatus;
    private CabinetCtrlByZS zs_CabinetCtrlByZS;
    private TextView zs_tv_log;

    //德尚硬件诊断
    private CabinetCtrlByDS ds_CabinetCtrlByZS=null;
    private EditText ds_hd_et_ck;
    private Button ds_hd_btn_connect;
    private Button ds_hd_btn_gozero;
    private Button ds_hd_btn_stop;
    private TextView ds_tv_log;

    Semaphore mCameraOpenCloseLock = new Semaphore(1);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smhardware);

        setNavTtile(this.getResources().getString(R.string.aty_smhardware_navtitle));
        setNavGoBackBtnVisible(true);

        initViewByCamera();
        initViewByDS();
        initViewByZS();

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 300);
            } else {
                //String[] cameraIdList = MyCamera.getCameraIdList(this);
                //renlian_cameraSurface= new CameraSurface(SmHardwareActivity.this, "0");
                //rennian_camera_surfaceView.setSurfaceTextureListener(renlian_cameraSurface);
            }

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 300);
            } else {
                //String[] cameraIdList = MyCamera.getCameraIdList(this);
                //renlian_cameraSurface= new CameraSurface(SmHardwareActivity.this, "0");
                //rennian_camera_surfaceView.setSurfaceTextureListener(renlian_cameraSurface);
            }
        }


    }


    private void  initViewByCamera() {

        rennian_camera_surfaceView = (SurfaceView) findViewById(R.id.rennian_camera_surfaceView);
        renlian_camera_surfaceholder = rennian_camera_surfaceView.getHolder();
        renlian_camera_surfaceholder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //renlian_camera_surfaceholder.addCallback(new surfaceholderCallbackBack());


        renlian_camera_btn_open = (Button) findViewById(R.id.renlian_camera_btn_open);
        renlian_camera_btn_open.setOnClickListener(this);
        renlian_camera_btn_captureStill = (Button) findViewById(R.id.renlian_camera_btn_captureStill);
        renlian_camera_btn_captureStill.setOnClickListener(this);
        renlian_camera_btn_record = (Button) findViewById(R.id.renlian_camera_btn_record);
        renlian_camera_btn_record.setOnClickListener(this);

        jigui_camera_surfaceView = (SurfaceView) findViewById(R.id.jigui_camera_surfaceView);
        jigui_camera_surfaceholder = jigui_camera_surfaceView.getHolder();
        jigui_camera_surfaceholder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //jigui_camera_surfaceholder.addCallback(new surfaceholderCallbackBack());

        jigui_camera_btn_open = (Button) findViewById(R.id.jigui_camera_btn_open);
        jigui_camera_btn_open.setOnClickListener(this);
        jigui_camera_btn_captureStill = (Button) findViewById(R.id.jigui_camera_btn_captureStill);
        jigui_camera_btn_captureStill.setOnClickListener(this);
        jigui_camera_btn_record = (Button) findViewById(R.id.jigui_camera_btn_record);
        jigui_camera_btn_record.setOnClickListener(this);

        chuhuokou_camera_surfaceView = (SurfaceView) findViewById(R.id.chuhuokou_camera_surfaceView);
        chuhuokou_camera_surfaceholder = chuhuokou_camera_surfaceView.getHolder();
        chuhuokou_camera_surfaceholder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //chuhuokou_camera_surfaceholder.addCallback(new surfaceholderCallbackBack());

        chuhuokou_camera_btn_open = (Button) findViewById(R.id.chuhuokou_camera_btn_open);
        chuhuokou_camera_btn_open.setOnClickListener(this);
        chuhuokou_camera_btn_captureStill = (Button) findViewById(R.id.chuhuokou_camera_btn_captureStill);
        chuhuokou_camera_btn_captureStill.setOnClickListener(this);
        chuhuokou_camera_btn_record = (Button) findViewById(R.id.chuhuokou_camera_btn_record);
        chuhuokou_camera_btn_record.setOnClickListener(this);
    }

    private void  initViewByZS(){

        zs_CabinetCtrlByZS=CabinetCtrlByZS.getInstance();

        zs_hd_layout= (LinearLayout) findViewById(R.id.zs_hd_layout);
        zs_hd_et_plateid= (EditText) findViewById(R.id.zs_hd_et_plateid);
        zs_hd_et_numid= (EditText) findViewById(R.id.zs_hd_et_numid);
        zs_hd_et_ck= (EditText) findViewById(R.id.zs_hd_et_ck);
        zs_hd_btn_connect= (Button) findViewById(R.id.zs_hd_btn_connect);
        zs_hd_btn_testopen= (Button) findViewById(R.id.zs_hd_btn_testopen);
        zs_hd_btn_teststatus= (Button) findViewById(R.id.zs_hd_btn_teststatus);
        zs_tv_log=(TextView) findViewById(R.id.zs_tv_log);

        zs_hd_btn_connect.setOnClickListener(this);
        zs_hd_btn_testopen.setOnClickListener(this);
        zs_hd_btn_teststatus.setOnClickListener(this);


        zs_CabinetCtrlByZS.setHandler(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                Bundle bundle = msg.getData();
                int status = bundle.getInt("status");
                String message = bundle.getString("message");

                showToast(message);

                switch (msg.what){
                    case CabinetCtrlByZS.MESSAGE_WHAT_ONEUNLOCK:
                        switch (status) {
                            case 4://反馈成功

                                CabinetCtrlByZS.ZSCabBoxStatusResult result = (CabinetCtrlByZS.ZSCabBoxStatusResult) bundle.getSerializable("result");
                                if (result != null) {
                                    if (result.getCabBoxs() != null) {
                                        HashMap<Integer, ZSCabBoxBean> cabBoxs = result.getCabBoxs();

                                        String t1 = "";
                                        for (HashMap.Entry<Integer, ZSCabBoxBean> entry : cabBoxs.entrySet()) {
                                            ZSCabBoxBean cabBox = entry.getValue();
                                            t1 += "第" + cabBox.getId() + "个格子打开状态：" + cabBox.isOpen() + ",是否有货物：" + cabBox.isNonGoods();
                                        }
                                        zs_tv_log.setText(t1);
                                    }
                                }
                                break;
                        }
                        break;
                    case CabinetCtrlByZS.MESSAGE_WHAT_QUERYLOCKSTATUS:
                        break;
                }
                return false;
            }
        }));

        zs_hd_et_ck.setText(zs_CabinetCtrlByZS.getComId());
    }

    private void  initViewByDS() {

        ds_CabinetCtrlByZS = CabinetCtrlByDS.getInstance();

        ds_CabinetCtrlByZS.setGoZeroHandler(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                int status = bundle.getInt("status");
                String message = bundle.getString("message");

                showToast(message);
                return false;
            }
        }));

        ds_hd_et_ck = (EditText) findViewById(R.id.ds_hd_et_ck);
        ds_hd_btn_connect = (Button) findViewById(R.id.ds_hd_btn_connect);
        ds_hd_btn_gozero = (Button) findViewById(R.id.ds_hd_btn_gozero);
        ds_hd_btn_stop= (Button) findViewById(R.id.ds_hd_btn_stop);
        ds_hd_btn_connect.setOnClickListener(this);
        ds_hd_btn_gozero.setOnClickListener(this);
        ds_hd_btn_stop.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (zs_CabinetCtrlByZS != null) {
            zs_CabinetCtrlByZS.disConnect();
            zs_CabinetCtrlByZS=null;
        }

        if (ds_CabinetCtrlByZS != null) {
            ds_CabinetCtrlByZS.disConnect();
            ds_CabinetCtrlByZS=null;
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        if (zs_CabinetCtrlByZS != null) {
            zs_CabinetCtrlByZS.disConnect();
            zs_CabinetCtrlByZS=null;
        }

        if (ds_CabinetCtrlByZS != null) {
            ds_CabinetCtrlByZS.disConnect();
            ds_CabinetCtrlByZS=null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (zs_CabinetCtrlByZS != null) {
            zs_CabinetCtrlByZS.disConnect();
            zs_CabinetCtrlByZS=null;
        }

        if (ds_CabinetCtrlByZS != null) {
            ds_CabinetCtrlByZS.disConnect();
            ds_CabinetCtrlByZS=null;
        }

        if (renlian_camera != null) {
            renlian_camera.stopPreview();
            renlian_camera.release();
            renlian_camera = null;
        }

        if (jigui_camera != null) {
            jigui_camera.stopPreview();
            jigui_camera.release();
            jigui_camera = null;
        }

        if (chuhuokou_camera != null) {
            chuhuokou_camera.stopPreview();
            chuhuokou_camera.release();
            chuhuokou_camera = null;
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        String str_ds_hd_et_ck=ds_hd_et_ck.getText()+"";
        String str_zs_hd_et_ck=zs_hd_et_ck.getText()+"";
        String str_zs_hd_et_plateid=zs_hd_et_plateid.getText()+"";
        String str_zs_hd_et_numid=zs_hd_et_numid.getText()+"";

        int camerasNumber= Camera.getNumberOfCameras();
        String imgId="";
        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.nav_back:
                    finish();
                    break;
                case R.id.renlian_camera_btn_open:
                    try {
                        LogUtil.e(TAG,"点击操作人脸摄像头:"+renlian_camera_isRunning);
                        if(!renlian_camera_isRunning) {
                            if (camerasNumber == 0) {
                                showToast("摄像头数量为0");
                                return;
                            }
                            renlian_camera = Camera.open(0);
                            if (renlian_camera == null) {
                                showToast("人脸摄像头对象为空");
                                return;
                            }
                            renlian_camera.setPreviewDisplay(renlian_camera_surfaceholder);
                            renlian_camera.startPreview();// 开始预览

                            renlian_camera_isRunning = true;
                            renlian_camera_btn_open.setText("关闭");
                        }
                        else {
                            renlian_camera_isRunning = false;
                            renlian_camera_btn_open.setText("打开");
                            renlian_camera.stopPreview();
                            renlian_camera.release();
                            renlian_camera = null;
                        }
                    }
                    catch (Exception ex){
                        showToast("("+camerasNumber+")人脸摄像头发生异常:"+ex.getMessage());
                    }
                    break;
                case R.id.renlian_camera_btn_captureStill:

                    if(!renlian_camera_isRunning){
                        showToast("人脸摄像头未打开");
                        return;
                    }


                    imgId= UUID.randomUUID().toString();

                    renlian_camera.takePicture(null, null, new CameraRenlianCallback(imgId));

                    break;
                case R.id.jigui_camera_btn_open:
                    try {
                        LogUtil.e(TAG,"点击操作机柜摄像头:"+jigui_camera_isRunning);
                        if(!jigui_camera_isRunning) {

                            if (camerasNumber == 0) {
                                showToast("摄像头数量为0");
                                return;
                            }
                            jigui_camera = Camera.open(1);
                            if (jigui_camera == null) {
                                showToast("机柜摄像头对象为空");
                                return;
                            }
                            jigui_camera.setPreviewDisplay(jigui_camera_surfaceholder);
                            jigui_camera.startPreview();// 开始预览

                            jigui_camera_isRunning=true;
                            jigui_camera_btn_open.setText("关闭");
                        }
                        else {
                            jigui_camera_isRunning = false;
                            jigui_camera_btn_open.setText("打开");
                            jigui_camera.stopPreview();
                            jigui_camera.release();
                            jigui_camera = null;
                        }
                    }
                    catch (Exception ex){
                        showToast("("+camerasNumber+")打开机柜摄像头发生异常:"+ex.getMessage());
                    }
                    break;
                case R.id.jigui_camera_btn_captureStill:

                    if(!jigui_camera_isRunning){
                        showToast("机柜摄像头未打开");
                        return;
                    }
                    imgId= UUID.randomUUID().toString();
                    jigui_camera.takePicture(null, null, new CameraJiguiCallback(imgId));

                    break;
                case R.id.chuhuokou_camera_btn_open:
                    try {
                        LogUtil.e(TAG,"点击操作出货口摄像头:"+chuhuokou_camera_isRunning);
                        if(!chuhuokou_camera_isRunning) {
                            if (camerasNumber == 0) {
                                showToast("摄像头数量为0");
                                return;
                            }
                            chuhuokou_camera = Camera.open(0);
                            if (chuhuokou_camera == null) {
                                showToast("出货口摄像头对象为空");
                                return;
                            }
                            chuhuokou_camera.setPreviewDisplay(chuhuokou_camera_surfaceholder);
                            chuhuokou_camera.startPreview();// 开始预览

                            chuhuokou_camera_isRunning=true;
                            chuhuokou_camera_btn_open.setText("关闭");
                        }
                        else {
                            chuhuokou_camera_isRunning = false;
                            chuhuokou_camera_btn_open.setText("打开");
                            chuhuokou_camera.stopPreview();
                            chuhuokou_camera.release();
                            chuhuokou_camera = null;
                        }

                    }
                    catch (Exception ex){
                        showToast("("+camerasNumber+")打开出货口发生异常:"+ex.getMessage());
                    }
                    break;
                case R.id.chuhuokou_camera_btn_captureStill:

                    if(!chuhuokou_camera_isRunning){
                        showToast("出货口摄像头未打开");
                        return;
                    }

                    imgId= UUID.randomUUID().toString();
                    chuhuokou_camera.takePicture(null, null, new CameraChuhuokouCallback(imgId));

                    break;
                case R.id.ds_hd_btn_connect:
                    if (StringUtil.isEmpty(str_ds_hd_et_ck)) {
                        showToast("[ds设备]请输入串口名称");
                        return;
                    }
                    ds_CabinetCtrlByZS.setComId(str_ds_hd_et_ck);
                    ds_CabinetCtrlByZS.connect();

                    if(!ds_CabinetCtrlByZS.isConnect()){
                        showToast("[ds设备]连接失败");
                        return;
                    }

                    showToast("[ds设备]连接成功");

                    break;
                case R.id.ds_hd_btn_gozero:
                    if(!ds_CabinetCtrlByZS.isConnect()){
                        showToast("[ds设备]未打开连接");
                        return;
                    }
                    ds_CabinetCtrlByZS.goZero();
                    break;
                case R.id.ds_hd_btn_stop:
                    if(!ds_CabinetCtrlByZS.isConnect()){
                        showToast("[zs设备]未打开连接");
                        return;
                    }
                    ds_CabinetCtrlByZS.emgStop();
                    break;
                case R.id.zs_hd_btn_connect:
                    if (StringUtil.isEmpty(str_zs_hd_et_ck)) {
                        showToast("[zs设备]请输入串口名称");
                        return;
                    }
                    zs_CabinetCtrlByZS.setComId(str_zs_hd_et_ck);
                    zs_CabinetCtrlByZS.connect();

                    if(!zs_CabinetCtrlByZS.isConnect()){
                        showToast("[zs设备]连接失败");
                        return;
                    }

                    showToast("[zs设备]连接成功");

                    break;
                case R.id.zs_hd_btn_testopen:
                    if (StringUtil.isEmpty(str_zs_hd_et_ck)) {
                        showToast("[zs设备]请输入串口名称");
                        return;
                    }
                    if (StringUtil.isEmpty(str_zs_hd_et_plateid)) {
                        showToast("[zs设备]请输入锁版ID");
                        return;
                    }
                    if (StringUtil.isEmpty(str_zs_hd_et_numid)) {
                        showToast("[zs设备]请输入箱子ID");
                        return;
                    }
                    if(!zs_CabinetCtrlByZS.isConnect()){
                        showToast("[zs设备]未打开连接");
                        return;
                    }
                    zs_CabinetCtrlByZS.unLock(Integer.valueOf(str_zs_hd_et_plateid),Integer.valueOf(str_zs_hd_et_numid));
                    break;
                case R.id.zs_hd_btn_teststatus:
                    if (StringUtil.isEmpty(str_zs_hd_et_ck)) {
                        showToast("[zs设备]请输入串口名称");
                        return;
                    }
                    if (StringUtil.isEmpty(str_zs_hd_et_plateid)) {
                        showToast("[zs设备]请输入锁版ID");
                        return;
                    }
                    if (StringUtil.isEmpty(str_zs_hd_et_numid)) {
                        showToast("[zs设备]请输入箱子ID");
                        return;
                    }
                    if(!zs_CabinetCtrlByZS.isConnect()){
                        showToast("[zs设备]未打开连接");
                        return;
                    }
                    zs_CabinetCtrlByZS.queryLockStatus(Integer.valueOf(str_zs_hd_et_plateid),Integer.valueOf(str_zs_hd_et_numid));
                    break;
            }
        }
    }

    private final class CameraRenlianCallback implements Camera.PictureCallback {

        private String imgId;
        public  CameraRenlianCallback(String imgId){
            this.imgId=imgId;
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            showToast("人脸拍照成功:"+imgId);
            savePic(imgId,data);
        }
    }

    private final class CameraJiguiCallback implements Camera.PictureCallback {

        private String imgId;
        public  CameraJiguiCallback(String imgId){
            this.imgId=imgId;
        }


        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            showToast("机柜拍照成功:"+imgId);
            savePic(imgId,data);
        }
    }

    private final class CameraChuhuokouCallback implements Camera.PictureCallback {

        private String imgId;
        public  CameraChuhuokouCallback(String imgId){
            this.imgId=imgId;
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            showToast("出货口拍照成功:"+imgId);

            savePic(imgId,data);
        }
    }

    private void  savePic(String imgId, byte[] data) {
        if (Config.IS_BUILD_DEBUG) {
            try {
                //保存在本地

                String mSaveDir = OwnFileUtil.getPicSaveDir();

                File pathFile = new File(mSaveDir);
                if (!pathFile.exists()) {
                    pathFile.mkdirs();
                }

                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                String filePath = mSaveDir + "/" + imgId + ".jpg";
                File file = new File(filePath);
                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                outputStream.close();

                //上传到服务器
                List<String> filePaths = new ArrayList<>();
                filePaths.add(filePath);
                Map<String, String> params = new HashMap<>();
                params.put("fileName", imgId);
                params.put("folder", "pickup");
                HttpClient.postFile(Config.URL.uploadfile, params, filePaths, null);

                Log.e(TAG, "拍照结束");
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}
