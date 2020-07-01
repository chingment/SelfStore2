package com.uplink.selfstore.deviceCtrl;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.uplink.selfstore.utils.serialport.ChangeToolUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android_serialport_api.SerialPort;

public class ScannerCtrl {
    private boolean isConnect=false;
    private static String TAG = "ScannerCtrl";
    private static ScannerCtrl mScannerCtrl = null;
    private static SerialPort mSerialPort = null;
    private OutputStream out = null;
    private InputStream in = null;
    private static String ComId="ttymxc3";
    private static boolean mRunFlag;
    public static final int MESSAGE_WHAT_SCANRESULT=1;

    private int message_what=-1;

    private ReadThread readThread=null;

    private ScannerCtrl() {

    }

    public static ScannerCtrl getInstance() {
        if (mScannerCtrl == null) {
            synchronized (ScannerCtrl.class) {
                if (mScannerCtrl == null) {
                    mScannerCtrl = new ScannerCtrl();
                }
            }
        }
        return mScannerCtrl;

    }

    public void setComId(String comId) {
        ScannerCtrl.ComId = comId;
    }

    public String getComId() {
        return ScannerCtrl.ComId;
    }

    public boolean connect() {
        String strPort = "/dev/"+ ScannerCtrl.ComId;
        int nBaudrate = 115200;
        try {
            File file = new File(strPort);
            if (file.exists()) {
                mSerialPort = new SerialPort(file, nBaudrate, 0);
                this.out = mSerialPort.getOutputStream();
                this.in = mSerialPort.getInputStream();
                isConnect = true;
                mRunFlag=true;
                readThread = new ReadThread();
                readThread.start();
            }
        } catch (SecurityException var4) {
            var4.printStackTrace();
            isConnect = false;
            mRunFlag=false;
            return isConnect;
        } catch (IOException var5) {
            var5.printStackTrace();
            Log.e(TAG, String.format("connect to %s:%d failed", strPort, nBaudrate));
            isConnect = false;
            mRunFlag=false;
            return isConnect;
        } finally {
            return isConnect;
        }
    }

    public void disConnect() {
        try {
            isConnect = false;
            mRunFlag=false;

            if (readThread != null) {
                readThread.interrupt();
                readThread = null;
            }

            if (this.out != null) {
                this.out.close();
            }

            if (this.in != null) {
                this.in.close();
            }

            if (mSerialPort != null) {
                mSerialPort.close();
            }

        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    public boolean isConnect()
    {
        return  isConnect;
    }

    private class ReadThread extends Thread {

        private ArrayList<Byte> buffer_List = new ArrayList<Byte>();

        private ReadThread() {

        }

        @Override
        public void run() {
            super.run();
            //判断进程是否在运行，更安全的结束进程
            while (mRunFlag) {

                try {
                    Thread.currentThread();
                    Thread.sleep(30L);
                } catch (InterruptedException var15) {
                    var15.printStackTrace();
                }

                //Log.d(TAG, "进入读线程监听接收扫描数据");

                byte[] buffer = new byte[1024];
                int size; //读取数据的大小
                try {

                    //Log.d(TAG, "availableLen：" + availableLen);

//                    size = in.read(buffer);
//                    Log.d(TAG, "扫描结果数据HEX：" + ChangeToolUtils.byteArrToHex(buffer, 0, size));
//                    Log.d(TAG, "扫描结果数据长度：" + String.valueOf(size));
//                    String scanResult = ChangeToolUtils.byteArrToString(buffer);
//                    Log.d(TAG, "扫描结果数据内容:" + scanResult);
//                    sendHandlerMessage(scanResult);


                    int availableLen=in.available();
                    //Log.d(TAG, "availableLen：" + availableLen);
                    if(availableLen>0) {

                        size = in.read(buffer,0,availableLen);

                        for (int i=0; i< size;i++) {
                            buffer_List.add(buffer[i]);
                        }
                    }
                    else {
                        if(buffer_List.size()>0) {
                            byte[] by = new byte[buffer_List.size()];
                            for (int i = 0; i < buffer_List.size(); i++) {
                                by[i] = buffer_List.get(i);
                            }

                            Log.d(TAG, "扫描结果数据HEX：" + ChangeToolUtils.byteArrToHex(by, 0, by.length));
                            Log.d(TAG, "扫描结果数据长度：" + String.valueOf(by.length));
                            String scanResult = ChangeToolUtils.byteArrToString(by);
                            Log.d(TAG, "扫描结果数据内容:" + scanResult);
                            sendHandlerMessage(scanResult);
                        }
                        buffer_List.clear();
                    }

                } catch (IOException e) {
                    Log.e(TAG, "扫描数据读取异常：" + e.toString());
                }
            }
        }
    }

    private Handler scanHandler = null;

    public void setScanHandler(Handler handler) {
        this.scanHandler = handler;
    }

    private void sendHandlerMessage(String result) {
        if(scanHandler!=null) {
            Message m = new Message();
            m.what = message_what;
            Bundle data=new Bundle();
            data.putString("result",result);
            m.setData(data);
            scanHandler.sendMessage(m);
        }
    }

    public void  setMessageWhat(int message_what){
        this.message_what=message_what;
    }
}
