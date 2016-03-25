package com.sohu.focus.chat.netcallback;

import android.util.Log;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.BaseListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sohu.focus.chat.data.BaseResponseData;
import com.sohu.focus.chat.data.user.UserData;
import com.sohu.focus.chat.data.user.UserListData;
import com.sohu.focus.chat.data.user.UserResponseData;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/3/23.
 */
public class UserDataCallBack extends BaseListener<ArrayList<UserData>> {
    public static final String TAG = "UserDataCallBack.class";
    /**知己*/
    public static final int TRUST = 5;
    /**好友*/
    public static final int FRIEND = 6;
    /**陌生人*/
    public static final int STRANGER = 7;
    /*自己**/
    public static final int SELF = 8;

    public static UserDataCallBack getInstance(int type){
        UserDataCallBack result  = null;
        synchronized (sInstance){
            result = (UserDataCallBack) sInstance.get(type);
            if(result==null){
                result = new UserDataCallBack(type);
                sInstance.put(type,result);
            }
        }
        return result;
    }

    /**
     * 生成获取相关用户列表的参数
     * @param id                      指定用户的ID
     * @param type                    用户的类型
     * @param isFocusGetDataFromNet   是否强制从网络更新数据
     *
     * @return
     */
    public static Object[] genrateParams(long id,int type,boolean isFocusGetDataFromNet){
        Object[] params = new Object[4];
        params[0] = id;
        params[1] = type;
        params[2] = getInstance(type);
        params[3] = isFocusGetDataFromNet;
        return params;
    }

    private UserDataCallBack(int type){
        super(type);
        mKey = type+"";
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(TAG, error.toString());
    }

    @Override
    public void onResponse(String response) {
        BaseResponseData responseData = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            responseData = objectMapper.readValue(response.toString(), UserListData.class);
        } catch (Exception e) {
            try {
                responseData = objectMapper.readValue(response.toString(), UserResponseData.class);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        if (responseData != null && responseData.getErrorCode() == 0) {
            ArrayList<UserData> userDatas = null;
            if(responseData instanceof UserListData){
                userDatas = ((UserListData)responseData).getData();
            }else{
                userDatas = new ArrayList<>();
                UserData data = ((UserResponseData)responseData).getData();
                userDatas.add(data);
            }
            for (UserData item : userDatas) {
                item.setType(mType);
            }
            mDatas.clear();
            mDatas.put(mKey, userDatas);
            invokeDatasRefresh();
        } else {

        }
    }

    @Override
    public boolean isInCache(Object object) {
        ArrayList<UserData> cache = mDatas.get(mKey);
        if(cache!=null&&cache.size()>0) {
            for (UserData item : cache) {
                if (item.getId() == object) {
                    return true;
                }
            }
        }
        return false;
    }
}
