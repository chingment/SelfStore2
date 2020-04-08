package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.model.api.ExHandleOrderBean;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.ui.my.MyListView;

import java.util.List;

public class ExHandleOrderAdapter extends BaseAdapter {

    private static final String TAG = "ExHandleOrderAdapter";
    private Context context;
    private List<ExHandleOrderBean> items;

    public ExHandleOrderAdapter(Context context, List<ExHandleOrderBean> items) {
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
        final ExHandleOrderBean item = items.get(position);
        TextView txt_OrderSn = ViewHolder.get(convertView, R.id.txt_OrderSn);
        MyListView list_OrderDetailItems = ViewHolder.get(convertView, R.id.list_exorder_detailitems);


        ExHandleOrderDetailItemAdapter exHandleOrderDetailItemAdapter=new ExHandleOrderDetailItemAdapter(context,item.getDetailItems());

        list_OrderDetailItems.setAdapter(exHandleOrderDetailItemAdapter);

        txt_OrderSn.setText(item.getSn());

        return convertView;
    }


}
