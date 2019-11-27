package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.model.LogBean;
import com.uplink.selfstore.ui.ViewHolder;

import java.util.List;

public class LogAdapter extends BaseAdapter {

    private static final String TAG = "LogAdapter";
    private Context context;
    private List<LogBean> items;


    public LogAdapter(Context context,List<LogBean> items) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_log, parent, false);
        }
        final LogBean item = items.get(position);
        TextView txt_content = ViewHolder.get(convertView, R.id.txt_content);
        TextView txt_dateTime = ViewHolder.get(convertView, R.id.txt_dateTime);

        txt_content.setText(item.getContent());
        txt_dateTime.setText(item.getDateTime());
        return convertView;
    }



}
