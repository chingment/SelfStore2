package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.uplink.selfstore.R;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.ui.my.MyListView;

public class CustomImSeatListDialog extends Dialog {
    private static final String TAG = "CustomImSeatListDialog";
    private View layoutRes;// 布局文件
    private View btn_close;
    private MyListView list_seats;
    public CustomImSeatListDialog(final Context context) {
        super(context, R.style.dialog_style);
        this.layoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_seatlist, null);
        initView();
        initEvent();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(layoutRes);
    }

    protected void initView() {
        btn_close = ViewHolder.get(this.layoutRes, R.id.btn_close);
        list_seats= ViewHolder.get(this.layoutRes, R.id.list_seats);
        list_seats.setFocusable(false);
        list_seats.setClickable(false);
    }

    protected void initEvent() {


        final Dialog _this = this;

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _this.dismiss();
            }
        });
    }


    private OnLinster l;

    public void  setOnLinster(OnLinster l){
        this.l=l;
    }

    public  interface OnLinster{
        public void setSeats(MyListView v);
    }

    @Override
    public void show() {
        super.show();
        this.l.setSeats(this.list_seats);
    }
}
