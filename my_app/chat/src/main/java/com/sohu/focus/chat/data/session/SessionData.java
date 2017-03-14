package com.sohu.focus.chat.data.session;

import com.sohu.focus.chat.data.BaseData;
import com.sohu.focus.chat.data.message.MessageData;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/1/29.
 */
public class SessionData extends BaseData {
    /**未读消息数*/
    private int count = 7;
    private ArrayList<MessageData> messages;

    public ArrayList<MessageData> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<MessageData> messages) {
        this.messages = messages;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
