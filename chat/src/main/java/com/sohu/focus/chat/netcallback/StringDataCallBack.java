package com.sohu.focus.chat.netcallback;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.BaseListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sohu.focus.chat.data.StringResponseData;

/**
 * Created by hangzhang209526 on 2016/3/25.
 */
public class StringDataCallBack extends BaseListener<String> {
    /**生成二维码，其子类型为生成二维码用户的ID*/
    public static final int NET_GENERATE_QR_CODE = 1;
    /**获取会话ID*/
    public static final int NET_GET_SESSION_ID = 2;
    /**上传位置信息*/
    public static final int NET_SEND_LOCATION = 3;
    /**添加好友*/
    public static final int NET_ADD_FRIEND = 4;
    /**添加知己*/
    public static final int NET_ADD_TRUST = 10;
    /**删除知己*/
    public static final int NET_DELETE_TRUST = 12;


    /**
     * 生成指定用户的二维码的网络参数
     * @param id  谁的二维码
     * @return
     */

    public static Object[] generateQRCodeNetParams(long id, boolean isFocusGetDataFromNet){
        Object[] result = new Object[3];
        result[0] = NET_GENERATE_QR_CODE;
        result[1] = id;
        result[2] = isFocusGetDataFromNet;
        return result;
    }

    /**
     * 生成会话ID的网络参数
     * @param id  与当前用户进行会话的另一方用户的ID
     * @return
     */

    public static Object[] generateSessionIdNetParams(long id){
        Object[] result = new Object[3];
        result[0] = NET_GET_SESSION_ID;
        result[1] = id;
        result[2] = true;//此接口强制每次都从网络中获取数据
        return result;
    }

    /**
     * 生成添加好友的网络参数
     * @param id  需要添加为好友的用户ID
     * @return
     */

    public static Object[] generateAddFriendNetParams(long id){
        Object[] result = new Object[3];
        result[0] = NET_ADD_FRIEND;
        result[1] = id;
        result[2] = true;//此接口强制每次都从网络中获取数据
        return result;
    }

    /**
     * 生成添加知己的网络参数
     * @param id  需要添加为知己的用户ID
     * @return
     */

    public static Object[] generateAddTrustNetParams(long id){
        Object[] result = new Object[3];
        result[0] = NET_ADD_TRUST;
        result[1] = id;
        result[2] = true;//此接口强制每次都从网络中获取数据
        return result;
    }
    /**
     * 生成删除知己的网络参数
     * @param id  需要添加为知己的用户ID
     * @return
     */

    public static Object[] generateDeleteTrustNetParams(long id){
        Object[] result = new Object[3];
        result[0] = NET_DELETE_TRUST;
        result[1] = id;
        result[2] = true;//此接口强制每次都从网络中获取数据
        return result;
    }

    /**
     * 生成上传位置信息的网络参数
     * @return
     */

    public static Object[] generateSendLocationNetParams(double lng,double lat){
        Object[] result = new Object[4];
        result[0] = NET_SEND_LOCATION;
        result[1] = lng;
        result[2] = lat;
        result[3] = true;//此接口强制每次都从网络中获取数据
        return result;
    }

    /**
     * 获取StringDataCallBack实例
     * @param type   主类型
     * @param key    子类型
     * @return
     */
    public static StringDataCallBack getStringDataCallBack(int type,String key){
        StringDataCallBack result  = null;
        synchronized (sInstance){
            result = (StringDataCallBack) sInstance.get(type);
            if(result==null){
                result = new StringDataCallBack(type);
                sInstance.put(type,result);
            }
        }
        result.setKey(key);
        return result;
    }

    private StringDataCallBack(int t){
        super(t);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        error.toString();
    }

    @Override
    public void onResponse(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            StringResponseData stringResponseData = objectMapper.readValue(response, StringResponseData.class);
            if (stringResponseData != null && stringResponseData.getErrorCode() == 0) {
                String data = stringResponseData.getData();
                mDatas.put(mKey,data);
                invokeDatasRefresh();
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
