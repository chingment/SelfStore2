package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.model.api.ExHandleItemBean;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.ui.my.MyListView;

import java.util.List;

public class ExHandleItemAdapter extends BaseAdapter {

    private static final String TAG = "ExHandleItemAdapter";
    private Context context;
    private List<ExHandleItemBean> items;
    public ExHandleItemAdapter(Context context, List<ExHandleItemBean> items) {
        this.context = context;
        this.items = items;
    }


    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_exhandleorder, parent, false);
        }
        final ExHandleItemBean item = items.get(position);
        TextView txt_OrderId = ViewHolder.get(convertView, R.id.txt_OrderId);
        MyListView list_OrderDetailItems = ViewHolder.get(convertView, R.id.list_exorder_detailitems);

        if(item.getUniques()!=null) {
            ExHandleItemUniqueAdapter exHandleItemUniqueAdapter = new ExHandleItemUniqueAdapter(context,items, item.getUniques());

            list_OrderDetailItems.setAdapter(exHandleItemUniqueAdapter);
        }

        txt_OrderId.setText(item.getItemId());

        return convertView;
    }


}
