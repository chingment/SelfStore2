package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.SmLoginActivity;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.BitmapUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.LongClickUtil;
import com.uplink.selfstore.utils.StringUtil;

public class CustomSystemWarnDialog extends Dialog {

    private View layoutRes;// 布局文件
    private Context mContext;
    private ImageView img_warn;
    private View btn_close;
    private TextView txt_WarnTitle;
    private View layout_csrQrcode;
    private ImageView img_crsQrcode;
    private View layout_csrPhoneNumber;
    private TextView txt_csrPhoneNumber;

    public CustomSystemWarnDialog(final Context context) {
        super(context, R.style.dialog_style);
        this.mContext = context;
        this.layoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_systemwarn, null);

        initView();
        initEvent();
        initData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(layoutRes);
    }

    public void setBtnCloseVisibility(int visibility) {
        this.btn_close.setVisibility(visibility);
    }

    public void setWarnTile(String title) {
        this.txt_WarnTitle.setText(title);
    }

    public void setCsrQrcode(String csrQrcode) {
        if(StringUtil.isEmptyNotNull(csrQrcode)) {
            this.layout_csrQrcode.setVisibility(View.GONE);
        }
        else {
            img_crsQrcode.setImageBitmap(BitmapUtil.createQrCodeBitmap(csrQrcode));
            this.layout_csrQrcode.setVisibility(View.VISIBLE);
        }
    }

    public void setCsrPhoneNumber(String csrPhoneNumber) {

        if(StringUtil.isEmptyNotNull(csrPhoneNumber))
        {
            this.txt_csrPhoneNumber.setText("");
            this.layout_csrPhoneNumber.setVisibility(View.GONE);
        }
        else {
            this.txt_csrPhoneNumber.setText(csrPhoneNumber);
            this.layout_csrPhoneNumber.setVisibility(View.VISIBLE);
        }
    }


    protected void initView() {
        img_warn= ViewHolder.get(this.layoutRes, R.id.img_warn);
        btn_close= ViewHolder.get(this.layoutRes, R.id.btn_close);
        txt_WarnTitle= ViewHolder.get(this.layoutRes, R.id.txt_WarnTitle);
        layout_csrQrcode= ViewHolder.get(this.layoutRes, R.id.layout_csrQrcode);
        img_crsQrcode= ViewHolder.get(this.layoutRes, R.id.img_crsQrcode);
        layout_csrPhoneNumber= ViewHolder.get(this.layoutRes, R.id.layout_csrPhoneNumber);
        txt_csrPhoneNumber= ViewHolder.get(this.layoutRes, R.id.txt_csrPhoneNumber);
    }

    protected void initEvent() {

        LongClickUtil.setLongClick(new Handler(), img_warn, 500, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LogUtil.e("长按触发");
                Intent intent = new Intent(mContext, SmLoginActivity.class);
                mContext.startActivity(intent);
                return true;
            }
        });
    }

    protected void initData() {


    }
}
