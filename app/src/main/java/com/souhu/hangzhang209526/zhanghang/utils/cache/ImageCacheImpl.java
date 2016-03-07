package com.souhu.hangzhang209526.zhanghang.utils.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;
import com.souhu.hangzhang209526.zhanghang.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by hangzhang209526 on 2016/3/7.
 */
public class ImageCacheImpl implements ImageLoader.ImageCache {
    private Context mContext;
    /**一级缓存*/
    private LruCache<String,Bitmap> mOneCache;
    /**一级缓存的大小*/
    private int mCacheSize;
    /**文件缓存的目录*/
    private File cacheDir;

    public ImageCacheImpl(Context context){
        this(context,(int) (Runtime.getRuntime().maxMemory()/8));
    }

    public ImageCacheImpl(Context context,int max){
        mOneCache = new LruCache<>(max);
        mCacheSize = max;
        mContext = context;
        cacheDir = mContext.getDir("imge_cache",Context.MODE_PRIVATE);
        FileUtils.ensureFileIsDir(cacheDir);//确保该目录存在
    }

    @Override
    public Bitmap getBitmap(String url) {
        Bitmap result = mOneCache.get(url);
        if(result==null){//如果一级缓存没有则从文件缓存中获取
            try {
                File file = new File(cacheDir,url+".png");
                if(file.exists()) {
                    result = BitmapFactory.decodeStream(new FileInputStream(file));
                    mOneCache.put(url,result);//放入一级缓存之中
                }
            }catch (Exception e){//从文件中获取失败
                result = null;
            }
        }
        return result;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        putBitmap(url,bitmap,false);
    }

    public void putBitmap(String url, Bitmap bitmap,boolean isRepace){
        if(isRepace||(!isRepace&&mOneCache.get(url)==null)) {
            //保存到一级缓存之中
            mOneCache.put(url, bitmap);
            putFile(url,bitmap);
        }
    }
    /**
     * 保存到二级文件缓存之中
     * @param fileName
     * @param bitmap
     */
    public File putFile(String fileName,Bitmap bitmap){
        File file = new File(cacheDir,fileName+".png");
        if(file.exists()) file.delete();
        try {
            file.createNewFile();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,new FileOutputStream(file));
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
