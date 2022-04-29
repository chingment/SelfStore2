package com.uplink.selfstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.NineGridItemAdapter;
import com.uplink.selfstore.model.common.NineGridItemBean;
import com.uplink.selfstore.model.common.NineGridItemType;
import com.uplink.selfstore.ostCtrl.OstCtrlInterface;
import com.uplink.selfstore.app.AppManager;
import com.uplink.selfstore.service.WhiteService;
import com.uplink.selfstore.ui.dialog.CustomDialogConfirm;
import com.uplink.selfstore.ui.my.MyGridView;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.util.ArrayList;
import java.util.List;

public class SmRescueToolActivity extends SwipeBackActivity implements View.OnClickListener {

    private CustomDialogConfirm dialog_Confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smrescuetool);
        setNavTtile("设置检查");
        setNavGoBackBtnVisible(false);

        initView();
        initEvent();


        dialog_Confirm = new CustomDialogConfirm(SmRescueToolActivity.this, "", true);
        dialog_Confirm.setOnClickListener(new CustomDialogConfirm.OnClickListener() {
            @Override
            public void onSure() {
                String tag = dialog_Confirm.getTag().toString();
                LogUtil.d("tag:" + tag);
                switch (tag) {
                    case "fun.closeapp":
                        setHideStatusBar(false);
                        Intent whiteIntent = new Intent(SmRescueToolActivity.this, WhiteService.class);
                        stopService(whiteIntent);
                        AppManager.getAppManager().AppExit(SmRescueToolActivity.this);
                        break;
                    case "fun.rootsys":
                        setHideStatusBar(false);
                        OstCtrlInterface.getInstance().reboot(SmRescueToolActivity.this);
                        break;
                    case "fun.exitmanager":
                        Intent intent = new Intent(getAppContext(), InitDataActivity.class);
                        startActivity(intent);
                        finishAffinity();
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
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_shownav), NineGridItemType.Function, "fun.shownav", R.drawable.ic_sm_deviceinfo));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_checkupdateapp), NineGridItemType.Function, "fun.checkupdateapp", R.drawable.ic_sm_checkupdateapp));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_closeapp), NineGridItemType.Function, "fun.closeapp", R.drawable.ic_sm_closeapp));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_rootsys), NineGridItemType.Function, "fun.rootsys", R.drawable.ic_sm_rootsys));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_exitmanager), NineGridItemType.Function, "fun.exitmanager", R.drawable.ic_sm_exitmanager));


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
                                case "fun.shownav":
                                    OstCtrlInterface.getInstance().setHideStatusBar(SmRescueToolActivity.this,false);
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
        super.onClick(v);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog_Confirm != null) {
            dialog_Confirm.cancel();
        }
    }
}
