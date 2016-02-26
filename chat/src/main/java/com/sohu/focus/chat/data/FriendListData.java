package com.sohu.focus.chat.data;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/2/23.
 */
public class FriendListData extends BaseResponseData {
    private ArrayList<FriendData> data;

    public ArrayList<FriendData> getData() {
        return data;
    }

    public void setData(ArrayList<FriendData> data) {
        this.data = data;
    }
}
