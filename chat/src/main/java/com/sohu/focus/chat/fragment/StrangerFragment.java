package com.sohu.focus.chat.fragment;

import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.BaseListener;
import com.sohu.focus.chat.ChatApplication;
import com.sohu.focus.chat.Const;
import com.sohu.focus.chat.R;
import com.sohu.focus.chat.adapter.FriendListAdapter;
import com.sohu.focus.chat.data.user.UserData;
import com.sohu.focus.chat.netcallback.UserDataCallBack;
import com.sohu.focus.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/3/25.
 */
public class StrangerFragment extends UserFragment {
    /**我的过客图标*/
    private ImageView myStrangerImage;
    @Override
    protected void initView() {
        super.initView();
        myStrangerImage = (ImageView) findViewById(R.id.my_unfriend_image);
        myStrangerImage.setImageResource(R.drawable.stranger);
        mUnFriendShow[1] = "红尘过客";
        mUnFriednTextView.setText(mUnFriendShow[1]);
    }

    @Override
    public void initData() {
        if(!isShowMyUnFriend){
            updateList(UserDataCallBack.NEARBY);
        }else{
            updateList(UserDataCallBack.STRANGER);
        }
    }
}
