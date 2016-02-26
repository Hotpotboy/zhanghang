package com.sohu.focus.chat.data;

/**
 * Created by hangzhang209526 on 2016/2/25.
 */
public class OffLineMessageResponseData extends BaseResponseData {
    private SessionListData data;

    public SessionListData getData() {
        return data;
    }

    public void setData(SessionListData data) {
        this.data = data;
    }
}
