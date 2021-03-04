package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.uplink.selfstore.R;
import com.uplink.selfstore.model.chat.SkuBean;

import java.util.ArrayList;
import java.util.List;

public class VideoSkuAdapter extends BaseAdapter {

    private static final String TAG = "VideoSkuAdapter";
    private Context context;
    private List<SkuBean> items = new ArrayList<>();

    public VideoSkuAdapter(Context context, List<SkuBean> items) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_sku_tmp4, parent, false);
        }


        final SkuBean item = items.get(position);

        final ImageView img_main =convertView.findViewById( R.id.img_main);
        TextView txt_name = convertView.findViewById( R.id.txt_name);


        txt_name.setText(item.getName());

        loadImageFromUrl(context, img_main, item.getMainImgUrl());


        convertView.setClickable(false);


        return convertView;
    }


    private   void loadImageFromUrl(Context context, final ImageView photoView, String imageUrl) {

        Picasso.with(context).load(imageUrl)
                .placeholder(R.drawable.default_image).fit().centerInside()
                .into(photoView, new Callback() {
                    @Override
                    public void onSuccess() {

                        photoView.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onError() {
                        photoView.setBackgroundResource(R.drawable.default_image);
                    }
                });
    }
}
