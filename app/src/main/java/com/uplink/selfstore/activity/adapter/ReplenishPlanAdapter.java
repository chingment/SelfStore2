package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.model.api.ReplenishPlanBean;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class ReplenishPlanAdapter extends BaseAdapter {

    private static final String TAG = "ReplenishPlanAdapter";
    private Context context;
    private List<ReplenishPlanBean> items = new ArrayList<>();
    public ReplenishPlanAdapter(Context context, List<ReplenishPlanBean> items) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_replenishplan, parent, false);
        }

        final ReplenishPlanBean item = items.get(position);

        TextView tv_PlanCumCode = ViewHolder.get(convertView, R.id.tv_PlanCumCode);
        TextView tv_Status = ViewHolder.get(convertView, R.id.tv_Status);
        TextView tv_RsherName = ViewHolder.get(convertView, R.id.tv_RsherName);
        TextView tv_RshTime = ViewHolder.get(convertView, R.id.tv_RshTime);
        TextView tv_MakerName = ViewHolder.get(convertView, R.id.tv_MakerName);
        TextView tv_MakeTime = ViewHolder.get(convertView, R.id.tv_MakeTime);
        TextView btn_Handle = ViewHolder.get(convertView, R.id.btn_Handle);
        tv_PlanCumCode.setText(item.getPlanCumCode());
        tv_Status.setText(item.getStatus().getText());
        tv_RsherName.setText(item.getRsherName());
        tv_RshTime.setText(item.getRshTime());
        tv_MakerName.setText(item.getMakerName());
        tv_MakeTime.setText(item.getMakeTime());


        if(item.getStatus().getValue()==1) {
            btn_Handle.setVisibility(View.VISIBLE);
            btn_Handle.setTag(item);
            btn_Handle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ReplenishPlanBean l_Bean = (ReplenishPlanBean) view.getTag();
                    if (onClickListener != null) {
                        onClickListener.onClick(l_Bean);
                    }
                }
            });
        }else {
            btn_Handle.setVisibility(View.GONE);
        }

        return convertView;
    }


    private OnClickListener onClickListener;

    public void  setOnClickListener(OnClickListener l){
        this.onClickListener=l;
    }

    public  interface OnClickListener{
        void onClick(ReplenishPlanBean v);
    }
}
