package com.souhu.hangzhang209526.zhanghang.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by hangzhang209526 on 2016/1/29.
 */
public class SystemUtils {
    /**
     SimpleDateFormat函数语法：
     G 年代标志符
     y 年
     M 月
     d 日
     h 时 在上午或下午 (1~12)
     H 时 在一天中 (0~23)
     m 分
     s 秒
     S 毫秒
     E 星期
     D 一年中的第几天
     F 一月中第几个星期几
     w 一年中第几个星期
     W 一月中第几个星期
     a 上午 / 下午 标记符
     k 时 在一天中 (1~24)
     K 时 在上午或下午 (0~11)
     z 时区
     *HH:mm格式
     */
    public static final String TIME_FORMAT_HH_mm= "HH:mm";
    public static final String TIME_FORMAT_yyyy_MM_dd_HH_mm_ss= "yyyy-MM-dd HH:mm:ss";
    public static Bitmap getBitmapFromDrawable(Drawable drawable){
        if(drawable instanceof BitmapDrawable){//如果可绘物是位图可绘物，则直接返回之
            return ((BitmapDrawable)drawable).getBitmap();
        }else {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.draw(canvas);
            return bitmap;
        }
    }

    /***
     * 格式化时间
     * @param paramDate
     * @return
     */
    public static String getTimestampStringListView(String str,Date paramDate) {
        return new SimpleDateFormat(str, Locale.CHINA).format(paramDate);
    }
}
