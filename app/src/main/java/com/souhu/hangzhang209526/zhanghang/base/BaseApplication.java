package com.souhu.hangzhang209526.zhanghang.base;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;

import com.sohu.focus.libandfix.patch.PatchManager;
import com.souhu.hangzhang209526.zhanghang.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/1/4.
 */
public class BaseApplication extends Application {

    private static BaseApplication instance;

    private ApplicationInfo info;

    /**
     * 是否开启热补丁的功能
     */
    private boolean isHotFix = true;
    /**
     * 热补丁管理器
     */
    private PatchManager mPatchManager;

    public static BaseApplication getInstance() {
        return instance;
    }

    public PatchManager getPatchManager(){
        return mPatchManager;
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
        instance = this;
        //未捕获异常的处理
        BaseUncaughtExceptionHandler deafultCaughter = new BaseUncaughtExceptionHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(deafultCaughter);
        //热补丁功能
        if (isHotFix) {
            mPatchManager = new PatchManager(this);//实例化补丁管理器
            try {
                String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                mPatchManager.init(version);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mPatchManager.init("1.0");
            }
            mPatchManager.loadPatch();
        }
    }
}
