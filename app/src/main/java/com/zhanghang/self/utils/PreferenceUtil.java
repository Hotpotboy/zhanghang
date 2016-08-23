package com.zhanghang.self.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * Created by hangzhang209526 on 2016/2/19.
 */
public class PreferenceUtil {
    /***
     * 保存SharedPreferences的String数据
     * @param context
     * @param preferenceName
     * @param key            保存的String数据的key
     * @param value          保存的String数据
     */
    public static void updateStringInPreferce(Context context,String preferenceName,String key,String value){
        SharedPreferences sharedPreference = context.getSharedPreferences(preferenceName,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString(key,value);
        editor.commit();
    }

    /**
     * 获取SharedPreferences的String数据
     * @param context
     * @param preferenceName SharePreference的名字
     * @param key            获取String数据的key
     * @param defValue       默认值
     * @return
     */
    public static String getStringInPreferce(Context context,String preferenceName,String key,String defValue){
        SharedPreferences sharedPreference = context.getSharedPreferences(preferenceName,Context.MODE_PRIVATE);
        return sharedPreference.getString(key,defValue);
    }

    /***
     * 保存SharedPreferences的long数据
     * @param context
     * @param preferenceName
     * @param key            保存的String数据的key
     * @param value          保存的String数据
     */
    public static void updateLongInPreferce(Context context,String preferenceName,String key,long value){
        SharedPreferences sharedPreference = context.getSharedPreferences(preferenceName,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    /**
     * 获取SharedPreferences的long数据
     * @param context
     * @param preferenceName SharePreference的名字
     * @param key            获取String数据的key
     * @param defValue       默认值
     * @return
     */
    public static long getLongInPreferce(Context context,String preferenceName,String key,long defValue){
        SharedPreferences sharedPreference = context.getSharedPreferences(preferenceName,Context.MODE_PRIVATE);
        return sharedPreference.getLong(key, defValue);
    }

    /***
     * 保存SharedPreferences的布尔数据
     * @param key            保存的String数据的key
     * @param context
     * @param preferenceName
     * @param value          保存的String数据
     */
    public static void updateBooleanInPreferce(Context context, String preferenceName, String key, boolean value){
        SharedPreferences sharedPreference = context.getSharedPreferences(preferenceName,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * 获取SharedPreferences的布尔数据
     * @param context
     * @param preferenceName SharePreference的名字
     * @param key            获取String数据的key
     * @param defValue       默认值
     * @return
     */
    public static boolean getBooleanInPreferce(Context context,String preferenceName,String key,boolean defValue){
        SharedPreferences sharedPreference = context.getSharedPreferences(preferenceName,Context.MODE_PRIVATE);
        return sharedPreference.getBoolean(key, defValue);
    }

    /***
     * 保存SharedPreferences的字符串集合数据
     * @param key            保存的String数据的key
     * @param context
     * @param preferenceName
     * @param value          保存的String数据
     */
    public static void updateStringSetInPreferce(Context context, String preferenceName, String key, Set<String> value){
        SharedPreferences sharedPreference = context.getSharedPreferences(preferenceName,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putStringSet(key, value);
        editor.commit();
    }

    /**
     * 获取SharedPreferences的字符串集合数据
     * @param context
     * @param preferenceName SharePreference的名字
     * @param key            获取字符串集合的key
     * @return
     */
    public static Set<String> getStringSetInPreferce(Context context,String preferenceName,String key){
        SharedPreferences sharedPreference = context.getSharedPreferences(preferenceName,Context.MODE_PRIVATE);
        return sharedPreference.getStringSet(key, null);
    }
}
