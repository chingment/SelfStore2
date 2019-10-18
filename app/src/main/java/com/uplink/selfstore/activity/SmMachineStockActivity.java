package com.uplink.selfstore.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.MachineSlotsResultBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.SlotBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.ui.dialog.CustomSlotEditDialog;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.util.HashMap;
import java.util.Map;

public class SmMachineStockActivity extends SwipeBackActivity implements View.OnClickListener {

    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    private final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    private TableLayout table_slotstock;

    private CustomSlotEditDialog dialog_SlotEdit;

    private HashMap<String, SlotBean> slots;

    private String cabinetId="";
    private String cabinetName="";
    private int cabinetMaxRow=0;
    private int cabinetMaxCol=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smmachinestock);

        setNavTtile(this.getResources().getString(R.string.activity_smmachinestock_navtitle));
        setNavBackVisible(true);
        setNavBtnVisible(true);

        MachineBean machine = AppCacheManager.getMachine();
        cabinetId=machine.getCabinetId_1();
        cabinetMaxRow=machine.getCabinetMaxRow_1();
        cabinetMaxCol=machine.getCabinetMaxCol_1();


        initView();
        initEvent();
        initData();
    }

    protected void initView() {
        table_slotstock = (TableLayout) findViewById(R.id.table_slotstock);
        dialog_SlotEdit = new CustomSlotEditDialog(SmMachineStockActivity.this);

    }


    protected void initEvent() {

    }

    protected void initData() {
        getSlots();
    }

    public  void  setSlot(SlotBean slot)
    {
        slots.get(slot.getId()).setProductSkuId(slot.getProductSkuId());
        slots.get(slot.getId()).setProductSkuName(slot.getProductSkuName());
        slots.get(slot.getId()).setProductSkuMainImgUrl(slot.getProductSkuMainImgUrl());
        slots.get(slot.getId()).setOffSell(slot.isOffSell());
        slots.get(slot.getId()).setLockQuantity(slot.getLockQuantity());
        slots.get(slot.getId()).setSellQuantity(slot.getSellQuantity());
        slots.get(slot.getId()).setSumQuantity(slot.getSumQuantity());
        slots.get(slot.getId()).setMaxQuantity(slot.getMaxQuantity());

        drawsStock(slots);
    }

    public   void drawsStock(HashMap<String, SlotBean> slots) {

        this.slots=slots;

        int row_int = cabinetMaxRow;
        int col_int = cabinetMaxCol;
        //清除表格所有行
        table_slotstock.removeAllViews();
        //全部列自动填充空白处
        table_slotstock.setStretchAllColumns(true);
        //生成X行，Y列的表格
        for (int i = cabinetMaxRow; i >0; i--) {
            TableRow tableRow = new TableRow(SmMachineStockActivity.this);

            for (int j = 0; j < col_int; j++) {
                //tv用于显示
                final View convertView = LayoutInflater.from(SmMachineStockActivity.this).inflate(R.layout.item_list_sku_tmp2, tableRow, false);

                TextView txt_SlotId = ViewHolder.get(convertView, R.id.txt_SlotId);

                TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
                TextView txt_sellQuantity = ViewHolder.get(convertView, R.id.txt_sellQuantity);
                TextView txt_lockQuantity = ViewHolder.get(convertView, R.id.txt_lockQuantity);
                TextView txt_sumQuantity = ViewHolder.get(convertView, R.id.txt_sumQuantity);
                ImageView img_main = ViewHolder.get(convertView, R.id.img_main);

                //CommonUtil.loadImageFromUrl(convertView, img_main, item.getMainImgUrl());

                final String slotId = cabinetId+"r"+(i-1) + "c" + j;

                txt_SlotId.setText(slotId);

                SlotBean slot=null;
                if(slots!=null) {
                    if(slots.size()>0) {
                        slot = slots.get(slotId);
                    }
                }

                if(slot==null)
                {
                    slot=new SlotBean();
                    slot.setId(slotId);

                    slots.put(slotId,slot);
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

                }

                convertView.setTag(slot);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SlotBean l_Slot=(SlotBean)v.getTag();
                        dialog_SlotEdit.setSlot(l_Slot);
                        dialog_SlotEdit.show();
                    }
                });

                tableRow.addView(convertView, new TableRow.LayoutParams(MP, WC, 1));
            }

            table_slotstock.addView(tableRow, new TableLayout.LayoutParams(MP, WC, 1));

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
            }
        }
    }

    private void getSlots(){

        Map<String, String> params = new HashMap<>();

        MachineBean machine = AppCacheManager.getMachine();

        params.put("machineId", machine.getId());


        getByMy(Config.URL.machine_Slots, params, true,"正在获取库存", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<MachineSlotsResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<MachineSlotsResultBean>>() {
                });


                if (rt.getResult() == Result.SUCCESS) {

                    MachineSlotsResultBean d=rt.getData();
                    drawsStock(d.getSlots());
                }
                else
                {
                    showToast(rt.getMessage());
                }
            }
        });
    }
}
