package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tamic.statinterface.stats.core.TcStatInterface;
import com.uplink.selfstore.R;
import com.uplink.selfstore.model.api.ImSeatBean;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class ImSeatAdapter extends BaseAdapter {

    private static final String TAG = "ImSeatAdapter";
    private Context context;
    private List<ImSeatBean> items = new ArrayList<>();
    public ImSeatAdapter(Context context, List<ImSeatBean> items) {
        this.context = context;
        this.items=items;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_seat, parent, false);
        }
        final ImSeatBean item = items.get(position);

        TextView txt_userName = ViewHolder.get(convertView, R.id.txt_userName);
        TextView txt_briefDes = ViewHolder.get(convertView, R.id.txt_briefDes);
        ImageView img_avatar = ViewHolder.get(convertView, R.id.img_avatar);
        Button btn_call = ViewHolder.get(convertView, R.id.btn_call);

        txt_briefDes.setText(item.getBriefDes());
        txt_userName.setText(item.getUserName());
        CommonUtil.loadImageFromUrl(context, img_avatar, item.getAvatar());


        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemListener!=null) {
                    onItemListener.call(item);
                }
            }
        });

        return convertView;
    }

    private OnItemListener onItemListener;

    public void  setOnLinster(OnItemListener l){
        this.onItemListener=l;
    }

    public  interface OnItemListener{
        public void call(ImSeatBean v);
    }

}
