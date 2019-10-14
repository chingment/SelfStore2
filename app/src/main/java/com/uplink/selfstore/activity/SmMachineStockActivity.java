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
import com.uplink.selfstore.model.api.MachineSlotStockResultBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.SlotProductSkuBean;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smmachinestock);

        setNavTtile(this.getResources().getString(R.string.activity_smmachinestock_navtitle));
        setNavBackVisible(true);
        setNavBtnVisible(true);

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

        getSlotStock();
    }

    private  void drawsStock(HashMap<String, SlotProductSkuBean> slotStocks) {
        int row_int = 10;
        int col_int = 10;
        //清除表格所有行
        table_slotstock.removeAllViews();
        //全部列自动填充空白处
        table_slotstock.setStretchAllColumns(true);
        //生成X行，Y列的表格
        for (int i = 1; i <= row_int; i++) {
            TableRow tableRow = new TableRow(SmMachineStockActivity.this);

            for (int j = 1; j <= col_int; j++) {
                //tv用于显示
                View convertView = LayoutInflater.from(SmMachineStockActivity.this).inflate(R.layout.item_list_sku_tmp2, tableRow, false);


                TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
                TextView txt_sellQuantity = ViewHolder.get(convertView, R.id.txt_sellQuantity);
                TextView txt_lockQuantity = ViewHolder.get(convertView, R.id.txt_lockQuantity);
                TextView txt_sumQuantity = ViewHolder.get(convertView, R.id.txt_sumQuantity);
                ImageView img_main = ViewHolder.get(convertView, R.id.img_main);

                //CommonUtil.loadImageFromUrl(convertView, img_main, item.getMainImgUrl());

                String slotId = i + "" + j;
                final SlotProductSkuBean slotStock = slotStocks.get(slotId);
                if (slotStock == null) {
                    txt_name.setText("暂无商品");
                } else {
                    txt_name.setText(slotStock.getName());
                    txt_sellQuantity.setText(slotStock.getSellQuantity() + "");
                    txt_lockQuantity.setText(slotStock.getLockQuantity() + "");
                    txt_sumQuantity.setText(slotStock.getSumQuantity() + "");

                    CommonUtil.loadImageFromUrl(SmMachineStockActivity.this, img_main, slotStock.getMainImgUrl());


                }

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog_SlotEdit.setSlotStock(slotStock);
                        dialog_SlotEdit.show();
                    }
                });

                tableRow.addView(convertView, new TableRow.LayoutParams(MP, WC, 1));
            }
            //新建的TableRow添加到TableLayout

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

    private void getSlotStock(){

        Map<String, String> params = new HashMap<>();

        MachineBean machine = AppCacheManager.getMachine();

        params.put("machineId", machine.getId());


        getByMy(Config.URL.machine_SlotStocks, params, true,"正在获取库存", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<MachineSlotStockResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<MachineSlotStockResultBean>>() {
                });


                if (rt.getResult() == Result.SUCCESS) {

                    MachineSlotStockResultBean d=rt.getData();
                    drawsStock(d.getSlotStocks());
                }
                else
                {
                    showToast(rt.getMessage());
                }
            }
        });
    }
}
