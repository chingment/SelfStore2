package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chingment on 2018/7/16.
 */

public class CustomNumKeyDialog extends Dialog {
    private View layoutRes;// 布局文件
    private Context context;


    private LinearLayout txt_all;

    private List<String> number = new ArrayList<>();

    public CustomNumKeyDialog(Context context) {
        super(context, R.style.dialog_style);
        this.context = context;
        this.layoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_numkey, null);


        number.add("");
        number.add("");
        number.add("");
        number.add("");
        number.add("");
        number.add("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(layoutRes);


        final Dialog _this=this;

        txt_all = (LinearLayout) this.layoutRes.findViewById(R.id.txt_all);
        final LinearLayout btn_close = (LinearLayout) this.layoutRes.findViewById(R.id.btn_close);
        final Button btn_0 = (Button) this.layoutRes.findViewById(R.id.btn_0);
        final Button btn_1 = (Button) this.layoutRes.findViewById(R.id.btn_1);
        final Button btn_2 = (Button) this.layoutRes.findViewById(R.id.btn_2);
        final Button btn_3 = (Button) this.layoutRes.findViewById(R.id.btn_3);
        final Button btn_4 = (Button) this.layoutRes.findViewById(R.id.btn_4);
        final Button btn_5 = (Button) this.layoutRes.findViewById(R.id.btn_5);
        final Button btn_6 = (Button) this.layoutRes.findViewById(R.id.btn_6);
        final Button btn_7 = (Button) this.layoutRes.findViewById(R.id.btn_7);
        final Button btn_8 = (Button) this.layoutRes.findViewById(R.id.btn_8);
        final Button btn_9 = (Button) this.layoutRes.findViewById(R.id.btn_9);
        final LinearLayout btn_delete = (LinearLayout) this.layoutRes.findViewById(R.id.btn_delete);
        final LinearLayout btn_sure = (LinearLayout) this.layoutRes.findViewById(R.id.btn_sure);

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _this.dismiss();
            }
        });


        btn_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String num = btn_0.getTag().toString();
                getNum(num);

            }
        });

        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String num = btn_1.getTag().toString();
                getNum(num);
            }
        });

        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String num = btn_2.getTag().toString();
                getNum(num);
            }
        });

        btn_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = btn_3.getTag().toString();
                getNum(num);

            }
        });

        btn_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String num = btn_4.getTag().toString();
                getNum(num);
            }
        });

        btn_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = btn_5.getTag().toString();
                getNum(num);

            }
        });

        btn_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String num = btn_6.getTag().toString();
                getNum(num);
            }
        });

        btn_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String num = btn_7.getTag().toString();
                getNum(num);
            }
        });

        btn_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = btn_8.getTag().toString();
                getNum(num);

            }
        });

        btn_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = btn_9.getTag().toString();
                getNum(num);

            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                int count = txt_all.getChildCount();

                for (int i = count - 1; i >= 0; i--) {

                    if (txt_all.getChildAt(i) instanceof TextView) {

                        TextView txt = (TextView) txt_all.getChildAt(i);
                        if (!StringUtil.isEmptyNotNull(txt.getText().toString())) {
                            txt.setText("");
                            number.set(i, "");
                            txt.setBackground(context.getResources().getDrawable(R.drawable.dialog_numkey_input_normal));
                            break;
                        }
                    }
                }
            }
        });


        btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String s_number = "";
                for (int i = 0; i < number.size(); i++) {
                    s_number += number.get(i);
                }

                surekListener.onClick(v, s_number);
            }
        });


    }

    private void getNum(String num) {
        LogUtil.e("num:" + num);


        int count = txt_all.getChildCount();

        for (int i = 0; i < count; i++) {

            if (txt_all.getChildAt(i) instanceof TextView) {

                TextView txt = (TextView) txt_all.getChildAt(i);
                if (StringUtil.isEmptyNotNull(txt.getText().toString())) {
                    txt.setText(num);
                    txt.setBackground(context.getResources().getDrawable(R.drawable.dialog_numkey_input_fill));
                    number.set(i, num);
                    break;
                }

            }
        }

    }

    public OnSureListener surekListener;

    public void setOnSureListener(OnSureListener surekListener) {
        this.surekListener = surekListener;
    }


    public interface OnSureListener {
        void onClick(View v, String number);
    }

    @Override
    public void show() {
        super.show();

        int count = txt_all.getChildCount();

        for (int i = 0; i < count; i++) {
            if (txt_all.getChildAt(i) instanceof TextView) {
                TextView txt = (TextView) txt_all.getChildAt(i);
                number.set(i, "");
                txt.setText("");
                txt.setBackground(context.getResources().getDrawable(R.drawable.dialog_numkey_input_normal));
            }
        }

    }
}
