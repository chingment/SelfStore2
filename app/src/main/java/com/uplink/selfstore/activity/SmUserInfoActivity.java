package com.uplink.selfstore.activity;

import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.OwnInfoResultBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.dialog.CustomConfirmDialog;
import com.uplink.selfstore.ui.dialog.CustomFingerVeinDialog;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.util.HashMap;
import java.util.Map;

public class SmUserInfoActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "SmUserInfoActivity";
    private TextView txt_UserName;
    private TextView txt_FullName;
    private TextView txt_FingerVein;
    private ImageView btn_DelFingerVein;
    private CustomFingerVeinDialog dialog_FingerVein;
    private CustomConfirmDialog confirmDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smuserinfo);

        setNavTtile(this.getResources().getString(R.string.aty_smuserinfo_navtitle));
        setNavGoBackBtnVisible(true);

        initView();
        initEvent();
        initData();

    }

    protected void initView() {
        txt_UserName = (TextView) findViewById(R.id.txt_UserName);
        txt_FullName = (TextView) findViewById(R.id.txt_FullName);
        txt_FingerVein = (TextView) findViewById(R.id.txt_FingerVein);
        btn_DelFingerVein = (ImageView) findViewById(R.id.btn_DelFingerVein);
        confirmDialog = new CustomConfirmDialog(SmUserInfoActivity.this, "", true);
        confirmDialog.getBtnSure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delFingerVein();
            }
        });
        confirmDialog.getBtnCancle().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                confirmDialog.dismiss();
            }
        });
        dialog_FingerVein = new CustomFingerVeinDialog(SmUserInfoActivity.this);
        dialog_FingerVein.setCollectHandler(new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        Bundle bundle = msg.getData();
                        int status = bundle.getInt("status");
                        String message = bundle.getString("message");
                        byte[] result;
                        switch (status) {
                            case 1://采集提示
                                dialog_FingerVein.getTxtMessage().setText(message);
                                break;
                            case 2://采集成功
                                //dialog_FingerVein.getTxtMessage().setText(message);
                                result = bundle.getByteArray("result");
                                uploadFingerVeinData(result);
                                break;
                            case 3://采集失败
                                dialog_FingerVein.getTxtMessage().setText(message);
                                dialog_FingerVein.getBtnReCollect().setVisibility(View.VISIBLE);
                                break;
                        }
                        return false;
                    }
                })
        );
    }

    protected void initEvent() {
        txt_FingerVein.setOnClickListener(this);
        btn_DelFingerVein.setOnClickListener(this);
    }

    protected void initData() {
        getInfo();
    }

    private void  getInfo(){

        Map<String, String> params = new HashMap<>();

        getByMy(Config.URL.own_GetInfo, params, false, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<OwnInfoResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<OwnInfoResultBean>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {

                    OwnInfoResultBean d=rt.getData();
                    txt_UserName.setText(d.getUserName());
                    txt_FullName.setText(d.getFullName());

                    if(d.getFingerVeinCount()==0){
                        txt_FingerVein.setText(R.string.aty_smuserinfo_tvtx_fv_clickin);
                        btn_DelFingerVein.setVisibility(View.GONE);
                    }
                    else {
                        txt_FingerVein.setText(R.string.aty_smuserinfo_tvtx_fv_hasdata);
                        btn_DelFingerVein.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(String msg, Exception e) {
                showToast(msg);
            }
        });
    }

    private void  delFingerVein(){

        Map<String, String> params = new HashMap<>();

        postByMy(Config.URL.own_DeleteFingerVeinData, null, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<OwnInfoResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<OwnInfoResultBean>>() {
                });

                showToast(rt.getMessage());

                if (rt.getResult() == Result.SUCCESS) {
                    getInfo();
                }

                confirmDialog.dismiss();

            }

            @Override
            public void onFailure(String msg, Exception e) {
                showToast(msg);
            }
        });
    }
    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.nav_back:
                    finish();
                    break;
                case R.id.btn_DelFingerVein:
                    confirmDialog.getTipsImage().setVisibility(View.GONE);
                    confirmDialog.getTipsText().setText(R.string.aty_smuserinfo_tvtx_fv_clickin);
                    confirmDialog.show();
                    break;
                case R.id.txt_FingerVein:
                    dialog_FingerVein.getTxtMessage().setText(R.string.aty_smuserinfo_tips_puthand);
                    dialog_FingerVein.startCollect();
                    dialog_FingerVein.show();
                    break;
            }
        }
    }

    private void  uploadFingerVeinData(byte[] data){

        MachineBean machine = AppCacheManager.getMachine();

        Map<String, Object> params = new HashMap<>();
        params.put("machineId", machine.getId() + "");
        params.put("veinData", Base64.encodeToString(data, Base64.NO_WRAP));

        postByMy(Config.URL.own_UploadFingerVeinData, params, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {

                ApiResultBean<Object> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<Object>>() {
                });

                dialog_FingerVein.getTxtMessage().setText(rt.getMessage());
                if (rt.getResult() == Result.SUCCESS) {
                    getInfo();
                }
            }

            @Override
            public void onFailure(String msg, Exception e) {
                showToast(msg);
            }
        });

    }
}
