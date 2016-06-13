package com.sohu.focus.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.zhanghang.self.adpter.BaseViewHolderExpandableAdapter;
import com.zhanghang.self.widget.CycleImageView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hangzhang209526 on 2016/1/29.
 */
public class FriendListAdapter extends BaseViewHolderExpandableAdapter implements View.OnTouchListener, View.OnClickListener {
    private final String KEY_HEAD_IMG = "key_head_img";
    private final String KEY_SESSION_NAME = "key_session_name";
    private final String KEY_SESSION_LAST_MSG_STR = "key_session_last_msg_str";
    private final String KEY_SESSION_LAST_TIME = "key_session_last_time";
    private final String KEY_SESSION_UNREAD_NUM = "key_session_nunread_num";
    private final String KEY_SESSION_OPERATION1 = "key_session_operation1";
    private final String KEY_SESSION_OPERATION2 = "key_session_operation2";
    private final String KEY_SESSION_OPERATION3 = "key_session_operation3";
    private final String KEY_SESSION_SPLITE = "key_session_splite";
    private final String KEY_GROUP_TEXT = "key_group_text";
    private final String KEY_GROUP_IMAGE = "key_group_image";
    private final String KEY_GROUP_ARROW = "key_group_arrow";
    /**
     * 将知己转变为好友
     */
    public static final int OPERA_TRUEST_TO_FRIEND = 1;
    /**
     * 将好友转变为知己
     */
    public static final int OPERA_FRIEND_TO_TRUEST = 2;
    /**
     * 将好友转变为陌生人
     */
    public static final int OPERA_FRIEND_TO_STRANGER = 3;
    /**
     * 将附近的人转变为好友
     */
    public static final int OPERA_NEARBY_TO_FRIEND = 4;
    /**
     * 将陌生人转变为好友
     */
    public static final int OPERA_STRANGER_TO_FRIEND = 5;
    /**
     * 滑动的敏感距离
     */
    private final static int SLOP_DISTANCE = 5;
    /**
     * 滑动的最大距离
     */
    private static SparseIntArray mMaxDistance;
    /**
     * 上一次的x位置
     */
    private static SparseIntArray mLastX;
    /**
     * 按下的x位置
     */
    private static SparseIntArray mInitX;
    /**
     * 移动的总距离
     */
    private static SparseIntArray mTotalDistance;
    /**
     * ListView
     */
    private ExpandableListView mListView;
    /**
     * 网络数据更新的回调接口数组
     */
    private static SparseArray<BaseListener.OnDataRefreshListener> mDataRefreshListeners = new SparseArray<>();
    /**
     * 实例数组
     */
    private static SparseArray<FriendListAdapter> sInstances = new SparseArray<>();

    static {
        mMaxDistance = new SparseIntArray();
        mMaxDistance.put(UserDataCallBack.FRIEND, getMaxDistance(3));//好友三个操作按钮
        mMaxDistance.put(UserDataCallBack.TRUST, getMaxDistance(2));//知己两个操作按钮
        mMaxDistance.put(UserDataCallBack.NEARBY, getMaxDistance(1));//附近的人1个操作按钮
        mMaxDistance.put(UserDataCallBack.STRANGER, getMaxDistance(1));//陌生人1个操作按钮
        mTotalDistance = new SparseIntArray();
        mLastX = new SparseIntArray();
        mInitX = new SparseIntArray();
    }

    private static void initTouchData() {
        initTouchDataForOneType(UserDataCallBack.FRIEND);
        initTouchDataForOneType(UserDataCallBack.TRUST);
        initTouchDataForOneType(UserDataCallBack.NEARBY);
        initTouchDataForOneType(UserDataCallBack.STRANGER);
    }

    private static void initTouchDataForOneType(int type) {
        mLastX.put(type, 0);
        mInitX.put(type, 0);
    }

    public FriendListAdapter(Context context, @NonNull ArrayList list, SparseArray<ArrayList> childrenData, ExpandableListView listView) {
        super(context, list, childrenData);
        mListView = listView;
        int size = sInstances.size();
        sInstances.put(size, this);
        initTouchData();
    }

    @Override
    public void setDatas(ArrayList datas, SparseArray<ArrayList> childrenData) {
        super.setDatas(datas, childrenData);
        initTouchData();
    }

    @Override
    protected View inflaterGroupView(int position) {
        return mLayoutInflater.inflate(R.layout.item_people_group, null);
    }

