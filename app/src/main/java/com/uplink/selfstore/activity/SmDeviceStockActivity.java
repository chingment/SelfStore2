package com.uplink.selfstore.activity;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.CabinetAdapter;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByZS;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.model.DSCabRowColLayoutBean;
import com.uplink.selfstore.model.ScanSlotResult;
import com.uplink.selfstore.model.ZSCabRowColLayoutBean;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.CabinetBean;
import com.uplink.selfstore.model.api.DeviceSlotsResultBean;
import com.uplink.selfstore.model.api.PickupSkuBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.SlotBean;
import com.uplink.selfstore.own.AppLogcatManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.service.UsbService;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.ui.dialog.CustomDialogConfirm;
import com.uplink.selfstore.ui.dialog.CustomLoadingDialog;
import com.uplink.selfstore.ui.dialog.CustomPickupAutoTestDialog;
import com.uplink.selfstore.ui.dialog.CustomDialogSlotEdit;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.InterUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;
import com.uplink.selfstore.utils.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SmDeviceStockActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "SmDeviceStockActivity";
    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    private final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    private TableLayout tl_Slots;
    private CustomDialogSlotEdit dialog_SlotEdit;
    private CustomPickupAutoTestDialog dialog_PickupAutoTest;
    private ListView lv_Cabinets;
    private CabinetBean cur_Cabinet =null;//当前机柜信息
    private static int cur_Cabinet_Position = 0;
    private HashMap<String, SlotBean> slots = null;//机柜货道信息
    private List<CabinetBean> cabinets=new ArrayList<>();
    private Button btn_ScanSlots;
    private Button btn_RefreshStock;
    private Button btn_AutoTest;
    private TextView tv_CabinetName;
    private CabinetCtrlByDS cabinetCtrlByDS;
    private CabinetCtrlByZS cabinetCtrlByZS;
    private CustomLoadingDialog dialog_Running;
    private CustomDialogConfirm dialog_Confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smdevicestock);

        setNavTtile(this.getResources().getString(R.string.aty_smdevicestock_navtitle));

        setNavGoBackBtnVisible(true);

        setScanCtrlHandler(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case UsbService.MESSAGE_FROM_SERIAL_PORT:
                        String data = (String) msg.obj;
                        LogUtil.d(TAG, "扫描数据：" + data);
                        if (!StringUtil.isEmptyNotNull(data)) {
                            data = data.trim();
                            if (dialog_SlotEdit != null) {
                                dialog_SlotEdit.searchSkus(data);
                            }
                        }
                        break;
                }
                return true;
            }
        }));

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

        cabinetCtrlByDS = CabinetCtrlByDS.getInstance();
        cabinetCtrlByDS.setScanSlotHandler(new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        Bundle bundle;
                        bundle = msg.getData();
                        int status = bundle.getInt("status");
                        String message = bundle.getString("message");
                        ScanSlotResult result = null;
                        if (bundle.getSerializable("result") != null) {
                            result = (ScanSlotResult) bundle.getSerializable("result");
                        }
                        switch (status) {
                            case 1:
                                showToast(message);
                                if (dialog_Running != null) {
                                    dialog_Running.hide();
                                }
                                break;
                            case 2://启动就绪
                                scanSlotsEventNotify(2000, "启动就绪");
                                if (dialog_Running != null) {
                                    dialog_Running.setProgressText(message);
                                    dialog_Running.show();
                                }
                                break;
                            case 3://扫描中
                                if (dialog_Running != null) {
                                    dialog_Running.setProgressText(message);
                                }
                                break;
                            case 4://扫描成功
                                if (result != null) {
                                    scanSlotsEventNotify(4000, "扫描成功,结果:" + InterUtil.arrayTransformString(result.rowColLayout, ",") + ",用时:" + result.getUseTime());
                                    DSCabRowColLayoutBean sSCabRowColLayoutBean = new DSCabRowColLayoutBean();
                                    sSCabRowColLayoutBean.setRows(result.rowColLayout);
                                    String strRowColLayout = JSON.toJSONString(sSCabRowColLayoutBean);
                                    saveCabinetRowColLayout(strRowColLayout);
                                }
                                break;
                            case 5://扫描超时
                                AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS ", "scanslot");
                                scanSlotsEventNotify(5000, "扫描超时");
                                if (dialog_Running != null) {
                                    dialog_Running.hide();
                                }
                                showToast(message);
                                break;
                            case 6://扫描失败
                                AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS ", "scanslot");
                                scanSlotsEventNotify(6000, "扫描失败");
                                if (dialog_Running != null) {
                                    dialog_Running.hide();
                                }
                                showToast(message);
                                break;
                        }
                        return false;
                    }
                })
        );
        cabinetCtrlByDS.connect();

        cabinetCtrlByZS = CabinetCtrlByZS.getInstance();
        cabinetCtrlByZS.connect();

        initView();
        initEvent();
        initData();
    }

    private void initView() {
        tl_Slots = (TableLayout) findViewById(R.id.tl_Slots);
        btn_ScanSlots = (Button) findViewById(R.id.btn_ScanSlots);
        btn_RefreshStock= (Button) findViewById(R.id.btn_RefreshStock);
        btn_AutoTest= (Button) findViewById(R.id.btn_AutoTest);
        tv_CabinetName= (TextView) findViewById(R.id.txt_CabinetName);
        lv_Cabinets = (ListView) findViewById(R.id.lv_Cabinets);

        dialog_Running = new CustomLoadingDialog(SmDeviceStockActivity.this);
        dialog_PickupAutoTest = new CustomPickupAutoTestDialog(SmDeviceStockActivity.this);
        dialog_Confirm = new CustomDialogConfirm(SmDeviceStockActivity.this, "", true);
        dialog_Confirm.setOnClickListener(new CustomDialogConfirm.OnClickListener() {
            @Override
            public void onSure() {
                String tag = dialog_Confirm.getTag().toString();
                switch (tag) {
                    case "fun.scanslots":
                        cabinetCtrlByDS.scanSlot();
                        break;
                    default:
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

    private void initEvent() {
        btn_ScanSlots.setOnClickListener(this);
        btn_RefreshStock.setOnClickListener(this);
        btn_AutoTest.setOnClickListener(this);
        lv_Cabinets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                cur_Cabinet_Position = position;
                loadCabinetData();
            }
        });
    }

    private void initData() {
        loadCabinetData();
    }

    private void loadCabinetData() {
        if (cabinets == null)
            return;
        if (cabinets.size() == 0)
            return;

        if(cabinets.size()==1){
            lv_Cabinets.setVisibility(View.GONE);
        }

        cur_Cabinet = cabinets.get(cur_Cabinet_Position);

        if (cur_Cabinet == null)
            return;

        CabinetAdapter list_cabinet_adapter = new CabinetAdapter(getAppContext(), cabinets, cur_Cabinet_Position);
        lv_Cabinets.setAdapter(list_cabinet_adapter);

        getCabinetSlots();

        tv_CabinetName.setText( cur_Cabinet.getName() + "(" + cur_Cabinet.getCabinetId() + ")");

        switch (cur_Cabinet.getModelNo()) {
            case "dsx01":
                btn_ScanSlots.setVisibility(View.VISIBLE);
                btn_AutoTest.setVisibility(View.VISIBLE);
                break;
            default:
                btn_ScanSlots.setVisibility(View.GONE);
                btn_AutoTest.setVisibility(View.GONE);
                break;
        }
    }

    public void setSlot(SlotBean slot) {

        if(cur_Cabinet==null)
            return;

        if (slot == null)
            return;

        String slotId = slot.getSlotId();

        if (StringUtil.isEmpty(slotId))
            return;

        if (slots == null)
            return;

        if (!slots.containsKey(slotId))
            return;

        SlotBean l_slot = slots.get(slot.getSlotId());
        if(l_slot==null)
            return;

        l_slot.setSlotId(slot.getSlotId());
        l_slot.setStockId(slot.getStockId());
        l_slot.setCabinetId(slot.getCabinetId());
        l_slot.setSkuId(slot.getSkuId());
        l_slot.setSkuCumCode(slot.getSkuCumCode());
        l_slot.setSkuName(slot.getSkuName());
        l_slot.setSkuMainImgUrl(slot.getSkuMainImgUrl());
        l_slot.setSkuSpecDes(slot.getSkuSpecDes());
        l_slot.setOffSell(slot.isOffSell());
        l_slot.setLockQuantity(slot.getLockQuantity());
        l_slot.setSellQuantity(slot.getSellQuantity());
        l_slot.setSumQuantity(slot.getSumQuantity());
        l_slot.setMaxQuantity(slot.getMaxQuantity());
        l_slot.setWarnQuantity(slot.getWarnQuantity());
        l_slot.setHoldQuantity(slot.getHoldQuantity());
        l_slot.setVersion(slot.getVersion());
        l_slot.setCanAlterMaxQuantity(slot.getCanAlterMaxQuantity());

        slots.put(slotId,l_slot);

        switch (cur_Cabinet.getModelNo()) {
            case "dsx01":
                drawsCabinetSlotsByDS(cur_Cabinet.getRowColLayout(), slots);
                break;
            case "zsx01":
                drawsCabinetSlotsByZS(cur_Cabinet.getRowColLayout(), slots);
                break;
        }


    }

    public void drawsCabinetSlotsByDS(String json_layout, HashMap<String, SlotBean> slots) {

        if(cur_Cabinet==null)
            return;

        this.cur_Cabinet.setRowColLayout(json_layout);

        if (slots == null) {
            slots = new HashMap<String, SlotBean>();
        }

        this.slots = slots;


        DSCabRowColLayoutBean dSCabRowColLayout = JSON.parseObject(cur_Cabinet.getRowColLayout(), new TypeReference<DSCabRowColLayoutBean>() {
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
            TableRow tableRow = new TableRow(SmDeviceStockActivity.this);
            int colLength = rowColLayout[i - 1];

            if(colLength==0){

                final View convertView = LayoutInflater.from(SmDeviceStockActivity.this).inflate(R.layout.item_list_sku_tmp2, tableRow, false);
                TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
                convertView.setVisibility(View.INVISIBLE);
                txt_name.setText("该行没有格数");

            }
            else {

                for (int j = colLength - 1; j >= 0; j--) {
                    //tv用于显示
                    final View convertView = LayoutInflater.from(SmDeviceStockActivity.this).inflate(R.layout.item_list_sku_tmp2, tableRow, false);
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


                    SlotBean slot = null;

                    if (slots.size() > 0) {
                        slot = slots.get(slotId);
                    }


                    if (slot == null) {
                        slot = new SlotBean();
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

                        CommonUtil.loadImageFromUrl(SmDeviceStockActivity.this, img_main, slot.getSkuMainImgUrl());

                        if (slot.getLockQuantity() > 0) {
                            GradientDrawable drawable = new GradientDrawable();
                            drawable.setCornerRadius(0);
                            drawable.setStroke(1, getResources().getColor(R.color.lockQuantity));
                            tmp_wapper.setBackgroundDrawable(drawable);
                        }
                    }

                    convertView.setTag(slot);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SlotBean l_Slot = (SlotBean) v.getTag();
                            dialog_SlotEdit=new CustomDialogSlotEdit(SmDeviceStockActivity.this);
                            dialog_SlotEdit.setData(cur_Cabinet,l_Slot);
                            dialog_SlotEdit.clearSearch();
                            dialog_SlotEdit.show();
                        }
                    });

                    slot_Name++;

                    tableRow.addView(convertView, new TableRow.LayoutParams(MP, WC, 1));
                }
            }

            tl_Slots.addView(tableRow, new TableLayout.LayoutParams(MP, WC, 1));

        }
    }

    public void drawsCabinetSlotsByZS(String json_layout, HashMap<String, SlotBean> slots) {

        if(cur_Cabinet==null)
            return;

        this.cur_Cabinet.setRowColLayout(json_layout);

        if (slots == null) {
            slots = new HashMap<String, SlotBean>();
        }

        this.slots = slots;


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

            TableRow tableRow = new TableRow(SmDeviceStockActivity.this);

            List<String> cols=rows.get(i);

            for (int j = 0; j < cols.size(); j++) {

                String slot_Id =cols.get(j);

                String[] slot_Prams=slot_Id.split("-");

                String slot_Plate=slot_Prams[1];
                String slot_Name=slot_Prams[2];
                String slot_NoUse=slot_Prams[3];

                final View convertView = LayoutInflater.from(SmDeviceStockActivity.this).inflate(R.layout.item_list_sku_tmp2, tableRow, false);

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

                SlotBean slot = null;

                if (slots.size() > 0) {
                    slot = slots.get(slot_Id);
                }

                if (slot == null) {
                    slot = new SlotBean();
                    slot.setSlotId(slot_Id);
                    slots.put(slot_Id, slot);
                } else {
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

                    CommonUtil.loadImageFromUrl(SmDeviceStockActivity.this, img_main, slot.getSkuMainImgUrl());

                    if (slot.getLockQuantity() > 0) {
                        GradientDrawable drawable = new GradientDrawable();
                        drawable.setCornerRadius(0);
                        drawable.setStroke(1, getResources().getColor(R.color.lockQuantity));
                        tmp_wapper.setBackgroundDrawable(drawable);
                    }
                }

                convertView.setTag(slot);

                if(slot_NoUse.equals("0")) {
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SlotBean l_Slot = (SlotBean) v.getTag();
                            dialog_SlotEdit=new CustomDialogSlotEdit(SmDeviceStockActivity.this);
                            dialog_SlotEdit.setData(cur_Cabinet,l_Slot);
                            dialog_SlotEdit.clearSearch();
                            dialog_SlotEdit.show();
                        }
                    });
                }

                tableRow.addView(convertView, new TableRow.LayoutParams(MP, WC, 1));
            }

            tl_Slots.addView(tableRow, new TableLayout.LayoutParams(MP, WC, 1));

        }
    }

    @Override
    public void onResume(){
        super.onResume();

        if(cabinetCtrlByDS==null){
            cabinetCtrlByDS=CabinetCtrlByDS.getInstance();
        }

        cabinetCtrlByDS.connect();

        if(cabinetCtrlByZS==null){
            cabinetCtrlByZS=CabinetCtrlByZS.getInstance();
        }

        cabinetCtrlByZS.connect();

        //if(getDevice().getScanner().getUse()) {
          //  if (scannerCtrl == null) {
          //      scannerCtrl = ScannerCtrl.getInstance();
          //  }
          //  scannerCtrl.connect();
        //}
    }

    @Override
    public void onStop() {
        super.onStop();

//        if (cabinetCtrlByDS != null) {
//            cabinetCtrlByDS.disConnect();
//            cabinetCtrlByDS = null;
//        }
//
//        if (cabinetCtrlByZS != null) {
//            cabinetCtrlByZS.disConnect();
//            cabinetCtrlByZS = null;
//        }
//
//        if (scannerCtrl != null) {
//            scannerCtrl.disConnect();
//            scannerCtrl = null;
//        }
    }

    @Override
    public void onPause(){
        super.onPause();

        if(cabinetCtrlByDS!=null){
            cabinetCtrlByDS.disConnect();
            cabinetCtrlByDS = null;
        }

        if (cabinetCtrlByZS != null) {
            cabinetCtrlByZS.disConnect();
            cabinetCtrlByZS = null;
        }

        //if (scannerCtrl != null) {
          //  scannerCtrl.disConnect();
            //scannerCtrl = null;
       // }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        if(dialog_SlotEdit!=null) {
            dialog_SlotEdit.cancel();
            dialog_SlotEdit.dismiss();
        }

        if(dialog_Running!=null){
            dialog_Running.cancel();
            dialog_Running.dismiss();
        }

        if(dialog_PickupAutoTest!=null) {
            dialog_PickupAutoTest.cancel();
            dialog_PickupAutoTest.dismiss();
        }

        if(tl_Slots!=null) {
            tl_Slots.removeAllViews();
        }

