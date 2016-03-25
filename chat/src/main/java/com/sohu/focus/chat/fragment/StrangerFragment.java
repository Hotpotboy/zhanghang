package com.sohu.focus.chat.fragment;

import com.android.volley.toolbox.BaseListener;
import com.sohu.focus.chat.ChatApplication;
import com.sohu.focus.chat.Const;
import com.sohu.focus.chat.adapter.FriendListAdapter;
import com.sohu.focus.chat.data.user.UserData;
import com.sohu.focus.chat.netcallback.UserDataCallBack;
import com.sohu.focus.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/3/25.
 */
public class StrangerFragment extends UserFragment {

    @Override
    public void initData() {
        UserDataCallBack.addOnDataRefreshListeners(UserDataCallBack.STRANGER, new BaseListener.OnDataRefreshListener() {

            @Override
            public void OnDataRefresh(Object data) {
                if(mFriendListAdapter==null) {
                    mFriendListAdapter = new FriendListAdapter(ChatApplication.getInstance(), (ArrayList<UserData>)data);
                    mFriendList.setAdapter(mFriendListAdapter);
                }else{
                    mFriendListAdapter.setDatas((ArrayList) data);
                }
                UserDataCallBack.removeOnDataRefreshListeners(UserDataCallBack.STRANGER,this);
            }
        });
        EventBus.getDefault().post(UserDataCallBack.genrateParams(Const.currentId, UserDataCallBack.STRANGER, true), Const.EVENT_BUS_TAG_GET_USER_DATAS);
    }
}
