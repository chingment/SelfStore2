package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.model.common.NineGridItemBean;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.CommonUtil;

import java.util.List;


public class NineGridItemdapter extends BaseAdapter {

    private List<NineGridItemBean> items;
    private Context context;

    public NineGridItemdapter(Context context, List<NineGridItemBean> items) {
        this.items = items;
        this.context = context;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_gridview_ninegrid, parent, false);
        }

        NineGridItemBean bean = items.get(position);

        ImageView item_img = ViewHolder.get(convertView, R.id.item_ninegrid_icon);
        TextView item_title = ViewHolder.get(convertView, R.id.item_ninegrid_title);
        item_title.setText(bean.getTitle());


        if (bean.getIcon() instanceof Integer) {
            item_img.setImageDrawable(context.getResources().getDrawable(((int) bean.getIcon())));
        } else {
            CommonUtil.loadImageFromUrl(context, item_img, bean.getIcon().toString());
        }

        return convertView;
    }
}
