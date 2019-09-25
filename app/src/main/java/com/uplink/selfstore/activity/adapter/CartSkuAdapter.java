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
import com.uplink.selfstore.model.api.GlobalDataSetBean;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.ui.dialog.CustomConfirmDialog;
import com.uplink.selfstore.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chingment on 2018/7/9.
 */

public class CartSkuAdapter extends BaseAdapter {

    private Context context;
    private List<CartSkuBean> beans = new ArrayList<>();
    private CustomConfirmDialog delete_Dialog;
    private GlobalDataSetBean globalDataSet;

    public CartSkuAdapter(Context context, GlobalDataSetBean globalDataSet, List<CartSkuBean> beans) {
        this.context = context;
        this.beans = beans;
        this.globalDataSet = globalDataSet;

        delete_Dialog = new CustomConfirmDialog(context, context.getString(R.string.activity_cart_tips_delete_confirm), true);

        delete_Dialog.getBtnSure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String skuId = v.getTag().toString();
                operate(CartOperateType.DELETE,"", skuId);
                delete_Dialog.dismiss();

            }
        });

        delete_Dialog.getBtnCancle().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                delete_Dialog.dismiss();
            }
        });
    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return beans.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return beans.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // TODO Auto-generated method stub
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_cart_sku, parent, false);
        }
        final CartSkuBean bean = beans.get(position);

        View btn_delete = ViewHolder.get(convertView, R.id.btn_delete);
        ImageView img_main = ViewHolder.get(convertView, R.id.img_main);
        TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
        TextView txt_price_currencySymbol = ViewHolder.get(convertView, R.id.txt_price_currencySymbol);
        TextView txt_price_integer = ViewHolder.get(convertView, R.id.txt_price_integer);
        TextView txt_price_decimal = ViewHolder.get(convertView, R.id.txt_price_decimal);
        View btn_decrease = ViewHolder.get(convertView, R.id.btn_decrease);
        TextView txt_quantity = ViewHolder.get(convertView, R.id.txt_quantity);
        View btn_increase = ViewHolder.get(convertView, R.id.btn_increase);

        CommonUtil.loadImageFromUrl(context, img_main, bean.getMainImgUrl());
        txt_name.setText(bean.getName());
        txt_quantity.setText(String.valueOf(bean.getQuantity()));
        txt_price_currencySymbol.setText(globalDataSet.getMachine().getCurrencySymbol());
        String[] price = CommonUtil.getPrice(String.valueOf(bean.getSalePrice()));
        txt_price_integer.setText(price[0]);
        txt_price_decimal.setText(price[1]);
//
//
//        //点击图片
//        img_main.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent intent = new Intent(context, ProductDetailsActivity.class);
//                Bundle b = new Bundle();
//                b.putSerializable("dataBean", bean);
//                intent.putExtras(b);
//                context.startActivity(intent);
//            }
//        });
//
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CommonUtil.loadImageFromUrl(context, delete_Dialog.getTipsImage(), bean.getMainImgUrl());
                delete_Dialog.getBtnSure().setTag(bean.getId());
                delete_Dialog.show();

            }
        });


        //点击减去
        btn_decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (bean.getQuantity() == 1)
                    return;
                operate(CartOperateType.DECREASE,bean.getProductId(), bean.getId());
            }
        });


        //点击添加
        btn_increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                operate(CartOperateType.INCREASE,bean.getProductId(), bean.getId());
            }
        });

        return convertView;
    }


    private void operate(int type,String productId, String productSkuId) {
        CartActivity.operate(type,productId, productSkuId, new CarOperateHandler() {
            @Override
            public void onSuccess(String response) {
                notifyDataSetChanged();
            }
        });
    }


}
