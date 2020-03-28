package com.uplink.selfstore.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByZS;
import com.uplink.selfstore.model.ZSCabBoxBean;
import com.uplink.selfstore.ui.MyCamera;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.util.HashMap;
import java.util.concurrent.Semaphore;


public class SmHardwareActivity extends SwipeBackActivity implements View.OnClickListener {

    private static final String TAG = "SmHardwareActivity";

    //摄像头
    private Camera renlian_camera;
    private SurfaceView rennian_camera_surfaceView;
    private Button renlian_camera_btn_open;
    private Button renlian_camera_btn_captureStill;
    private Button renlian_camera_btn_record;
    private SurfaceHolder renlian_camera_surfaceholder;

    private Camera jigui_camera;
    private SurfaceView jigui_camera_surfaceView;
    private Button jigui_camera_btn_open;
    private Button jigui_camera_btn_captureStill;
    private Button jigui_camera_btn_record;
    private SurfaceHolder jigui_camera_surfaceholder;

    private Camera chuhuokou_camera;
    private SurfaceView chuhuokou_camera_surfaceView;
    private Button chuhuokou_camera_btn_open;
    private Button chuhuokou_camera_btn_captureStill;
    private Button chuhuokou_camera_btn_record;
    private SurfaceHolder chuhuokou_camera_surfaceholder;

    private CabinetCtrlByDS cabinetCtrlByDS=null;

    //中顺硬件诊断
    private LinearLayout zs_hd_layout;
    private EditText zs_hd_et_plateid;
    private EditText zs_hd_et_numid;
    private EditText zs_hd_et_ck;
    private Button zs_hd_btn_testopen;
    private Button zs_hd_btn_teststatus;
    private CabinetCtrlByZS zs_CabinetCtrlByZS;

    private TextView tv_log;

    Semaphore mCameraOpenCloseLock = new Semaphore(1);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smhardware);

        setNavTtile(this.getResources().getString(R.string.aty_smhardware_navtitle));
        setNavGoBackBtnVisible(true);
        

//        cabinetCtrlByDS=CabinetCtrlByDS.getInstance();
//
//        cabinetCtrlByDS.setGoZeroHandler(new Handler(new Handler.Callback() {
//            @Override
//            public boolean handleMessage(Message msg) {
//
//                Bundle bundle = msg.getData();
//                int status = bundle.getInt("status");
//                String message = bundle.getString("message");
//
//                showToast(message);
//                return false;
//            }
//        }));

        initViewByCamera();
        initViewByZS();

        tv_log=(TextView) findViewById(R.id.tv_log);


//        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.CAMERA}, 300);
//        }
//        else {
//            String[] cameraIdList = MyCamera.getCameraIdList(this);
//            renlian_cameraSurface= new CameraSurface(SmHardwareActivity.this, "0");
//            rennian_camera_surfaceView.setSurfaceTextureListener(renlian_cameraSurface);
//        }


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
                                        tv_log.setText(t1);
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (zs_CabinetCtrlByZS != null) {
            zs_CabinetCtrlByZS.disConnect();
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

        String str_zs_hd_et_ck=zs_hd_et_ck.getText()+"";
        String str_zs_hd_et_plateid=zs_hd_et_plateid.getText()+"";
        String str_zs_hd_et_numid=zs_hd_et_numid.getText()+"";

        int camerasNumber= Camera.getNumberOfCameras();
        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.nav_back:
                    finish();
                    break;
                case R.id.renlian_camera_btn_open:
                    try {
                        if(camerasNumber==0){
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
                    }
                    catch (Exception ex){
                        showToast("打开人脸摄像头发生异常:"+ex.getMessage());
                    }
                    break;
                case R.id.jigui_camera_btn_open:
                    try {
                        if(camerasNumber==0){
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
                    }
                    catch (Exception ex){
                        showToast("打开机柜摄像头发生异常:"+ex.getMessage());
                    }
                    break;
                case R.id.chuhuokou_camera_btn_open:
                    try {
                        if(camerasNumber==0){
                            showToast("摄像头数量为0");
                            return;
                        }
                        chuhuokou_camera = Camera.open(2);
                        if (chuhuokou_camera == null) {
                            showToast("出货口摄像头对象为空");
                            return;
                        }
                        chuhuokou_camera.setPreviewDisplay(chuhuokou_camera_surfaceholder);
                        chuhuokou_camera.startPreview();// 开始预览
                    }
                    catch (Exception ex){
                        showToast("打开出货口发生异常:"+ex.getMessage());
                    }
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
                    zs_CabinetCtrlByZS.setComId(str_zs_hd_et_ck);
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
                    zs_CabinetCtrlByZS.setComId(str_zs_hd_et_ck);
                    zs_CabinetCtrlByZS.queryLockStatus(Integer.valueOf(str_zs_hd_et_plateid),Integer.valueOf(str_zs_hd_et_numid));
                    break;
            }
        }
    }
}
