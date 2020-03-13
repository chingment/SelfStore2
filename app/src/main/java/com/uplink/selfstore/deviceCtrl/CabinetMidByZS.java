package com.uplink.selfstore.deviceCtrl;

import com.uplink.selfstore.model.ZSCabBoxBean;
import com.uplink.selfstore.model.ResultBean;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.serialport.ChangeToolUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import android_serialport_api.SerialPort;

public class CabinetMidByZS {
    private String TAG = "CabinetMidByZS";


    public static int RC_SUCCESS = 0;
    public static int RC_INVALID_PARAM = 1;
    public static int RC_ERROR = 2;
    public static int RC_NOPERMISSION = 3;
    public static int RC_WRITEERROR = 4;
    public static int RC_READERROR = 5;

    private static SerialPort mSerialPort = null;
    private OutputStream out = null;
    private InputStream in = null;
    private int nTimeout = 200;
    private boolean isReadStop = false;
    public int connect(String strPort, int nBaudrate) {
        if (strPort.equals("")) {
            LogUtil.e(TAG, "the serial path is null");
            return RC_INVALID_PARAM;
        } else if (nBaudrate != 2400 && nBaudrate != 4800 && nBaudrate != 9600 && nBaudrate != 19200 && nBaudrate != 38400 && nBaudrate != 57600 && nBaudrate != 115200) {
            LogUtil.e(TAG, String.format("the baudrate %d is not supported", nBaudrate));
            return RC_INVALID_PARAM;
        } else {
            try {
                mSerialPort = new SerialPort(new File("/dev/" + strPort), nBaudrate, 0);
                this.out = mSerialPort.getOutputStream();
                this.in = mSerialPort.getInputStream();

                isReadStop=false;
                new ReadThread().start();

                return RC_SUCCESS;
            } catch (SecurityException var4) {
                var4.printStackTrace();
                return RC_NOPERMISSION;
            } catch (IOException var5) {
                var5.printStackTrace();
                LogUtil.e(TAG, String.format("connect to %s:%d failed", strPort, nBaudrate));
                return RC_ERROR;
            }
        }
    }

    public int unLock(int plate, int num) {

        byte[] sz = new byte[9];
        sz[0] = 0x06;
        sz[1] = 0x05;
        sz[2] = 0x00;
        sz[3] = 0x11;
        sz[4] = 0x00;
        sz[5] = (byte) plate;
        sz[6] = (byte) num;
        sz[7] = (byte) (sz[1] ^ sz[2] ^ sz[3] ^ sz[4] ^ sz[5] ^ sz[6]);
        sz[8] = 0x08;

        return  this.write(sz);
    }


    private void printWD(byte[] data){
        if(data==null){
            LogUtil.v(TAG, "数据为空");
            return;
        }

        String strLog = String.format("write[%d]:", data.length);

        for (int i = 0; i < data.length; ++i) {
            strLog = strLog + String.format("%02x ", data[i]);
        }

        LogUtil.v(TAG, strLog);
    }

    private int write(byte[] data) {
        printWD(data);
        if (this.out == null) {
            return RC_WRITEERROR;
        } else {
            try {
                this.out.write(data);
                return RC_SUCCESS;
            } catch (IOException ex) {
                ex.printStackTrace();
                return RC_WRITEERROR;
            }
        }
    }

