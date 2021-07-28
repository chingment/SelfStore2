package com.uplink.selfstore.activity;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
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
import com.uplink.selfstore.ui.dialog.CustomLoadingDialog;
import com.uplink.selfstore.ui.dialog.CustomPickupAutoTestDialog;
import com.uplink.selfstore.ui.dialog.CustomSlotEditDialog;
import com.uplink.selfstore.ui.my.MyBreathLight;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SmDeviceStockActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "SmDeviceStockActivity";
    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    private final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    private TableLayout table_slotstock;
    private CustomSlotEditDialog dialog_SlotEdit;
    private CustomPickupAutoTestDialog dialog_PickupAutoTest;
    private CabinetBean cabinet =null;//机柜信息
    private HashMap<String, SlotBean> cabinetSlots = null;//机柜货道信息
    private Button btn_ScanSlots;
    private Button btn_RefreshStock;
    private Button btn_AutoTest;
    private CabinetCtrlByDS cabinetCtrlByDS;
    private CabinetCtrlByZS cabinetCtrlByZS;
    //private ScannerCtrl scannerCtrl;

    private CustomLoadingDialog dialog_Running;

    private TextView txt_CabinetName;
    private Handler handler_UpdateUI;
    private MyBreathLight breathlight_device;
    private MyBreathLight breathlight_scangan;
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
                        if(!StringUtil.isEmptyNotNull(data)) {
                            data = data.trim();
                            if (dialog_SlotEdit != null) {
                                dialog_SlotEdit.searchSkus(data);
                            }
                        }
                        break;
                }

                return  true;
            }
        }));

        String cabinetId = getIntent().getStringExtra("cabinetId");
        cabinet = getDevice().getCabinets().get(cabinetId);
        if (cabinet == null) {
            showToast("未配置对应机柜，请联系管理员");
            return;
        }

        cabinetCtrlByDS = CabinetCtrlByDS.getInstance();
        cabinetCtrlByDS.connect();
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
                                    saveCabinetRowColLayout(cabinet.getCabinetId(), strRowColLayout);
                                }
                                break;
                            case 5://扫描超时
                                AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS ","scanslot");
                                scanSlotsEventNotify(5000, "扫描超时");
                                if (dialog_Running != null) {
                                    dialog_Running.hide();
                                }
                                showToast(message);
                                break;
                            case 6://扫描失败
                                AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS ","scanslot");
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

        cabinetCtrlByZS = CabinetCtrlByZS.getInstance();
        cabinetCtrlByZS.connect();

        //if (getDevice().getScanner().getUse()) {
          //  scannerCtrl = ScannerCtrl.getInstance();
            //scannerCtrl.connect();
        //}


        initView();
        initEvent();
        initData();
    }

    private void initView() {
        table_slotstock = (TableLayout) findViewById(R.id.table_slotstock);

        dialog_PickupAutoTest = new CustomPickupAutoTestDialog(SmDeviceStockActivity.this);
        btn_ScanSlots = (Button) findViewById(R.id.btn_ScanSlots);
        btn_RefreshStock= (Button) findViewById(R.id.btn_RefreshStock);
        btn_AutoTest= (Button) findViewById(R.id.btn_AutoTest);
        txt_CabinetName= (TextView) findViewById(R.id.txt_CabinetName);
        dialog_Running = new CustomLoadingDialog(this);


        switch (cabinet.getModelNo()) {
            case "dsx01":
                btn_ScanSlots.setVisibility(View.VISIBLE);
                break;
        }

//        breathlight_device=(MyBreathLight) findViewById(R.id.breathlight_device);
//        breathlight_device.setInterval(2000) //设置闪烁间隔时间
//                .setCoreRadius(5f)//设置中心圆半径
//                .setDiffusMaxWidth(8f)//设置闪烁圆的最大半径
//                .setDiffusColor(Color.parseColor("#ff4600"))//设置闪烁圆的颜色
//                .setCoreColor(Color.parseColor("#FA931E"))//设置中心圆的颜色
//                .onStart();
//
//        breathlight_scangan=(MyBreathLight) findViewById(R.id.breathlight_scangan);
//        breathlight_scangan.setInterval(2000) //设置闪烁间隔时间
//                .setCoreRadius(5f)//设置中心圆半径
//                .setDiffusMaxWidth(8f)//设置闪烁圆的最大半径
//                .setDiffusColor(Color.parseColor("#ff4600"))//设置闪烁圆的颜色
//                .setCoreColor(Color.parseColor("#FA931E"))//设置中心圆的颜色
//                .onStart();
    }

    private void initEvent() {
        btn_ScanSlots.setOnClickListener(this);
        btn_RefreshStock.setOnClickListener(this);
        btn_AutoTest.setOnClickListener(this);
    }

    private void initData() {


        getCabinetSlots();


        txt_CabinetName.setText(cabinet.getName() + "(" + cabinet.getCabinetId() + ")");
    }

    public void setSlot(SlotBean slot) {

        if (slot == null)
            return;

        String slotId = slot.getSlotId();

        if (StringUtil.isEmpty(slotId))
            return;

        if (cabinetSlots == null)
            return;

        if (!cabinetSlots.containsKey(slotId))
            return;


        SlotBean l_slot = cabinetSlots.get(slot.getSlotId());

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

        cabinetSlots.put(slotId,l_slot);

        switch (cabinet.getModelNo()) {
            case "dsx01":
                drawsCabinetSlotsByDS(cabinet.getRowColLayout(), cabinetSlots);
                break;
            case "zsx01":
                drawsCabinetSlotsByZS(cabinet.getRowColLayout(), cabinetSlots);
                break;
        }


    }

    public void drawsCabinetSlotsByDS(String strRowColLayout, HashMap<String, SlotBean> slots) {

        this.cabinet.setRowColLayout(strRowColLayout);

        if (slots == null) {
            slots = new HashMap<String, SlotBean>();
        }

        this.cabinetSlots = slots;


        DSCabRowColLayoutBean dSCabRowColLayout = JSON.parseObject(cabinet.getRowColLayout(), new TypeReference<DSCabRowColLayoutBean>() {
        });


        int[] rowColLayout = dSCabRowColLayout.getRows();

        int rowLength = rowColLayout.length;

        //清除表格所有行
        table_slotstock.removeAllViews();
        //全部列自动填充空白处
        table_slotstock.setStretchAllColumns(true);
        //生成X行，Y列的表格
        int no=1;
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


                    String slotId = "r" + (i - 1) + "c" + j;
                    txt_SlotId.setText(slotId);
                    //txt_SlotId.setVisibility(View.GONE);




                    txt_SlotName.setText(no+"");
                    no++;
                    SlotBean slot = null;

                    if (slots.size() > 0) {
                        slot = slots.get(slotId);
                    }


                    if (slot == null) {
                        slot = new SlotBean();
                        slot.setSlotId(slotId);
                        slots.put(slotId, slot);
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
                            dialog_SlotEdit=new CustomSlotEditDialog(SmDeviceStockActivity.this);
                            dialog_SlotEdit.setCabinet(cabinet);
                            dialog_SlotEdit.setSlot(l_Slot);
                            dialog_SlotEdit.clearSearch();
                            dialog_SlotEdit.show();
                        }
                    });

                    tableRow.addView(convertView, new TableRow.LayoutParams(MP, WC, 1));
                }
            }

            table_slotstock.addView(tableRow, new TableLayout.LayoutParams(MP, WC, 1));

        }
    }

    public void drawsCabinetSlotsByZS(String json_layout, HashMap<String, SlotBean> slots) {

        this.cabinet.setRowColLayout(json_layout);

        if (slots == null) {
            slots = new HashMap<String, SlotBean>();
        }

        this.cabinetSlots = slots;


        ZSCabRowColLayoutBean layout = JSON.parseObject(json_layout, new TypeReference<ZSCabRowColLayoutBean>() {});


        if(layout==null)
            return;

        //清除表格所有行
        table_slotstock.removeAllViews();
        //全部列自动填充空白处
        table_slotstock.setStretchAllColumns(true);


        //生成X行，Y列的表格


        List<List<String>> rows=layout.getRows();

        for (int i = 0; i <rows.size(); i++) {

            TableRow tableRow = new TableRow(SmDeviceStockActivity.this);

            List<String> cols=rows.get(i);

            for (int j = 0; j < cols.size(); j++) {
                //tv用于显示
                String[] col=cols.get(j).split("-");

                String id=col[0];
                String plate=col[1];
                String nick=col[2];
                String nouse=col[3];

                final View convertView = LayoutInflater.from(SmDeviceStockActivity.this).inflate(R.layout.item_list_sku_tmp2, tableRow, false);

                LinearLayout tmp_wapper = ViewHolder.get(convertView, R.id.tmp_wapper);

                TextView txt_SlotId = ViewHolder.get(convertView, R.id.txt_SlotId);
                TextView txt_SlotName = ViewHolder.get(convertView, R.id.txt_SlotName);
                TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
                TextView txt_sellQuantity = ViewHolder.get(convertView, R.id.txt_sellQuantity);
                TextView txt_lockQuantity = ViewHolder.get(convertView, R.id.txt_lockQuantity);
                TextView txt_sumQuantity = ViewHolder.get(convertView, R.id.txt_sumQuantity);
                ImageView img_main = ViewHolder.get(convertView, R.id.img_main);


                final String slotId =cols.get(j);

                txt_SlotId.setText(slotId);
                txt_SlotId.setVisibility(View.GONE);

                txt_SlotName.setText(nick);

                SlotBean slot = null;

                if (slots.size() > 0) {
                    slot = slots.get(slotId);
                }


                if (slot == null) {
                    slot = new SlotBean();
                    slot.setSlotId(slotId);
                    slots.put(slotId, slot);
                }

                if (slot.getSkuId() == null) {
                    if(nouse.equals("0")) {
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

                if(nouse.equals("0")) {
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SlotBean l_Slot = (SlotBean) v.getTag();
                            dialog_SlotEdit=new CustomSlotEditDialog(SmDeviceStockActivity.this);
                            dialog_SlotEdit.setCabinet(cabinet);
                            dialog_SlotEdit.setSlot(l_Slot);
                            dialog_SlotEdit.clearSearch();
                            dialog_SlotEdit.show();
                        }
                    });
                }

                tableRow.addView(convertView, new TableRow.LayoutParams(MP, WC, 1));
            }

            table_slotstock.addView(tableRow, new TableLayout.LayoutParams(MP, WC, 1));

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

        if(table_slotstock!=null) {
            table_slotstock.removeAllViews();
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
                    cabinetCtrlByDS.scanSlot();
                    break;
                case R.id.btn_RefreshStock:
                    getCabinetSlots();
                    break;
                case R.id.btn_AutoTest:
                    dialog_PickupAutoTest=new CustomPickupAutoTestDialog(SmDeviceStockActivity.this);
                    dialog_PickupAutoTest.setSlots(cabinet,getPickupSkus());
                    dialog_PickupAutoTest.show();
                    break;
                default:
                    break;
            }
        }
    }

    private void scanSlotsEventNotify(int status, String remark) {
        try {
            JSONObject content = new JSONObject();
            content.put("cabinetId", cabinet.getCabinetId());
            content.put("status", status);
            content.put("remark", remark);
            eventNotify("vending_scan_slots","扫描货道",content);
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getCabinetSlots() {


        Map<String, Object> params = new HashMap<>();

        params.put("deviceId", getDevice().getDeviceId());
        params.put("cabinetId",String.valueOf(cabinet.getCabinetId()));

        //显示loading 会影响点击屏幕触发
        postByMy(SmDeviceStockActivity.this, Config.URL.stockSetting_GetCabinetSlots, params, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<DeviceSlotsResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<DeviceSlotsResultBean>>() {
                });


                if (rt.getResult() == Result.SUCCESS) {
                    DeviceSlotsResultBean d = rt.getData();

                    switch (cabinet.getModelNo()){
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

    private void saveCabinetRowColLayout(final String cabinetId, final String cabinetRowColLayout) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", getDevice().getDeviceId());
        params.put("cabinetId", cabinetId);
        params.put("rowColLayout", cabinetRowColLayout);

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

        List<PickupSkuBean> slots=new ArrayList<>();

        for (String key :this.cabinetSlots.keySet()){
             SlotBean l_slot= this.cabinetSlots.get(key);
             for (int i=1;i<l_slot.getSellQuantity();i++){
                 PickupSkuBean a_slot=new PickupSkuBean();
                 a_slot.setUniqueId(UUID.randomUUID().toString());
                 a_slot.setSkuId(l_slot.getSkuId());
                 a_slot.setSlotId(l_slot.getSlotId());
                 a_slot.setCabinetId(l_slot.getCabinetId());
                 a_slot.setMainImgUrl(l_slot.getSkuMainImgUrl());
                 a_slot.setName(l_slot.getSkuName());
                 a_slot.setStatus(3010);
                 a_slot.setTips("待取货");
                 slots.add(a_slot);
             }
        }

        Collections.shuffle(slots);

        return slots;

    }

}
