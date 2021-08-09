package com.uplink.selfstore.activity;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.CabinetAdapter;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.CabinetLayoutUtil;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.CabinetBean;
import com.uplink.selfstore.model.api.ReplenishGetPlanDetailResultBean;
import com.uplink.selfstore.model.api.ReplenishPlanBean;
import com.uplink.selfstore.model.api.ReplenishSlotBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.ui.dialog.CustomDialogConfirm;
import com.uplink.selfstore.ui.dialog.CustomDialogReplenish;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;
import com.uplink.selfstore.utils.StringUtil;


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
    private int cur_Cabinet_Position = 0;
    private List<CabinetBean> cabinets=new ArrayList<>();
    private TextView tv_CabinetName;
    private TextView tv_PlanCumCode;
    private ReplenishPlanBean replenishPlan;
    private ReplenishGetPlanDetailResultBean planDetailResult=null;
    private CustomDialogConfirm dialog_Confirm;
    private Button btn_Handle;
    private Button btn_GoBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smreplenishplandetail);

        replenishPlan=(ReplenishPlanBean)getIntent().getSerializableExtra("replenishPlan");

        setNavTtile(this.getResources().getString(R.string.aty_smreplenishplandetail_navtitle));

        setNavGoBackBtnVisible(true);

        initView();
        initEvent();
        initData();
    }

    private void initView() {
        tl_Slots = (TableLayout) findViewById(R.id.tl_Slots);
        tv_CabinetName= (TextView) findViewById(R.id.tv_CabinetName);
        tv_PlanCumCode= (TextView) findViewById(R.id.tv_PlanCumCode);
        lv_Cabinets = (ListView) findViewById(R.id.lv_Cabinets);
        btn_Handle  = (Button) findViewById(R.id.btn_Handle);
        btn_GoBack = (Button) findViewById(R.id.btn_GoBack);

        dialog_Confirm = new CustomDialogConfirm(SmReplenishPlanDetailActivity.this, "确定要处理该补货计划单:"+replenishPlan.getPlanCumCode()+"？", true);
        dialog_Confirm.setTipsImageDrawable(ContextCompat.getDrawable(SmReplenishPlanDetailActivity.this, (R.drawable.dialog_icon_warn)));
        dialog_Confirm.setOnClickListener(new CustomDialogConfirm.OnClickListener() {
            @Override
            public void onSure() {

                if (dialog_Confirm != null) {
                    dialog_Confirm.hide();
                }

                Map<String, Object> params = new HashMap<>();

                try {


                    params.put("deviceId", getDevice().getDeviceId() + "");
                    params.put("planDeviceId", replenishPlan.getId());
                    JSONArray json_Slots = new JSONArray();
                    HashMap<String, CabinetBean> l_Cabinets = planDetailResult.getCabinets();

                    for (Map.Entry<String, CabinetBean> entry : l_Cabinets.entrySet()) {

                        HashMap<String, ReplenishSlotBean> l_Slots = entry.getValue().getRshSlots();

                        for (Map.Entry<String, ReplenishSlotBean> entry2 : l_Slots.entrySet()) {

                            ReplenishSlotBean l_Slot = entry2.getValue();

                            if (l_Slot.isPlanRsh()) {
                                JSONObject json_Slot = new JSONObject();
                                json_Slot.put("slotId", l_Slot.getSlotId());
                                json_Slot.put("cabinetId", l_Slot.getCabinetId());
                                json_Slot.put("realRshQuantity", l_Slot.getRealRshQuantity());
                                json_Slots.put(json_Slot);
                            }
                        }
                    }

                    params.put("rshSlots", json_Slots);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                postByMy(SmReplenishPlanDetailActivity.this, Config.URL.replenish_ConfirmReplenish, params, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {

                        ApiResultBean<Object> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<Object>>() {
                        });

                        if (rt.getResult() == Result.SUCCESS) {
                            dialog_Confirm.setTipsImageDrawable(ContextCompat.getDrawable(SmReplenishPlanDetailActivity.this, (R.drawable.dialog_icon_warn)));
                            dialog_Confirm.setCloseVisibility(View.GONE);
                            dialog_Confirm.setCancleVisibility(View.GONE);
                            dialog_Confirm.setTipsText("处理完成，返回主界面");
                            dialog_Confirm.setOnClickListener(new CustomDialogConfirm.OnClickListener() {
                                @Override
                                public void onSure() {
                                    dialog_Confirm.hide();
                                    finish();
                                }

                                @Override
                                public void onCancle() {

                                }
                            });
                            dialog_Confirm.show();

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

            @Override
            public void onCancle() {
                dialog_Confirm.hide();
            }
        });
    }

    private void initEvent() {
        btn_Handle.setOnClickListener(this);
        btn_GoBack.setOnClickListener(this);
        lv_Cabinets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                cur_Cabinet_Position = position;
                loadCabinetSlots();
            }
        });
    }

    private void initData() {
        getPlanDetail();
    }

    public void loadCabinetSlots() {

        if (planDetailResult == null)
            return;

        tv_PlanCumCode.setText(replenishPlan.getPlanCumCode());

        if (cabinets == null)
            return;

        showLoading(SmReplenishPlanDetailActivity.this);

        CabinetAdapter list_cabinet_adapter = new CabinetAdapter(getAppContext(), cabinets, cur_Cabinet_Position);
        lv_Cabinets.setAdapter(list_cabinet_adapter);

        cur_Cabinet = cabinets.get(cur_Cabinet_Position);


        tv_CabinetName.setText(cur_Cabinet.getName() + "(" + cur_Cabinet.getCabinetId() + ")");


        drawsCabinetLayout(cur_Cabinet.getCabinetId(), cur_Cabinet.getRowColLayout(), cur_Cabinet.getRshSlots());

        hideLoading(SmReplenishPlanDetailActivity.this);
    }


    public void drawsCabinetLayout(String cabinetId, String json_layout, HashMap<String, ReplenishSlotBean> slots) {

        this.cur_Cabinet.setRowColLayout(json_layout);

        if (StringUtil.isEmptyNotNull(json_layout))
            return;

        if (slots == null) {
            slots = new HashMap<>();
        }

        com.alibaba.fastjson.JSONObject layout_keys = null;
        try {
            layout_keys = JSON.parseObject(json_layout);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (layout_keys == null)
            return;

        if (!layout_keys.containsKey("rows"))
            return;


        List<List<String>> rows = new ArrayList<>();
        if (cabinetId.contains("ds")) {
            com.alibaba.fastjson.JSONArray arr = layout_keys.getJSONArray("rows");
            int[] l_rows = new int[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                l_rows[i] = (int) arr.get(i);
            }
            rows = CabinetLayoutUtil.getRowsByDs(l_rows);
        } else if (cabinetId.contains("zs")) {
            rows = layout_keys.getObject("rows", new TypeReference<List<List<String>>>() {
            });
        }


        tl_Slots.removeAllViews();
        //全部列自动填充空白处
        tl_Slots.setStretchAllColumns(true);

        //生成X行，Y列的表格

        for (int i = 0; i < rows.size(); i++) {

            TableRow tableRow = new TableRow(SmReplenishPlanDetailActivity.this);

            List<String> cols = rows.get(i);

            for (int j = 0; j < cols.size(); j++) {

                String col = cols.get(j);

                String[] col_Prams = col.split("-");

                String slot_Id = col;
                String slot_Name = "";
                String slot_NoUse = "0";
                if (cabinetId.contains("ds")) {
                    slot_Name = col_Prams[2];
                } else if (cabinetId.contains("zs")) {
                    slot_Name = col_Prams[2];
                    slot_NoUse = col_Prams[3];
                }

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
                    slot.setSlotName(slot_Name);
                    slot.setSlotId(slot_Id);
                    slots.put(slot_Id, slot);
                } else {
                    slot.setSlotName(slot_Name);
                }

                if (slot.getSkuId() == null) {
                    if (slot_NoUse.equals("0")) {
                        txt_name.setText(R.string.tips_noproduct);
                    } else {
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

                if (slot_NoUse.equals("0")) {
                    if(slot.isPlanRsh()) {
                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ReplenishSlotBean l_Slot = (ReplenishSlotBean) v.getTag();
                                if (dialog_Replenish == null) {
                                    dialog_Replenish = new CustomDialogReplenish(SmReplenishPlanDetailActivity.this);
                                }
                                dialog_Replenish.setData(l_Slot);
                                dialog_Replenish.setOnClickListener(new CustomDialogReplenish.OnClickListener() {
                                    @Override
                                    public void onSave(ReplenishSlotBean bean) {
                                        planDetailResult.getCabinets().get(bean.getCabinetId()).getRshSlots().put(bean.getSlotId(), bean);
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

//    public void drawsCabinetSlotsByDS(String json_layout, HashMap<String, ReplenishSlotBean> slots) {
//
//
//        DSCabRowColLayoutBean dSCabRowColLayout = JSON.parseObject(json_layout, new TypeReference<DSCabRowColLayoutBean>() {
//        });
//
//
//        int[] rowColLayout = dSCabRowColLayout.getRows();
//
//        int rowLength = rowColLayout.length;
//
//        //清除表格所有行
//        tl_Slots.removeAllViews();
//        //全部列自动填充空白处
//        tl_Slots.setStretchAllColumns(true);
//        //生成X行，Y列的表格
//        int slot_Name=1;
//        for (int i = rowLength; i > 0; i--) {
//            TableRow tableRow = new TableRow(SmReplenishPlanDetailActivity.this);
//            int colLength = rowColLayout[i - 1];
//
//            if(colLength==0){
//
//                final View convertView = LayoutInflater.from(SmReplenishPlanDetailActivity.this).inflate(R.layout.item_list_sku_tmp2, tableRow, false);
//                TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
//                convertView.setVisibility(View.INVISIBLE);
//                txt_name.setText("该行没有格数");
//
//            }
//            else {
//
//                for (int j = colLength - 1; j >= 0; j--) {
//                    //tv用于显示
//                    final View convertView = LayoutInflater.from(SmReplenishPlanDetailActivity.this).inflate(R.layout.item_list_sku_tmp2, tableRow, false);
//                    LinearLayout tmp_wapper = ViewHolder.get(convertView, R.id.tmp_wapper);
//                    TextView txt_SlotId = ViewHolder.get(convertView, R.id.txt_SlotId);
//                    TextView txt_SlotName = ViewHolder.get(convertView, R.id.txt_SlotName);
//
//                    TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
//                    TextView txt_sellQuantity = ViewHolder.get(convertView, R.id.txt_sellQuantity);
//                    TextView txt_lockQuantity = ViewHolder.get(convertView, R.id.txt_lockQuantity);
//                    TextView txt_sumQuantity = ViewHolder.get(convertView, R.id.txt_sumQuantity);
//                    ImageView img_main = ViewHolder.get(convertView, R.id.img_main);
//
//
//                    String slotId = (i - 1) + "-" + j+"-"+slot_Name;
//
//                    txt_SlotId.setText(slotId);
//                    txt_SlotName.setText(String.valueOf(slot_Name));
//
//
//                    ReplenishSlotBean slot = null;
//
//                    if (slots.size() > 0) {
//                        slot = slots.get(slotId);
//                    }
//
//
//                    if (slot == null) {
//                        slot = new ReplenishSlotBean();
//                        slot.setSlotId(slotId);
//                        slot.setSlotName(slot_Name+"");
//                        slots.put(slotId, slot);
//                    }
//                    else
//                    {
//                        slot.setSlotName(slot_Name+"");
//                    }
//
//                    if (slot.getSkuId() == null) {
//                        txt_name.setText(R.string.tips_noproduct);
//                        txt_sellQuantity.setText("0");
//                        txt_lockQuantity.setText("0");
//                        txt_sumQuantity.setText("0");
//
//                    } else {
//                        txt_name.setText(slot.getSkuName());
//                        txt_sellQuantity.setText(String.valueOf(slot.getSellQuantity()));
//                        txt_lockQuantity.setText(String.valueOf(slot.getLockQuantity()));
//                        txt_sumQuantity.setText(String.valueOf(slot.getSumQuantity()));
//
//                        CommonUtil.loadImageFromUrl(SmReplenishPlanDetailActivity.this, img_main, slot.getSkuMainImgUrl());
//
//                        if (slot.isPlanRsh()) {
//                            GradientDrawable drawable = new GradientDrawable();
//                            drawable.setCornerRadius(0);
//                            drawable.setStroke(1, getResources().getColor(R.color.lockQuantity));
//                            tmp_wapper.setBackgroundDrawable(drawable);
//                        }
//                    }
//
//                    convertView.setTag(slot);
//
//                    if(slot.isPlanRsh()) {
//                        convertView.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                ReplenishSlotBean l_Slot = (ReplenishSlotBean) v.getTag();
//                                if(dialog_Replenish==null) {
//                                    dialog_Replenish = new CustomDialogReplenish(SmReplenishPlanDetailActivity.this);
//                                }
//                                dialog_Replenish.setData(l_Slot);
//                                dialog_Replenish.setOnClickListener(new CustomDialogReplenish.OnClickListener() {
//                                    @Override
//                                    public void onSave(ReplenishSlotBean bean) {
//                                        planDetailResult.getCabinets().get(bean.getCabinetId()).getRshSlots().put(bean.getSlotId(),bean);
//                                        loadCabinetSlots();
//                                        dialog_Replenish.hide();
//                                    }
//                                });
//                                dialog_Replenish.show();
//                            }
//                        });
//                    }
//
//                    slot_Name++;
//
//                    tableRow.addView(convertView, new TableRow.LayoutParams(MP, WC, 1));
//                }
//            }
//
//            tl_Slots.addView(tableRow, new TableLayout.LayoutParams(MP, WC, 1));
//
//        }
//    }
//
//    public void drawsCabinetSlotsByZS(String json_layout, HashMap<String, ReplenishSlotBean> slots) {
//
//        ZSCabRowColLayoutBean layout = JSON.parseObject(json_layout, new TypeReference<ZSCabRowColLayoutBean>() {});
//
//        if(layout==null)
//            return;
//
//        //清除表格所有行
//        tl_Slots.removeAllViews();
//        //全部列自动填充空白处
//        tl_Slots.setStretchAllColumns(true);
//
//        //生成X行，Y列的表格
//
//
//        List<List<String>> rows=layout.getRows();
//
//        for (int i = 0; i <rows.size(); i++) {
//
//            TableRow tableRow = new TableRow(SmReplenishPlanDetailActivity.this);
//
//            List<String> cols=rows.get(i);
//
//            for (int j = 0; j < cols.size(); j++) {
//
//                String slot_Id =cols.get(j);
//
//                String[] slot_Prams=slot_Id.split("-");
//
//                String slot_Plate=slot_Prams[1];
//                String slot_Name=slot_Prams[2];
//                String slot_NoUse=slot_Prams[3];
//
//                final View convertView = LayoutInflater.from(SmReplenishPlanDetailActivity.this).inflate(R.layout.item_list_sku_tmp2, tableRow, false);
//
//                LinearLayout tmp_wapper = ViewHolder.get(convertView, R.id.tmp_wapper);
//
//                TextView txt_SlotId = ViewHolder.get(convertView, R.id.txt_SlotId);
//                TextView txt_SlotName = ViewHolder.get(convertView, R.id.txt_SlotName);
//                TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
//                TextView txt_sellQuantity = ViewHolder.get(convertView, R.id.txt_sellQuantity);
//                TextView txt_lockQuantity = ViewHolder.get(convertView, R.id.txt_lockQuantity);
//                TextView txt_sumQuantity = ViewHolder.get(convertView, R.id.txt_sumQuantity);
//                ImageView img_main = ViewHolder.get(convertView, R.id.img_main);
//
//                txt_SlotId.setText(slot_Id);
//                txt_SlotName.setText(slot_Name);
//
//                ReplenishSlotBean slot = null;
//
//                if (slots.size() > 0) {
//                    slot = slots.get(slot_Id);
//                }
//
//                if (slot == null) {
//                    slot = new ReplenishSlotBean();
//                    slot.setSlotId(slot_Id);
//                    slot.setSlotName(slot_Name);
//                    slots.put(slot_Id, slot);
//                }
//                else {
//                    slot.setSlotName(slot_Name);
//                }
//
//                if (slot.getSkuId() == null) {
//                    if(slot_NoUse.equals("0")) {
//                        txt_name.setText(R.string.tips_noproduct);
//                    }
//                    else {
//                        convertView.setVisibility(View.INVISIBLE);
//                        txt_name.setText(R.string.tips_nocanuse);
//                    }
//
//                    txt_sellQuantity.setText("0");
//                    txt_lockQuantity.setText("0");
//                    txt_sumQuantity.setText("0");
//
//                } else {
//                    txt_name.setText(slot.getSkuName());
//                    txt_sellQuantity.setText(String.valueOf(slot.getSellQuantity()));
//                    txt_lockQuantity.setText(String.valueOf(slot.getLockQuantity()));
//                    txt_sumQuantity.setText(String.valueOf(slot.getSumQuantity()));
//
//                    CommonUtil.loadImageFromUrl(SmReplenishPlanDetailActivity.this, img_main, slot.getSkuMainImgUrl());
//
//                    if (slot.isPlanRsh()) {
//                        GradientDrawable drawable = new GradientDrawable();
//                        drawable.setCornerRadius(0);
//                        drawable.setStroke(1, getResources().getColor(R.color.lockQuantity));
//                        tmp_wapper.setBackgroundDrawable(drawable);
//                    }
//                }
//
//                convertView.setTag(slot);
//
//                if(slot_NoUse.equals("0")) {
//                    if(slot.isPlanRsh()) {
//                        convertView.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                ReplenishSlotBean l_Slot = (ReplenishSlotBean) v.getTag();
//                                dialog_Replenish = new CustomDialogReplenish(SmReplenishPlanDetailActivity.this);
//                                dialog_Replenish.setData(l_Slot);
//                                dialog_Replenish.setOnClickListener(new CustomDialogReplenish.OnClickListener() {
//                                    @Override
//                                    public void onSave(ReplenishSlotBean bean) {
//
//                                        planDetailResult.getCabinets().get(bean.getCabinetId()).getRshSlots().put(bean.getSlotId(),bean);
//                                        loadCabinetSlots();
//                                        dialog_Replenish.hide();
//
//                                    }
//                                });
//                                dialog_Replenish.show();
//                            }
//                        });
//                    }
//                }
//
//                tableRow.addView(convertView, new TableRow.LayoutParams(MP, WC, 1));
//            }
//
//            tl_Slots.addView(tableRow, new TableLayout.LayoutParams(MP, WC, 1));
//
//        }
//    }

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

        if(dialog_Confirm!=null){
            dialog_Confirm.cancel();
            dialog_Confirm.dismiss();
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
                case R.id.btn_GoBack:
                    finish();
                    break;
                case R.id.btn_Handle:
                    dialog_Confirm.show();
                    break;
            }
        }
    }


    private void getPlanDetail() {

        Map<String, Object> params = new HashMap<>();

        params.put("deviceId", getDevice().getDeviceId());
        params.put("planDeviceId",replenishPlan.getId());

        postByMy(SmReplenishPlanDetailActivity.this, Config.URL.replenish_GetPlanDetail, params, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<ReplenishGetPlanDetailResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<ReplenishGetPlanDetailResultBean>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {
                    planDetailResult= rt.getData();

                    if(planDetailResult.getCabinets().size()==0) {
                        lv_Cabinets.setVisibility(View.GONE);
                    }

                    HashMap<String, CabinetBean> l_Cabinets = planDetailResult.getCabinets();
                    if (l_Cabinets != null) {

                        cabinets = new ArrayList<>(l_Cabinets.values());

                        Collections.sort(cabinets, new Comparator<CabinetBean>() {
                            @Override
                            public int compare(CabinetBean t1, CabinetBean t2) {
                                return t2.getPriority() - t1.getPriority();
                            }
                        });
                    }

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
