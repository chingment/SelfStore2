package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.CartActivity;
import com.uplink.selfstore.activity.handler.CarOperateHandler;
import com.uplink.selfstore.model.api.CartOperateType;
import com.uplink.selfstore.model.api.CartSkuBean;
import com.uplink.selfstore.model.api.OrderDetailsSkuBean;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.ui.dialog.CustomConfirmDialog;
import com.uplink.selfstore.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chingment on 2019/2/28.
 */

public class OrderDetailsSkuAdapter extends BaseAdapter {


    private Context context;
    private List<OrderDetailsSkuBean> beans = new ArrayList<>();


    public OrderDetailsSkuAdapter(Context context, List<OrderDetailsSkuBean> beans) {
        this.context = context;
        this.beans = beans;
    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return beans.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return beans.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // TODO Auto-generated method stub
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_orderdetails_sku, parent, false);
        }

        final OrderDetailsSkuBean bean = beans.get(position);


        ImageView img_main = ViewHolder.get(convertView, R.id.img_main);
        TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
        TextView txt_quantity = ViewHolder.get(convertView, R.id.txt_quantity);
        TextView txt_quantitybysuccess = ViewHolder.get(convertView, R.id.txt_quantitybysuccess);

        CommonUtil.loadImageFromUrl(context, img_main, bean.getImgUrl());
        txt_name.setText(bean.getName());
        txt_quantity.setText(String.valueOf(bean.getQuantity()));
        txt_quantitybysuccess.setText(String.valueOf(bean.getQuantityBySuccess()));

        return convertView;
    }


}



