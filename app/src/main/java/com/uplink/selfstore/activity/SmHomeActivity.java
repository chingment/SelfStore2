package com.uplink.selfstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.view.View;
import android.widget.AdapterView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.NineGridItemAdapter;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

public class SmHomeActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "SmHomeActivity";
    private CustomConfirmDialog confirmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smhome);

        setNavTtile(this.getResources().getString(R.string.activity_smhome_navtitle));
        initView();
        initEvent();

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
                    case "fun.exitmanager":

                        //todo 改为重新获取数据方法
                        Map<String, Object> params = new HashMap<>();
                        params.put("machineId", machine.getId());
                        params.put("loginWay", 5);

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

        sendRunStatus("setting");
    }

    protected void initView() {

        MyGridView gridview = (MyGridView) findViewById(R.id.gridview_ninegrid);

        final List<NineGridItemBean> gridviewitems = new ArrayList<NineGridItemBean>();

        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.activity_smhome_ninegriditem_title_stockset), NineGridItemType.Function, "fun.machinestock", R.drawable.ic_sm_stock));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.activity_smhome_ninegriditem_title_machineset), NineGridItemType.Function, "fun.machineinfo", R.drawable.ic_sm_machine));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.activity_smhome_ninegriditem_title_userinfo), NineGridItemType.Function, "fun.userinfo", R.drawable.ic_sm_userinfo));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.activity_smhome_ninegriditem_title_cameratest), NineGridItemType.Function, "fun.cameratest", R.drawable.ic_sm_userinfo));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.activity_smhome_ninegriditem_title_checkupdateapp), NineGridItemType.Function, "fun.checkupdateapp", R.drawable.ic_sm_updateapp));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.activity_smhome_ninegriditem_title_closeapp), NineGridItemType.Function, "fun.closeapp", R.drawable.ic_sm_closeapp));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.activity_smhome_ninegriditem_title_rootsys), NineGridItemType.Function, "fun.rootsys", R.drawable.ic_sm_root));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.activity_smhome_ninegriditem_title_exitmanager), NineGridItemType.Function, "fun.exitmanager", R.drawable.ic_sm_exit));


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
                                case "fun.cameratest":
                                    intent = new Intent(getAppContext(), TestCameraActivity.class);
                                    startActivity(intent);
                                    break;
                                case "fun.checkupdateapp":
                                    intent = new Intent();
                                    intent.putExtra("from",2);
                                    intent.setAction("android.intent.action.updateAppService");
                                    sendBroadcast(intent);
                                    break;
                                case "fun.closeapp":
                                    confirmDialog.getTipsImage().setVisibility(View.GONE);
                                    confirmDialog.getBtnSure().setTag("fun.closeapp");
                                    confirmDialog.getTipsText().setText(getAppContext().getString(R.string.activity_smhome_confrimtips_closeapp));
                                    confirmDialog.show();
                                    break;
                                case "fun.rootsys":
                                    confirmDialog.getTipsImage().setVisibility(View.GONE);
                                    confirmDialog.getBtnSure().setTag("fun.rootsys");
                                    confirmDialog.getTipsText().setText(getAppContext().getString(R.string.activity_smhome_confrimtips_rootsys));
                                    confirmDialog.show();
                                    break;
                                case "fun.exitmanager":
                                    confirmDialog.getTipsImage().setVisibility(View.GONE);
                                    confirmDialog.getBtnSure().setTag("fun.exitmanager");
                                    confirmDialog.getTipsText().setText(getAppContext().getString(R.string.activity_smhome_confrimtips_exitmanager));
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
