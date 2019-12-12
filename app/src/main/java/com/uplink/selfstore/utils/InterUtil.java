package com.uplink.selfstore.utils;

public class InterUtil {

    public static String arrayTransformString(int[] arr,String sign) {
        if(arr==null)
            return "";
        if(arr.length==0)
            return "";

        StringBuffer sb = new StringBuffer();
        for(int i=0;i<arr.length;i++){
            sb.append(arr[i]+sign);
        }

        String str="";
        if(sb.length()>1) {
            str=sb.substring(0,sb.length()-1);
        }



        return str;
    }
}
