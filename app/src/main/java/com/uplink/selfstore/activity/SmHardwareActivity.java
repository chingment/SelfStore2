package com.uplink.selfstore.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByZS;
import com.uplink.selfstore.model.ZSCabBoxBean;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.NoDoubleClickUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.util.HashMap;


public class SmHardwareActivity extends SwipeBackActivity implements View.OnClickListener {

    private static final String TAG = "SmHardwareActivity";

//    private UVCCameraProxy mUVCCamera;
//    private Button mCameraOpenByChuHuoKou;
//    private Button mCameraOpenByJiGui;
//    private Button mCameraClose;
//    private Button mCameraCaptureStill;
//    private Button mCameraRecord;
//    private Button mCameraTest;
//    private TextureView mCameraTextureView;

//    private int mCameraPreviewWidth=640;
//    private int mCameraPreviewHeight=480;
//
//    private CabinetCtrlByDS cabinetCtrlByDS=null;
//    private Button btnMachineGoZero;


    //中顺硬件诊断
    private LinearLayout zs_hd_layout;
    private EditText zs_hd_et_plateid;
    private EditText zs_hd_et_numid;
    private EditText zs_hd_et_ck;
    private Button zs_hd_btn_testopen;
    private Button zs_hd_btn_teststatus;
    private CabinetCtrlByZS zs_CabinetCtrlByZS;

    private TextView tv_log;
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

        initViewByZS();

        tv_log=(TextView) findViewById(R.id.tv_log);

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

    public  void saveCaptureStill(byte[] data,String saveDir,String uniqueId) {
//        try {
//            if (data == null)
//                return;
//            if (saveDir == null)
//                return;
//            if (uniqueId == null)
//                return;
//
//            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, mCameraPreviewWidth, mCameraPreviewHeight, null);
//            ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
//            yuvImage.compressToJpeg(new Rect(0, 0, mCameraPreviewWidth, mCameraPreviewHeight), 100, bos);
//            byte[] buffer = bos.toByteArray();
//
//            Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
//
//            String mSaveDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + saveDir;
//
//            File pathFile = new File(mSaveDir);
//            if (!pathFile.exists()) {
//                pathFile.mkdirs();
//            }
//
//            String filePath = mSaveDir + "/" + uniqueId + ".jpg";
//            File outputFile = new File(filePath);
//            final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
//            os.flush();
//            os.close();
//
//        } catch (Exception e) {
//            Log.e(TAG, e.toString());
//        }
    }
}
