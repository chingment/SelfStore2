package com.uplink.selfstore.activity.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.model.api.ImgSetBean;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chingment on 2018/6/5.
 */

public class BannerAdapter extends PagerAdapter {

    private Context context;
    private List<ImgSetBean> beans = new ArrayList<>();
    private ImageView.ScaleType scaleType;

    public BannerAdapter(Context context, List<ImgSetBean> beans, ImageView.ScaleType scaleType) {
        this.context = context;
        this.beans = beans;
        this.scaleType = scaleType;
    }

    @Override
    public int getCount() {
        return beans.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView item = new ImageView(context);
        if (beans.size() > 0) {
            ImgSetBean bean = beans.get(position);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(-1, -1);
            item.setLayoutParams(params);
            //item.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            //ImageView.ScaleType.FIT_XY
            item.setScaleType(scaleType);
            
            LogUtil.d("图片："+bean.getUrl());
            CommonUtil.loadImageFromUrl(context, item, bean.getUrl());
            container.addView(item);
        }

        return item;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }


}
