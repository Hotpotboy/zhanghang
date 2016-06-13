package com.zhanghang.self.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

/**
 * Created by hangzhang209526 on 2016/6/12.
 */
public class BitmapUtil {
    /**
     * 获取指定路径缩略图
     *
     * @param path   原始图路径
     * @param width  缩略图宽度
     * @param height 缩略图高度
     * @return
     */
    public static Bitmap getThumbBitmapForSpecialPath(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        int bitmapWidth = options.outWidth;//图片原始宽度
        int bitmapHeight = options.outHeight;//图片原始高度
        int widthSampleSize = bitmapWidth / width;
        int heightSampleSize = bitmapHeight / height;
        int sampleSize = 1;//默认不缩放
        sampleSize = (widthSampleSize > heightSampleSize) ? (widthSampleSize > 0 ? widthSampleSize : 1) : (heightSampleSize > 0 ? heightSampleSize : 1);
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    /**
     * 获取指定图片的缩略图
     *
     * @param bitmap    指定的原始图片
     * @param width     缩略图宽度
     * @param height    缩略图高度
     * @param isRecycle 是否回收源位图
     * @return
     */
    public static Bitmap getThumbBitmapForSpecialBitmap(Bitmap bitmap, int width, int height, boolean isRecycle) {
        int sourceWidth = bitmap.getWidth();
        int sourceHeight = bitmap.getHeight();
        int widthSampleSize = sourceWidth / width;
        int heightSampleSize = sourceHeight / height;
        Matrix matrix = new Matrix();
        matrix.postScale(widthSampleSize, heightSampleSize);
        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, sourceWidth, sourceHeight, matrix, true);
        if (isRecycle) bitmap.recycle();
        ;
        return result;
    }
}
