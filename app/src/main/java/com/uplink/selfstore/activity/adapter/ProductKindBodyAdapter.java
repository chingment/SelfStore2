package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.model.api.ProductKindBean;
import com.uplink.selfstore.model.api.ProductSkuBean;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.ui.my.MyListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chingment on 2018/6/8.
 */

public class ProductKindBodyAdapter extends BaseAdapter {

    private Context context;
    private List<ProductKindBean> items = new ArrayList<>();
    private HashMap<String, ProductSkuBean> productSkus = new HashMap<String, ProductSkuBean>();
    private int current_position;


    public ProductKindBodyAdapter(Context context, List<ProductKindBean> items, HashMap<String, ProductSkuBean> productSkus) {
        this.context = context;
        this.items = items;
        this.productSkus = productSkus;
    }

//    public void notifyDataSetChanged(int position) {
//        current_position = position;
//        notifyDataSetChanged();
//    }

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
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_productkind_body, parent, false);
        }
        ProductKindBean item = items.get(position);

        TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
        txt_name.setText(item.getName());

        // MyGridView list_kind_child =ViewHolder.get(convertView, R.id.list_kind_child);
        MyListView list_kind_child = ViewHolder.get(convertView, R.id.list_kind_child);
        list_kind_child.setFocusable(false);
        list_kind_child.setClickable(false);
        list_kind_child.setPressed(false);
        list_kind_child.setEnabled(false);

//        ProductChildKindAdapter adapter = new ProductChildKindAdapter(context, bean.getChilds(), productSkus);
//
//        adapter.setCallBackListener(new ProductKindSkuAdapter.CallBackListener() {
//            @Override
//            public void callBackImg(ImageView goodsImg) {
//                mCallBackListener.callBackImg(goodsImg);
//            }
//        });
//
//        list_kind_child.setAdapter(adapter);



        return convertView;
    }

    private ProductKindSkuAdapter.CallBackListener mCallBackListener;

    public void setCallBackListener(ProductKindSkuAdapter.CallBackListener mCallBackListener) {
        this.mCallBackListener = mCallBackListener;
    }

    public interface CallBackListener {
        void callBackImg(ImageView goodsImg);
    }
}
