package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uplink.selfstore.R;

public class CustomSlotEditDialog extends Dialog {

    private View layoutRes;// 布局文件
    private Context context;
    private View btn_close;

    public CustomSlotEditDialog(Context context) {
        super(context, R.style.dialog_style);
        this.context = context;
        this.layoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_slotedit, null);

        btn_close = (View) this.layoutRes.findViewById(R.id.btn_close);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(layoutRes);

        final Dialog _this=this;
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _this.dismiss();
            }
        });
    }
}
