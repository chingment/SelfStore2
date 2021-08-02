package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.CommonUtil;

/**
 * <p>
 * Title: CustomDialog
 * </p>
 * <p>
 * Description:自定义Dialog（参数传入Dialog样式文件，Dialog布局文件）
 * </p>
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 *
 * @author archie
 * @version 1.0
 */
public class CustomDialogConfirm extends Dialog {
    private static final String TAG = "CustomDialogConfirm";
    private CustomDialogConfirm mThis;
    private Context mContext;
    private View mLayoutRes;

    private View dlg_Close;
    private LinearLayout ll_Cancle;
    private Button btn_Sure;
    private Button btn_Cancle;
    private TextView tv_TipsText;
    private ImageView iv_TipsImage;
    private Object tag;


    public void setTipsImageDrawable(Drawable drawable) {
        this.iv_TipsImage.setImageDrawable(drawable);
    }

    public void  setTipsImageNetwork(String url){
        CommonUtil.loadImageFromUrl(mContext,iv_TipsImage, url);
    }

    public void setTipsImageVisibility(int visibility){
        iv_TipsImage.setVisibility(visibility);
    }

    public  ImageView getTipsImage(){
        return iv_TipsImage;
    }

    public void setTipsText(String tips){
        tv_TipsText.setText(tips);
    }

    public void setTipsText(int tips){
        tv_TipsText.setText(tips);
    }

    public void setCloseVisibility(int visibility){
        dlg_Close.setVisibility(visibility);
    }

    public void setCancleVisibility(int visibility){
    }
    public void setTag(Object tag){
        this.tag=tag;
    }

    public Object getTag(){
        return this.tag;
    }

    public CustomDialogConfirm(Context context, String tips, boolean isCancle) {
        super(context, R.style.dialog_style);
        mThis=this;
        mContext = context;
        mLayoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_confirm, null);

        dlg_Close =ViewHolder.get(mLayoutRes,R.id.dlg_Close);
        tv_TipsText = ViewHolder.get(mLayoutRes,R.id.tv_TipsText);
        tv_TipsText.setText(tips);
        iv_TipsImage = ViewHolder.get(mLayoutRes,R.id.iv_TipsImage);
        btn_Sure = ViewHolder.get(mLayoutRes,R.id.btn_Sure);
        btn_Cancle = ViewHolder.get(mLayoutRes,R.id.btn_Cancle);
        ll_Cancle = ViewHolder.get(mLayoutRes,R.id.ll_Cancle);

        if (!isCancle) {
            ll_Cancle.setVisibility(View.GONE);
        }

        dlg_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThis.hide();
            }
        });

        btn_Sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onClickListener!=null){
                    onClickListener.onSure();
                }
            }
        });

        btn_Cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onClickListener!=null){
                    onClickListener.onCancle();
                }
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(mLayoutRes);
    }

    private OnClickListener onClickListener;

    public void  setOnClickListener(OnClickListener onClickListener){
        this.onClickListener=onClickListener;
    }

    public  interface OnClickListener{
        void onSure();
        void onCancle();
    }

}