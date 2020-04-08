package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.model.api.ExHandleOrderDetailItemBean;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.CommonUtil;

import java.util.List;

public class ExHandleOrderDetailItemAdapter  extends BaseAdapter {

    private static final String TAG = "ExHandleOrderDetailItemAdapter";

    private Context context;
    private List<ExHandleOrderDetailItemBean> items;


    public ExHandleOrderDetailItemAdapter(Context context, List<ExHandleOrderDetailItemBean> items) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_exhandleorderdetailitem, parent, false);
        }

        final ExHandleOrderDetailItemBean item = items.get(position);


        ImageView img_main = ViewHolder.get(convertView, R.id.img_main);
        TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
        TextView txt_quantity = ViewHolder.get(convertView, R.id.txt_quantity);
        CheckBox cbx_sign_pickup = ViewHolder.get(convertView, R.id.cbx_sign_pickup);
        CheckBox cbx_sign_unpickup = ViewHolder.get(convertView, R.id.cbx_sign_unpickup);

        cbx_sign_pickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                cbx_sign_pickup.setChecked(true);
                cbx_sign_unpickup.setChecked(false);
            }
        });

        cbx_sign_unpickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cbx_sign_pickup.setChecked(false);
                cbx_sign_unpickup.setChecked(true);

            }
        });

        CommonUtil.loadImageFromUrl(context, img_main, item.getMainImgUrl());
        txt_name.setText(item.getName());
        txt_quantity.setText(String.valueOf(item.getQuantity()));


       
        return convertView;
    }
}
