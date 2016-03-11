package com.sohu.focus.chat.data.session;

import com.sohu.focus.chat.data.BaseData;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/1/29.
 */
public class SessionListData extends BaseData {
    private ArrayList<SessionData> messages;

    public ArrayList<SessionData> getSessionDatas() {
        return messages;
    }

    public void setSessionDatas(ArrayList<SessionData> sessionDatas) {
        messages = sessionDatas;
    }
}
