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
import com.uplink.selfstore.model.api.ExHandleOrderBean;
import com.uplink.selfstore.model.api.ExHandleOrderDetailItemBean;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.CommonUtil;

import java.util.List;
import java.util.Map;

public class ExHandleOrderDetailItemAdapter  extends BaseAdapter {

    private static final String TAG = "ExHandleOrderDetailItemAdapter";

    private Context context;
    private List<ExHandleOrderDetailItemBean> exOrderDetailItems;
    private List<ExHandleOrderBean> exOrders;
    public ExHandleOrderDetailItemAdapter(Context context,List<ExHandleOrderBean> exOrders, List<ExHandleOrderDetailItemBean> exOrderDetailItems) {
        this.context = context;
        this.exOrders = exOrders;
        this.exOrderDetailItems=exOrderDetailItems;
    }


    @Override
    public int getCount() {
        return exOrderDetailItems.size();
    }

    @Override
    public Object getItem(int position) {
        return exOrderDetailItems.get(position);
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

        final ExHandleOrderDetailItemBean exOrderDetailItem = exOrderDetailItems.get(position);


        ImageView img_main = ViewHolder.get(convertView, R.id.img_main);
        TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
        TextView txt_quantity = ViewHolder.get(convertView, R.id.txt_quantity);
        CheckBox cbx_sign_pickup = ViewHolder.get(convertView, R.id.cbx_sign_pickup);
        CheckBox cbx_sign_unpickup = ViewHolder.get(convertView, R.id.cbx_sign_unpickup);
        TextView txt_statusName = ViewHolder.get(convertView, R.id.txt_statusName);


        if(exOrderDetailItem.isCanHandle()){
            cbx_sign_pickup.setVisibility(View.VISIBLE);
            cbx_sign_unpickup.setVisibility(View.VISIBLE);
        }
        else {
            cbx_sign_pickup.setVisibility(View.GONE);
            cbx_sign_unpickup.setVisibility(View.GONE);
        }

        cbx_sign_pickup.setTag(exOrderDetailItem.getUniqueId());
        cbx_sign_pickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String uniqueId=v.getTag().toString();

                for (int i=0;i<exOrders.size();i++){
                    for (int j=0;j<exOrders.get(i).getDetailItems().size();j++){
                        if(exOrders.get(i).getDetailItems().get(j).getUniqueId().equals(uniqueId)){
                            exOrders.get(i).getDetailItems().get(j).setSignStatus(1);
                            break;
                        }
                    }
                }

                cbx_sign_pickup.setChecked(true);
                cbx_sign_unpickup.setChecked(false);
            }
        });

        cbx_sign_unpickup.setTag(exOrderDetailItem.getUniqueId());
        cbx_sign_unpickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String uniqueId=v.getTag().toString();
                for (int i=0;i<exOrders.size();i++){
                    for (int j=0;j<exOrders.get(i).getDetailItems().size();j++){
                        if(exOrders.get(i).getDetailItems().get(j).getUniqueId().equals(uniqueId)){
                            exOrders.get(i).getDetailItems().get(j).setSignStatus(2);
                            break;
                        }
                    }
                }

                cbx_sign_pickup.setChecked(false);
                cbx_sign_unpickup.setChecked(true);

            }
        });

        CommonUtil.loadImageFromUrl(context, img_main, exOrderDetailItem.getMainImgUrl());
        txt_name.setText(exOrderDetailItem.getName());
        txt_quantity.setText(String.valueOf(exOrderDetailItem.getQuantity()));

        txt_statusName.setText(exOrderDetailItem.getStatus().getText());

       
        return convertView;
    }
}