//        if (cabinetCtrlByDS != null) {
//            cabinetCtrlByDS.disConnect();
//            cabinetCtrlByDS = null;
//        }
//
//        if (cabinetCtrlByZS != null) {
//            cabinetCtrlByZS.disConnect();
//            cabinetCtrlByZS = null;
//        }
//
//        if (scannerCtrl != null) {
//            scannerCtrl.disConnect();
//            scannerCtrl = null;
//        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        LogUtil.i(TAG,"点击了");
        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.nav_back:
                    finish();
                    break;
                case R.id.btn_ScanSlots:
                    dialog_Confirm.setTipsImageVisibility(View.GONE);
                    dialog_Confirm.setTag("fun.scanslots");
                    dialog_Confirm.setTipsText("确定要扫描货道？");
                    dialog_Confirm.show();
                    break;
                case R.id.btn_RefreshStock:
                    getCabinetSlots();
                    break;
                case R.id.btn_AutoTest:
                    dialog_PickupAutoTest=new CustomPickupAutoTestDialog(SmDeviceStockActivity.this);
                    dialog_PickupAutoTest.setSlots(cur_Cabinet,getPickupSkus());
                    dialog_PickupAutoTest.show();
                    break;
                default:
                    break;
            }
        }
    }

    private void scanSlotsEventNotify(int status, String remark) {
        if (cur_Cabinet == null)
            return;
        try {
            JSONObject content = new JSONObject();
            content.put("cabinetId", cur_Cabinet.getCabinetId());
            content.put("status", status);
            content.put("remark", remark);
            eventNotify("vending_scan_slots", "扫描货道", content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getCabinetSlots() {

        if(cur_Cabinet==null)
            return;

        Map<String, Object> params = new HashMap<>();

        params.put("deviceId", getDevice().getDeviceId());
        params.put("cabinetId",String.valueOf(cur_Cabinet.getCabinetId()));

        //显示loading 会影响点击屏幕触发
        postByMy(SmDeviceStockActivity.this, Config.URL.stockSetting_GetCabinetSlots, params, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<DeviceSlotsResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<DeviceSlotsResultBean>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {
                    DeviceSlotsResultBean d = rt.getData();

                    switch (cur_Cabinet.getModelNo()){
                        case "dsx01":
                            drawsCabinetSlotsByDS(d.getRowColLayout(), d.getSlots());
                            break;
                        case "zsx01":
                            drawsCabinetSlotsByZS(d.getRowColLayout(), d.getSlots());
                            break;
                    }
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

    private void saveCabinetRowColLayout(final String rowColLayout) {

        if(cur_Cabinet==null)
            return;

        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", getDevice().getDeviceId());
        params.put("cabinetId", String.valueOf(cur_Cabinet.getCabinetId()));
        params.put("rowColLayout", rowColLayout);

        postByMy(SmDeviceStockActivity.this, Config.URL.stockSetting_SaveCabinetRowColLayout, params, null, false, getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {

                ApiResultBean<SlotBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<SlotBean>>() {
                });

                showToast(rt.getMessage());

                if (rt.getResult() == Result.SUCCESS) {
                    getCabinetSlots();
                }

                if(dialog_Running!=null) {
                    dialog_Running.hide();
                }
            }

            @Override
            public void onFailure(String msg, Exception e) {
                if (dialog_Running!=null) {
                    dialog_Running.hide();
                }
            }
        });
    }

    public List<PickupSkuBean> getPickupSkus(){

        List<PickupSkuBean> skus=new ArrayList<>();


        int j=0;
        switch (cur_Cabinet.getModelNo()) {
            case "dsx01":
                j = 1;
                break;
            default:
                j = 0;
                break;
        }


        for (String key :slots.keySet()){
             SlotBean l_slot= slots.get(key);
             for (int i=j;i<l_slot.getSellQuantity();i++){
                 PickupSkuBean a_slot=new PickupSkuBean();
                 a_slot.setUniqueId(UUID.randomUUID().toString());
                 a_slot.setSkuId(l_slot.getSkuId());
                 a_slot.setSlotId(l_slot.getSlotId());
                 a_slot.setCabinetId(l_slot.getCabinetId());
                 a_slot.setMainImgUrl(l_slot.getSkuMainImgUrl());
                 a_slot.setName(l_slot.getSkuName());
                 a_slot.setStatus(3010);
                 a_slot.setTips("待取货");
                 skus.add(a_slot);
             }
        }

        Collections.shuffle(skus);

        return skus;

    }

}
