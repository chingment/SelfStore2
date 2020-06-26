package com.uplink.selfstore.own;

import android.os.Environment;

public  class OwnFileUtil {

    public  static String getLogDir(){

        //String mSaveDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SelfStore";


        String dir = Environment.getExternalStorageDirectory() + "/SelfStoreLog";


        return dir;
    }

    public  static String getPicSaveDir(){

        //String mSaveDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SelfStore";

        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/SelfStore";


        return dir;
    }

    public  static String getMovieSaveDir(){

        //String mSaveDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SelfStore";

        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/SelfStore";


        return dir;
    }
}
