package com.souhu.hangzhang209526.zhanghang.utils;

import android.content.Context;
import android.content.SharedPreferences;

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
}
