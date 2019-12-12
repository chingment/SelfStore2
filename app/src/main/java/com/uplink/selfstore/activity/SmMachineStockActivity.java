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
import com.uplink.selfstore.deviceCtrl.MachineCtrl;
import com.uplink.selfstore.model.ScanSlotResult;
import com.uplink.selfstore.model.api.ApiResultBean;
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
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class SmMachineStockActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "SmMachineStockActivity";
    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    private final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    private TableLayout table_slotstock;
    private CustomSlotEditDialog dialog_SlotEdit;
    private int cabinetId = 0;//默认第一个机柜，以后扩展机柜需要
    private String cabinetName = "第一个机柜";
    private int[] cabinetRowColLayout = null;
    private int[] cabinetRowIndexByPendant=null;
    private HashMap<String, SlotBean> cabinetSlots = null;
    private Button btn_ScanSlots;
    private MachineCtrl machineCtrl;
    private CustomDialogLoading customDialogRunning;
    private Handler handler_UpdateUI;

    private  MyBreathLight breathlight_machine;
    private  MyBreathLight breathlight_scangan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smmachinestock);

        setNavTtile(this.getResources().getString(R.string.activity_smmachinestock_navtitle));
        setNavBackVisible(true);
        setNavBtnVisible(true);


        MachineBean machine = AppCacheManager.getMachine();
        cabinetId = machine.getCabinetId_1();
        cabinetRowColLayout = machine.getCabinetRowColLayout_1();

        machineCtrl = new MachineCtrl();
        machineCtrl.setScanSlotHandler(new Handler(  new Handler.Callback() {
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
                                LogUtil.e("xxxx:0");
                                if(customDialogRunning!=null) {
                                    LogUtil.e("xxxx:1");
                                    customDialogRunning.setProgressText(message);
                                    if (!customDialogRunning.isShowing()) {
                                        LogUtil.e("xxxx:2");
                                        customDialogRunning.showDialog();
//                                        new Handler().postDelayed(new Runnable() {
//                                            public void run() {
//                                                LogUtil.i("正在执行关闭窗口");
//                                                if (customDialogRunning != null && customDialogRunning.isShowing()) {
//                                                    customDialogRunning.cancelDialog();
//                                                }
//                                            }
//                                        }, 600 * 1000);
                                    }
                                    else
                                    {
                                        LogUtil.e("xxxx:3");
                                    }
                                }
                                break;
                            case 3://扫描中
                                if(customDialogRunning!=null) {
                                    customDialogRunning.setProgressText(message);
                                }
                                break;
                            case 4://扫描成功
                                scanSlotsEventNotify(4000,"扫描成功");
                                if (result != null) {
                                    saveCabinetRowColLayout(cabinetId, result.rowColLayout);
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
                                scanSlotsEventNotify(5000,"扫描失败");
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
    }

    protected void initData() {
        getCabinetSlots();
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
            cabinetSlots.get(slot.getId()).setMaxLimitSumQuantity(slot.getMaxLimitSumQuantity());
            cabinetSlots.get(slot.getId()).setVersion(slot.getVersion());
            drawsCabinetSlots(cabinetRowColLayout, cabinetSlots);
        }
    }

    public void drawsCabinetSlots(int[] rowColLayout, HashMap<String, SlotBean> slots) {


        this.cabinetRowColLayout = rowColLayout;

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

            for (int j = 0; j < colLength; j++) {
                //tv用于显示
                final View convertView = LayoutInflater.from(SmMachineStockActivity.this).inflate(R.layout.item_list_sku_tmp2, tableRow, false);

                TextView txt_SlotId = ViewHolder.get(convertView, R.id.txt_SlotId);

                TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
                TextView txt_sellQuantity = ViewHolder.get(convertView, R.id.txt_sellQuantity);
                TextView txt_lockQuantity = ViewHolder.get(convertView, R.id.txt_lockQuantity);
                TextView txt_sumQuantity = ViewHolder.get(convertView, R.id.txt_sumQuantity);
                ImageView img_main = ViewHolder.get(convertView, R.id.img_main);

                String m = "0";
                if (i == rowLength) {
                    m = "1";

                    if(j==0){
                        convertView.setVisibility(View.GONE);
                    }
                }

                final String slotId = "n" + cabinetId + "r" + (i - 1) + "c" + j + "m" + m;

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

        if(machineCtrl!=null) {
            machineCtrl.disConnect();
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

                    if (!machineCtrl.isConnect()) {
                        machineCtrl.connect();
                    }

                    if(!machineCtrl.isConnect()){
                        showToast("机器连接失败");
                        return;
                    }

                    if (!machineCtrl.isNormarl()) {
                        showToast("机器状态异常");
                        return;
                    }

                    machineCtrl.scanSlot();
                    break;
                default:
                    break;
            }
        }
    }


    private void scanSlotsEventNotify(int status, String remark) {

        Map<String, Object> params = new HashMap<>();

        MachineBean machine = AppCacheManager.getMachine();
        params.put("machineId", machine.getId());
        params.put("cabinetId", cabinetId);
        params.put("status", status);
        params.put("remark", remark);
        postByMy(Config.URL.machine_ScanSlotsEventNotify, params, null, false, "", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);
            }
        });
    }

    private void getCabinetSlots() {

        Map<String, String> params = new HashMap<>();

        MachineBean machine = AppCacheManager.getMachine();

        params.put("machineId", machine.getId());
        params.put("cabinetId",String.valueOf(cabinetId));

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

    private void saveCabinetRowColLayout(final int cabinetId, final int[]cabinetRowColLayout) {
        MachineBean machine = AppCacheManager.getMachine();

        Map<String, Object> params = new HashMap<>();
        params.put("machineId", machine.getId());
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
                    drawsCabinetSlots(cabinetRowColLayout,cabinetSlots);
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
