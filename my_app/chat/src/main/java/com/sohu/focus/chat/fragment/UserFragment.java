package com.sohu.focus.chat.fragment;

import android.util.SparseArray;
import android.view.View;
import android.widget.ExpandableListView;
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
public class UserFragment extends BaseFragment {
    private static final String TAG = "UserFragment.class";
    int[] showType = {UserDataCallBack.FRIEND,UserDataCallBack.TRUST};;
    ArrayList<String> mUnFriendShow = new ArrayList<>();
    SparseArray<ArrayList> mChildData = new SparseArray<>();
    ExpandableListView mFriendList;
    FriendListAdapter mFriendListAdapter;

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_users;
    }

    @Override
    protected void initView() {
        mFriendList = (ExpandableListView) findViewById(R.id.friend_list);
    }

    @Override
    public void initData() {
        mUnFriendShow.add("我的好友");
        mUnFriendShow.add("红颜知己");
        //我的好友
        updateList(showType[0]);
    }

    void updateList(final int type){
        UserDataCallBack.addOnDataRefreshListeners(type, new BaseListener.OnDataRefreshListener() {

            @Override
            public void OnDataRefresh(Object data) {
                mChildData.put(type == showType[0] ? 0 : 1, (ArrayList<UserData>) data);
                if (mFriendListAdapter == null) {
                    mFriendListAdapter = new FriendListAdapter(ChatApplication.getInstance(), mUnFriendShow,mChildData, mFriendList);
                    mFriendList.setAdapter(mFriendListAdapter);
                } else {
                    mFriendListAdapter.setDatas(mUnFriendShow,mChildData);
                }
                UserDataCallBack.removeOnDataRefreshListeners(type, this);
                if(type==showType[0]) {
                    updateList(showType[1]);
                }
            }
        });
        EventBus.getDefault().post(UserDataCallBack.genrateParams(Const.currentId, type, true), Const.EVENT_BUS_TAG_GET_USER_DATAS);
    }
}
