package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.uplink.selfstore.model.api.ImgSetBean;
import com.uplink.selfstore.utils.CommonUtil;

import java.util.List;

/**
 * Created by chingment on 2018/6/5.
 */

public class BannerAdapter extends PagerAdapter {
    private static final String TAG = "BannerAdapter";
    private Context context;
    private List<ImgSetBean> items;
    private ImageView.ScaleType scaleType;

    public BannerAdapter(Context context, List<ImgSetBean> items, ImageView.ScaleType scaleType) {
        this.context = context;
        this.items = items;
        this.scaleType = scaleType;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        if (items.size() > 0) {
            ImgSetBean item = items.get(position);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(-1, -1);
            imageView.setLayoutParams(params);
            //item.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            //ImageView.ScaleType.FIT_XY
            imageView.setScaleType(scaleType);
            CommonUtil.loadImageFromUrl(context, imageView, item.getUrl());
            container.addView(imageView);
        }

        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }


}
