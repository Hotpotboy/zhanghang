package com.sohu.focus.chat;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.BaseListener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.UploadRequest;
import com.sohu.focus.chat.netcallback.FileUpLoadCallBack;
import com.sohu.focus.chat.netcallback.StringDataCallBack;
import com.sohu.focus.chat.netcallback.UserDataCallBack;
import com.sohu.focus.eventbus.EventBus;
import com.sohu.focus.eventbus.subscribe.Subscriber;
import com.sohu.focus.eventbus.subscribe.ThreadMode;
import com.zhanghang.self.utils.FileUtils;
import com.zhanghang.self.utils.PreferenceUtil;
import com.zhanghang.self.utils.VolleyUtils;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by hangzhang209526 on 2016/3/24.
 */
public class NetRequestInterfaceUtil {

    public NetRequestInterfaceUtil(){
        EventBus.getDefault().register(this);
    }


    /***
     * EventBus事件（上传文件的网络请求）的执行方法
     * @param params   Object数组，第一个元素用来表示获取网络接口的主类型,
     *                 最后一个元素用来表示是否强制从网络获取
     */
    @Subscriber(tag = Const.EVENT_BUS_TAG_UP_LOAD_FILE,mode = ThreadMode.MAIN)
    private boolean upLoadFile(Object[] params){
        int type = (int)params[0];//获取类型
        boolean isNeedGetNetWork = false;
        if ((boolean) params[params.length-1]) {//是否强制更新
            isNeedGetNetWork = true;
        }
        String url="";
        String key = "";
        File bitmapFile = null;
        HashMap<String,String> hashMap = null;
        if(type== FileUpLoadCallBack.NET_UPLOAD_IMAGE) {//聊天上传图片
            bitmapFile = (File) params[1];//上传图片文件
            hashMap = (HashMap<String, String>) params[2];//上传文件的HashMap
            url = Const.URL_UPLOAD_IMAGE;
            key = bitmapFile.getAbsolutePath();
        }
        FileUpLoadCallBack fileUpLoadCallBack = FileUpLoadCallBack.getFileUpLoadCallBack(FileUpLoadCallBack.NET_UPLOAD_IMAGE,key);
        if(isNeedGetNetWork) {
            VolleyUtils.requestNet(new UploadRequest(url, fileUpLoadCallBack, bitmapFile, hashMap));
        }else{
            fileUpLoadCallBack.invokeDatasRefresh();
        }
        return false;
    }

    /**
     * 获取相关用户信息列表
     * @param  params    该数组第一个元素为long类型，含义为指定用户的ID<br>
     *                   该数组第二个元素为int类型，含义为用户的类型是好友还是陌生人<br>
     *                   该数组第三个元素为{@link BaseListener}类型，含义为从网络获取数组后的回调接口<br>
     *                   该数组第四个元素为布尔类型（可选，默认为false），含义为是否强制从网络更新数据
     */
    @Subscriber(tag = Const.EVENT_BUS_TAG_GET_USER_DATAS, mode = ThreadMode.MAIN)
    private boolean getUsersInfoList(Object[] params) {
        long id = (long) params[0];
        final int type = (int) params[1];
        UserDataCallBack listener = (UserDataCallBack) params[2];
        boolean isNeedGetNetWork = false;
        if (params.length == 4 && (boolean) params[3]) {
            isNeedGetNetWork = true;
        }
        String url = "";
        if (type == UserDataCallBack.FRIEND) {
            url = Const.URL_GET_FRIENDS + "?userId=" + id;
        } else if (type == UserDataCallBack.STRANGER) {//过客
            url = Const.URL_GET_STRANGER_LIST + "?userId=" + id;
        } else if (type ==UserDataCallBack.SELF){//自己
            url = Const.URL_GET_USER_INFO + "?userId=" + id;
        }else if (type ==UserDataCallBack.TRUST){//知己
            url = Const.URL_GET_TRUST_LIST + "?userId=" + id;
        }else if (type ==UserDataCallBack.NEARBY){//附近
            long lat = PreferenceUtil.getLongInPreferce(ChatApplication.getInstance(), ChatApplication.getInstance().getVersionName(), Const.SHARE_LAT_KEY,-1);
            long lng = PreferenceUtil.getLongInPreferce(ChatApplication.getInstance(), ChatApplication.getInstance().getVersionName(), Const.SHARE_LON_KEY, -1);
            url = Const.URL_GET_NEAR_LIST + "?userId=" + id+"&lng="+lng+"&lat="+lat;
        }
        getDataFromNetOrCache(isNeedGetNetWork?url:"",listener);
        return false;
    }


    /***
     * EventBus事件（结果是字符串类型的网络请求）的执行方法
     * @param params   Object数组，第一个元素用来表示获取网络接口的主类型,
     *                 最后一个元素用来表示是否强制从网络获取
     */
    @Subscriber(tag = Const.EVENT_BUS_TAG_GET_STRING_DATA,mode = ThreadMode.MAIN)
    private boolean getStringDataFromNet(Object[] params){
        int type = (int)params[0];//获取类型
        boolean isNeedGetNetWork = false;
        if ((boolean) params[params.length-1]) {//是否强制更新
            isNeedGetNetWork = true;
        }
        String url="";
        String key = "";
        if(type==StringDataCallBack.NET_GENERATE_QR_CODE) {//生成二维码
            long id = (long) params[1];
            key = id + "";
            url = Const.URL_GET_QR_CODE + "?userId=" + id;
        }else if(type==StringDataCallBack.NET_GET_SESSION_ID){//获取会话ID
            long id = (long) params[1];
            key = id + "";
            url = Const.URL_GET_SESSION_ID + "?userId=" + Const.currentId + "&friendId=" + id;
        }else if(type==StringDataCallBack.NET_SEND_LOCATION){//发送位置信息
            double geoLng = (double)params[1];
            double geoLat = (double)params[2];
            key = geoLng + ","+geoLng;
            url = Const.URL_SEND_LOCATION + "?userId=" + Const.currentId + "&lat=" + ((int)(geoLat * 10000)) + "&lng=" + ((int)(geoLng * 10000));
        }else if(type==StringDataCallBack.NET_ADD_FRIEND) {//添加好友
            long id = (long) params[1];
            key = id+"";
            url = Const.URL_ADD_FRIEND + "?userId="+Const.currentId+"&friendId="+id;
        }else if(type==StringDataCallBack.NET_ADD_TRUST) {//添加知己
            long id = (long) params[1];
            key = id+"";
            url = Const.URL_ADD_TRUST + "?userId="+Const.currentId+"&friendId="+id;
        }else if(type==StringDataCallBack.NET_DELETE_TRUST) {//删除知己
            long id = (long) params[1];
            key = id+"";
            url = Const.URL_DELETE_TRUST + "?userId="+Const.currentId+"&friendId="+id;
        }
        getDataFromNetOrCache(isNeedGetNetWork?url:"",StringDataCallBack.getStringDataCallBack(type,key));
        return false;
    }

    /**
     * 获取数据
     * @param url                 获取数据的网络URL,不为空则表示从网络中获取数据，否则从缓存中获取数据
     * @param listener            数据缓存即网络回调接口
     */
    private void getDataFromNetOrCache(String url,BaseListener listener){
        if(!TextUtils.isEmpty(url)) {
            StringRequest genrateQRCodeRequest = new StringRequest(url, listener);
            VolleyUtils.requestNet(genrateQRCodeRequest);
        }else{//直接从缓存中加载
            listener.invokeDatasRefresh();
        }
    }
}
