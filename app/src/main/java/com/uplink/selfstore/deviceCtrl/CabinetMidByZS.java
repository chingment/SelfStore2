package com.uplink.selfstore.deviceCtrl;

import android.util.Log;

import com.uplink.selfstore.utils.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public class CabinetMidByZS {
    private String TAG = "ScanMidCtrl";
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
                return RC_SUCCESS;
            } catch (SecurityException var4) {
                var4.printStackTrace();
                return RC_NOPERMISSION;
            } catch (IOException var5) {
                var5.printStackTrace();
                LogUtil.e(TAG, String.format("connect to %s:%d failed", strPort, nBaudrate));
                return 0;
            }
        }
    }

    public int unlock(int plate,int num) {

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


        if (this.write(sz) != sz.length) {
            return RC_WRITEERROR;
        } else if (this.read(sz, 7, this.nTimeout) != 7) {
            return RC_READERROR;
        } else if (sz[3] != 97) {
            return RC_ERROR;
        } else {
            return RC_SUCCESS;
        }
    }

    private int write(byte[] byData) {

        String strLog = String.format("write[%d]:", byData.length);

        for (int i = 0; i < byData.length; ++i) {
            strLog = strLog + String.format("%02x ", byData[i]);
        }

        LogUtil.v(TAG, strLog);

        if (this.out == null) {
            return 0;
        } else {
            try {
                this.out.write(byData);
                return byData.length;
            } catch (IOException var4) {
                var4.printStackTrace();
                return RC_WRITEERROR;
            }
        }
    }

    private int read(byte[] byRead, int nLen, int nTimeout) {
        if (byRead.length < nLen) {
            LogUtil.e(TAG, String.format("the Read buffer is Out of Bound[%d < %d]", byRead.length, nLen));
            nLen = byRead.length;
        }

        int nRead = 0;
        if (this.in != null) {
            int nMaxLen = nLen;
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


                            if (nRead == 0 && byRead[0] != 36) {
                                int nFind = 0;

                                for (i = 1; i < nReaded; ++i) {
                                    if (byRead[i] == 36) {
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
            } catch (IOException var16) {
                var16.printStackTrace();
            }
        }

        return nRead;
    }
}
