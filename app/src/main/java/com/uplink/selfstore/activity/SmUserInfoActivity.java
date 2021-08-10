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
import com.uplink.selfstore.model.api.DeviceBean;
import com.uplink.selfstore.model.api.RetOwnInfo;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.dialog.CustomDialogConfirm;
import com.uplink.selfstore.ui.dialog.CustomDialogFingerVein;
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
    private CustomDialogFingerVein dialog_FingerVeinCollect;
    private CustomDialogConfirm dialog_Confirm;
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

    private void initView() {
        txt_UserName = (TextView) findViewById(R.id.txt_UserName);
        txt_FullName = (TextView) findViewById(R.id.txt_FullName);
        txt_FingerVein = (TextView) findViewById(R.id.txt_FingerVein);
        btn_DelFingerVein = (ImageView) findViewById(R.id.btn_DelFingerVein);
        dialog_Confirm = new CustomDialogConfirm(SmUserInfoActivity.this, "确定要删除？", true);
        dialog_Confirm.setOnClickListener(new CustomDialogConfirm.OnClickListener() {
            @Override
            public void onSure() {
                delFingerVein();
            }

            @Override
            public void onCancle() {
                dialog_Confirm.hide();
            }
        });

        dialog_FingerVeinCollect = new CustomDialogFingerVein(SmUserInfoActivity.this);
        dialog_FingerVeinCollect.setCollectHandler(new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        Bundle bundle = msg.getData();
                        int status = bundle.getInt("status");
                        String message = bundle.getString("message");
                        byte[] result;
                        switch (status) {
                            case 1://采集提示
                                dialog_FingerVeinCollect.getTxtMessage().setText(message);
                                break;
                            case 2://采集成功
                                //dialog_FingerVein.getTxtMessage().setText(message);
                                result = bundle.getByteArray("result");
                                uploadFingerVeinData(result);
                                break;
                            case 3://采集失败
                                dialog_FingerVeinCollect.getTxtMessage().setText(message);
                                dialog_FingerVeinCollect.getBtnReCollect().setVisibility(View.VISIBLE);
                                break;
                        }
                        return false;
                    }
                })
        );
    }

    private void initEvent() {
        txt_FingerVein.setOnClickListener(this);
        btn_DelFingerVein.setOnClickListener(this);
    }

    private void initData() {
        getInfo();
    }

    private void  getInfo(){

        Map<String, Object> params = new HashMap<>();

        postByMy(SmUserInfoActivity.this, Config.URL.own_GetInfo, params,null, false, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<RetOwnInfo> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<RetOwnInfo>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {

                    RetOwnInfo d=rt.getData();
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

        postByMy(SmUserInfoActivity.this, Config.URL.own_DeleteFingerVeinData, null, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<RetOwnInfo> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<RetOwnInfo>>() {
                });

                showToast(rt.getMessage());

                if (rt.getResult() == Result.SUCCESS) {
                    getInfo();
                }
                dialog_Confirm.hide();
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
                    dialog_Confirm.setTipsImageVisibility(View.GONE);
                    dialog_Confirm.setTipsText(R.string.aty_smuserinfo_tips_confirmdel);
                    dialog_Confirm.show();
                    break;
                case R.id.txt_FingerVein:
                    dialog_FingerVeinCollect.getTxtMessage().setText(R.string.aty_smuserinfo_tips_puthand);
                    dialog_FingerVeinCollect.startCollect();
                    dialog_FingerVeinCollect.show();
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(dialog_Confirm!=null){
            dialog_Confirm.cancel();
        }

        if(dialog_FingerVeinCollect!=null){
            dialog_FingerVeinCollect.cancel();
        }
    }

    private void  uploadFingerVeinData(byte[] data){

        DeviceBean device = AppCacheManager.getDevice();

        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", device.getDeviceId() + "");
        params.put("veinData", Base64.encodeToString(data, Base64.NO_WRAP));

        postByMy(SmUserInfoActivity.this, Config.URL.own_UploadFingerVeinData, params, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {

                ApiResultBean<Object> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<Object>>() {
                });

                dialog_FingerVeinCollect.getTxtMessage().setText(rt.getMessage());
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
