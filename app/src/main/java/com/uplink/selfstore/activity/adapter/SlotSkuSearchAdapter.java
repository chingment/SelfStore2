package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.model.api.SearchProductSkuBean;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.CommonUtil;

import java.util.List;

public class SlotSkuSearchAdapter extends BaseAdapter {

    private Context context;
    private List<SearchProductSkuBean> items;
    private SlotSkuSearchAdapter.CallBackListener mCallBackListener;

    public SlotSkuSearchAdapter(Context context, List<SearchProductSkuBean> items) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_sku_tmp3, parent, false);
        }

        final SearchProductSkuBean item = items.get(position);


        ImageView img_main = ViewHolder.get(convertView, R.id.img_main);
        TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);


        txt_name.setText(item.getName());
        CommonUtil.loadImageFromUrl(context, img_main, item.getMainImgUrl());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mCallBackListener != null) {
                    mCallBackListener.setSlot(item);
                }

            }
        });

        return convertView;
    }

    public void setCallBackListener(SlotSkuSearchAdapter.CallBackListener mCallBackListener) {
        this.mCallBackListener = mCallBackListener;
    }

    public interface CallBackListener {
        void setSlot(SearchProductSkuBean skuBean);
    }

}
