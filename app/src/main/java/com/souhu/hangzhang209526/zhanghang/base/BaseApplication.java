package com.souhu.hangzhang209526.zhanghang.base;

import android.app.Application;
import android.content.pm.ApplicationInfo;

/**
 * Created by hangzhang209526 on 2016/1/4.
 */
public class BaseApplication extends Application {

    private static BaseApplication instance;

    private ApplicationInfo info;

    public static BaseApplication getInstance(){
        return instance;
    }

    /***
     * 获取meta-data数据
     * @param key
     * @return
     */
    public String getMetaData(String key){
        synchronized (this) {
            if (info == null) {
                info = getApplicationInfo();
            }
        }
        return (String)info.metaData.get(key);
    }

    @Override
    public void onCreate(){
        instance = this;
        //未捕获异常的处理
        BaseUncaughtExceptionHandler deafultCaughter = new BaseUncaughtExceptionHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(deafultCaughter);
    }
}
