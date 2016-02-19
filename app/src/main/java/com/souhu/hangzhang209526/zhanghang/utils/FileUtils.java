package com.souhu.hangzhang209526.zhanghang.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/1/26.
 */
public class FileUtils {
    /**
     * 遍历指定目录下的所有文件
     * @param diectory
     * @return 该目录下的所有文件
     */
    public static ArrayList<File> traversalFiles(File diectory){
        if(diectory!=null&&diectory.isDirectory()){
            ArrayList<File> result = new ArrayList<File>();
            File[] files = diectory.listFiles();
            for(File item:files){
                if(item!=null){
                    if(item.isFile()){
                        result.add(item);
                    }else{
                        result.addAll(traversalFiles(item));//递归
                    }
                }
            }
            return result;
        }
        return null;
    }

    /**
     * 复制文件
     * @param from
     * @param to
     * @return
     */
    public static boolean copyFile(File from, File to) {
        BufferedInputStream bis = null;
        OutputStream dexWriter = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(from));
            dexWriter = new BufferedOutputStream(new FileOutputStream(to));
            byte[] buf = new byte[1024];
            int len;
            while ((len = bis.read(buf, 0, 1024)) > 0) {
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            bis.close();
            return true;
        } catch (IOException e) {
            if (dexWriter != null) {
                try {
                    dexWriter.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            return false;
        }
    }
}
