package com.sohu.focus.chat.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.toolbox.BaseListener;
import com.sohu.focus.chat.ChatActivity;
import com.sohu.focus.chat.ChatApplication;
import com.sohu.focus.chat.Const;
import com.sohu.focus.chat.R;
import com.sohu.focus.chat.adapter.FriendListAdapter;
import com.sohu.focus.chat.data.user.UserData;
import com.sohu.focus.chat.netcallback.StringDataCallBack;
import com.sohu.focus.chat.netcallback.UserDataCallBack;
import com.sohu.focus.eventbus.EventBus;
import com.zhanghang.self.base.BaseFragment;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/3/16.
 */
public class UserFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "UserFragment.class";
    ListView mFriendList;
    FriendListAdapter mFriendListAdapter;

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_users;
    }

    @Override
    protected void initView() {
        mFriendList = (ListView) mRootView.findViewById(R.id.friend_list);
        mFriendList.setOnItemClickListener(this);
    }

    @Override
    public void initData() {
        UserDataCallBack.addOnDataRefreshListeners(UserDataCallBack.FRIEND, new BaseListener.OnDataRefreshListener() {

            @Override
            public void OnDataRefresh(Object data) {
                if(mFriendListAdapter==null) {
                    mFriendListAdapter = new FriendListAdapter(ChatApplication.getInstance(), (ArrayList<UserData>)data);
                    mFriendList.setAdapter(mFriendListAdapter);
                }else{
                    mFriendListAdapter.setDatas((ArrayList) data);
                }
                UserDataCallBack.removeOnDataRefreshListeners(UserDataCallBack.FRIEND,this);
            }
        });
        EventBus.getDefault().post(UserDataCallBack.genrateParams(Const.currentId, UserDataCallBack.FRIEND, true), Const.EVENT_BUS_TAG_GET_USER_DATAS);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserData userInfo = (UserData) parent.getAdapter().getItem(position);
        final long userId = userInfo.getId();
        Object netParams = StringDataCallBack.generateSessionIdNetParams(userId);
        StringDataCallBack.addOnDataRefreshListeners(StringDataCallBack.NET_GET_SESSION_ID, new BaseListener.OnDataRefreshListener() {
            @Override
            public void OnDataRefresh(Object stringData) {
                Intent intent = new Intent(mActivity, ChatActivity.class);
                intent.putExtra(Const.INTENT_KEY_USER_ID, userId);
                intent.putExtra(Const.INTENT_KEY_SESSION_ID, Long.valueOf((String) stringData));
                startActivity(intent);
                StringDataCallBack.removeOnDataRefreshListeners(StringDataCallBack.NET_GET_SESSION_ID, this);
            }
        });
        EventBus.getDefault().post(netParams, Const.EVENT_BUS_TAG_GET_STRING_DATA);
    }
}
