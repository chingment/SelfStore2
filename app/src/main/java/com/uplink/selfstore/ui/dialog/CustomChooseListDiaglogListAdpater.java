package com.uplink.selfstore.ui.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import com.uplink.selfstore.R;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.LogUtil;

/**
 * Created by chingment on 2017/12/22.
 */

public class CustomChooseListDiaglogListAdpater extends BaseAdapter {
    private LayoutInflater inflater;
    private List<String> list;
    private String choosedVal;
    public void setData(List<String> list,String choosedVal) {
        this.list = list;
        this.choosedVal=choosedVal;
        notifyDataSetChanged();
    }

    public CustomChooseListDiaglogListAdpater(LayoutInflater inflater, List<String> xuexiao_list) {
        this.inflater = inflater;
        this.list = xuexiao_list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {

        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        convertView = inflater.inflate(R.layout.item_customchooselistdialog, null);
        TextView item_tv = ViewHolder.get(convertView, R.id.dialog_chooselist_item);
        String str = list.get(position);

        item_tv.setText(str);

        //LogUtil.i("默认值-?:" + str);
        //LogUtil.i("默认值-?:" + choosedVal);

        if(str.equals(choosedVal))
        {
            item_tv.setTextColor(item_tv.getResources().getColor(R.color.dialog_chooselist_seleced));
        }

        return convertView;
    }
}
