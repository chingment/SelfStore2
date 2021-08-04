package com.uplink.selfstore.model;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.SmDeviceStockActivity;
import com.uplink.selfstore.model.api.SlotBean;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.ui.dialog.CustomDialogSlotEdit;
import com.uplink.selfstore.utils.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CabinetLayoutUtil {

    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    private final int MP = ViewGroup.LayoutParams.MATCH_PARENT;

    public static List<List<String>> getRowsByDs(int[] rowColLayout) {

        List<List<String>> rows = new ArrayList<>();



        int rowLength = rowColLayout.length;

        int num = 1;
        for (int i = rowLength - 1; i >= 0; i--) {

            List<String> cols = new ArrayList<>();

            int colLength = rowColLayout[i];

            for (int j = colLength - 1; j >= 0; j--) {

                String slotId = i+ "-" + j + "-" + num;
                cols.add(slotId);
                num++;
            }

            rows.add(cols);

        }

        return rows;

    }


//    public void drawLayout(Context context, TableLayout tl_Slots, String cabinetId, List<List<String>> rows, HashMap<String, Object> slots){
//
//        tl_Slots.removeAllViews();
//        //全部列自动填充空白处
//        tl_Slots.setStretchAllColumns(true);
//
//        //生成X行，Y列的表格
//        for (int i = 0; i < rows.size(); i++) {
//
//            TableRow tableRow = new TableRow(context);
//
//            List<String> cols = rows.get(i);
//
//            for (int j = 0; j < cols.size(); j++) {
//
//                String col = cols.get(j);
//
//                String[] col_Prams = col.split("-");
//
//                String slot_Id = col;
//                String slot_Name = "";
//                String slot_NoUse = "0";
//                if (cabinetId.contains("ds")) {
//                    slot_Name = col_Prams[2];
//                } else if (cabinetId.contains("zs")) {
//                    slot_Name = col_Prams[2];
//                    slot_NoUse = col_Prams[3];
//                }
//
//                final View convertView = LayoutInflater.from(context).inflate(R.layout.item_list_sku_tmp2, tableRow, false);
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
//                SlotBean slot = null;
//
//                if (slots.size() > 0) {
//                    slot = slots.get(slot_Id);
//                }
//
//                if (slot == null) {
//                    slot = new SlotBean();
//                    slot.setSlotName(slot_Name);
//                    slot.setSlotId(slot_Id);
//                    slots.put(slot_Id, slot);
//                } else {
//                    slot.setSlotName(slot_Name);
//                }
//
//                if (slot.getSkuId() == null) {
//                    if (slot_NoUse.equals("0")) {
//                        txt_name.setText(R.string.tips_noproduct);
//                    } else {
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
//                    CommonUtil.loadImageFromUrl(context, img_main, slot.getSkuMainImgUrl());
//
//                    if (slot.getLockQuantity() > 0) {
//                        GradientDrawable drawable = new GradientDrawable();
//                        drawable.setCornerRadius(0);
//                        drawable.setStroke(1,context.getResources().getColor(R.color.lockQuantity));
//                        tmp_wapper.setBackgroundDrawable(drawable);
//                    }
//                }
//
//                convertView.setTag(slot);
//
//                if (slot_NoUse.equals("0")) {
//                    convertView.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            //SlotBean l_Slot = (SlotBean) v.getTag();
//                            //dialog_SlotEdit = new CustomDialogSlotEdit(SmDeviceStockActivity.this);
//                            //dialog_SlotEdit.setData(cur_Cabinet, l_Slot);
//                            //dialog_SlotEdit.clearSearch();
//                            //dialog_SlotEdit.show();
//                        }
//                    });
//                }
//
//                tableRow.addView(convertView, new TableRow.LayoutParams(MP, WC, 1));
//            }
//
//            tl_Slots.addView(tableRow, new TableLayout.LayoutParams(MP, WC, 1));
//
//        }
//    }
}
