package com.uplink.selfstore.own;

import com.uplink.selfstore.http.HttpClient;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.utils.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AppLogcatManager {


    public static void saveLogcat2Server(String cmd,String action) {

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

        FileOutputStream fos=null;
        try {

            Process process = Runtime.getRuntime().exec(cmd);//抓取当前的缓存日志
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(process.getInputStream()));//获取输入流
            //Runtime.getRuntime().exec("logcat -c");//清除是为了下次抓取不会从头抓取
            String line = null;
            String newline = System.getProperty("line.separator");
            int logCount=0;

            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());

            String fileName = "logcat_"+action+"_" + time + "_" + timestamp;
            String fileExt=".log";

            String path = OwnFileUtil.getLogDir();
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filePath = path + "/" + fileName+fileExt;

            fos = new FileOutputStream(filePath);

            while ((line = buffRead.readLine()) != null) {//循环读取每一行

                fos.write((line).getBytes());//追加内容
                fos.write(newline.getBytes());//换行

                logCount++;
                if (logCount > 1000) {//判断是否大于1000行 退出
                    break;
                }
            }

            Runtime.getRuntime().exec("logcat -c");

            fos.flush();
            fos.close();
            fos = null;

            HashMap<String, String> fields = new HashMap<>();
            fields.put("folder", "SelfStoreLogcatLog");
            fields.put("fileName", fileName);
            List<String> filePaths = new ArrayList<>();
            filePaths.add(filePath);

            HttpClient.postFile(Config.URL.uploadfile, fields, filePaths, new HttpResponseHandler() {
                @Override
                public void onSuccess(String response) {
                    if(response!=null){
                        if(response.contains("上传成功")){
                            FileUtil.deleteFile(filePath);
                        }
                    }
                }
            });

        } catch (Exception var1) {

            if(fos!=null){
                try {
                    fos.flush();
                    fos.close();
                    fos = null;
                }
                catch (Exception var2){

                }
            }
        }
    }
}
