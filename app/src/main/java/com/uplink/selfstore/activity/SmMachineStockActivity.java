package com.uplink.selfstore.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

public class SmMachineStockActivity extends SwipeBackActivity implements View.OnClickListener {

    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    private final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    private TableLayout table_slotstock;
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

    private void initView() {
        table_slotstock = (TableLayout) findViewById(R.id.table_slotstock);
    }


    private void initEvent() {

    }

    private void initData() {
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
}
