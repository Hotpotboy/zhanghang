package com.sohu.focus.libandfix.patch;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.annotation.AnimRes;
import android.support.annotation.ArrayRes;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.LayoutRes;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;

import com.sohu.focus.libandfix.security.SecurityChecker;
import com.sohu.focus.libandfix.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;

/**
 * Created by Administrator on 2016-02-27.
 */
class CanReplaceResource extends Resources {
    private static final String TAG = "CanReplaceResource";
    private static final String RES_APK_DIC = "res_patch_apk";
    private static final String RES_APK_OPT_DIC = "res_patch_opt_apk";
//    private final String RES_APK_URL = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + PatchManager.SD_DIR + File.separator + RES_APK_DIC;
    /**资源补丁APK路径*/
    private File mResApkDir;
    /**资源补丁优化路径*/
    private File mResApkOptDir;
    /**上下文*/
    private Resources baseResource;
    /**
     * 补丁资源
     * */
    private ArrayList<Resources> apatchResource = new ArrayList<Resources>();

    private Context mContext;
    /**
     * 需要替换的资源ID；key为替换前的ID，value为替换后的ID
     */
    SparseIntArray  replacesIdsMap = new SparseIntArray();

    /**
     *
     * @param context
     * @throws PackageManager.NameNotFoundException
     */
    public  CanReplaceResource(Context context) {
        super(context.getResources().getAssets(), context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
        mContext = context;
        mResApkOptDir = context.getDir(RES_APK_OPT_DIC,0);
        ensureFileIsDir(mResApkOptDir);
        mResApkDir = context.getDir(RES_APK_DIC,0);
        ensureFileIsDir(mResApkDir);
        baseResource = context.getResources();
        File[] apkFiles = mResApkDir.listFiles();
        if(apkFiles!=null&&apkFiles.length>0){
            for(File apkFile:apkFiles){
                if(apkFile.getName().endsWith(".apk")) {
                    addFile(apkFile);
                }
            }
        }
    }

    /**
     * 确保指定的文件是一个目录且存在
     * @param dirFile
     */
    private void ensureFileIsDir(File dirFile){
        if(dirFile.exists()&&dirFile.isFile()){//如果是一个文件，删掉
            dirFile.delete();
        }
        if(!dirFile.exists()){
            dirFile.mkdirs();
        }
    }

    void cleanAllDir(){
        if(mResApkDir!=null&&mResApkDir.exists()){
            if (!FileUtil.deleteFile(mResApkDir)) {
                Log.e(TAG, mResApkDir.getName() + " delete error.");
            }
        }
        if(mResApkOptDir!=null&&mResApkOptDir.exists()){
            if (!FileUtil.deleteFile(mResApkOptDir)) {
                Log.e(TAG, mResApkOptDir.getName() + " delete error.");
            }
        }
    }

    void addOrgFile(File orgFile) throws IOException {
        if(orgFile!=null&&orgFile.exists()&&(orgFile.getName().endsWith(".apk"))) {
            File apkFile = new File(mResApkDir, orgFile.getName());
            FileUtil.copyFile(orgFile, apkFile);
            addFile(apkFile);
        }
    }

    /**
     *
     * @param apatchFile   补丁文件
     * @throws PackageManager.NameNotFoundException
     */
    private void addFile(File apatchFile){
        SecurityChecker securityChecker = SecurityChecker.getInstance(mContext);
        if(!securityChecker.verifyApk(apatchFile)){//签名未通过
            Log.e(TAG,"没有通过签名");
            return;
        }
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, apatchFile.getAbsolutePath());

            Resources apatchResoucre =  new Resources(assetManager, baseResource.getDisplayMetrics(),baseResource.getConfiguration());
            apatchResource.add(apatchResoucre);
            //更新配置
            DexClassLoader dexClassLoader = new DexClassLoader(apatchFile.getAbsolutePath(),mResApkOptDir.getAbsolutePath(),null,mContext.getClassLoader());
            Class clazz = dexClassLoader.loadClass("com.sohu.focus.libandfixrestool.InitConfig");
            Field resIds = clazz.getDeclaredField("needChangeResIds");
            SparseArray<String> configs = (SparseArray<String>) resIds.get(clazz);
            if(configs!=null&&configs.size()>0){
                for(int i=0;i<configs.size();i++){
                    int fixedId = configs.keyAt(i);
                    String needFixedIdName = configs.get(fixedId,null);
                    if(!TextUtils.isEmpty(needFixedIdName)){
                        int needFixedId = getBugResId(needFixedIdName);
                        if(needFixedId!=-1){
                            replacesIdsMap.put(needFixedId,fixedId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getBugResId(String name) throws Exception {
        int result = -1;
        String[] args = name.split("\\.");
        if(args==null||args.length!=3) return result;
        if(!"R".equals(args[0])) return result;
        String className = mContext.getPackageName()+".R$"+args[1];
        Class clazz = mContext.getClassLoader().loadClass(className);
        Field resId = clazz.getDeclaredField(args[2]);
        result = resId.getInt(clazz);
        return result;
    }

//    @Override
//    public XmlResourceParser getAnimation(@AnimRes int id) throws NotFoundException {
//        XmlResourceParser  result = null;
//        for(Resources item:apatchResource) {
//            try {
//                    result = item.getAnimation(id);
//            }catch (NotFoundException e){
//                result = null;
//            }
//        }
//        if(result==null){//未在补丁文件中找到资源
//            result = baseResource.getAnimation(id);
//        }
//        if(result==null) throw new NotFoundException();
//        return result;
//    }

    @Override
    public int getColor(@ColorRes int id) throws NotFoundException {
        int result = -1;
        if(replacesIdsMap.indexOfKey(id)>=0) id = replacesIdsMap.get(id);
        for(Resources item:apatchResource) {
            try {
                result = item.getColor(id);
            }catch (NotFoundException e){
                result = -1;
            }
        }
        if(result==-1){//未在补丁文件中找到资源
            result = baseResource.getColor(id);
            return result;
        }
        if(result==-1) throw new NotFoundException();
        return result;
    }

    @Override
    public Drawable getDrawable(@ColorRes int id) throws NotFoundException {
        Drawable result = null;
        if(replacesIdsMap.indexOfKey(id)>=0) id = replacesIdsMap.get(id);
        for(Resources item:apatchResource) {
            try {
                result = item.getDrawable(id);
            } catch (NotFoundException e) {
                result = null;
            }
        }
        if(result==null){//未在补丁文件中找到资源
            result = baseResource.getDrawable(id);
        }
        if(result==null) throw new NotFoundException();
        return result;
    }

    @Override
     public XmlResourceParser getLayout(@LayoutRes int id) throws NotFoundException {
        XmlResourceParser result = null;
        if(replacesIdsMap.indexOfKey(id)>=0) id = replacesIdsMap.get(id);
        for(Resources item:apatchResource) {
            try {
                result = item.getLayout(id);
            } catch (NotFoundException e) {
                result = null;
            }
        }
        if(result==null){//未在补丁文件中找到资源
            result = baseResource.getLayout(id);
        }
        if(result==null) throw new NotFoundException();
        return result;
    }

//    @Override
//    public void getValue(String name, TypedValue outValue, boolean resolveRefs)throws NotFoundException {
//        for(Resources item:apatchResource) {
//            try {
//                item.getValue(name, outValue, resolveRefs);
//                return;
//            } catch (NotFoundException e) {
//
//            }
//        }
//        baseResource.getValue(name, outValue, resolveRefs);
//    }
//    @Override
//    public void getValue(int id, TypedValue outValue, boolean resolveRefs)throws NotFoundException {
//        if(replacesIdsMap.indexOfKey(id)>=0) id = replacesIdsMap.get(id);
//        for(Resources item:apatchResource) {
//            try {
//                item.getValue(id, outValue, resolveRefs);
//                return;
//            } catch (NotFoundException e) {
//
//            }
//        }
//        baseResource.getValue(id, outValue, resolveRefs);
//    }

    @Override
    public String getString(int id) throws NotFoundException {
        if(replacesIdsMap.indexOfKey(id)>=0) id = replacesIdsMap.get(id);
        String result = null;
        for(Resources item:apatchResource) {
            try {
                result = item.getString(id);
            } catch (NotFoundException e) {
                result = null;
            }
        }
        if(result==null){//未在补丁文件中找到资源
            result = baseResource.getString(id);
        }
        if(result==null) throw new NotFoundException();
        return result;
    }

    @Override
    public String[] getStringArray(@ArrayRes int id)throws NotFoundException {
        String[] result = null;
        if(replacesIdsMap.indexOfKey(id)>=0) id = replacesIdsMap.get(id);
        for(Resources item:apatchResource) {
            try {
                result = item.getStringArray(id);
            } catch (NotFoundException e) {
                result = null;
            }
        }
        if(result==null){//未在补丁文件中找到资源
            result = baseResource.getStringArray(id);
        }
        if(result==null) throw new NotFoundException();
        return result;
    }

    @Override
    public float getDimension(@DimenRes int id) throws NotFoundException {
        float result = -1;
        if(replacesIdsMap.indexOfKey(id)>=0) id = replacesIdsMap.get(id);
        for(Resources item:apatchResource) {
            try {
                result = item.getDimension(id);
            } catch (NotFoundException e) {
                result = -1;
            }
        }
        if(result==-1){//未在补丁文件中找到资源
            result = baseResource.getDimension(id);
        }
        if(result==-1) throw new NotFoundException();
        return result;
    }
}