    @Override
    protected View inflaterChildView(int groupPosition, int childPosition) {
        View view = mLayoutInflater.inflate(R.layout.item_message_list, null);
        return view;
    }

    @Override
    protected void reBindDataAndGroupView(int groupPosition, boolean isExpanded, HashMap<String, View> baseViewHolder, View convertView) {
        TextView groupTextView = (TextView) getViewByTag(R.id.item_group_text, KEY_GROUP_TEXT, baseViewHolder, convertView);
        String text = (String) mGroupDatas.get(groupPosition);
        groupTextView.setText(text);

        ImageView groupImage = (ImageView) getViewByTag(R.id.item_group_image, KEY_GROUP_IMAGE, baseViewHolder, convertView);
        ImageView arrowImage = (ImageView) getViewByTag(R.id.item_group_arrow, KEY_GROUP_ARROW, baseViewHolder, convertView);
        if (isExpanded) arrowImage.setImageResource(R.drawable.downarrow_gray);
        else arrowImage.setImageResource(R.drawable.rightarrow_gray);

    }

    @Override
    protected void reBindDataAndChildView(int groupPosition, int childPosition, boolean isLastChild, HashMap<String, View> baseViewHolder, View convertView) {
        final UserData data = (UserData) mChildDatas.get(groupPosition).get(childPosition);
        if (baseViewHolder.containsKey(KEY_HEAD_IMG)) {
            CycleImageView headImag = (CycleImageView) baseViewHolder.get(KEY_HEAD_IMG);
        }
        //更新姓名
        TextView sessionOtherName = (TextView) getViewByTag(R.id.item_message_list_sender_name, KEY_SESSION_NAME, baseViewHolder, convertView);
        sessionOtherName.setText(data.getNickName() + "[" + data.getUserName() + "]");
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
        //操作按钮3
        TextView sessionOperation3 = (TextView) getViewByTag(R.id.item_message_list_operation3, KEY_SESSION_OPERATION3, baseViewHolder, convertView);
        updateOperationButtons(data.getType(), data.getId(), sessionOperation1, sessionOperation2, sessionOperation3);
        View parent = ((View) sessionOtherName.getParent());
        parent.setOnTouchListener(this);
    }

    private void updateOperationButtons(int type, final long userId, TextView sessionOperation1, TextView sessionOperation2, TextView sessionOperation3) {
        if (type == UserDataCallBack.FRIEND) {//我的好友
            sessionOperation2.setVisibility(View.VISIBLE);
            sessionOperation3.setVisibility(View.VISIBLE);
            sessionOperation1.setText("删除");
            sessionOperation2.setText("添香红颜");
            sessionOperation2.setTag(OPERA_FRIEND_TO_TRUEST);
            sessionOperation2.setOnClickListener(this);
            sessionOperation3.setText("与子相弃");
            sessionOperation3.setTag(OPERA_FRIEND_TO_STRANGER);
            sessionOperation3.setOnClickListener(this);
        } else if (type == UserDataCallBack.TRUST) {//红颜知己
            sessionOperation2.setVisibility(View.VISIBLE);
            sessionOperation3.setVisibility(View.GONE);
            sessionOperation1.setText("删除");
            sessionOperation2.setText("逝去知己");
            sessionOperation2.setTag(OPERA_TRUEST_TO_FRIEND);
            sessionOperation2.setOnClickListener(this);
        } else if (type == UserDataCallBack.NEARBY
                || type == UserDataCallBack.STRANGER) {//附近的人及陌生人
            sessionOperation2.setVisibility(View.GONE);
            sessionOperation3.setVisibility(View.GONE);
            sessionOperation1.setText("加为好友");
            if (type == UserDataCallBack.NEARBY) sessionOperation1.setTag(OPERA_NEARBY_TO_FRIEND);
            else sessionOperation1.setTag(OPERA_STRANGER_TO_FRIEND);
            sessionOperation1.setOnClickListener(this);
        }
    }

    private static int getMaxDistance(int muilt) {
        return (int) (ChatApplication.getInstance().getResources().getDimension(R.dimen.ninty_dp) * muilt);
    }

