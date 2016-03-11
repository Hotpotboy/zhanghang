package com.souhu.hangzhang209526.zhanghang.base;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.sohu.focus.hotfixlib.HotFix;
import com.sohu.focus.libandfix.patch.PatchManager;
import com.souhu.hangzhang209526.zhanghang.BuildConfig;
import com.souhu.hangzhang209526.zhanghang.utils.FileUtils;
import com.souhu.hangzhang209526.zhanghang.utils.LocationUtil;
import com.souhu.hangzhang209526.zhanghang.utils.VolleyUtils;

import java.io.File;
import java.util.ArrayList;

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
    /**
     * 热补丁管理器
     */
    private PatchManager mPatchManager;
    /**是否开启HotFix热补丁功能*/
    private boolean isHotFix = false;
    /**请求网络队列*/
    private RequestQueue mRequestQueue;
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

        //初始化定位工具
        LocationUtil.init(this);

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
        //AndFix热补丁功能
        if (isAndFix) {
            exeAndFix();
        }
        //HotFix热补丁功能
        if(isHotFix){
            exeHotFix();
        }
        VolleyUtils.init(this);//初始化Volley
    }

    private void exeAndFix(){
        mPatchManager = new PatchManager(this);//实例化补丁管理器
        mPatchManager.init(versionName,false);
        mPatchManager.loadPatch();//加载已经存在的补丁
    }

    public void exeHotFix(){
        String patchDir = Environment.getExternalStorageDirectory().getAbsolutePath() + BuildConfig.HOTFIX_DIR;
        File patchFile = new File(patchDir);
        if(patchFile.exists()&&!patchFile.isFile()){
            ArrayList<File> files = FileUtils.traversalFiles(patchFile);
            for(File file:files){
                if(file.exists()&&file.getName().endsWith(".jar")){
                    File internalFile = new File(getDir("dex",MODE_PRIVATE),file.getName());
                    if(internalFile.exists()) internalFile.delete();
                    FileUtils.copyFile(file,internalFile);
                    HotFix.patch(this,internalFile.getAbsolutePath(),"com.sohu.focus.hotfixtest.BugClass");
                }
            }
        }
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }
}
