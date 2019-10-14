package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.model.api.GlobalDataSetBean;
import com.uplink.selfstore.model.api.ProductChildKindBean;
import com.uplink.selfstore.model.api.ProductSkuBean;
import com.uplink.selfstore.ui.ViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chingment on 2018/6/8.
 */

public class ProductChildKindAdapter extends BaseAdapter {

    private Context context;
    private List<ProductChildKindBean> items;
    private HashMap<String, ProductSkuBean> productSkus = new HashMap<String, ProductSkuBean>();
    private GlobalDataSetBean globalDataSet;

    public ProductChildKindAdapter(Context context, List<ProductChildKindBean> items, HashMap<String, ProductSkuBean> productSkus) {
        this.context = context;
        this.items = items;
        this.productSkus = productSkus;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_productkind_child, parent, false);
        }
        ProductChildKindBean item = items.get(position);


        TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);

        txt_name.setText(item.getName());

        //GridView list_productsku = ViewHolder.get(convertView, R.id.list_productsku);
        GridView list_productsku = ViewHolder.get(convertView, R.id.list_productsku);
        list_productsku.setFocusable(false);
        list_productsku.setClickable(false);
        list_productsku.setPressed(false);
        list_productsku.setEnabled(false);

        List<ProductSkuBean> productSkusByKind = new ArrayList<>();

        for (String child : item.getChilds()) {

            ProductSkuBean sku = productSkus.get(child);
            if (sku != null) {
                productSkusByKind.add(sku);
            }
        }

        ProductKindSkuAdapter productKindSkuAdapter = new ProductKindSkuAdapter(context, productSkusByKind,globalDataSet);
        productKindSkuAdapter.setCallBackListener(new ProductKindSkuAdapter.CallBackListener() {
            @Override
            public void callBackImg(ImageView goodsImg) {
                // 添加商品到购物车
                if (mCallBackListener != null) {
                    mCallBackListener.callBackImg(goodsImg);
                }
            }
        });
        list_productsku.setAdapter(productKindSkuAdapter);


        return convertView;
    }

    private ProductKindSkuAdapter.CallBackListener mCallBackListener;

    public void setCallBackListener(ProductKindSkuAdapter.CallBackListener mCallBackListener) {
        this.mCallBackListener = mCallBackListener;
    }

}
