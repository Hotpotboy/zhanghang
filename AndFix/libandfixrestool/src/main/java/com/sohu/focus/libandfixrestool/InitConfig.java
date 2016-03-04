package com.sohu.focus.libandfixrestool;

import android.util.SparseArray;

/**
 * Created by hangzhang209526 on 2016/2/29.
 */
public class InitConfig {
    public static SparseArray<String> needChangeResIds = new SparseArray<String>();

    static {
        needChangeResIds.put(R.layout.activity_main,"R.layout.activity_main");
    }
}
