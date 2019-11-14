package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.model.api.OrderDetailsSkuBean;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.CommonUtil;

import java.util.List;

/**
 * Created by chingment on 2019/2/28.
 */

public class OrderDetailsSkuAdapter extends BaseAdapter {


    private Context context;
    private List<OrderDetailsSkuBean> items;


    public OrderDetailsSkuAdapter(Context context, List<OrderDetailsSkuBean> items) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_orderdetails_sku, parent, false);
        }

        final OrderDetailsSkuBean item = items.get(position);


        ImageView img_main = ViewHolder.get(convertView, R.id.img_main);
        TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
        TextView txt_quantity = ViewHolder.get(convertView, R.id.txt_quantity);
        TextView txt_quantitybysuccess = ViewHolder.get(convertView, R.id.txt_quantitybysuccess);
        TextView txt_quantitybyexception = ViewHolder.get(convertView, R.id.txt_quantitybyexception);

        CommonUtil.loadImageFromUrl(context, img_main, item.getMainImgUrl());
        txt_name.setText(item.getName());
        txt_quantity.setText(String.valueOf(item.getQuantity()));
        txt_quantitybysuccess.setText(String.valueOf(item.getQuantityBySuccess()));
        txt_quantitybyexception.setText(String.valueOf(item.getQuantityByException()));

        return convertView;
    }


}



