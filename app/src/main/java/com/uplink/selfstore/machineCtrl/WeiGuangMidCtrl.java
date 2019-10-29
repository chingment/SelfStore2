package com.uplink.selfstore.machineCtrl;

import android.util.Log;

import com.uplink.selfstore.utils.serialport.ChangeToolUtils;
import com.uplink.selfstore.utils.serialport.SerialPortUtils;

public class WeiGuangMidCtrl {

    private String TAG = "WeiGuangMidCtrl";
    private SerialPortUtils serialPortUtils;


    public WeiGuangMidCtrl() {


        serialPortUtils = new SerialPortUtils(3, 115200);

        //串口数据监听事件
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void onDataReceive(byte[] buffer, int size) {
                //接收到的串口数据进行分析
                dataAnalysis(buffer, size);
            }
        });



    }

    public  void open() {
        if (serialPortUtils != null) {
            serialPortUtils.openSerialPort();
        }
    }

    //接收到的串口数据进行分析
    private void dataAnalysis(byte [] buffer,int length) {

        Log.d(TAG, "正在处理读逻辑");
        String str1 = ChangeToolUtils.byteArrToString(buffer);
        Log.d(TAG, "内容:"+str1);
        onSendUIReport.OnSendUI(1, 1, str1);
    }

    public void close(){
        if (serialPortUtils != null) {
            serialPortUtils.closeSerialPort();
        }
    }

    //type 类型   1： 接受数据读取条形码数据
    //status 状态 1： 正常
    //content 内容
    private OnSendUIReport onSendUIReport = null;
    public interface OnSendUIReport {
        void OnSendUI(int type,int status,String content);//发送信息到界面处理
    }
    public void setOnSendUIReport(OnSendUIReport dataReceiveListener) {
        onSendUIReport = dataReceiveListener;
    }
}
