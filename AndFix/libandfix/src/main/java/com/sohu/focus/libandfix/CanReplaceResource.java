package com.sohu.focus.libandfix;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.support.annotation.AnimRes;
import android.support.annotation.ArrayRes;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.util.TypedValue;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016-02-27.
 */
public class CanReplaceResource extends Resources {
    /**上下文*/
    private Resources baseResource;
    /**
     * 补丁资源
     * */
    private ArrayList<Resources> apatchResource = new ArrayList<Resources>();

    /**
     *
     * @param context
     * @throws PackageManager.NameNotFoundException
     */
    public  CanReplaceResource(Context context) {
        super(context.getResources().getAssets(), context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
        baseResource = context.getResources();
    }

    /**
     *
     * @param apatchFile   补丁文件
     * @throws PackageManager.NameNotFoundException
     */
    public void addFile(File apatchFile) throws PackageManager.NameNotFoundException{
        //Context tmpContext = mContext.createPackageContext(apatchFile.getAbsolutePath(),Context.CONTEXT_IGNORE_SECURITY);
        //apatchResource.add(tmpContext.getResources());
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, apatchFile.getAbsolutePath());

            Resources apatchResoucre =  new Resources(assetManager, baseResource.getDisplayMetrics(),
                    baseResource.getConfiguration());
            apatchResource.add(apatchResoucre);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public XmlResourceParser getAnimation(@AnimRes int id) throws NotFoundException {
        XmlResourceParser  result = null;
        for(Resources item:apatchResource) {
            try {
                    result = item.getAnimation(id);
            }catch (NotFoundException e){
                result = null;
            }
        }
        if(result==null){//未在补丁文件中找到资源
            result = baseResource.getAnimation(id);
        }
        if(result==null) throw new NotFoundException();
        return result;
    }

    @Override
    public int getColor(@ColorRes int id) throws NotFoundException {
        int result = -1;
        for(Resources item:apatchResource) {
            try {
                result = item.getColor(id);
            }catch (NotFoundException e){
                result = -1;
            }
        }
        if(result==-1){//未在补丁文件中找到资源
            result = baseResource.getColor(id);
        }
        if(result==-1) throw new NotFoundException();
        return result;
    }

    @Override
    public Drawable getDrawable(@ColorRes int id) throws NotFoundException {
        Drawable result = null;
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

    @Override
    public void getValue(String name, TypedValue outValue, boolean resolveRefs)throws NotFoundException {
        for(Resources item:apatchResource) {
            try {
                item.getValue(name, outValue, resolveRefs);
                return;
            } catch (NotFoundException e) {

            }
        }
        baseResource.getValue(name, outValue, resolveRefs);
    }
    @Override
    public void getValue(int id, TypedValue outValue, boolean resolveRefs)throws NotFoundException {
        for(Resources item:apatchResource) {
            try {
                item.getValue(id, outValue, resolveRefs);
                return;
            } catch (NotFoundException e) {

            }
        }
        baseResource.getValue(id, outValue, resolveRefs);
    }

    @Override
    public String getString(@StringRes int id) throws NotFoundException {
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
