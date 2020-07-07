package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chingment on 2018/7/16.
 */

public class CustomNumKeyDialog extends Dialog {
    private static final String TAG = "CustomNumKeyDialog";

    private View mLayoutRes;
    private Context mContext;
    private Dialog mThis;

    // private LinearLayout txt_all;
    private TextView txt_val;

    public CustomNumKeyDialog(Context context) {
        super(context, R.style.dialog_style);
        mContext= context;
        mLayoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_numkey, null);
        mThis=this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(mLayoutRes);

        txt_val = ViewHolder.get(mLayoutRes,R.id.txt_val);
        final LinearLayout btn_close = ViewHolder.get(mLayoutRes,R.id.btn_close);
        final Button btn_0 = ViewHolder.get(mLayoutRes,R.id.btn_0);
        final Button btn_1 = ViewHolder.get(mLayoutRes,R.id.btn_1);
        final Button btn_2 = ViewHolder.get(mLayoutRes,R.id.btn_2);
        final Button btn_3 = ViewHolder.get(mLayoutRes,R.id.btn_3);
        final Button btn_4 = ViewHolder.get(mLayoutRes,R.id.btn_4);
        final Button btn_5 = ViewHolder.get(mLayoutRes,R.id.btn_5);
        final Button btn_6 = ViewHolder.get(mLayoutRes,R.id.btn_6);
        final Button btn_7 = ViewHolder.get(mLayoutRes,R.id.btn_7);
        final Button btn_8 =ViewHolder.get(mLayoutRes,R.id.btn_8);
        final Button btn_9 = ViewHolder.get(mLayoutRes,R.id.btn_9);

        final ImageView btn_delete = ViewHolder.get(mLayoutRes,R.id.btn_delete);
        final LinearLayout btn_clear = ViewHolder.get(mLayoutRes,R.id.btn_clear);
        final LinearLayout btn_sure = ViewHolder.get(mLayoutRes,R.id.btn_sure);

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThis.dismiss();
            }
        });


        btn_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //playSound(R.raw.s0);
                String num = btn_0.getTag().toString();
                getNum(num);

            }
        });

        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //playSound(R.raw.s1);
                String num = btn_1.getTag().toString();
                getNum(num);
            }
        });

        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //playSound(R.raw.s2);
                String num = btn_2.getTag().toString();
                getNum(num);
            }
        });

        btn_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //playSound(R.raw.s3);
                String num = btn_3.getTag().toString();
                getNum(num);

            }
        });

        btn_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //playSound(R.raw.s4);
                String num = btn_4.getTag().toString();
                getNum(num);
            }
        });

        btn_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //playSound(R.raw.s5);
                String num = btn_5.getTag().toString();
                getNum(num);

            }
        });

        btn_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //playSound(R.raw.s6);
                String num = btn_6.getTag().toString();
                getNum(num);
            }
        });

        btn_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //playSound(R.raw.s7);
                String num = btn_7.getTag().toString();
                getNum(num);
            }
        });

        btn_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //playSound(R.raw.s8);
                String num = btn_8.getTag().toString();
                getNum(num);

            }
        });

        btn_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //playSound(R.raw.s9);
                String num = btn_9.getTag().toString();
                getNum(num);

            }
        });

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt_val.setText("");
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String val= txt_val.getText().toString();
                if(val.length()>=1) {
                    val = val.substring(0, val.length() - 1);
                }
                txt_val.setText(val);
            }
        });

        btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String val= txt_val.getText().toString();
                surekListener.onClick(v, val);
            }
        });


    }
    private void playSound(int id) {
        MediaPlayer mediaPlayer = MediaPlayer.create(mContext, id);
        mediaPlayer.start();

    }

    private void getNum(String num) {
        String val=txt_val.getText()+num;
        txt_val.setText(val);
    }

    private OnSureListener surekListener;

    public void setOnSureListener(OnSureListener surekListener) {
        this.surekListener = surekListener;
    }


    public interface OnSureListener {
        void onClick(View v, String number);
    }

    @Override
    public void show() {
        super.show();
        txt_val.setText("");
    }
}
