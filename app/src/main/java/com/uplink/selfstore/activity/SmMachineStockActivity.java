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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.model.ScanSlotResult;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.CabinetBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.MachineSlotsResultBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.SlotBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.ui.dialog.CustomDialogLoading;
import com.uplink.selfstore.ui.dialog.CustomSlotEditDialog;
import com.uplink.selfstore.ui.my.MyBreathLight;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.InterUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SmMachineStockActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "SmMachineStockActivity";
    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    private final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    private TableLayout table_slotstock;
    private CustomSlotEditDialog dialog_SlotEdit;
    private CabinetBean cabinet =null;//机柜信息
    private HashMap<String, SlotBean> cabinetSlots = null;//机柜货道信息
    private Button btn_ScanSlots;
    private Button btn_RefreshStock;
    private CabinetCtrlByDS cabinetCtrlByDS;
    private CustomDialogLoading customDialogRunning;
    private TextView txt_CabinetName;
    private Handler handler_UpdateUI;
    private MyBreathLight breathlight_machine;
    private MyBreathLight breathlight_scangan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smmachinestock);

        setNavTtile(this.getResources().getString(R.string.aty_smmachinestock_navtitle));
        setNavGoBackBtnVisible(true);


        String cabinetId = getIntent().getStringExtra("cabinetId");
        cabinet = getMachine().getCabinets().get(cabinetId);
        if(cabinet==null){
            showToast("未配置对应机柜，请联系管理员");
            return;
        }
        cabinetCtrlByDS=CabinetCtrlByDS.getInstance();
        cabinetCtrlByDS.setScanSlotHandler(new Handler(  new Handler.Callback() {
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
                                //异常消息
                                showToast(message);
                                if(customDialogRunning!=null) {
                                    if(customDialogRunning.isShowing()) {
                                        customDialogRunning.cancelDialog();
                                    }
                                }
                                break;
                            case 2://启动就绪
                                scanSlotsEventNotify(2000,"启动就绪");
                                if(customDialogRunning!=null) {
                                    customDialogRunning.setProgressText(message);
                                    if (!customDialogRunning.isShowing()) {
                                        customDialogRunning.showDialog();
                                    }
                                }
                                break;
                            case 3://扫描中
                                if(customDialogRunning!=null) {
                                    customDialogRunning.setProgressText(message);
                                }
                                break;
                            case 4://扫描成功
                                if (result != null) {
                                    scanSlotsEventNotify(4000,"扫描成功,结果:"+ InterUtil.arrayTransformString(result.rowColLayout,",")+",用时:"+result.getUseTime());
                                    saveCabinetRowColLayout(cabinet.getId(), result.rowColLayout);
                                }
                                break;
                            case 5://扫描超时
                                scanSlotsEventNotify(5000,"扫描超时");
                                if(customDialogRunning!=null) {
                                    if(customDialogRunning.isShowing()) {
                                        customDialogRunning.cancelDialog();
                                    }
                                }
                                showToast(message);
                                break;
                            case 6://扫描失败
                                scanSlotsEventNotify(6000,"扫描失败");
                                if(customDialogRunning!=null) {
                                    if(customDialogRunning.isShowing()) {
                                        customDialogRunning.cancelDialog();
                                    }
                                }
                                showToast(message);
                                break;

                        }
                        return false;
                    }
                })
        );

        initView();
        initEvent();
        initData();
    }

    protected void initView() {
        table_slotstock = (TableLayout) findViewById(R.id.table_slotstock);
        dialog_SlotEdit = new CustomSlotEditDialog(SmMachineStockActivity.this);
        btn_ScanSlots = (Button) findViewById(R.id.btn_ScanSlots);
        btn_RefreshStock= (Button) findViewById(R.id.btn_RefreshStock);
        txt_CabinetName= (TextView) findViewById(R.id.txt_CabinetName);
        customDialogRunning = new CustomDialogLoading(this);

//        breathlight_machine=(MyBreathLight) findViewById(R.id.breathlight_machine);
//        breathlight_machine.setInterval(2000) //设置闪烁间隔时间
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

    protected void initEvent() {
        btn_ScanSlots.setOnClickListener(this);
        btn_RefreshStock.setOnClickListener(this);
    }

    protected void initData() {
        getCabinetSlots();

        txt_CabinetName.setText(cabinet.getName()+"("+cabinet.getId()+")");
    }

    public void setSlot(SlotBean slot) {
        if(slot!=null) {
            cabinetSlots.get(slot.getId()).setProductSkuId(slot.getProductSkuId());
            cabinetSlots.get(slot.getId()).setProductSkuName(slot.getProductSkuName());
            cabinetSlots.get(slot.getId()).setProductSkuMainImgUrl(slot.getProductSkuMainImgUrl());
            cabinetSlots.get(slot.getId()).setOffSell(slot.isOffSell());
            cabinetSlots.get(slot.getId()).setLockQuantity(slot.getLockQuantity());
            cabinetSlots.get(slot.getId()).setSellQuantity(slot.getSellQuantity());
            cabinetSlots.get(slot.getId()).setSumQuantity(slot.getSumQuantity());
            cabinetSlots.get(slot.getId()).setMaxQuantity(slot.getMaxQuantity());
            cabinetSlots.get(slot.getId()).setVersion(slot.getVersion());
            drawsCabinetSlots(cabinet.getRowColLayout(), cabinetSlots);
        }
    }

    public void drawsCabinetSlots(int[] rowColLayout, HashMap<String, SlotBean> slots) {

        this.cabinet.setRowColLayout(rowColLayout);

        if (slots == null) {
            slots = new HashMap<String, SlotBean>();
        }

        this.cabinetSlots = slots;

        int rowLength = rowColLayout.length;

        //清除表格所有行
        table_slotstock.removeAllViews();
        //全部列自动填充空白处
        table_slotstock.setStretchAllColumns(true);
        //生成X行，Y列的表格
        for (int i = rowLength; i > 0; i--) {
            TableRow tableRow = new TableRow(SmMachineStockActivity.this);
            int colLength = rowColLayout[i - 1];

            boolean isPndantRow = false;
            int[] cabinetPendantRows=this.cabinet.getRowColLayout();
            if ( cabinetPendantRows != null) {
                for (int z = 0; z < cabinetPendantRows.length; z++) {
                    if (cabinetPendantRows[z] == (i-1)) {
                        isPndantRow = true;
                        break;
                    }
                }
            }

            for (int j = 0; j < colLength; j++) {
                //tv用于显示
                final View convertView = LayoutInflater.from(SmMachineStockActivity.this).inflate(R.layout.item_list_sku_tmp2, tableRow, false);

                TextView txt_SlotId = ViewHolder.get(convertView, R.id.txt_SlotId);

                TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
                TextView txt_sellQuantity = ViewHolder.get(convertView, R.id.txt_sellQuantity);
                TextView txt_lockQuantity = ViewHolder.get(convertView, R.id.txt_lockQuantity);
                TextView txt_sumQuantity = ViewHolder.get(convertView, R.id.txt_sumQuantity);
                ImageView img_main = ViewHolder.get(convertView, R.id.img_main);


                final String slotId = "r" + (i - 1) + "c" + j;

                if(isPndantRow){
                    if(j==0){
                        convertView.setVisibility(View.GONE);
                    }
                }

                txt_SlotId.setText(slotId);
                txt_SlotId.setVisibility(View.GONE);
                SlotBean slot = null;

                if (slots.size() > 0) {
                    slot = slots.get(slotId);
                }


                if (slot == null) {
                    slot = new SlotBean();
                    slot.setId(slotId);
                    slots.put(slotId, slot);
                }

                if (slot.getProductSkuId() == null) {
                    txt_name.setText("暂无商品");
                    txt_sellQuantity.setText("0");
                    txt_lockQuantity.setText("0");
                    txt_sumQuantity.setText("0");

                } else {
                    txt_name.setText(slot.getProductSkuName());
                    txt_sellQuantity.setText(String.valueOf(slot.getSellQuantity()));
                    txt_lockQuantity.setText(String.valueOf(slot.getLockQuantity()));
                    txt_sumQuantity.setText(String.valueOf(slot.getSumQuantity()));

                    CommonUtil.loadImageFromUrl(SmMachineStockActivity.this, img_main, slot.getProductSkuMainImgUrl());

                    if (slot.getLockQuantity() > 0) {
                        GradientDrawable drawable = new GradientDrawable();
                        drawable.setCornerRadius(0);
                        drawable.setStroke(1, getResources().getColor(R.color.lockQuantity));
                        convertView.setBackgroundDrawable(drawable);
                    }
                }

                convertView.setTag(slot);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SlotBean l_Slot = (SlotBean) v.getTag();
                        dialog_SlotEdit.setCabinet(cabinet);
                        dialog_SlotEdit.setSlot(l_Slot);
                        dialog_SlotEdit.clearSearch();
                        dialog_SlotEdit.show();
                    }
                });

                tableRow.addView(convertView, new TableRow.LayoutParams(MP, WC, 1));
            }

            table_slotstock.addView(tableRow, new TableLayout.LayoutParams(MP, WC, 1));

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(cabinetCtrlByDS!=null) {
            cabinetCtrlByDS.disConnect();
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
                case R.id.btn_ScanSlots:

                    if (!cabinetCtrlByDS.isConnect()) {
                        cabinetCtrlByDS.connect();
                    }

                    if(!cabinetCtrlByDS.isConnect()){
                        showToast("机器连接失败");
                        return;
                    }

                    if (!cabinetCtrlByDS.isNormarl()) {
                        showToast("机器状态异常");
                        return;
                    }

                    cabinetCtrlByDS.scanSlot();
                    break;
                case R.id.btn_RefreshStock:
                    getCabinetSlots();
                    break;
                default:
                    break;
            }
        }
    }


    private void scanSlotsEventNotify(int status, String remark) {
        try {
            JSONObject content = new JSONObject();
            content.put("cabinetId", cabinet.getId());
            content.put("status", status);
            content.put("remark", remark);
            eventNotify(3, content);
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getCabinetSlots() {

        Map<String, String> params = new HashMap<>();

        params.put("machineId", getMachine().getId());
        params.put("cabinetId",String.valueOf(cabinet.getId()));

        getByMy(Config.URL.stockSetting_GetCabinetSlots, params, true, "正在获取库存", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<MachineSlotsResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<MachineSlotsResultBean>>() {
                });


                if (rt.getResult() == Result.SUCCESS) {
                    MachineSlotsResultBean d = rt.getData();
                    drawsCabinetSlots(d.getRowColLayout(), d.getSlots());
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

    private void saveCabinetRowColLayout(final String cabinetId, final int[]cabinetRowColLayout) {
        Map<String, Object> params = new HashMap<>();
        params.put("machineId", getMachine().getId());
        params.put("cabinetId", cabinetId);


        JSONArray json_cabinetRowColLayout = new JSONArray();
        for (int item : cabinetRowColLayout) {
            json_cabinetRowColLayout.put(item);
        }

        params.put("cabinetRowColLayout", json_cabinetRowColLayout);

        postByMy(Config.URL.stockSetting_SaveCabinetRowColLayout, params, null, false, getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {

                ApiResultBean<SlotBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<SlotBean>>() {
                });

                showToast(rt.getMessage());

                if (rt.getResult() == Result.SUCCESS) {
                    drawsCabinetSlots(cabinetRowColLayout, cabinetSlots);
                }

                if(customDialogRunning!=null) {
                    if (customDialogRunning.isShowing()) {
                        customDialogRunning.cancelDialog();
                    }
                }
            }

            @Override
            public void onFailure(String msg, Exception e) {
                if (customDialogRunning.isShowing()) {
                    customDialogRunning.cancelDialog();
                }
            }
        });
    }
}