    private int read(byte[] byRead,int nTimeout) {
        int nRead = 0;
        int t1Len=-1;
        if (this.in != null) {
            int nMaxLen = byRead.length;
            long nStart = System.currentTimeMillis();
            long nEnd = System.currentTimeMillis();

            boolean bTryAgain = false;

            try {
                for (; nRead < nMaxLen && (nEnd - nStart <= (long) nTimeout || bTryAgain); nEnd = System.currentTimeMillis()) {
                    try {
                        Thread.currentThread();
                        Thread.sleep(20L);
                    } catch (InterruptedException var15) {
                        var15.printStackTrace();
                    }

                    int nReadReady = this.in.available();
                    t1Len=nReadReady;
                    if (nReadReady > nMaxLen - nRead) {
                        nReadReady = nMaxLen - nRead;
                    }

                    if (nReadReady <= 0) {
                        bTryAgain = false;
                    } else {
                        int nReaded = this.in.read(byRead, nRead, nReadReady);
                        if (nReaded > 0) {
                            int i;

                            String strLog = String.format("Read[%d]:", nReaded);

                            for (i = 0; i < nReaded; ++i) {
                                strLog = strLog + String.format("%02x ", byRead[i]);
                            }

                            LogUtil.v(TAG, strLog);


                            if (nRead == 0 && byRead[0] !=6) {
                                int nFind = 0;

                                for (i = 1; i < nReaded; ++i) {
                                    if (byRead[i] == 6) {
                                        nFind = i;
                                        break;
                                    }
                                }

                                if (nFind > 0) {
                                    nReaded -= nFind;

                                    for (i = 0; i < nReaded; ++i) {
                                        byRead[i] = byRead[nFind + i];
                                    }
                                } else {
                                    nReaded = 0;
                                }


                                LogUtil.v(TAG, String.format("Read change to :%d", nReaded));

                            }

                            nRead += nReaded;
                        }

                        bTryAgain = true;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return t1Len;
    }


    public int read2(byte[] byRead, int nLen, int nTimeout) {
        if (byRead.length < nLen) {
            LogUtil.e(TAG, String.format("the Read buffer is Out of Bound[%d < %d]", byRead.length, nLen));
            nLen = byRead.length;
        }

        int nRead = 0;

        int nMaxLen = nLen;
        long nStart = System.currentTimeMillis();
        long nEnd = System.currentTimeMillis();

        boolean bTryAgain = false;

        for (; nRead < nMaxLen && (nEnd - nStart <= (long) nTimeout || bTryAgain); nEnd = System.currentTimeMillis()) {
            try {
                Thread.currentThread();
                Thread.sleep(20L);
            } catch (InterruptedException var15) {
                var15.printStackTrace();
            }

            int nReadReady = 11;
            if (nReadReady > nMaxLen - nRead) {
                nReadReady = nMaxLen - nRead;
            }

            if (nReadReady <= 0) {
                bTryAgain = false;
            } else {
                int nReaded = 11;
                if (nReaded > 0) {
                    int i;

                    String strLog = String.format("Read[%d]:", nReaded);

                    for (i = 0; i < nReaded; ++i) {
                        strLog = strLog + String.format("%02x ", byRead[i]);
                    }

                    LogUtil.v(TAG, strLog);


                    if (nRead == 0 && byRead[0] != 6) {
                        int nFind = 0;

                        for (i = 1; i < nReaded; ++i) {
                            if (byRead[i] == 6) {
                                nFind = i;
                                break;
                            }
                        }

                        if (nFind > 0) {
                            nReaded -= nFind;

                            for (i = 0; i < nReaded; ++i) {
                                byRead[i] = byRead[nFind + i];
                            }
                        } else {
                            nReaded = 0;
                        }


                        LogUtil.v(TAG, String.format("Read change to :%d", nReaded));

                    }

                    nRead += nReaded;
                }

                bTryAgain = true;
            }


        }

        byRead[2]=0x08;
        return nRead;
    }




    private class ReadThread extends Thread{
        @Override
        public void run() {
            super.run();
            //判断进程是否在运行，更安全的结束进程
            while (!isReadStop){
                LogUtil.d(TAG, "ZS格子柜：进入接受数据线程");
                byte[] buffer = new byte[1024];
                int size;
                try {
                    size =in.read(buffer);
                    if (size > 0){
                        LogUtil.d(TAG, "ZS格子柜： 接收到了数据：" + ChangeToolUtils.byteArrToHex(buffer,0,size));
                        LogUtil.d(TAG, "ZS格子柜： 接收到了数据大小：" + String.valueOf(size));
                        onDataReceiveListener.onDataReceive(buffer,size);
                    }
                } catch (IOException e) {
                    LogUtil.e(TAG, "ZS格子柜： 数据读取异常：" +e.toString());
                }
            }
        }
    }

    public OnDataReceiveListener onDataReceiveListener = null;
    public static interface OnDataReceiveListener {
        public void onDataReceive(byte[] buffer, int size);
    }
    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

}
