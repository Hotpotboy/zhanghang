package com.sohu.focus.chat;

import android.content.Intent;

import com.souhu.hangzhang209526.zhanghang.base.BaseApplication;
import com.souhu.hangzhang209526.zhanghang.utils.DefaultWebSocketUtils;

/**
 * Created by hangzhang209526 on 2016/2/26.
 */
public class ChatApplication extends BaseApplication {
    private DefaultWebSocketUtils mDefaultWebSocketUtils;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mDefaultWebSocketUtils = DefaultWebSocketUtils.getInstanceByUrl(this,Const.WS_CREATE_WEB_SOCKET+"?userId="+Const.currentId);
        startService(new Intent(this,ChatService.class));//启动webSocket
    }

    public static ChatApplication getInstance() {
        return (ChatApplication) instance;
    }

    public DefaultWebSocketUtils getDefaultWebSocketUtils(){
        return mDefaultWebSocketUtils;
    }
}
