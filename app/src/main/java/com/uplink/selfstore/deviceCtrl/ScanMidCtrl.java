package com.uplink.selfstore.deviceCtrl;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.uplink.selfstore.utils.DateUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.serialport.ChangeToolUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public class ScanMidCtrl {
    private boolean isConnect=false;
    private static String TAG = "ScanMidCtrl";
    private static SerialPort mSerialPort = null;
    private OutputStream out = null;
    private InputStream in = null;

    public static final int MESSAGE_WHAT_SCANRESULT=1;

    private ReadThread readThread=null;
    public ScanMidCtrl() {

    }

    public void connect() {
        String strPort = "/dev/ttymxc2";
        int nBaudrate = 115200;
        try {
            mSerialPort = new SerialPort(new File(strPort), nBaudrate, 0);
            this.out = mSerialPort.getOutputStream();
            this.in = mSerialPort.getInputStream();
            readThread = new ReadThread();
            readThread.start();

            isConnect=true;
        } catch (SecurityException var4) {
            var4.printStackTrace();
        } catch (IOException var5) {
            var5.printStackTrace();
            Log.e(TAG, String.format("connect to %s:%d failed", strPort, nBaudrate));
        }
    }

    public void disconnect() {
        try {
            if (this.out != null) {
                this.out.close();
            }

            if (this.in != null) {
                this.in.close();
            }

            if (mSerialPort != null) {
                mSerialPort.close();
            }

            if(readThread!=null)
            {
                readThread.onStop();
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

        private boolean mRunFlag;

        private ReadThread() {
            mRunFlag = true;
        }

        private void onStop() {
            LogUtil.d("线程停止");
            mRunFlag = false;
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

                Log.d(TAG, "进入读线程监听接收扫描数据");

                byte[] buffer = new byte[1024];
                int size; //读取数据的大小
                try {
                    size = in.read(buffer);
                    Log.d(TAG, "扫描数据：" + ChangeToolUtils.byteArrToHex(buffer, 0, size));
                    Log.d(TAG, "扫描数据长度：" + String.valueOf(size));
                    String scanResult = ChangeToolUtils.byteArrToString(buffer);
                    Log.d(TAG, "扫描数据内容:" + scanResult);
                    sendHandlerMessage(scanResult);
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
            m.what = MESSAGE_WHAT_SCANRESULT;
            Bundle data=new Bundle();
            data.putString("result",result);
            m.setData(data);
            scanHandler.sendMessage(m);
        }
    }
}
