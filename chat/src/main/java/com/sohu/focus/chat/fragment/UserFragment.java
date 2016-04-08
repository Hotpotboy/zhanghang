package com.sohu.focus.chat.fragment;

import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.BaseListener;
import com.sohu.focus.chat.ChatApplication;
import com.sohu.focus.chat.Const;
import com.sohu.focus.chat.R;
import com.sohu.focus.chat.adapter.FriendListAdapter;
import com.sohu.focus.chat.data.user.UserData;
import com.sohu.focus.chat.netcallback.UserDataCallBack;
import com.sohu.focus.eventbus.EventBus;
import com.zhanghang.self.base.BaseFragment;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/3/16.
 */
public class UserFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "UserFragment.class";
    String[] mUnFriendShow = {"我的好友", "红颜知己"};
    ListView mFriendList;
    FriendListAdapter mFriendListAdapter;
    /**
     * 我的知己/我的过客操作布局视图
     */
    RelativeLayout mUnFriendAreaView;
    /**
     * 我的知己/我的过客显示视图
     */
    TextView mUnFriednTextView;
    /**
     * 是否显示我的知己/我的过客列表
     */
    boolean isShowMyUnFriend = false;
    /**
     * 是否显示我的好友/附近的人列表
     */
    boolean isShowMyFriend = true;

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_users;
    }

    @Override
    protected void initView() {
        mUnFriednTextView = (TextView) findViewById(R.id.my_unfriend_text);
        mUnFriednTextView.setText(mUnFriendShow[1]);
        mUnFriendAreaView = (RelativeLayout) findViewById(R.id.my_unfriend_layout);
        mFriendList = (ListView) findViewById(R.id.friend_list);
        mUnFriendAreaView.setOnClickListener(this);
    }

    @Override
    public void initData() {
        if(!isShowMyUnFriend) {//我的好友
            updateList(UserDataCallBack.FRIEND);
        }else{//我的知己
            updateList(UserDataCallBack.TRUST);
        }
    }

    void updateList(final int type){
        UserDataCallBack.addOnDataRefreshListeners(type, new BaseListener.OnDataRefreshListener() {

            @Override
            public void OnDataRefresh(Object data) {
                if (mFriendListAdapter == null) {
                    mFriendListAdapter = new FriendListAdapter(ChatApplication.getInstance(), (ArrayList<UserData>) data, mFriendList);
                    mFriendList.setAdapter(mFriendListAdapter);
                } else {
                    mFriendListAdapter.setDatas((ArrayList) data);
                }
                UserDataCallBack.removeOnDataRefreshListeners(type, this);
            }
        });
        EventBus.getDefault().post(UserDataCallBack.genrateParams(Const.currentId, type, true), Const.EVENT_BUS_TAG_GET_USER_DATAS);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_unfriend_layout:
                if (!isShowMyUnFriend) {
                    isShowMyUnFriend = true;
                    mUnFriednTextView.setText(mUnFriendShow[0]);
                } else {
                    isShowMyUnFriend = false;
                    mUnFriednTextView.setText(mUnFriendShow[1]);
                }
                initData();
                break;
        }
    }
}
