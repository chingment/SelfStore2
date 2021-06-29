package com.uplink.selfstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.NineGridItemAdapter;
import com.uplink.selfstore.model.common.NineGridItemBean;
import com.uplink.selfstore.model.common.NineGridItemType;
import com.uplink.selfstore.ostCtrl.OstCtrlInterface;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.ui.dialog.CustomConfirmDialog;
import com.uplink.selfstore.ui.my.MyGridView;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.util.ArrayList;
import java.util.List;

public class SmRescueToolActivity extends SwipeBackActivity implements View.OnClickListener {

    private CustomConfirmDialog dialog_confirm;
    private Button btn_ShowNav;
    private Button btn_AppExit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smrescuetool);
        setNavTtile("设置检查");
        setNavGoBackBtnVisible(false);

        initView();
        initEvent();


        dialog_confirm = new CustomConfirmDialog(SmRescueToolActivity.this, "", true);
        dialog_confirm.getBtnSure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = v.getTag().toString();
                LogUtil.e("tag:" + tag);
                switch (tag) {
                    case "fun.closeapp":
                        setHideStatusBar(false);
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
                dialog_confirm.hide();
            }
        });

        dialog_confirm.getBtnCancle().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_confirm.hide();
            }
        });
    }

    private void initView() {
        MyGridView gridview = (MyGridView) findViewById(R.id.gridview_ninegrid);

        final List<NineGridItemBean> gridviewitems = new ArrayList<NineGridItemBean>();
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_shownav), NineGridItemType.Function, "fun.shownav", R.drawable.ic_sm_device));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_checkupdateapp), NineGridItemType.Function, "fun.checkupdateapp", R.drawable.ic_sm_updateapp));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_closeapp), NineGridItemType.Function, "fun.closeapp", R.drawable.ic_sm_closeapp));
        gridviewitems.add(new NineGridItemBean(getAppContext().getString(R.string.aty_smhome_ngtitle_rootsys), NineGridItemType.Function, "fun.rootsys", R.drawable.ic_sm_root));
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
                                    dialog_confirm.getTipsImage().setVisibility(View.GONE);
                                    dialog_confirm.getBtnSure().setTag("fun.closeapp");
                                    dialog_confirm.getTipsText().setText(getAppContext().getString(R.string.aty_smhome_confrimtips_closeapp));
                                    dialog_confirm.show();
                                    break;
                                case "fun.rootsys":
                                    dialog_confirm.getTipsImage().setVisibility(View.GONE);
                                    dialog_confirm.getBtnSure().setTag("fun.rootsys");
                                    dialog_confirm.getTipsText().setText(getAppContext().getString(R.string.aty_smhome_confrimtips_rootsys));
                                    dialog_confirm.show();
                                    break;
                                case "fun.exitmanager":
                                    dialog_confirm.getTipsImage().setVisibility(View.GONE);
                                    dialog_confirm.getBtnSure().setTag("fun.exitmanager");
                                    dialog_confirm.getTipsText().setText(getAppContext().getString(R.string.aty_smhome_confrimtips_exitmanager));
                                    dialog_confirm.show();
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
        if (dialog_confirm != null) {
            dialog_confirm.cancel();
        }
    }
}