    private static void refreashData(final int type, final FriendListAdapter instance) {
        int index = sInstances.indexOfValue(instance);
        BaseListener.OnDataRefreshListener listener = mDataRefreshListeners.get(index);
        listener = new BaseListener.OnDataRefreshListener() {

            @Override
            public void OnDataRefresh(Object data) {
                if (type == UserDataCallBack.FRIEND || type == UserDataCallBack.NEARBY) {
                    instance.mChildDatas.put(0, (ArrayList) data);
                } else {
                    instance.mChildDatas.put(1, (ArrayList) data);
                }
                instance.setDatas(instance.mGroupDatas, instance.mChildDatas);
                UserDataCallBack.removeOnDataRefreshListeners(type,this);
            }
        };
        UserDataCallBack.addOnDataRefreshListeners(type, listener);
        mDataRefreshListeners.put(index, listener);
        EventBus.getDefault().post(UserDataCallBack.genrateParams(Const.currentId, type, true), Const.EVENT_BUS_TAG_GET_USER_DATAS);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v instanceof ViewGroup) {
            ((ViewGroup) v).requestDisallowInterceptTouchEvent(true);
        }
        int[] positions = getPositonForSepialView(v);
        UserData data = (UserData) getChild(positions[0], positions[1]);
        int userType = data.getType();//用户类型
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX.put(userType, (int) event.getRawX());
                mInitX.put(userType, (int) event.getRawX());
                return true;
            case MotionEvent.ACTION_MOVE:
                int currentX = (int) event.getRawX();
                if (Math.abs(currentX - mLastX.get(userType)) < SLOP_DISTANCE) {
                    return false;
                } else {
                    int distance = mLastX.get(userType) - currentX;
                    if (positions != null && positions.length == 2) {
                        int maxDistance = mMaxDistance.get(userType);
                        if (canScroll(distance < 0, userType)) {
                            int totalDistance = mTotalDistance.get(userType);
                            if ((totalDistance - distance) > 0) {
                                distance = totalDistance;
                            }
                            if (totalDistance - distance < -maxDistance) {
                                distance = totalDistance + maxDistance;
                            }
                            v.offsetLeftAndRight(-distance);
                            mTotalDistance.put(userType, totalDistance - distance);
                            mLastX.put(userType, currentX);
                            return true;
                        } else {//不能滑动了
//                        if(v instanceof ViewGroup){
//                            ((ViewGroup) v).requestDisallowInterceptTouchEvent(false);
//                        }
                            return false;
                        }
                    }
                    return false;
                }
            case MotionEvent.ACTION_UP:
                currentX = (int) event.getRawX();
                if (Math.abs(currentX - mInitX.get(userType)) < SLOP_DISTANCE) {//点击
                    UserData userInfo = getDataByView(v);
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

    private UserData getDataByView(View v) {
        int[] position = getPositonForSepialView(v);
        if (position != null && position.length == 2) {
            UserData userInfo = (UserData) getChild(position[0], position[1]);
            return userInfo;
        } else {
            return null;
        }
    }

    /**
     * 获取指定视图对应数据的坐标
     *
     * @param v
     * @return 长度为2的数组，第一个元素表示数据对应父数据的索引，第二个元素表示数据所在父数据中的索引
     */
    private int[] getPositonForSepialView(View v) {
        int position = mListView.getFirstVisiblePosition() - mListView.getHeaderViewsCount();
        int index = -1;
        while (v.getParent() instanceof View) {
            index = mListView.indexOfChild(v);
            if (index >= 0 && index < mListView.getChildCount()) {
                break;
            }
            v = (View) v.getParent();
        }
        if (index < 0) return null;
        position += index;//item视图在整个ListView之中的索引，包括group视图
        long packedPosition = mListView.getExpandableListPosition(position);
        int groupPostion = ExpandableListView.getPackedPositionGroup(packedPosition);//此item视图对应的Group视图在Group视图列表中的索引
        int childPostion = ExpandableListView.getPackedPositionChild(packedPosition);//此item视图在其对应的Group视图中的索引
        int[] result = new int[2];
        result[0] = groupPostion;
        result[1] = childPostion;
        return result;
    }

    /**
     * @param isleft 是否朝着右滑动
     * @return
     */
    private boolean canScroll(boolean isleft, int type) {
        return (mTotalDistance.get(type) > -mMaxDistance.get(type) && !isleft) || (mTotalDistance.get(type) < 0 && isleft);
    }

    @Override
    public void onClick(View v) {
        int type = (int) v.getTag();
        UserData user = getDataByView(v);
        operationForAllType(type, user);
    }

    /**
     * 4种类型之间的用户相互转换关系的操作~
     *
     * @param type
     * @param user
     */
    public static void operationForAllType(int type, UserData user) {
        final SparseBooleanArray isFreashData = getisRefreashSpecialList();
        int interfaceType = -1;//接口类型
        String tip = "";
        Object netParams = null;
        long userId = user.getId();
        if (type == OPERA_TRUEST_TO_FRIEND) {//知己到好友
            netParams = StringDataCallBack.generateDeleteTrustNetParams(userId);
            interfaceType = StringDataCallBack.NET_DELETE_TRUST;
            tip = "君子相交，淡如逝水……";
            isFreashData.put(UserDataCallBack.TRUST, true);//刷新知己列表
            isFreashData.put(UserDataCallBack.FRIEND, true);//刷新好友列表
        } else if (type == OPERA_FRIEND_TO_TRUEST) {//好友到知己
            netParams = StringDataCallBack.generateAddTrustNetParams(userId);
            interfaceType = StringDataCallBack.NET_ADD_TRUST;
            tip = "幸得知己，红袖添香……";
            isFreashData.put(UserDataCallBack.TRUST, true);//刷新知己列表
            isFreashData.put(UserDataCallBack.FRIEND, true);//刷新好友列表
        } else if (type == OPERA_FRIEND_TO_STRANGER) {//好友到陌生人
            netParams = StringDataCallBack.generateFriendToStrangerNetParams(userId);
            interfaceType = StringDataCallBack.NET_FRIEND_TO_STRANGER;
            tip = "不为同袍，自为陌路……";
            isFreashData.put(UserDataCallBack.STRANGER, true);//刷新陌生人列表
            isFreashData.put(UserDataCallBack.FRIEND, true);//刷新好友列表
        } else if (type == OPERA_NEARBY_TO_FRIEND
                || type == OPERA_STRANGER_TO_FRIEND) {//附近/陌生人的人到好友
            netParams = StringDataCallBack.generateAddFriendNetParams(user, "能添加你为好友吗?");
            interfaceType = StringDataCallBack.NET_ADD_FRIEND;
            isFreashData.put(UserDataCallBack.FRIEND, true);//刷新好友列表
            tip = "飞书已传,静候佳音……";
            if (type == OPERA_NEARBY_TO_FRIEND) {
//                tip = "既为近邻，岂可不约……";
                isFreashData.put(UserDataCallBack.NEARBY, true);//刷新陌生人列表
            } else {
//                tip = "无意前缘,只取当下……";
                isFreashData.put(UserDataCallBack.STRANGER, true);//刷新陌生人列表
            }
        }
        if (interfaceType > 0) {
            final String tipFinal = tip;
            StringDataCallBack.addOnDataRefreshListeners(interfaceType, new BaseListener.OnDataRefreshListener() {
                @Override
                public void OnDataRefresh(Object stringData) {//刷新
                    for (int i = 0; i < isFreashData.size(); i++) {
                        boolean isRefreash = isFreashData.valueAt(i);
                        if (isRefreash) {
                            int listType = isFreashData.keyAt(i);
                            FriendListAdapter instance = null;
                            if (listType == UserDataCallBack.FRIEND || listType == UserDataCallBack.TRUST) {
                                instance = sInstances.get(0);
                            } else {
                                instance = sInstances.get(1);
                            }
                            refreashData(listType, instance);//刷新相应列表
                        }
                    }
                    if (TextUtils.isEmpty(tipFinal)) {
                        Toast.makeText(ChatApplication.getInstance(), tipFinal, Toast.LENGTH_LONG).show();
                    }
                    StringDataCallBack.removeOnDataRefreshListeners(StringDataCallBack.NET_DELETE_TRUST, this);
                }
            });
            EventBus.getDefault().post(netParams, Const.EVENT_BUS_TAG_GET_STRING_DATA);
        }
    }

    private static SparseBooleanArray getisRefreashSpecialList() {
        SparseBooleanArray isFreashData = new SparseBooleanArray();//是否刷新该类别的列表
        isFreashData.put(UserDataCallBack.TRUST, false);
        isFreashData.put(UserDataCallBack.FRIEND, false);
        isFreashData.put(UserDataCallBack.NEARBY, false);
        isFreashData.put(UserDataCallBack.STRANGER, false);
        return isFreashData;
    }
}
