package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.CartActivity;
import com.uplink.selfstore.activity.ProductDetailsActivity;
import com.uplink.selfstore.activity.handler.CarOperateHandler;
import com.uplink.selfstore.model.api.CartOperateType;
import com.uplink.selfstore.model.api.GlobalDataSetBean;
import com.uplink.selfstore.model.api.ProductBean;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chingment on 2018/6/13.
 */

public class ProductKindSkuAdapter extends BaseAdapter {

    private Context context;
    private List<ProductBean> items = new ArrayList<>();
    private CallBackListener mCallBackListener;
    private GlobalDataSetBean globalDataSet;

    public ProductKindSkuAdapter(Context context, List<ProductBean> items,GlobalDataSetBean globalDataSet) {
        this.context = context;
        this.items = items;
        this.globalDataSet = globalDataSet;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_sku_tmp1, parent, false);
        }

        boolean isGONE = false;
        if (items == null) {
            isGONE = true;
        }

        if (items.size() <= 0) {
            isGONE = true;
        }

       final ProductBean   item = items.get(position);

        if (item == null) {
            isGONE = true;
        }

        if (isGONE) {
            convertView.setVisibility(View.GONE);
            AbsListView.LayoutParams param = new AbsListView.LayoutParams(0, 0);
            convertView.setLayoutParams(param);
            return convertView;
        }

        //convertView.setVisibility(View.VISIBLE);


        final ImageView img_main = ViewHolder.get(convertView, R.id.img_main);
        TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
        TextView txt_price_currencySymbol = ViewHolder.get(convertView, R.id.txt_price_currencySymbol);
        TextView txt_price_integer = ViewHolder.get(convertView, R.id.txt_price_integer);
        TextView txt_price_decimal = ViewHolder.get(convertView, R.id.txt_price_decimal);
        ImageView btn_decrease = ViewHolder.get(convertView, R.id.btn_decrease);
        TextView txt_quantity = ViewHolder.get(convertView, R.id.txt_quantity);
        ImageView btn_increase = ViewHolder.get(convertView, R.id.btn_increase);

        txt_name.setText(item.getName());


        int quantity = CartActivity.getQuantity(item.getRefSku().getId());

        if (quantity == 0) {
            btn_decrease.setVisibility(View.INVISIBLE);
            txt_quantity.setVisibility(View.INVISIBLE);
        } else {
            btn_decrease.setVisibility(View.VISIBLE);
            txt_quantity.setVisibility(View.VISIBLE);
        }

        txt_quantity.setText(String.valueOf(quantity));


        txt_price_currencySymbol.setText(globalDataSet.getMachine().getCurrencySymbol());


        String[] price = CommonUtil.getPrice(String.valueOf(item.getRefSku().getSalePrice()));
        txt_price_integer.setText(price[0]);
        txt_price_decimal.setText(price[1]);


        //点击图片
        img_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ProductDetailsActivity.class);
                Bundle b = new Bundle();
                b.putSerializable("dataBean", item);
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });

        //点击减去
        btn_decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                CartActivity.operate(CartOperateType.DECREASE,item.getId(),item.getRefSku().getId(), new CarOperateHandler() {
                    @Override
                    public void onSuccess(String response) {
                        notifyDataSetChanged();
                    }

                    @Override
                    public void callAnimation() {


                    }
                });
            }
        });


        //点击添加
        btn_increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CartActivity.operate(CartOperateType.INCREASE, item.getId(),item.getRefSku().getId(), new CarOperateHandler() {
                    @Override
                    public void onSuccess(String response) {
                        notifyDataSetChanged();
                    }

                    @Override
                    public void callAnimation() {

                        if (mCallBackListener != null) {
                            mCallBackListener.callBackImg(img_main);
                        }
                    }
                });
            }
        });

        CommonUtil.loadImageFromUrl(context, img_main, item.getMainImgUrl());
        convertView.setClickable(false);


        return convertView;
    }


    public void setCallBackListener(CallBackListener mCallBackListener) {
        this.mCallBackListener = mCallBackListener;
    }

    public interface CallBackListener {
        void callBackImg(ImageView goodsImg);
    }

}
