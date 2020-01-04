package com.uplink.selfstore.service;

import com.uplink.selfstore.utils.DateUtil;
import com.uplink.selfstore.utils.FileUtil;

import java.io.File;
import java.io.FilenameFilter;

public class AlarmService {


    public static void autoClear(final String dirPath, final int autoClearDay) {
        FileUtil.delete(dirPath, new FilenameFilter() {

            @Override
            public boolean accept(File file, String filename) {
                String s = FileUtil.getFileNameWithoutExtension(filename);
                int day = autoClearDay < 0 ? autoClearDay : -1 * autoClearDay;
                String date = "crash-" + DateUtil.getOtherDay(day);
                return date.compareTo(s) >= 0;
            }
        });
    }
}
