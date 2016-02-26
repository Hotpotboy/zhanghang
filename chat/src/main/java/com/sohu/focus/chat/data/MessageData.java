package com.sohu.focus.chat.data;

import com.souhu.hangzhang209526.zhanghang.utils.SystemUtils;

import java.util.Date;

/**
 * Created by hangzhang209526 on 2016/1/29.
 */
public class MessageData extends BaseData {
    /**即将（正在）发送*/
    public static final int STATUE_SENDING = 0;
    /**已经发送*/
    public static final int STATUE_SENDED = 1;
    /**发送失败*/
    public static final int STATUE_FAILED = 2;
    /**客户端类型*/
    private int clientType;
    /**消息内容*/
    private Content content;
    /**消息创建时间*/
    private long createTime;
    /**消息发送者*/
    private long from;
    /**消息ID*/
    private long id;
    /**服务器时间*/
    private long serverTime;
    /**消息ID*/
    private long sessionId;
    /**消息状态*/
    private int statue;
    /**消息接收者*/
    private long to;
    /**消息类型*/
    private int type;

    /**
     * 通过ID获取用户的名字
     * @param id
     * @return
     */
    public String getNameFromId(long id){
        return id+"";
    }

    /**通过毫秒数返回对应格式的字符串*/
    public String getFormatBymillSec(long time){
        Date date = new Date(time);
        return SystemUtils.getTimestampStringListView(SystemUtils.TIME_FORMAT_HH_mm,date);
    }

    public int getClientType() {
        return clientType;
    }

    public void setClientType(int clientType) {
        this.clientType = clientType;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStatue() {
        return statue;
    }

    public void setStatue(int statue) {
        this.statue = statue;
    }

    @Override
    public boolean equals(Object other){
        if(!(other instanceof MessageData)) return false;
        return id==((MessageData) other).getId();
    }
}
