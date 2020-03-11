package com.uplink.selfstore.utils;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.uplink.selfstore.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chingment on 2017/12/18.
 */

public class CommonUtil {

    public static void loadImageFromUrl(Context context, final ImageView photoView, String imageUrl) {
        if (StringUtil.isEmptyNotNull(imageUrl)) {
            photoView.setBackgroundResource(R.drawable.default_image);
        } else
            //.centerCrop()
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

    public static void setListViewHeightBasedOnChildren(ListView listView) {

        // 获取ListView对应的Adapter

        ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;

        for (int i = 0; i < listAdapter.getCount(); i++) { // listAdapter.getCount()返回数据项的数目

            View listItem = listAdapter.getView(i, null, listView);

            listItem.measure(0, 0); // 计算子项View 的宽高
            totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度

        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));

        // listView.getDividerHeight()获取子项间分隔符占用的高度

        // params.height最后得到整个ListView完整显示需要的高度

        listView.setLayoutParams(params);

    }


    public static void setListViewHeightBasedOnChildren(
            View contentlayout, ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null || listAdapter.getCount() == 0) {
            return;
        }

        int totalHeight = 0;
        int count = listAdapter.getCount();
        for (int i = 0; i < count; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        ViewGroup.LayoutParams contentlayout_params = contentlayout
                .getLayoutParams();
        contentlayout_params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1))
                + 13;
        contentlayout.setLayoutParams(contentlayout_params);
    }

    public static void setGridViewHeightBasedOnChildren(GridView gridview) {

        ListAdapter listAdapter = gridview.getAdapter();
        if (listAdapter == null) {
            return;
        }
        // 固定列宽，有多少列
        int numColumns= gridview.getNumColumns(); //5
        int totalHeight = 0;
        // 计算每一列的高度之和
        for (int i = 0; i < listAdapter.getCount(); i += numColumns) {
            // 获取gridview的每一个item
            View listItem = listAdapter.getView(i, null, gridview);
            listItem.measure(0, 0);
            // 获取item的高度和
            totalHeight += listItem.getMeasuredHeight();
        }
        // 获取gridview的布局参数
        ViewGroup.LayoutParams params = gridview.getLayoutParams();
        params.height = totalHeight;
        gridview.setLayoutParams(params);


    }


    public static int px2dip(int pxValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static float dip2px(float dipValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (dipValue * scale + 0.5f);
    }

    public static void setRadioGroupCheckedByStringTag(RadioGroup testRadioGroup, String checkedtag) {
        for (int i = 0; i < testRadioGroup.getChildCount(); i++) {
            RadioButton rb = (RadioButton) testRadioGroup.getChildAt(i);
            String tag = String.valueOf(rb.getTag());

            if (tag.equals(checkedtag)) {
                rb.setChecked(true);
            }
        }
    }

    public static String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        return str;
    }

    public static boolean isDateOneBigger(String str1, String str2) {
        boolean isBigger = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dt1 = null;
        Date dt2 = null;
        try {
            dt1 = sdf.parse(str1);
            dt2 = sdf.parse(str2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (dt1.getTime() > dt2.getTime()) {
            isBigger = true;
        } else if (dt1.getTime() < dt2.getTime()) {
            isBigger = false;
        }
        return isBigger;
    }

    public static String ConvertPrice(float price) {
        String s = String.format("%.2f", price);

        return s;
    }

    public static String[] getPrice(String price) {


        String[] arr = new String[]{"0", ".00"};


        String[] arrPrice = price.split("\\.");

        if (arrPrice.length == -1) {
            arr[0] = price;
        } else {
            if (arrPrice.length >= 2) {
                arr[0] = arrPrice[0];
                arr[1] = "." + arrPrice[1];
            }
        }


        return arr;

    }

    public static Boolean Char2Bool(char c) {
        if(c=='0')
        return false;

        return true;
    }
}
