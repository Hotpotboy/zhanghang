package com.sohu.focus.chat;

import android.content.Intent;

import com.zhanghang.self.base.BaseApplication;
import com.zhanghang.self.utils.DefaultWebSocketUtils;

/**
 * Created by hangzhang209526 on 2016/2/26.
 */
public class ChatApplication extends BaseApplication {
    private DefaultWebSocketUtils mDefaultWebSocketUtils;
    public static ChatApplication getInstance() {
        return (ChatApplication) instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        new NetRequestInterfaceUtil();
        instance = this;
        mDefaultWebSocketUtils = DefaultWebSocketUtils.getInstanceByUrl(this,Const.WS_CREATE_WEB_SOCKET+"?userId="+Const.currentId,Const.HEARTE_MSG_STR);
        startService(new Intent(this,ChatService.class));//启动webSocket
    }
    public DefaultWebSocketUtils getDefaultWebSocketUtils(){
        return mDefaultWebSocketUtils;
    }

    public void onStopApplication(){
        stopService(new Intent(this,ChatService.class));//关闭应用
    }
}
