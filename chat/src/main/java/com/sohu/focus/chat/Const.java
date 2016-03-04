package com.sohu.focus.chat;

import java.io.File;

/**
 * Created by hangzhang209526 on 2016/1/29.
 */
public class Const {
    public static int currentId = 1;
    public static int otherId = 2;
    /***********************Intent保存值的key**************************************************************/
    /**朋友Id在Intent之中的key值*/
    public static final String INTENT_KEY_OTHER_ID = "intent_key_other_id";
    /**session Id在Intent之中的key值*/
    public static final String INTENT_KEY_SESSION_ID = "intent_key_session_id";
    /***********************常用字符集**************************************************************/
    /**数据库名字*/
    public static final String DB_NAME = "im_chat.db";
    /**心跳包的字符串*/
    public static final String HEARTE_MSG_STR = "{\"type\":11}";
    /***********************接口地址**************************************************************/
    /**服务端地址*/
    private static final String HTTP_HOST = "http://iim.focustest.cn";
    /**ws地址*/
    private static final String WS_HOST = "ws://iim.focustest.cn";
    /**获取好友url*/
    public static final String URL_GET_FRIENDS = HTTP_HOST + File.separator +"friend/list";
    /**获取会话ID url*/
    public static final String URL_GET_SESSION_ID = HTTP_HOST + File.separator +"chat/session";
    /**创建web socket*/
    public static final String WS_CREATE_WEB_SOCKET = WS_HOST + File.separator + "ws/connect";
}
