package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.model.api.PickupSkuBean;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.CommonUtil;

import java.util.List;

public class PickupAutoTestSlotAdapter extends BaseAdapter {

    private static final String TAG = "OrderDetailsSkuAdapter";

    private Context context;
    private List<PickupSkuBean> items;


    public PickupAutoTestSlotAdapter(Context context, List<PickupSkuBean> items) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_pickupautotest_slot, parent, false);
        }

        final PickupSkuBean item = items.get(position);


        ImageView img_main = ViewHolder.get(convertView, R.id.img_main);
        TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
        TextView txt_message = ViewHolder.get(convertView, R.id.txt_tipMessage);


        CommonUtil.loadImageFromUrl(context, img_main, item.getMainImgUrl());
        txt_name.setText(item.getName());
        txt_message.setText(String.valueOf(item.getTips()));

        return convertView;
    }

}
