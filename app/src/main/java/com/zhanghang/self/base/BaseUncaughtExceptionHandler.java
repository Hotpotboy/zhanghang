package com.zhanghang.self.base;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by hangzhang209526 on 2016/1/4.
 */
public class BaseUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    /**默认异常处理*/
public final static int DEAFULT_EXCEPTION_HANDLER = 0;
    /**将异常写入文件之中*/
    public final static int FILE_EXCEPTION_HANDLER = 1;
    /**未捕获异常的处理方式*/
    private int mExceptionHanlderType = DEAFULT_EXCEPTION_HANDLER;

    private BaseApplication mBaseApplication;

    public BaseUncaughtExceptionHandler(BaseApplication application){
        mBaseApplication = application;
    }

    public void setExceptionHandlerType(int type){
        mExceptionHanlderType = type;
    }
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        dealException(thread, ex);
    }

    /***
     * 可以用来扩展更多的异常处理方式，默认为两种
     * @param thread
     * @param ex
     */
    protected void dealException(Thread thread, Throwable ex){
        switch (mExceptionHanlderType){
            case DEAFULT_EXCEPTION_HANDLER:
//                Thread.UncaughtExceptionHandler deafult = Thread.getDefaultUncaughtExceptionHandler();
//                if(deafult!=null){
//                    deafult.uncaughtException(thread, ex);
//                }

                StringWriter stringWriter = new StringWriter();
                new Exception(ex).printStackTrace(new PrintWriter(stringWriter));
                Log.e("erro",stringWriter.toString());
                break;
            case FILE_EXCEPTION_HANDLER:
                fileDealException(thread, ex);
                break;
        }
    }

    private void fileDealException(Thread thread, Throwable ex){
        StringBuffer sb = new StringBuffer();
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);//打印异常
        Throwable cause = ex.getCause();//打印异常的引起异常
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        Log.e("erro",sb.toString());

        //生成文件
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {//如果sd卡存在，且正常安装好（MEDIA_MOUNTED的含义）
            StringBuffer filePath = new StringBuffer(Environment.getExternalStorageDirectory().getAbsolutePath());
            String fileName = "log.txt";
            String company = mBaseApplication.getMetaData("company");
            if(TextUtils.isEmpty(company)) company = "deafult";
            filePath.append(File.separator).append(company).append(File.separator);
            File file = new File(filePath.toString());
            if(!file.exists()) {
                file.mkdirs();
            }
            FileOutputStream fos = null;
            try {
                file = new File(file.getAbsolutePath()+File.separator+fileName);
                boolean isSuc = false;
                if(!file.exists()){
                    isSuc = file.createNewFile();
                }
                fos = new FileOutputStream(file);
                fos.write(sb.toString().getBytes());
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(fos!=null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
