package com.souhu.hangzhang209526.zhanghang.utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by hangzhang209526 on 2016/2/23.
 */
public class VolleyUtils {
    private static RequestQueue sRequestQueue;
    public static void init(Context context){
        sRequestQueue = Volley.newRequestQueue(context);//启动Volley网络线程
    }

    public static void requestNet(Request request) {
        sRequestQueue.add(request);
    }
}
