package com.uplink.selfstore.deviceCtrl;

import android.util.Log;

import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.serialport.ChangeToolUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public class ScanMidCtrl {
    private static String TAG = "ScanMidCtrl";
    public static int RC_SUCCESS = 0;
    public static int RC_INVALID_PARAM = 1;
    public static int RC_ERROR = 2;
    public static int RC_NOPERMISSION = 3;
    public static int RC_WRITEERROR = 4;
    public static int RC_READERROR = 5;
    private static SerialPort mSerialPort = null;
    private OutputStream out = null;
    private InputStream in = null;
    private int nTimeout = 50;
    private boolean TRUE = true;
    private boolean FALSE = false;
    private boolean bLog = true;

    private boolean isReadStream = false; //线程状态，为了安全终止线程
    private byte[] frameHead =new byte[]{(byte)0x55,(byte)0xAA};

    public ScanMidCtrl() {

    }

    public void setTimeOut(int nTimeout) {
        this.nTimeout = nTimeout;
    }

    public boolean connect() {
        String strPort="/dev/ttymxc2";
        int nBaudrate=115200;
        try {
            mSerialPort = new SerialPort(new File(strPort), nBaudrate, 0);
            this.out = mSerialPort.getOutputStream();
            this.in = mSerialPort.getInputStream();
            this.isReadStream=true;
            new ReadThread().start();
            return true;
        } catch (SecurityException var4) {
            var4.printStackTrace();
            return false;
        } catch (IOException var5) {
            var5.printStackTrace();
            Log.e(TAG, String.format("connect to %s:%d failed", strPort, nBaudrate));
            return false;
        }
    }

    public void disconnect() {
        try {
            isReadStream=false;
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

    private int write(byte[] byData) {
        if (this.out == null) {
            return 0;
        } else {
            try {
                this.out.write(byData);
                if (this.bLog) {
                    String strLog = String.format("write[%d]:", byData.length);

                    for(int i = 0; i < byData.length; ++i) {
                        strLog = strLog + String.format("%02x ", byData[i]);
                    }

                    Log.v(TAG, strLog);
                }

                return byData.length;
            } catch (IOException var4) {
                var4.printStackTrace();
                return RC_WRITEERROR;
            }
        }
    }

    private int clear() {
        if (this.in != null) {
            try {
                int nReady = this.in.available();
                if (nReady > 0) {
                    return (int)this.in.skip((long)nReady);
                }
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

        return RC_SUCCESS;
    }

    private int read(byte[] byRead, int nLen, int nTimeout) {
        if (byRead.length < nLen) {
            Log.e(TAG, String.format("the Read buffer is Out of Bound[%d < %d]", byRead.length, nLen));
            nLen = byRead.length;
        }

        int nRead = 0;
        if (this.in != null) {
            int nMaxLen = nLen;
            long nStart = System.currentTimeMillis();
            long nEnd = System.currentTimeMillis();

            boolean bTryAgain = false;

            try {
                for(; nRead < nMaxLen && (nEnd - nStart <= (long)nTimeout || bTryAgain); nEnd = System.currentTimeMillis()) {
                    try {
                        Thread.currentThread();
                        Thread.sleep(20L);
                    } catch (InterruptedException var15) {
                        var15.printStackTrace();
                    }

                    int nReadReady = this.in.available();
                    if (nReadReady > nMaxLen - nRead) {
                        nReadReady = nMaxLen - nRead;
                    }

                    if (nReadReady <= 0) {
                        bTryAgain = false;
                    } else {
                        int nReaded = this.in.read(byRead, nRead, nReadReady);
                        if (nReaded > 0) {
                            int i;
                            if (this.bLog) {
                                String strLog = String.format("Read[%d]:", nReaded);

                                for(i = 0; i < nReaded; ++i) {
                                    strLog = strLog + String.format("%02x ", byRead[i]);
                                }

                                Log.v(TAG, strLog);
                            }
                            nRead = nReaded;
                        }

                        bTryAgain = true;
                    }
                }
            } catch (IOException var16) {
                var16.printStackTrace();
            }
        }

        return nRead;
    }

    public boolean isNormarl() {
        byte[] sz = new byte[6];
        sz[0] = (byte) 0x55;
        sz[1] = (byte) 0xAA;
        sz[2] = (byte) 0x01;
        sz[3] = (byte) 0x00;
        sz[4] = (byte) 0x00;

        byte xorAns = 0x00;

        for (int j = 0; j < sz.length - 1; j++) {
            xorAns = (byte) (xorAns ^ sz[j]);
        }

        sz[5] = xorAns;


        byte[] rd = new byte[9];

        if (this.write(sz) != 6) {
            return false;
        } else if (this.read(rd, 9, this.nTimeout) == 9) {
            return rd[3] == 0 & (rd[0] ^ rd[1] ^ rd[2] ^ rd[3] ^ rd[4] ^ rd[5] ^ rd[6]^ rd[7]) == 3;
        } else {
            return false;
        }
    }

    private class ReadThread extends Thread{
        @Override
        public void run() {
            super.run();
            //判断进程是否在运行，更安全的结束进程
            while (isReadStream){

                try {
                    currentThread().sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                Log.d(TAG, "进入读线程run");
                byte[] buffer = new byte[1024];
                int size; //读取数据的大小
                try {
                    size = in.read(buffer);
                    Log.d(TAG, "run: 接收到了数据：" + ChangeToolUtils.byteArrToHex(buffer,0,size));
                    Log.d(TAG, "run: 接收到了数据大小：" + String.valueOf(size));

                    //判断长度是否大于3个字节，有效命令数据
                    if (size > 3){

                        byte cmd=buffer[2];

                        switch (cmd)
                        {
                            case (byte)0x30:
                                LogUtil.d("解释扫描结果");
                                byte[] data=new byte[size-6];
                                for(int i=0;i<data.length;i++)
                                {
                                    //String str1 = ChangeToolUtils.byte2Hex(buffer[i+5]);
                                    //Log.d(TAG, "str1:"+str1);
                                    data[i]=buffer[i+5];
                                }
                                String scanResult = ChangeToolUtils.byteArrToString(data);
                                Log.d(TAG, "扫描内容:"+scanResult);
                                if(scanListener!=null) {
                                    scanListener.receive(scanResult);
                                }
                                break;
                                default:
                                    break;
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "run: 数据读取异常：" +e.toString());
                }
            }
        }
    }

    private ScanListener scanListener = null;

    public void setScanListener(ScanListener scanListener) {
        this.scanListener = scanListener;
    }

    public interface ScanListener {
        //1 异常错误
        void receive(String result);
    }
}
