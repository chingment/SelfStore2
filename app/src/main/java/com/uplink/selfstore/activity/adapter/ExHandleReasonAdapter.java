package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.CartActivity;
import com.uplink.selfstore.activity.ProductDetailsActivity;
import com.uplink.selfstore.activity.handler.CarOperateHandler;
import com.uplink.selfstore.model.api.CartOperateType;
import com.uplink.selfstore.model.api.ExHandleReasonBean;
import com.uplink.selfstore.model.api.GlobalDataSetBean;
import com.uplink.selfstore.model.api.ProductSkuBean;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExHandleReasonAdapter extends BaseAdapter {

    private static final String TAG = "ExHandleReasonAdapter";
    private Context context;
    private List<ExHandleReasonBean> items = new ArrayList<>();

    public ExHandleReasonAdapter(Context context, List<ExHandleReasonBean> items) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_exhandlereason, parent, false);
        }

        final ExHandleReasonBean item = items.get(position);

        CheckBox cbx_IsChecked = ViewHolder.get(convertView, R.id.cbx_IsChecked);

        cbx_IsChecked.setText(item.getTitle());

        cbx_IsChecked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean ischeked=cbx_IsChecked.isChecked();
                if(ischeked){
                    cbx_IsChecked.setChecked(true);

                    items.get(position).setChecked(true);
                }
                else
                {
                    cbx_IsChecked.setChecked(false);

                    items.get(position).setChecked(false);
                }


            }
        });

        return convertView;
    }

}
