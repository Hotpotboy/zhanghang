package com.sohu.focus.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.BaseListener;
import com.sohu.focus.chat.ChatActivity;
import com.sohu.focus.chat.ChatApplication;
import com.sohu.focus.chat.Const;
import com.sohu.focus.chat.R;
import com.sohu.focus.chat.data.user.UserData;
import com.sohu.focus.chat.netcallback.StringDataCallBack;
import com.sohu.focus.chat.netcallback.UserDataCallBack;
import com.sohu.focus.eventbus.EventBus;
import com.zhanghang.self.adpter.BaseViewHolderAdapter;
import com.zhanghang.self.widget.CycleImageView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hangzhang209526 on 2016/1/29.
 */
public class FriendListAdapter extends BaseViewHolderAdapter implements View.OnTouchListener {
    private final String KEY_HEAD_IMG = "key_head_img";
    private final String KEY_SESSION_NAME = "key_session_name";
    private final String KEY_SESSION_LAST_MSG_STR = "key_session_last_msg_str";
    private final String KEY_SESSION_LAST_TIME = "key_session_last_time";
    private final String KEY_SESSION_UNREAD_NUM = "key_session_nunread_num";
    private final String KEY_SESSION_OPERATION1 = "key_session_operation1";
    private final String KEY_SESSION_OPERATION2 = "key_session_operation2";
    /**滑动的敏感距离*/
    private final static int SLOP_DISTANCE = 5;
    /**滑动的最大距离*/
    private int mMaxDistance;
    /**上一次的x位置*/
    private int mLastX = -1;
    /**按下的x位置*/
    private int mInitX = -1;
    /**移动的总距离*/
    private int mTotalDistance=0;
    /**ListView*/
    private ListView mListView;

    public FriendListAdapter(Context context, ArrayList<UserData> list,ListView listView) {
        super(context, list);
        mMaxDistance = (int) (context.getResources().getDimension(R.dimen.ninty_dp)*2);
        mListView = listView;
    }

    @Override
    public void setDatas(ArrayList datas){
        super.setDatas(datas);
        mLastX=-1;
        mInitX = -1;
        mTotalDistance=0;
    }

    @Override
    protected View inflaterView(int position) {
        View view = mLayoutInflater.inflate(R.layout.item_message_list, null);
        return view;
    }

