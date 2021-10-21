package com.uplink.selfstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.NineGridItemAdapter;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByZS;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.CabinetBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.common.NineGridItemBean;
import com.uplink.selfstore.model.common.NineGridItemType;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ostCtrl.OstCtrlInterface;
import com.uplink.selfstore.service.MqttService;
import com.uplink.selfstore.service.WhiteService;
import com.uplink.selfstore.ui.dialog.CustomDialogConfirm;
import com.uplink.selfstore.ui.my.MyGridView;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmHomeActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "SmHomeActivity";
    private CustomDialogConfirm dialog_Confirm;
    private CabinetCtrlByDS cabinetCtrlByDS;
    private CabinetCtrlByZS cabinetCtrlByZS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smhome);


        cabinetCtrlByDS=CabinetCtrlByDS.getInstance();
        cabinetCtrlByZS=CabinetCtrlByZS.getInstance();

        setNavTtile(this.getResources().getString(R.string.aty_smhome_navtitle));
        initView();
        initEvent();

        cabinetCtrlByDS.setDoorHandler(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                int status = bundle.getInt("status");
                String message = bundle.getString("message");
                switch (status) {
                    case 1: //消息提示超时
                        showToast(message);
                        break;
                }
                return true;
            }
          })
        );

        cabinetCtrlByZS.setHandler(new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        Bundle bundle = msg.getData();
                        int status = bundle.getInt("status");
                        String message = bundle.getString("message");
                        switch (status) {
                            case 6: //消息提示超时
                                showToast(message);
                                break;
                        }
                        return true;
                    }
                })
        );

        Intent updateAppService = new Intent();
        updateAppService.putExtra("from",1);
        updateAppService.setAction("android.intent.action.updateAppService");
        sendBroadcast(updateAppService);

        dialog_Confirm = new CustomDialogConfirm(SmHomeActivity.this, "", true);
        dialog_Confirm.setOnClickListener(new CustomDialogConfirm.OnClickListener() {
            @Override
            public void onSure() {
                String tag = dialog_Confirm.getTag().toString();
                LogUtil.e("tag:" + tag);
                switch (tag) {
                    case "fun.closeapp":
                        setHideStatusBar(false);
                        Intent whiteIntent = new Intent(SmHomeActivity.this, WhiteService.class);
                        stopService(whiteIntent);
                        AppManager.getAppManager().AppExit(SmHomeActivity.this);
                        break;
                    case "fun.rootsys":
                        setHideStatusBar(false);
                        OstCtrlInterface.getInstance().reboot(SmHomeActivity.this);
                        break;
                    case "fun.door":

                        String mstVern=getDevice().getMstVern();
                        if(mstVern!=null) {
                            switch (mstVern) {
                                case "DS":
                                    cabinetCtrlByDS.doorControl();
                                    break;
                                case "ZS":
                                    cabinetCtrlByZS.doorControl();
                                    break;
                            }
                        }

                        break;
                    case "fun.exitmanager":

                        //todo 改为重新获取数据方法
                        Map<String, Object> params = new HashMap<>();

                        params.put("appId", BuildConfig.APPLICATION_ID);
                        params.put("loginWay", 5);
                        params.put("deviceId", getDevice().getDeviceId() + "");

                        postByMy(SmHomeActivity.this, Config.URL.own_Logout, params, true, "正在退出", new HttpResponseHandler() {
                            @Override
                            public void onSuccess(String response) {
                                ApiResultBean<Object> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<Object>>() {
                                });
                                if (rt.getResult() == Result.SUCCESS) {
                                    Intent intent = new Intent(getAppContext(), InitDataActivity.class);
                                    startActivity(intent);
                                    finishAffinity();
                                } else {
                                  showToast(rt.getMessage());
                                }
                            }

                            @Override
                            public void onFailure(String msg, Exception e) {
                                showToast(msg);
                            }
                        });

                        break;
                }
                dialog_Confirm.hide();
            }

            @Override
            public void onCancle() {
                dialog_Confirm.hide();
            }
        });

    }

    private void initView() {

        MyGridView gridview = (MyGridView) findViewById(R.id.gridview_ninegrid);

        final List<NineGridItemBean> gridviewitems = new ArrayList<NineGridItemBean>();

        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_stockset), NineGridItemType.Function, "fun.devicestock",R.drawable.ic_sm_stock));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_replenishplan), NineGridItemType.Function, "fun.replenishplan",R.drawable.ic_sm_replenishplan));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_deviceset), NineGridItemType.Function, "fun.deviceinfo", R.drawable.ic_sm_device));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_runexhandle), NineGridItemType.Function, "fun.runexhandle", R.drawable.ic_sm_runexhandle));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_userinfo), NineGridItemType.Function, "fun.userinfo", R.drawable.ic_sm_userinfo));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_hardware), NineGridItemType.Function, "fun.hardware", R.drawable.ic_sm_hardware));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_checkupdateapp), NineGridItemType.Function, "fun.checkupdateapp", R.drawable.ic_sm_updateapp));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_closeapp), NineGridItemType.Function, "fun.closeapp", R.drawable.ic_sm_closeapp));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_rootsys), NineGridItemType.Function, "fun.rootsys", R.drawable.ic_sm_root));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_door), NineGridItemType.Function, "fun.door", R.drawable.ic_sm_door));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_exitmanager), NineGridItemType.Function, "fun.exitmanager", R.drawable.ic_sm_exit));

        NineGridItemAdapter nineGridItemdapter = new NineGridItemAdapter(getAppContext(), gridviewitems);

        gridview.setAdapter(nineGridItemdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!NoDoubleClickUtil.isDoubleClick()) {
                    NineGridItemBean gridviewitem = gridviewitems.get(position);
                    int type = gridviewitem.getType();
                    String action = gridviewitem.getAction();
                    Intent intent;
                    switch (type) {
                        case NineGridItemType.Function:
                            intent = new Intent();
                            switch (action) {
                                case "fun.deviceinfo":
                                    intent = new Intent(getAppContext(), SmDeviceInfoActivity.class);
                                    startActivity(intent);
                                    break;
                                case "fun.devicestock":
                                    intent = new Intent(getAppContext(), SmDeviceStockActivity.class);
                                    startActivity(intent);
                                    break;
                                case "fun.replenishplan":
                                    intent = new Intent(getAppContext(), SmReplenishPlanActivity.class);
                                    startActivity(intent);
                                    break;
                                case "fun.runexhandle":
                                    intent = new Intent(getAppContext(), SmRunExHandleActivity.class);
                                    startActivity(intent);
                                    break;
                                case "fun.userinfo":
                                    intent = new Intent(getAppContext(), SmUserInfoActivity.class);
                                    startActivity(intent);
                                    break;
                                case "fun.hardware":
                                    intent = new Intent(getAppContext(), SmHardwareActivity.class);
                                    startActivity(intent);
                                    break;
                                case "fun.checkupdateapp":
                                    intent = new Intent();
                                    intent.putExtra("from", 2);
                                    intent.setAction("android.intent.action.updateAppService");
                                    sendBroadcast(intent);
                                    break;
                                case "fun.closeapp":
                                    dialog_Confirm.setTipsImageVisibility(View.GONE);
                                    dialog_Confirm.setTag("fun.closeapp");
                                    dialog_Confirm.setTipsText(getAppContext().getString(R.string.aty_smhome_confrimtips_closeapp));
                                    dialog_Confirm.show();
                                    break;
                                case "fun.rootsys":
                                    dialog_Confirm.setTipsImageVisibility(View.GONE);
                                    dialog_Confirm.setTag("fun.rootsys");
                                    dialog_Confirm.setTipsText(getAppContext().getString(R.string.aty_smhome_confrimtips_rootsys));
                                    dialog_Confirm.show();
                                    break;
                                case "fun.door":
                                    dialog_Confirm.setTipsImageVisibility(View.GONE);
                                    dialog_Confirm.setTag("fun.door");
                                    dialog_Confirm.setTipsText(getAppContext().getString(R.string.aty_smhome_confrimtips_door));
                                    dialog_Confirm.show();
                                    break;
                                case "fun.exitmanager":
                                    dialog_Confirm.setTipsImageVisibility(View.GONE);
                                    dialog_Confirm.setTag("fun.exitmanager");
                                    dialog_Confirm.setTipsText(getAppContext().getString(R.string.aty_smhome_confrimtips_exitmanager));
                                    dialog_Confirm.show();
                                    break;
                            }
                    }
                }
            }
        });
    }

    private void initEvent() {

    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.nav_back:
                    finish();
                    break;
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        if(cabinetCtrlByDS==null) {
            cabinetCtrlByDS = CabinetCtrlByDS.getInstance();
        }

        cabinetCtrlByDS.connect();

        if(cabinetCtrlByZS==null) {
            cabinetCtrlByZS = CabinetCtrlByZS.getInstance();
        }

        cabinetCtrlByZS.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (cabinetCtrlByDS != null) {
            cabinetCtrlByDS.disConnect();
            cabinetCtrlByDS = null;
        }

        if(cabinetCtrlByZS!=null){
            cabinetCtrlByZS.disConnect();
            cabinetCtrlByZS = null;
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        if(cabinetCtrlByDS!=null){
            cabinetCtrlByDS.disConnect();
            cabinetCtrlByDS = null;
        }

        if(cabinetCtrlByZS!=null){
            cabinetCtrlByZS.disConnect();
            cabinetCtrlByZS = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog_Confirm != null) {
            dialog_Confirm.cancel();
        }

        if(cabinetCtrlByZS!=null){
            cabinetCtrlByZS.disConnect();
        }

        if(cabinetCtrlByDS!=null){
            cabinetCtrlByDS.disConnect();
            cabinetCtrlByDS = null;
        }
    }
}
