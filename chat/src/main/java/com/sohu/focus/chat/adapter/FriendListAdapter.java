package com.sohu.focus.chat.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.sohu.focus.chat.R;
import com.sohu.focus.chat.data.FriendData;
import com.souhu.hangzhang209526.zhanghang.adpter.BaseViewHolderAdapter;
import com.souhu.hangzhang209526.zhanghang.widget.CycleImageView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hangzhang209526 on 2016/1/29.
 */
public class FriendListAdapter extends BaseViewHolderAdapter {
    private final String KEY_HEAD_IMG = "key_head_img";
    private final String KEY_SESSION_NAME = "key_session_name";
    private final String KEY_SESSION_LAST_MSG_STR = "key_session_last_msg_str";
    private final String KEY_SESSION_LAST_TIME = "key_session_last_time";
    private final String KEY_SESSION_UNREAD_NUM = "key_session_nunread_num";

    public FriendListAdapter(Context context, ArrayList<FriendData> list) {
        super(context, list);
    }

    @Override
    protected View inflaterView(int position) {
        return mLayoutInflater.inflate(R.layout.item_message_list, null);
    }

    @Override
    protected void reBindDataAndView(int position, HashMap<String, View> baseViewHolder, View convertView) {
        FriendData data = (FriendData) mDatas.get(position);
        if (baseViewHolder.containsKey(KEY_HEAD_IMG)) {
            CycleImageView headImag = (CycleImageView) baseViewHolder.get(KEY_HEAD_IMG);
        }
        //更新姓名
        TextView sessionOtherName = (TextView) getViewByTag(R.id.item_message_list_sender_name, KEY_SESSION_NAME, baseViewHolder, convertView);
        sessionOtherName.setText(data.getNickName()+"["+data.getUserName()+"]");
        //更新消息
        TextView sessionLastMsg = (TextView) getViewByTag(R.id.item_message_list_last_msg, KEY_SESSION_LAST_MSG_STR, baseViewHolder, convertView);
        sessionLastMsg.setText("暂无说说更新!");
        //更新最后对话时间
        TextView sessionLastTime = (TextView) getViewByTag(R.id.item_message_list_time, KEY_SESSION_LAST_TIME, baseViewHolder, convertView);
        sessionLastTime.setVisibility(View.GONE);
        //更新未读消息数
        TextView sessionUnReadNum = (TextView) getViewByTag(R.id.item_message_list_msg_num, KEY_SESSION_UNREAD_NUM, baseViewHolder, convertView);
        sessionUnReadNum.setVisibility(View.GONE);
    }
}
