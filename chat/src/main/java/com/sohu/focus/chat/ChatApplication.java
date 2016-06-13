package com.sohu.focus.chat;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sohu.focus.chat.data.message.ImageMessageData;
import com.sohu.focus.chat.data.message.MessageData;
import com.sohu.focus.chat.data.message.MessageType;
import com.sohu.focus.chat.data.message.TextMessageData;
import com.sohu.focus.eventbus.EventBus;
import com.sohu.focus.eventbus.subscribe.Subscriber;
import com.sohu.focus.eventbus.subscribe.ThreadMode;
import com.zhanghang.self.base.BaseApplication;
import com.zhanghang.self.utils.DefaultWebSocketUtils;

import java.io.IOException;

/**
 * Created by hangzhang209526 on 2016/2/26.
 */
public class ChatApplication extends BaseApplication {
    private DefaultWebSocketUtils mDefaultWebSocketUtils;
    public static ChatApplication getInstance() {
        return (ChatApplication) instance;
    }
    private NetRequestInterfaceUtil mNetRequestInterfaceUtil;
    @Override
    public void onCreate() {
        super.onCreate();
        mNetRequestInterfaceUtil = new NetRequestInterfaceUtil();
        instance = this;
        mDefaultWebSocketUtils = DefaultWebSocketUtils.getInstanceByUrl(this,Const.WS_CREATE_WEB_SOCKET+"?userId="+Const.currentId,Const.HEARTE_MSG_STR);
        startService(new Intent(this,ChatService.class));//启动webSocket
        EventBus.getDefault().register(this);
    }
    public DefaultWebSocketUtils getDefaultWebSocketUtils(){
        return mDefaultWebSocketUtils;
    }

    public void onStopApplication(){
        stopService(new Intent(this,ChatService.class));//关闭应用
        EventBus.getDefault().unregister(this);
    }

    /**
     * 接收到消息时的处理方法,第一个处理接收消息的方法
     * 优先级最高
     * @param intent
     * @return
     */
    @Subscriber(tag = DefaultWebSocketUtils.TAG_RECEIVE_TEXT, mode = ThreadMode.MAIN,proirity = Integer.MAX_VALUE)
    public boolean receiveTextMessageBradCast(Intent intent) {
        String msg = intent.getStringExtra(DefaultWebSocketUtils.TAG_RECEIVE_TEXT);
        ObjectMapper objectMapper = new ObjectMapper();
        MessageData messageData = null;
        try {
            messageData = objectMapper.readValue(msg, TextMessageData.class);
        } catch (IOException e) {
            try {
                messageData = objectMapper.readValue(msg, ImageMessageData.class);
            } catch (IOException e1) {
                e1.printStackTrace();
                Toast.makeText(this, "解析消息失败!" + e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
        if(messageData!=null){
            if(messageData.getType()== MessageType.ADD_FRIEND_SURE.id()){//添加盆友确认消息
                Log.e("zhanghang",messageData.toString());
                return true;
            }
        }
        intent.putExtra(DefaultWebSocketUtils.TAG_RECEIVE_TEXT, messageData);
        return false;
    }
}
