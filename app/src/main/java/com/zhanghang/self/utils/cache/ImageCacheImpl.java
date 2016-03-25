package com.zhanghang.self.utils.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader;
import com.zhanghang.self.utils.FileUtils;
import com.zhanghang.zhanghang.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by hangzhang209526 on 2016/3/7.
 */
public class ImageCacheImpl implements ImageLoader.ImageCache {
    private static final String TAG = "ImageCacheImpl";
    private static ImageCacheImpl mInstance;
    private Context mContext;
    /**一级缓存*/
    private LruCache<String,Bitmap> mOneCache;
    /**一级缓存的大小*/
    private int mCacheSize;
    /**文件缓存的目录*/
    private File cacheDir;

    public static ImageCacheImpl getInstance(Context context){
        if(mInstance==null){
            mInstance = new ImageCacheImpl(context);
        }
        return mInstance;
    }

    private ImageCacheImpl(Context context){
        this(context, (int) (Runtime.getRuntime().maxMemory() / 8));
    }

    public ImageCacheImpl(Context context,int max){
        Log.i(TAG,"图片一级缓存的最大值为:"+max+"");
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
                File file = new File(cacheDir,url+".jpg");
                if(file.exists()) {
                    result = getBitmapFromFile(file.getAbsolutePath(),true);
                    mOneCache.put(url, result);//放入一级缓存之中
                }
            }catch (Exception e){//从文件中获取失败
                result = null;
            }
        }
        return result;
    }

    public Bitmap removeCache(String url){
        return mOneCache.remove(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        putBitmap(url,bitmap,false);
    }

    public void putBitmap(String url, Bitmap bitmap,boolean isRepace){
        if(isRepace||(!isRepace&&mOneCache.get(url)==null)) {
            //保存到一级缓存之中
            mOneCache.put(url, bitmap);
        }
    }
    /**
     * 保存到二级文件缓存之中
     * @param fileName
     * @param bitmap
     */
    public File putFile(String fileName,Bitmap bitmap){
        File file = new File(cacheDir,fileName+".jpg");
        if(file.exists()) file.delete();
        try {
            file.createNewFile();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,new FileOutputStream(file));
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 返回二级缓存的目录路径
     * @return
     */
    public String getCacheDir(){
        return cacheDir.getAbsolutePath();
    }

    /**
     * 从文件中提取bitmap
     * @param path
     * @param isCompress 是否压缩
     * @return
     */
    public Bitmap  getBitmapFromFile(String path,boolean isCompress){
        Bitmap tmpBitmap = BitmapFactory.decodeFile(path);
        if(isCompress) {
            int size = (int) mContext.getResources().getDimension(R.dimen.image_height);
            Bitmap bitmap = ThumbnailUtils.extractThumbnail(tmpBitmap, size, size);
            tmpBitmap.recycle();
            return bitmap;
        }
        return tmpBitmap;
    }
}
