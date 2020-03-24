package com.uplink.selfstore.deviceCtrl;

import com.uplink.selfstore.utils.DateUtil;
import com.uplink.selfstore.utils.LogUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

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
    private static boolean isReadStop = false;
    private static boolean isReadRunning=false;
    private static ReadThread readThread;
    private ArrayList<Byte> buffer_List = new ArrayList<Byte>();
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

                if(!isReadRunning) {
                    isReadRunning = true;
                    isReadStop = false;
                    readThread = new ReadThread();
                    readThread.start();
                }

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
        buffer_List.clear();
        return  this.write(sz);
    }

    public int queryLockStatus(int plate,int num) {

        byte[] sz = new byte[9];
        sz[0] = 0x06;
        sz[1] = 0x05;
        sz[2] = 0x01;
        sz[3] = 0x11;
        sz[4] = 0x00;
        sz[5] = (byte) plate;
        sz[6] = (byte) num;
        sz[7] = (byte) (sz[1] ^ sz[2] ^ sz[3] ^ sz[4] ^ sz[5] ^ sz[6]);
        sz[8] = 0x08;
        buffer_List.clear();
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

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isReadStop) {
                try {
                    if(in==null) {
                        LogUtil.i(TAG,"[" + DateUtil.getStringDate() + "] in is null");
                    }
                    else {
                        LogUtil.i(TAG,"[" + DateUtil.getStringDate() + "] in is not null");
                        int size;

                        byte[] buffer = new byte[512];

                        int av= in.available();

                        LogUtil.i(TAG, "[" + DateUtil.getStringDate()  + "] in data available:" + av);


                        size = in.read(buffer,0,buffer.length);

                        LogUtil.i(TAG, "[" + DateUtil.getStringDate()  + "] in data size:" + size);

                        if (size > 0 && buffer_List.size() < 11) {
                            for (int i = 0; i < size; i++) {
                                buffer_List.add(buffer[i]);
                            }
                        }


                        if (buffer_List.size() == 11) {
                            byte[] by = new byte[11];
                            for (int i = 0; i < buffer_List.size(); i++) {
                                by[i] = buffer_List.get(i);
                            }
                            onDataReceiveListener.onDataReceive(by);
                            buffer_List.clear();
                        }

                    }

                    Thread.sleep(20L);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void  disconnect() {
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

            isReadStop = true;
            isReadRunning = false;
            if (readThread != null) {
                readThread.interrupt();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public OnDataReceiveListener onDataReceiveListener = null;

    public static interface OnDataReceiveListener {
        public void onDataReceive(byte[] buffer_List);
        public void onSendLog(String message);
    }
    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

}
