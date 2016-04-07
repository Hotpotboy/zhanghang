package com.sohu.focus.chat.adapter;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import com.sohu.focus.chat.R;
import com.sohu.focus.chat.data.user.UserData;
import com.zhanghang.self.adpter.BaseViewHolderAdapter;
import com.zhanghang.self.widget.CycleImageView;

import java.lang.reflect.Field;
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
    /**滑动的敏感距离*/
    private final static int SLOP_DISTANCE = 5;
    /**滑动的最大距离*/
    private int mMaxDistance;
    /**上一次的x位置*/
    private int mLastX = -1;

    public FriendListAdapter(Context context, ArrayList<UserData> list) {
        super(context, list);
        mMaxDistance = (int) (context.getResources().getDimension(R.dimen.ninty_dp)*2);
    }

    @Override
    protected View inflaterView(int position) {
        View view = mLayoutInflater.inflate(R.layout.item_message_list, null);
        return view;
    }

    @Override
    protected void reBindDataAndView(int position, HashMap<String, View> baseViewHolder, View convertView) {
        UserData data = (UserData) mDatas.get(position);
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
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v instanceof ViewGroup){
            ((ViewGroup)v).requestDisallowInterceptTouchEvent(true);
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getX();
                return true;
            case MotionEvent.ACTION_MOVE:
                int currentX = (int) event.getX();
                if(Math.abs(currentX-mLastX)<SLOP_DISTANCE){
                    return false;
                }else if(currentX>mLastX){
                    return false;
                }else{
                    int distance = mLastX-currentX;
                    if(Math.abs(v.getLeft())<=mMaxDistance) {
                        v.offsetLeftAndRight(-distance);
                        return true;
                    }else{
                        if(v instanceof ViewGroup){
                            ((ViewGroup)v).requestDisallowInterceptTouchEvent(false);
                            ViewParent parent = (ViewParent)v;
                            while (parent instanceof ViewGroup) {
                                if (parent instanceof ViewPager) {
                                    try {
                                        Field lastX = ViewPager.class.getDeclaredField("mLastMotionX");
                                        Field initX = ViewPager.class.getDeclaredField("mInitialMotionX");
                                        lastX.setAccessible(true);
                                        initX.setAccessible(true);
                                        int[] location = new int[2];
                                        ((ViewPager) parent).getLocationOnScreen(location);
                                        int relativeX = (int) (event.getRawX() - location[0]);
                                        lastX.set(parent, relativeX);
                                        initX.set(parent, relativeX);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                                parent = parent.getParent();
                            }
                            Log.e("ttttttttttttttt", v.getLeft() + "");
                        }
                        return false;
                    }
                }
            case MotionEvent.ACTION_UP:
                return true;
            case MotionEvent.ACTION_CANCEL:
                return true;
            default:
                return false;
        }
    }
}
