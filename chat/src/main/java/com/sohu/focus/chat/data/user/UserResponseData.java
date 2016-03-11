package com.sohu.focus.chat.data.user;

import com.sohu.focus.chat.data.BaseResponseData;

/**
 * Created by hangzhang209526 on 2016/3/10.
 */
public class UserResponseData extends BaseResponseData {
    private UserData data;

    public UserData getData() {
        return data;
    }

    public void setData(UserData data) {
        this.data = data;
    }
}
