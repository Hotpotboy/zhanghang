package com.souhu.hangzhang209526.zhanghang.utils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/1/26.
 */
public class FileUtils {
    /**
     * 遍历指定目录下的所有文件
     * @param diectory
     * @return
     */
    public static ArrayList<File> searchFiles(File diectory){
        if(diectory!=null&&diectory.isDirectory()){
            ArrayList<File> result = new ArrayList<File>();
            File[] files = diectory.listFiles();
            for(File item:files){
                if(item!=null){
                    if(item.isFile()){
                        result.add(item);
                    }else{
                        result.addAll(searchFiles(item));//递归
                    }
                }
            }
            return result;
        }
        return null;
    }
}
