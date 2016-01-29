package com.sohu.focus.chat.data;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/1/29.
 */
public class SessionData extends BaseData {
    /**未读消息数*/
    private int mUnReadNum = 7;
    private ArrayList<MessageData> messages;

    public ArrayList<MessageData> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<MessageData> messages) {
        this.messages = messages;
    }

    public int getUnReadNum() {
        return mUnReadNum;
    }

    public void setUnReadNum(int mUnReadNum) {
        this.mUnReadNum = mUnReadNum;
    }
}