    @Override
    protected void reBindDataAndView(int position, HashMap<String, View> baseViewHolder, View convertView) {
        final UserData data = (UserData) mDatas.get(position);
        if (baseViewHolder.containsKey(KEY_HEAD_IMG)) {
            CycleImageView headImag = (CycleImageView) baseViewHolder.get(KEY_HEAD_IMG);
        }
        //更新姓名
        TextView sessionOtherName = (TextView) getViewByTag(R.id.item_message_list_sender_name, KEY_SESSION_NAME, baseViewHolder, convertView);
        sessionOtherName.setText(data.getNickName()+"["+data.getUserName()+"]");
        View parent = ((View) sessionOtherName.getParent());
        parent.setOnTouchListener(this);
        //更新消息
        TextView sessionLastMsg = (TextView) getViewByTag(R.id.item_message_list_last_msg, KEY_SESSION_LAST_MSG_STR, baseViewHolder, convertView);
        sessionLastMsg.setText("暂无说说更新!");
        //更新最后对话时间
        TextView sessionLastTime = (TextView) getViewByTag(R.id.item_message_list_time, KEY_SESSION_LAST_TIME, baseViewHolder, convertView);
        sessionLastTime.setVisibility(View.GONE);
        //更新未读消息数
        TextView sessionUnReadNum = (TextView) getViewByTag(R.id.item_message_list_msg_num, KEY_SESSION_UNREAD_NUM, baseViewHolder, convertView);
        sessionUnReadNum.setVisibility(View.GONE);
        //操作按钮1
        TextView sessionOperation1 = (TextView) getViewByTag(R.id.item_message_list_operation1, KEY_SESSION_OPERATION1, baseViewHolder, convertView);
        //操作按钮2
        TextView sessionOperation2 = (TextView) getViewByTag(R.id.item_message_list_operation2, KEY_SESSION_OPERATION2, baseViewHolder, convertView);
        if(data.getType()== UserDataCallBack.FRIEND){
            sessionOperation1.setText("删除");
            sessionOperation2.setText("添香红颜");
            sessionOperation2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final long userId = data.getId();
                    Object netParams = StringDataCallBack.generateAddTrustNetParams(userId);
                    StringDataCallBack.addOnDataRefreshListeners(StringDataCallBack.NET_ADD_TRUST, new BaseListener.OnDataRefreshListener() {
                        @Override
                        public void OnDataRefresh(Object stringData) {//刷新
                            refreashData(UserDataCallBack.FRIEND);//刷新好友列表
                            Toast.makeText(mContext,"幸得知己，红袖添香……",Toast.LENGTH_LONG).show();
                            StringDataCallBack.removeOnDataRefreshListeners(StringDataCallBack.NET_GET_SESSION_ID, this);
                        }
                    });
                    EventBus.getDefault().post(netParams, Const.EVENT_BUS_TAG_GET_STRING_DATA);
                }
            });
        }else if(data.getType()== UserDataCallBack.TRUST){//红颜知己
            sessionOperation1.setText("删除");
            sessionOperation2.setText("逝去知己");
            sessionOperation2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final long userId = data.getId();
                    Object netParams = StringDataCallBack.generateDeleteTrustNetParams(userId);
                    StringDataCallBack.addOnDataRefreshListeners(StringDataCallBack.NET_DELETE_TRUST, new BaseListener.OnDataRefreshListener() {
                        @Override
                        public void OnDataRefresh(Object stringData) {//刷新
                            refreashData(UserDataCallBack.TRUST);//刷新知己列表
                            Toast.makeText(mContext,"君子相交，淡如逝水……",Toast.LENGTH_LONG).show();
                            StringDataCallBack.removeOnDataRefreshListeners(StringDataCallBack.NET_GET_SESSION_ID, this);
                        }
                    });
                    EventBus.getDefault().post(netParams, Const.EVENT_BUS_TAG_GET_STRING_DATA);
                }
            });
        }
    }

    private void refreashData(final int type){
        UserDataCallBack.addOnDataRefreshListeners(type, new BaseListener.OnDataRefreshListener() {

            @Override
            public void OnDataRefresh(Object data) {
                setDatas((ArrayList) data);
                UserDataCallBack.removeOnDataRefreshListeners(type,this);
            }
        });
        EventBus.getDefault().post(UserDataCallBack.genrateParams(Const.currentId, type, true), Const.EVENT_BUS_TAG_GET_USER_DATAS);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v instanceof ViewGroup){
            ((ViewGroup) v).requestDisallowInterceptTouchEvent(true);
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getRawX();
                mInitX = (int) event.getRawX();
                return true;
            case MotionEvent.ACTION_MOVE:
                int currentX = (int) event.getRawX();
                if(Math.abs(currentX-mLastX)<SLOP_DISTANCE){
                    return false;
                }else {
                    int distance = mLastX-currentX;
                    if(canScroll(distance<0)) {
                        if((mTotalDistance-distance)>0){
                            distance = mTotalDistance;
                        }
                        if(mTotalDistance-distance<-mMaxDistance){
                            distance = mTotalDistance + mMaxDistance;
                        }
                        v.offsetLeftAndRight(-distance);
                        mTotalDistance -= distance;
                        mLastX = currentX;
                        return true;
                    }else{//不能滑动了
//                        if(v instanceof ViewGroup){
//                            ((ViewGroup) v).requestDisallowInterceptTouchEvent(false);
//                        }
                        return false;
                    }
                }
            case MotionEvent.ACTION_UP:
                currentX = (int) event.getRawX();
                if(Math.abs(currentX-mInitX)<SLOP_DISTANCE){//点击
                    int position = mListView.getFirstVisiblePosition() - mListView.getHeaderViewsCount();
                    position+=mListView.indexOfChild((View) v.getParent());
                    UserData userInfo = (UserData) getItem(position);
                    final long userId = userInfo.getId();
                    Object netParams = StringDataCallBack.generateSessionIdNetParams(userId);
                    StringDataCallBack.addOnDataRefreshListeners(StringDataCallBack.NET_GET_SESSION_ID, new BaseListener.OnDataRefreshListener() {
                        @Override
                        public void OnDataRefresh(Object stringData) {
                            Intent intent = new Intent(mContext, ChatActivity.class);
                            intent.putExtra(Const.INTENT_KEY_USER_ID, userId);
                            intent.putExtra(Const.INTENT_KEY_SESSION_ID, Long.valueOf((String) stringData));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                            StringDataCallBack.removeOnDataRefreshListeners(StringDataCallBack.NET_GET_SESSION_ID, this);
                        }
                    });
                    EventBus.getDefault().post(netParams, Const.EVENT_BUS_TAG_GET_STRING_DATA);
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                return true;
            default:
                return false;
        }
    }

    /**
     *
     * @param isleft    是否朝着右滑动
     * @return
     */
    private boolean canScroll(boolean isleft){
        return (mTotalDistance>-mMaxDistance&&!isleft)||(mTotalDistance<0&&isleft);
    }
}
