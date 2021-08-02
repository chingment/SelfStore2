package com.uplink.selfstore.activity;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.CabinetAdapter;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.DSCabRowColLayoutBean;
import com.uplink.selfstore.model.ZSCabRowColLayoutBean;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.CabinetBean;
import com.uplink.selfstore.model.api.ReplenishCabinetBean;
import com.uplink.selfstore.model.api.ReplenishGetPlanDetailResultBean;
import com.uplink.selfstore.model.api.ReplenishSlotBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.SlotBean;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.ui.dialog.CustomDialogReplenish;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmReplenishPlanDetailActivity extends SwipeBackActivity implements View.OnClickListener {

    private static final String TAG = "SmReplenishPlanDetailActivity";
    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    private final int MP = ViewGroup.LayoutParams.MATCH_PARENT;

    private TableLayout tl_Slots;
    private CustomDialogReplenish dialog_Replenish;
    private ListView lv_Cabinets;
    private CabinetBean cur_Cabinet =null;//当前机柜信息
    private static int cur_Cabinet_Position = 0;
    private List<CabinetBean> cabinets=new ArrayList<>();
    private TextView tv_CabinetName;
    private String planDeviceId="";
    private ReplenishGetPlanDetailResultBean planDetailResult=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smreplenishplandetail);

        planDeviceId=getIntent().getStringExtra("planDeviceId");

        setNavTtile(this.getResources().getString(R.string.aty_smreplenishplandetail_navtitle));

        setNavGoBackBtnVisible(true);

        HashMap<String, CabinetBean> l_Cabinets = getDevice().getCabinets();
        if (l_Cabinets != null) {

            cabinets = new ArrayList<>(l_Cabinets.values());

            Collections.sort(cabinets, new Comparator<CabinetBean>() {
                @Override
                public int compare(CabinetBean t1, CabinetBean t2) {
                    return t2.getPriority() - t1.getPriority();
                }
            });
        }

        initView();
        initEvent();
        initData();
    }

    private void initView() {
        tl_Slots = (TableLayout) findViewById(R.id.tl_Slots);
        tv_CabinetName= (TextView) findViewById(R.id.txt_CabinetName);
        lv_Cabinets = (ListView) findViewById(R.id.lv_Cabinets);
    }

    private void initEvent() {
        lv_Cabinets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                cur_Cabinet_Position = position;
                cur_Cabinet = cabinets.get(cur_Cabinet_Position);
                CabinetAdapter list_cabinet_adapter = new CabinetAdapter(getAppContext(), cabinets, cur_Cabinet_Position);
                lv_Cabinets.setAdapter(list_cabinet_adapter);
                loadCabinetSlots();
            }
        });
    }

    private void initData() {

        if (cabinets == null)
            return;
        if (cabinets.size() == 0)
            return;
        if(cabinets.size()==1) {
            lv_Cabinets.setVisibility(View.GONE);
        }

        cur_Cabinet = cabinets.get(cur_Cabinet_Position);

        if (cur_Cabinet == null)
            return;

        CabinetAdapter list_cabinet_adapter = new CabinetAdapter(getAppContext(), cabinets, cur_Cabinet_Position);
        lv_Cabinets.setAdapter(list_cabinet_adapter);

        getPlanDetail();

    }

    public void loadCabinetSlots() {

        if (planDetailResult == null)
            return;

        HashMap<String, ReplenishCabinetBean> cabinets = planDetailResult.getCabinets();

        if (cabinets == null)
            return;

        if (!cabinets.containsKey(cur_Cabinet.getCabinetId()))
            return;

        tv_CabinetName.setText(cur_Cabinet.getName() + "(" + cur_Cabinet.getCabinetId() + ")");

        ReplenishCabinetBean cabinet = cabinets.get(cur_Cabinet.getCabinetId());

        if (cabinet == null)
            return;

        switch (cur_Cabinet.getModelNo()) {
            case "dsx01":
                drawsCabinetSlotsByDS(cabinet.getRowColLayout(), cabinet.getSlots());
                break;
            case "zsx01":
                drawsCabinetSlotsByZS(cabinet.getRowColLayout(), cabinet.getSlots());
                break;
        }
    }

    public void drawsCabinetSlotsByDS(String json_layout, HashMap<String, ReplenishSlotBean> slots) {


        DSCabRowColLayoutBean dSCabRowColLayout = JSON.parseObject(json_layout, new TypeReference<DSCabRowColLayoutBean>() {
        });


        int[] rowColLayout = dSCabRowColLayout.getRows();

        int rowLength = rowColLayout.length;

        //清除表格所有行
        tl_Slots.removeAllViews();
        //全部列自动填充空白处
        tl_Slots.setStretchAllColumns(true);
        //生成X行，Y列的表格
        int slot_Name=1;
        for (int i = rowLength; i > 0; i--) {
            TableRow tableRow = new TableRow(SmReplenishPlanDetailActivity.this);
            int colLength = rowColLayout[i - 1];

            if(colLength==0){

                final View convertView = LayoutInflater.from(SmReplenishPlanDetailActivity.this).inflate(R.layout.item_list_sku_tmp2, tableRow, false);
                TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
                convertView.setVisibility(View.INVISIBLE);
                txt_name.setText("该行没有格数");

            }
            else {

                for (int j = colLength - 1; j >= 0; j--) {
                    //tv用于显示
                    final View convertView = LayoutInflater.from(SmReplenishPlanDetailActivity.this).inflate(R.layout.item_list_sku_tmp2, tableRow, false);
                    LinearLayout tmp_wapper = ViewHolder.get(convertView, R.id.tmp_wapper);
                    TextView txt_SlotId = ViewHolder.get(convertView, R.id.txt_SlotId);
                    TextView txt_SlotName = ViewHolder.get(convertView, R.id.txt_SlotName);

                    TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
                    TextView txt_sellQuantity = ViewHolder.get(convertView, R.id.txt_sellQuantity);
                    TextView txt_lockQuantity = ViewHolder.get(convertView, R.id.txt_lockQuantity);
                    TextView txt_sumQuantity = ViewHolder.get(convertView, R.id.txt_sumQuantity);
                    ImageView img_main = ViewHolder.get(convertView, R.id.img_main);


                    String slotId = (i - 1) + "-" + j+"-"+slot_Name;

                    txt_SlotId.setText(slotId);
                    txt_SlotName.setText(String.valueOf(slot_Name));


                    ReplenishSlotBean slot = null;

                    if (slots.size() > 0) {
                        slot = slots.get(slotId);
                    }


                    if (slot == null) {
                        slot = new ReplenishSlotBean();
                        slot.setSlotId(slotId);
                        slot.setSlotName(slot_Name+"");
                        slots.put(slotId, slot);
                    }
                    else
                    {
                        slot.setSlotName(slot_Name+"");
                    }

                    if (slot.getSkuId() == null) {
                        txt_name.setText(R.string.tips_noproduct);
                        txt_sellQuantity.setText("0");
                        txt_lockQuantity.setText("0");
                        txt_sumQuantity.setText("0");

                    } else {
                        txt_name.setText(slot.getSkuName());
                        txt_sellQuantity.setText(String.valueOf(slot.getSellQuantity()));
                        txt_lockQuantity.setText(String.valueOf(slot.getLockQuantity()));
                        txt_sumQuantity.setText(String.valueOf(slot.getSumQuantity()));

                        CommonUtil.loadImageFromUrl(SmReplenishPlanDetailActivity.this, img_main, slot.getSkuMainImgUrl());

                        if (slot.isPlanRsh()) {
                            GradientDrawable drawable = new GradientDrawable();
                            drawable.setCornerRadius(0);
                            drawable.setStroke(1, getResources().getColor(R.color.lockQuantity));
                            tmp_wapper.setBackgroundDrawable(drawable);
                        }
                    }

                    convertView.setTag(slot);

                    if(slot.isPlanRsh()) {
                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ReplenishSlotBean l_Slot = (ReplenishSlotBean) v.getTag();
                                dialog_Replenish = new CustomDialogReplenish(SmReplenishPlanDetailActivity.this);
                                dialog_Replenish.setData(l_Slot);
                                dialog_Replenish.setOnClickListener(new CustomDialogReplenish.OnClickListener() {
                                    @Override
                                    public void onSave(ReplenishSlotBean bean) {
                                        planDetailResult.getCabinets().get(bean.getCabinetId()).getSlots().put(bean.getSlotId(),bean);
                                        loadCabinetSlots();
                                        dialog_Replenish.hide();
                                    }
                                });
                                dialog_Replenish.show();
                            }
                        });
                    }

                    slot_Name++;

                    tableRow.addView(convertView, new TableRow.LayoutParams(MP, WC, 1));
                }
            }

            tl_Slots.addView(tableRow, new TableLayout.LayoutParams(MP, WC, 1));

        }
    }

    public void drawsCabinetSlotsByZS(String json_layout, HashMap<String, ReplenishSlotBean> slots) {

        ZSCabRowColLayoutBean layout = JSON.parseObject(json_layout, new TypeReference<ZSCabRowColLayoutBean>() {});

        if(layout==null)
            return;

        //清除表格所有行
        tl_Slots.removeAllViews();
        //全部列自动填充空白处
        tl_Slots.setStretchAllColumns(true);

        //生成X行，Y列的表格


        List<List<String>> rows=layout.getRows();

        for (int i = 0; i <rows.size(); i++) {

            TableRow tableRow = new TableRow(SmReplenishPlanDetailActivity.this);

            List<String> cols=rows.get(i);

            for (int j = 0; j < cols.size(); j++) {

                String slot_Id =cols.get(j);

                String[] slot_Prams=slot_Id.split("-");

                String slot_Plate=slot_Prams[1];
                String slot_Name=slot_Prams[2];
                String slot_NoUse=slot_Prams[3];

                final View convertView = LayoutInflater.from(SmReplenishPlanDetailActivity.this).inflate(R.layout.item_list_sku_tmp2, tableRow, false);

                LinearLayout tmp_wapper = ViewHolder.get(convertView, R.id.tmp_wapper);

                TextView txt_SlotId = ViewHolder.get(convertView, R.id.txt_SlotId);
                TextView txt_SlotName = ViewHolder.get(convertView, R.id.txt_SlotName);
                TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
                TextView txt_sellQuantity = ViewHolder.get(convertView, R.id.txt_sellQuantity);
                TextView txt_lockQuantity = ViewHolder.get(convertView, R.id.txt_lockQuantity);
                TextView txt_sumQuantity = ViewHolder.get(convertView, R.id.txt_sumQuantity);
                ImageView img_main = ViewHolder.get(convertView, R.id.img_main);

                txt_SlotId.setText(slot_Id);
                txt_SlotName.setText(slot_Name);

                ReplenishSlotBean slot = null;

                if (slots.size() > 0) {
                    slot = slots.get(slot_Id);
                }

                if (slot == null) {
                    slot = new ReplenishSlotBean();
                    slot.setSlotId(slot_Id);
                    slot.setSlotName(slot_Name);
                    slots.put(slot_Id, slot);
                }
                else {
                    slot.setSlotName(slot_Name);
                }

                if (slot.getSkuId() == null) {
                    if(slot_NoUse.equals("0")) {
                        txt_name.setText(R.string.tips_noproduct);
                    }
                    else {
                        convertView.setVisibility(View.INVISIBLE);
                        txt_name.setText(R.string.tips_nocanuse);
                    }

                    txt_sellQuantity.setText("0");
                    txt_lockQuantity.setText("0");
                    txt_sumQuantity.setText("0");

                } else {
                    txt_name.setText(slot.getSkuName());
                    txt_sellQuantity.setText(String.valueOf(slot.getSellQuantity()));
                    txt_lockQuantity.setText(String.valueOf(slot.getLockQuantity()));
                    txt_sumQuantity.setText(String.valueOf(slot.getSumQuantity()));

                    CommonUtil.loadImageFromUrl(SmReplenishPlanDetailActivity.this, img_main, slot.getSkuMainImgUrl());

                    if (slot.isPlanRsh()) {
                        GradientDrawable drawable = new GradientDrawable();
                        drawable.setCornerRadius(0);
                        drawable.setStroke(1, getResources().getColor(R.color.lockQuantity));
                        tmp_wapper.setBackgroundDrawable(drawable);
                    }
                }

                convertView.setTag(slot);

                if(slot_NoUse.equals("0")) {
                    if(slot.isPlanRsh()) {
                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ReplenishSlotBean l_Slot = (ReplenishSlotBean) v.getTag();
                                dialog_Replenish = new CustomDialogReplenish(SmReplenishPlanDetailActivity.this);
                                dialog_Replenish.setData(l_Slot);
                                dialog_Replenish.setOnClickListener(new CustomDialogReplenish.OnClickListener() {
                                    @Override
                                    public void onSave(ReplenishSlotBean bean) {

                                        planDetailResult.getCabinets().get(bean.getCabinetId()).getSlots().put(bean.getSlotId(),bean);
                                        loadCabinetSlots();
                                        dialog_Replenish.hide();

                                    }
                                });
                                dialog_Replenish.show();
                            }
                        });
                    }
                }

                tableRow.addView(convertView, new TableRow.LayoutParams(MP, WC, 1));
            }

            tl_Slots.addView(tableRow, new TableLayout.LayoutParams(MP, WC, 1));

        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        if(dialog_Replenish!=null) {
            dialog_Replenish.cancel();
            dialog_Replenish.dismiss();
        }

        if(tl_Slots!=null) {
            tl_Slots.removeAllViews();
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.nav_back:
                    finish();
                    break;
                default:
                    break;
            }
        }
    }


    private void getPlanDetail() {

        Map<String, Object> params = new HashMap<>();

        params.put("deviceId", getDevice().getDeviceId());
        params.put("planDeviceId",planDeviceId);

        postByMy(SmReplenishPlanDetailActivity.this, Config.URL.replenish_GetPlanDetail, params, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<ReplenishGetPlanDetailResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<ReplenishGetPlanDetailResultBean>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {
                    planDetailResult= rt.getData();
                    loadCabinetSlots();
                } else {
                    showToast(rt.getMessage());
                }
            }

            @Override
            public void onFailure(String msg, Exception e) {
                showToast(msg);
            }
        });
    }
}
