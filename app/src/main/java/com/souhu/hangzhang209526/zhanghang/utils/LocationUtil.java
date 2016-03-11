package com.souhu.hangzhang209526.zhanghang.utils;

import android.content.Context;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

/**
 * 定位工具类
 */
public class LocationUtil {
    /**定位管理器*/
    private static AMapLocationClient mlocationClient;
    /**定位监听器*/
    private static AMapLocationListener mListener;

    /**设置定位监听器，在调用requestLocation方法之前调用该方法*/
    public static void setAMapLocationListener(AMapLocationListener listener){
        mListener = listener;
    }

    /**
     * 初始化定位管理器
     * @param context  上下文
     * @throws Exception
     */
    public static void init(Context context) {
        if(mlocationClient !=null) stopLocation();//先停止定位
        mlocationClient = new AMapLocationClient(context);
    }

    /**
     * 开始请求定位，此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
     * 注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求，
     * 在定位结束后，在合适的生命周期调用destroy()方法。
     * 如果要设置定位监听器，请在调用该方法之前调用setAMapLocationListener方法
     * @param duration 间隔多少毫秒重新定位一次，如果间隔时间为-1，则定位只定一次
     * @throws Exception
     */
    public static void requestLocation(long duration) throws Exception {
        if(mlocationClient ==null) throw new Exception("定位管理器为空，请先调用init方法");
        if(mListener!=null) mlocationClient.setLocationListener(mListener);//设置监听器
        AMapLocationClientOption aMapLocationClientOption = new AMapLocationClientOption();
        if(duration<=0){
            aMapLocationClientOption.setOnceLocation(true);
            aMapLocationClientOption.setInterval(-1);
        }else{
            aMapLocationClientOption.setOnceLocation(false);
            aMapLocationClientOption.setInterval(duration);
        }
        //高耗模式
        aMapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mlocationClient.setLocationOption(aMapLocationClientOption);
        mlocationClient.startLocation();
    }

    /***
     * 停止定位
     */
    public static void stopLocation(){
       if(mlocationClient !=null){
           mlocationClient.stopLocation();
           mlocationClient.onDestroy();
           mlocationClient = null;
       }
    }
}
