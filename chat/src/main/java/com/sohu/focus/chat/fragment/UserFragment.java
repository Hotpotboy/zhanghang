package com.sohu.focus.chat.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.BaseListener;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sohu.focus.chat.ChatActivity;
import com.sohu.focus.chat.Const;
import com.sohu.focus.chat.R;
import com.sohu.focus.chat.adapter.FriendListAdapter;
import com.sohu.focus.chat.data.LongResponseData;
import com.sohu.focus.chat.data.user.UserData;
import com.sohu.focus.chat.data.user.UserListData;
import com.sohu.focus.eventbus.EventBus;
import com.sohu.focus.eventbus.subscribe.Subscriber;
import com.sohu.focus.eventbus.subscribe.ThreadMode;
import com.souhu.hangzhang209526.zhanghang.base.BaseFragment;
import com.souhu.hangzhang209526.zhanghang.utils.VolleyUtils;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/3/16.
 */
public class UserFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    private ListView mFriendList;
    /**
     * 好友列表适配器
     */
    private FriendListAdapter mFriendListAdapter;
    /**
     * 用户与好友的数据列表
     */
    private ArrayList<UserData> mDatas = new ArrayList<UserData>();

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
    protected void initData() {
        getFriendList(Const.currentId);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().register(this);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserData userInfo = (UserData) mFriendListAdapter.getItem(position);
        final long userId = userInfo.getId();
        String url = Const.URL_GET_SESSION_ID + "?userId=" + Const.currentId + "&friendId=" + userId;
        StringRequest friendListRequest = new StringRequest(url, new BaseListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(String response) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    LongResponseData sessionIdData = objectMapper.readValue(response, LongResponseData.class);
                    if (sessionIdData != null && sessionIdData.getErrorCode() == 0) {
                        long sessionId = sessionIdData.getData();
                        Intent intent = new Intent(mActivity, ChatActivity.class);
                        intent.putExtra(Const.INTENT_KEY_USER_ID, userId);
                        intent.putExtra(Const.INTENT_KEY_SESSION_ID, sessionId);
                        startActivity(intent);
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        VolleyUtils.requestNet(friendListRequest);
    }

    /**
     * 判断指定用户是否在列表之中
     * @param userId   指定用户的Id
     * @return
     */
    public boolean isInList(long userId){
        boolean result = false;
        for(UserData item:mDatas){
            if(item.getId()==userId){
                return true;
            }
        }
        return result;
    }

    /**
     * 获取好友列表
     */
    @Subscriber(tag = Const.EVENT_BUS_TAG_GET_FRIENDS, mode = ThreadMode.MAIN)
    private boolean getFriendList(long id) {
        String url = Const.URL_GET_FRIENDS + "?userId=" + id;
        StringRequest friendListRequest = new StringRequest(url, new BaseListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(String response) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    UserListData friendList = objectMapper.readValue(response.toString(), UserListData.class);
                    if (friendList != null && friendList.getErrorCode() == 0) {
                        ArrayList<UserData> friendDatas = friendList.getData();
                        if (mFriendListAdapter == null) {
                            mDatas.addAll(friendDatas);
                            mFriendListAdapter = new FriendListAdapter(mActivity, mDatas);
                            mFriendList.setAdapter(mFriendListAdapter);
                        } else {
                            mDatas.clear();
                            mDatas.addAll(friendDatas);
                            mFriendListAdapter.notifyDataSetInvalidated();
                        }
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        VolleyUtils.requestNet(friendListRequest);
        return false;
    }
}
