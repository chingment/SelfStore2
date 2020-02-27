package com.uplink.selfstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.NineGridItemAdapter;
import com.uplink.selfstore.deviceCtrl.MachineCtrl;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.common.NineGridItemBean;
import com.uplink.selfstore.model.common.NineGridItemType;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.dialog.CustomConfirmDialog;
import com.uplink.selfstore.ui.my.MyGridView;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

public class SmHomeActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "SmHomeActivity";
    private CustomConfirmDialog confirmDialog;
    private MachineCtrl machineCtrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smhome);

        setNavTtile(this.getResources().getString(R.string.aty_smhome_navtitle));
        initView();
        initEvent();

        machineCtrl=MachineCtrl.getInstance();
        machineCtrl.setDoorHandler(new Handler(new Handler.Callback() {
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

        Intent updateAppService = new Intent();
        updateAppService.putExtra("from",1);
        updateAppService.setAction("android.intent.action.updateAppService");
        sendBroadcast(updateAppService);

        final MachineBean machine = AppCacheManager.getMachine();

        confirmDialog = new CustomConfirmDialog(SmHomeActivity.this, "", true);
        confirmDialog.getBtnSure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = v.getTag().toString();
                LogUtil.e("tag:" + tag);
                switch (tag) {
                    case "fun.closeapp":
                        setHideStatusBar(false);
                        AppManager.getAppManager().AppExit(SmHomeActivity.this);
                        break;
                    case "fun.rootsys":
                        setHideStatusBar(false);
                        Intent it = new Intent();
                        it.setAction("com.fourfaith.reboot");
                        it.putExtra("mode", "0");//0 重启 1 关机
                        sendBroadcast(it);
                        break;
                    case "fun.door":
                        machineCtrl.doorControl();
                        break;
                    case "fun.exitmanager":

                        //todo 改为重新获取数据方法
                        Map<String, Object> params = new HashMap<>();

                        params.put("appId", BuildConfig.APPLICATION_ID);
                        params.put("loginWay", 5);

                        try {
                            JSONObject loginPms = new JSONObject();
                            loginPms.put("machineId", machine.getId() + "");
                            params.put("loginPms", loginPms);
                        }catch (JSONException e) {
                            e.printStackTrace();
                            return;
                        }
                        postByMy(Config.URL.own_Logout, params,null, true, "正在退出", new HttpResponseHandler() {
                            @Override
                            public void onSuccess(String response) {
                                ApiResultBean<Object> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<Object>>() {
                                });
                                if (rt.getResult() == Result.SUCCESS) {
                                    Intent intent = new Intent(getAppContext(), InitDataActivity.class);
                                    startActivity(intent);
                                    finish();
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
                confirmDialog.dismiss();
            }
        });

        confirmDialog.getBtnCancle().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                confirmDialog.dismiss();
            }
        });
    }

    protected void initView() {

        MyGridView gridview = (MyGridView) findViewById(R.id.gridview_ninegrid);

        final List<NineGridItemBean> gridviewitems = new ArrayList<NineGridItemBean>();

        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_stockset), NineGridItemType.Function, "fun.machinestock", R.drawable.ic_sm_stock));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_machineset), NineGridItemType.Function, "fun.machineinfo", R.drawable.ic_sm_machine));
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
                                case "fun.machineinfo":
                                    intent = new Intent(getAppContext(), SmMachineInfoActivity.class);
                                    startActivity(intent);
                                    break;
                                case "fun.machinestock":
                                    intent = new Intent(getAppContext(), SmMachineStockActivity.class);
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
                                    confirmDialog.getTipsImage().setVisibility(View.GONE);
                                    confirmDialog.getBtnSure().setTag("fun.closeapp");
                                    confirmDialog.getTipsText().setText(getAppContext().getString(R.string.aty_smhome_confrimtips_closeapp));
                                    confirmDialog.show();
                                    break;
                                case "fun.rootsys":
                                    confirmDialog.getTipsImage().setVisibility(View.GONE);
                                    confirmDialog.getBtnSure().setTag("fun.rootsys");
                                    confirmDialog.getTipsText().setText(getAppContext().getString(R.string.aty_smhome_confrimtips_rootsys));
                                    confirmDialog.show();
                                    break;
                                case "fun.door":
                                    confirmDialog.getTipsImage().setVisibility(View.GONE);
                                    confirmDialog.getBtnSure().setTag("fun.door");
                                    confirmDialog.getTipsText().setText(getAppContext().getString(R.string.aty_smhome_confrimtips_door));
                                    confirmDialog.show();
                                    break;
                                case "fun.exitmanager":
                                    confirmDialog.getTipsImage().setVisibility(View.GONE);
                                    confirmDialog.getBtnSure().setTag("fun.exitmanager");
                                    confirmDialog.getTipsText().setText(getAppContext().getString(R.string.aty_smhome_confrimtips_exitmanager));
                                    confirmDialog.show();
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
    public void onDestroy() {
        super.onDestroy();
        if (confirmDialog != null && confirmDialog.isShowing()) {
            confirmDialog.cancel();
        }
    }
}
