package com.sohu.focus.chat.data.user;

import com.sohu.focus.chat.data.BaseResponseData;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/2/23.
 */
public class UserListData extends BaseResponseData {
    private ArrayList<UserData> data;

    public ArrayList<UserData> getData() {
        return data;
    }

    public void setData(ArrayList<UserData> data) {
        this.data = data;
    }
}
