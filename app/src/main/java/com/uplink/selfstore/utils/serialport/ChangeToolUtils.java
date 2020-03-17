package com.uplink.selfstore.utils.serialport;

import java.util.List;

public  class ChangeToolUtils {
    //-------------------------------------------------------
    // 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
    public static int isOdd(int num) {
        return num & 1;
    }

    //-------------------------------------------------------
    //Hex字符串转int
    public static int hexToInt(String inHex) {
        return Integer.parseInt(inHex, 16);
    }

    //-------------------------------------------------------
    //Hex字符串转byte
    public static byte hexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }

    //-------------------------------------------------------
    //1字节转2个Hex字符
    public static String byte2Hex(Byte inByte) {
        return String.format("%02x", new Object[]{inByte}).toUpperCase();
    }

    //-------------------------------------------------------
    //字节数组转转hex字符串
    public static String byteArrToHex(byte[] inBytArr) {
        StringBuilder strBuilder = new StringBuilder();
        for (byte valueOf : inBytArr) {
            strBuilder.append(byte2Hex(Byte.valueOf(valueOf)));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    //-------------------------------------------------------
    //字节数组转转hex字符串，可选长度
    public static String byteArrToHex(byte[] inBytArr, int offset, int byteCount) {
        StringBuilder strBuilder = new StringBuilder();
        int j = byteCount;
        for (int i = offset; i < j; i++) {
            strBuilder.append(byte2Hex(Byte.valueOf(inBytArr[i])));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    //-------------------------------------------------------
    //把hex字符串转字节数组
    public static byte[] hexToByteArr(String inHex) {
        byte[] result;
        int hexlen = inHex.length();
        if (isOdd(hexlen) == 1) {
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    //-------------------------------------------------------
    //把int转字节数组
    public  static byte[] intToByteArray(int a) {
        return new byte[]{
                (byte) (a & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 24) & 0xFF)
        };
    }




    //-------------------------------------------------------
    //把字节数组转int
    public static int byteArrayToInt(byte[] b) {
        return   b[0] & 0xFF |
                (b[1] & 0xFF) << 8 |
                (b[2] & 0xFF) << 16 |
                (b[3] & 0xFF) << 24;
    }

    public static String intToHex(int i){
        String hex = Integer.toHexString(i);
        return hex;
    }

    public  static byte intToByte(int i){
        String hex = Integer.toHexString(i);
        return  hexToByte(hex);
    }

    public static String byteArrToString(byte[] bytearray) {
        String result = "";
        char temp;
        char s2 = '\u0000';
        int length = bytearray.length;
        for (int i = 0; i < length; i++) {
            temp = (char) bytearray[i];
            if(temp!=s2) {
                result += temp;
            }
        }
        return result;
    }


    public static String hexString2binaryString(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0)
            return null;
        String bString = "", tmp;
        for (int i = 0; i < hexString.length(); i++) {
            tmp = "0000"
                    + Integer.toBinaryString(Integer.parseInt(hexString
                    .substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }

    public static String hexbyte2binaryString(byte bt) {
        String d1= ChangeToolUtils.byte2Hex(bt);
        String b1 =ChangeToolUtils.hexString2binaryString(d1);

        return b1;
    }

    public static byte[] ListToByte(List<byte[]> list) {
        if (list == null || list.size() < 0)
            return null;
        int iLen=0;
        for(int i=0;i<list.size();i++) iLen+=list.get(i).length;
        byte[] bytes=new byte[iLen];
        int k=0;
        for(int i=0;i<list.size();i++){
            byte[] tbyte=list.get(i);
            for(int j=0;j<tbyte.length;j++){
                bytes[k]=tbyte[j];
                k++;
            }
        }
        return bytes;
    }

    public static int byteToInt(byte b){
        System.out.println("byte 是:"+b);
        int x = b & 0xff;
        System.out.println("int 是:"+x);
        return x;
    }


}
