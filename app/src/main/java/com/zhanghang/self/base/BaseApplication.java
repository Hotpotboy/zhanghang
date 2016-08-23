package com.zhanghang.self.base;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.android.volley.RequestQueue;
import com.zhanghang.self.utils.VolleyUtils;

/**
 * Created by hangzhang209526 on 2016/1/4.
 */
public class BaseApplication extends Application {
    /**版本号*/
    private int versionCode;
    /**版本名称*/
    private String versionName;

    protected static BaseApplication instance;

    private ApplicationInfo info;

    /**
     * 是否开启AndFix热补丁的功能
     */
    private boolean isAndFix = true;
    /**请求网络队列*/
    private RequestQueue mRequestQueue;
    public static BaseApplication getInstance() {
        return instance;
    }

    /**
     * 获取meta-data数据
     *
     * @param key
     * @return
     */
    public String getMetaData(String key) {
        synchronized (this) {
            if (info == null) {
                info = getApplicationInfo();
            }
        }
        if (info.metaData == null) return null;
        return (String) info.metaData.get(key);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionName = "1.0";
            versionCode = 1;
        }
        //未捕获异常的处理
        BaseUncaughtExceptionHandler deafultCaughter = new BaseUncaughtExceptionHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(deafultCaughter);

        VolleyUtils.init(this);//初始化Volley
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }
}
