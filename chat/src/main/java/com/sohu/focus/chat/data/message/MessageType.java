package com.sohu.focus.chat.data.message;

import java.util.Arrays;
import java.util.List;

/**
 * @author changhuhuang@sohu-inc.com
 * @version 创建时间：2014-12-12 下午5:10:55
 */
public enum MessageType {

    /**
     * 文本消息
     */
    TEXT_MESSAGE(1),

    /**
     * 图片消息
     */
    IMAGE_MESSAGE(2),

    /**
     * 公告消息
     */
    ANNOUNCEMENT_MESSAGE(3),

    /**
     * 消息接收确认
     */
    ACK_MESSAGE(4),


    /**
     * 语音消息
     */
    SOUND_MESSAGE(5),


    /**
     * 客户端向服务器的心跳
     */
    PING_MESSAGE(11),

    /**
     * 服务器回复客户端的心跳
     */
    PONG_MESSAGE(12),

    /**
     * 已在其它设备登录，强制退出
     */
    LOGOUT_MESSAGE(13),

    /**
     * 保持服务节点的订阅消息通道处理活动状态
     */
    KEEP_CHANNEL_MESSAGE(14);

    private MessageType(int id) {
        this.id = id;
    }

    public static MessageType type(int id) {
        for (MessageType type : MessageType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }


    /**
     * 客户端支持的消息类型
     */
    public static List<Integer> allowTypes() {
        return Arrays.asList(IMAGE_MESSAGE.id(), TEXT_MESSAGE.id(), ANNOUNCEMENT_MESSAGE.id(),SOUND_MESSAGE.id);
    }

    private int id;

    public int id() {
        return this.id;
    }
}
