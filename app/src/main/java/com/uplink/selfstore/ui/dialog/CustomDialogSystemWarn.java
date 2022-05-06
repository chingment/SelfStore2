package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.SmLoginActivity;
import com.uplink.selfstore.utils.tinytaskonebyone.TinySyncExecutor;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.BitmapUtil;
import com.uplink.selfstore.utils.LongClickUtil;
import com.uplink.selfstore.utils.StringUtil;

public class CustomDialogSystemWarn extends Dialog {
    private static final String TAG = "CustomDialogSystemWarn";
    private View mLayoutRes;
    private Context mContext;
    private Dialog mThis;

    private ImageView img_warn;
    private View btn_close;
    private TextView txt_WarnTitle;
    private View layout_csrQrcode;
    private ImageView img_crsQrcode;
    private View layout_csrPhoneNumber;
    private TextView txt_csrPhoneNumber;
    private TextView wv_csrHelpTips;
    public CustomDialogSystemWarn(final Context context) {
        super(context, R.style.dialog_style);
        mThis=this;
        mContext = context;
        mLayoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_systemwarn, null);

        initView();
        initEvent();
        initData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(mLayoutRes);
    }

    public void setCloseVisibility(int visibility) {
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

        if(StringUtil.isEmptyNotNull(csrPhoneNumber)) {
            this.txt_csrPhoneNumber.setText("");
            this.layout_csrPhoneNumber.setVisibility(View.GONE);
        }
        else {
            this.txt_csrPhoneNumber.setText(csrPhoneNumber);
            this.layout_csrPhoneNumber.setVisibility(View.VISIBLE);
        }
    }

    public void setCsrHelpTip(String csrHelpTip) {

        String html = "<html><head><title></title></head><body>"
                + csrHelpTip
                + "</body></html>";

        //this.wv_csrHelpTips.loadData(html, "text/html", "uft-8");
        this.wv_csrHelpTips.setText("\t\t\t\t"+csrHelpTip);
        if(StringUtil.isEmptyNotNull(csrHelpTip))
        {
            this.wv_csrHelpTips.setVisibility(View.GONE);
        }
        else {

            this.wv_csrHelpTips.setVisibility(View.VISIBLE);
        }
    }

    protected void initView() {
        img_warn= ViewHolder.get(mLayoutRes, R.id.img_warn);
        btn_close= ViewHolder.get(mLayoutRes, R.id.btn_close);
        txt_WarnTitle= ViewHolder.get(mLayoutRes, R.id.txt_WarnTitle);
        layout_csrQrcode= ViewHolder.get(mLayoutRes, R.id.layout_csrQrcode);
        img_crsQrcode= ViewHolder.get(mLayoutRes, R.id.img_crsQrcode);
        layout_csrPhoneNumber= ViewHolder.get(mLayoutRes, R.id.layout_csrPhoneNumber);
        txt_csrPhoneNumber= ViewHolder.get(mLayoutRes, R.id.txt_csrPhoneNumber);
        wv_csrHelpTips=ViewHolder.get(mLayoutRes, R.id.wv_csrHelpTips);
    }

    protected void initEvent() {

        LongClickUtil.setLongClick(new Handler(), img_warn, 500, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TinySyncExecutor.getInstance().clearTask();
                Intent intent = new Intent(mContext, SmLoginActivity.class);
                mContext.startActivity(intent);
                return true;
            }
        });

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }

    protected void initData() {


    }
}
