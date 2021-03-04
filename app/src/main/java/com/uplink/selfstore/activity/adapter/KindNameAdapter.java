package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.model.api.KindBean;
import com.uplink.selfstore.ui.ViewHolder;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by chingment on 2018/3/27.
 */

public class KindNameAdapter extends BaseAdapter {
    private static final String TAG = "KindNameAdapter";
    private Context context;
    private List<KindBean> items = new ArrayList<>();
    private int current_position;

    public KindNameAdapter(Context context, List<KindBean> items, int position) {
        this.context = context;
        this.items = items;
        this.current_position = position;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_productkind_name, parent, false);
        }
        KindBean item = items.get(position);

        TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
        View selected_bg = ViewHolder.get(convertView, R.id.selected_bg);
        txt_name.setText(item.getName());

        if (position == current_position) {
            selected_bg.setVisibility(View.VISIBLE);
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        } else {
            selected_bg.setVisibility(View.INVISIBLE);
            convertView.setBackgroundColor(ContextCompat.getColor(context,R.color.gray));
        }
        return convertView;
    }
}
