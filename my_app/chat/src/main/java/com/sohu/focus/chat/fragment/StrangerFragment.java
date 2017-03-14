package com.sohu.focus.chat.fragment;

import android.widget.ImageView;

import com.sohu.focus.chat.R;
import com.sohu.focus.chat.netcallback.UserDataCallBack;

/**
 * Created by hangzhang209526 on 2016/3/25.
 */
public class StrangerFragment extends UserFragment {

    @Override
    public void initData() {
        mUnFriendShow.add("附近的人");
        mUnFriendShow.add("红尘过客");
        showType[0] = UserDataCallBack.NEARBY;
        showType[1] = UserDataCallBack.STRANGER;
        //附近的人
        updateList(showType[0]);
    }
}
