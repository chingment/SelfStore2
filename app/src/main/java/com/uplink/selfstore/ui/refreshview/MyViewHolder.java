package com.uplink.selfstore.ui.refreshview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * 项目名称：Pro_selfstoreurance
 * 类描述：
 * 创建人：tuchg
 * 创建时间：17/1/18 14:40
 */

public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private MyItemClickListener itemClickListener;

    public MyViewHolder(View rootView) {
        super(rootView);
    }

    public MyViewHolder(View rootView, MyItemClickListener itemClickListener) {
        super(rootView);
        if (itemClickListener != null) {
            this.itemClickListener = itemClickListener;
            rootView.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (itemClickListener != null) {
            itemClickListener.onItemClick(v, getPosition());
        }
    }
}
