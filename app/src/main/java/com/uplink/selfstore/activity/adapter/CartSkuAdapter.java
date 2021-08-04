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
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.ui.dialog.CustomDialogConfirm;
import com.uplink.selfstore.utils.CommonUtil;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by chingment on 2018/7/9.
 */

public class CartSkuAdapter extends BaseAdapter {
    private static final String TAG = "CartSkuAdapter";
    private Context context;
    private CustomDialogConfirm dialog_Confirm;
    private List<CartSkuBean> items = new ArrayList<>();
    public CartSkuAdapter(Context context, LinkedHashMap<String, CartSkuBean> cartSkus) {
        this.context = context;



        for(String key : cartSkus.keySet()) {
            items.add(cartSkus.get(key));
        }
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_cart_sku, parent, false);
        }
        final CartSkuBean item = items.get(position);

        View btn_delete = ViewHolder.get(convertView, R.id.btn_delete);
        ImageView img_main = ViewHolder.get(convertView, R.id.img_main);
        TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
        TextView txt_price_currencySymbol = ViewHolder.get(convertView, R.id.txt_price_currencySymbol);
        TextView txt_price_integer = ViewHolder.get(convertView, R.id.txt_price_integer);
        TextView txt_price_decimal = ViewHolder.get(convertView, R.id.txt_price_decimal);
        View btn_decrease = ViewHolder.get(convertView, R.id.btn_decrease);
        TextView txt_quantity = ViewHolder.get(convertView, R.id.txt_quantity);
        View btn_increase = ViewHolder.get(convertView, R.id.btn_increase);

        TextView tag_isTrgVideoService = ViewHolder.get(convertView, R.id.tag_isTrgVideoService);

        CommonUtil.loadImageFromUrl(context, img_main, item.getMainImgUrl());
        txt_name.setText(item.getName());
        txt_quantity.setText(String.valueOf(item.getQuantity()));
        txt_price_currencySymbol.setText(item.getCurrencySymbol());
        String[] price = CommonUtil.getPrice(String.valueOf(item.getSalePrice()));
        txt_price_integer.setText(price[0]);
        txt_price_decimal.setText(price[1]);

        if(item.isTrgVideoService()){
            tag_isTrgVideoService.setVisibility(View.VISIBLE);
        }
        else {
            tag_isTrgVideoService.setVisibility(View.GONE);
        }


        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(dialog_Confirm==null) {
                    dialog_Confirm = new CustomDialogConfirm(context, context.getString(R.string.aty_cart_confirmtips_delete), true);
                }

                dialog_Confirm.setTipsImageNetwork(item.getMainImgUrl());
                dialog_Confirm.setOnClickListener(new CustomDialogConfirm.OnClickListener() {
                    @Override
                    public void onSure() {
                        CartSkuBean sku = (CartSkuBean) dialog_Confirm.getTag();
                        operate(CartOperateType.DELETE, sku.getSkuId());
                        dialog_Confirm.hide();
                        dialog_Confirm.dismiss();
                        dialog_Confirm=null;
                    }

                    @Override
                    public void onCancle() {
                        dialog_Confirm.hide();
                    }
                });
                dialog_Confirm.setTag(item);
                dialog_Confirm.show();

            }
        });


        //点击减去
        btn_decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.getQuantity() == 1)
                    return;
                operate(CartOperateType.DECREASE, item.getSkuId());
            }
        });


        //点击添加
        btn_increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                operate(CartOperateType.INCREASE, item.getSkuId());
            }
        });

        return convertView;
    }


    private void operate(int type, String skuId) {
        CartActivity.operate(type, skuId, new CarOperateHandler() {
            @Override
            public void onSuccess(String response) {
                notifyDataSetChanged();
            }
        });
    }

    public void dismiss() {
        if (dialog_Confirm != null) {
            dialog_Confirm.cancel();
            dialog_Confirm = null;
        }
    }

}
