package com.sohu.focus.chat.data;

import com.souhu.hangzhang209526.zhanghang.utils.SystemUtils;

import java.util.Date;

/**
 * Created by hangzhang209526 on 2016/1/29.
 */
public class MessageData extends BaseData {
    /**消息ID*/
    public long id;
    /**消息类型*/
    public int type;
    /**消息内容*/
    public byte[] content;
    /**消息发送者*/
    public long from;
    /**消息接收者*/
    public long to;
    /**消息创建时间*/
    public long createTime;
    /**服务时间*/
    public long serverTime;
    /**属于哪一个会话*/
    public SessionData sessionData;

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
}
