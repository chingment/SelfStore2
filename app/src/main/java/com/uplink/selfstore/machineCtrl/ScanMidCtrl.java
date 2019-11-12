package com.uplink.selfstore.machineCtrl;

import android.util.Log;
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

    private byte[] frameHead =new byte[]{(byte)0x55,(byte)0xAA};

    public ScanMidCtrl() {
    }

    public void setTimeOut(int nTimeout) {
        this.nTimeout = nTimeout;
    }

    public int connect() {
        String strPort="/dev/ttymxc2";
        int nBaudrate=115200;
        try {
            mSerialPort = new SerialPort(new File(strPort), nBaudrate, 0);
            this.out = mSerialPort.getOutputStream();
            this.in = mSerialPort.getInputStream();
            return RC_SUCCESS;
        } catch (SecurityException var4) {
            var4.printStackTrace();
            return RC_NOPERMISSION;
        } catch (IOException var5) {
            var5.printStackTrace();
            Log.e(TAG, String.format("connect to %s:%d failed", strPort, nBaudrate));
            return RC_ERROR;
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

                            if (nRead == 0 && byRead[0] != 36) {
                                int nFind = 0;

                                for(i = 1; i < nReaded; ++i) {
                                    if (byRead[i] == 36) {
                                        nFind = i;
                                        break;
                                    }
                                }

                                if (nFind > 0) {
                                    nReaded -= nFind;

                                    for(i = 0; i < nReaded; ++i) {
                                        byRead[i] = byRead[nFind + i];
                                    }
                                } else {
                                    nReaded = 0;
                                }

                                if (this.bLog) {
                                    Log.v(TAG, String.format("Read change to :%d", nReaded));
                                }
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

    public boolean getDeviceStatus() {
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


        if (this.write(sz) != 6) {
            return false;
        } else if (this.read(sz, 6, this.nTimeout) == 6) {
            return sz[2] == -63 & (sz[1] ^ sz[2] ^ sz[3]) == 0;
        } else {
            return false;
        }
    }
}
