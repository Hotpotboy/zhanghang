package com.sohu.focus.chat.fragment;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.sohu.focus.chat.Const;
import com.sohu.focus.chat.R;
import com.sohu.focus.chat.data.user.UserData;
import com.sohu.focus.eventbus.EventBus;
import com.souhu.hangzhang209526.zhanghang.base.BaseFragment;
import com.souhu.hangzhang209526.zhanghang.utils.CallBack;
import com.souhu.hangzhang209526.zhanghang.utils.PopupWindowUtils;
import com.souhu.hangzhang209526.zhanghang.utils.SystemUtils;
import com.souhu.hangzhang209526.zhanghang.utils.VolleyUtils;
import com.souhu.hangzhang209526.zhanghang.utils.cache.ImageCacheImpl;
import com.souhu.hangzhang209526.zhanghang.utils.camera.CameraUtils;
import com.souhu.hangzhang209526.zhanghang.widget.CycleImageView;

import java.util.Date;

/**
 * Created by hangzhang209526 on 2016/3/17.
 */
public class UserInfoFragment extends BaseFragment {
    /**当前用户是否是好友*/
    private boolean isFriend = false;
    /**头像*/
    private CycleImageView mHeadImage;
    //niceName
    private TextView mNickName;
    /**用户名*/
    private TextView mUserName;
    /**注册时间*/
    private TextView mCreateTime;
    /**上次登陆时间*/
    private TextView mLastLoginTime;
    /**上次下线时间*/
    private TextView mLastOffTime;
    /**我的二维码按钮*/
    private TextView mMyQrCode;
    /**添加好友按钮*/
    private TextView mAddFriend;
    /**用户ID*/
    private long mUserId;
    /**用户信息*/
    private UserData mUserInfo;
    /**
     *获取UserInfoFragment初始化所需的Intent
     **/
    public static Intent getUserInfoFragmentIntent(long id,boolean isFriendOrSelf){
        Intent intent = new Intent();
        intent.putExtra(Const.INTENT_KEY_USER_ID, id);
        boolean isFirendOrSelf = id==Const.currentId||isFriendOrSelf;
        intent.putExtra(Const.INTENT_KEY_IS_FRIEND, isFirendOrSelf);
        return intent;
    }

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_user_info;
    }

    @Override
    protected void initDataFromArguments(Bundle arguments){
        isFriend = arguments.getBoolean(Const.INTENT_KEY_IS_FRIEND, false);
        mUserId = arguments.getLong(Const.INTENT_KEY_USER_ID, -1L);
    }

    @Override
    protected void initView(){
        mHeadImage = (CycleImageView) findViewById(R.id.my_info_headImage);

        mNickName = (TextView)findViewById(R.id.my_info_niceName);

        mUserName = (TextView)findViewById(R.id.my_info_userName);

        mCreateTime = (TextView)findViewById(R.id.my_info_createTime);

        mLastLoginTime = (TextView)findViewById(R.id.my_info_loginTime);

        mLastOffTime = (TextView)findViewById(R.id.my_info_offTime);

        mMyQrCode = (TextView)findViewById(R.id.my_info_myQrCode);
        mMyQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateQRCode(mUserInfo.getId());
            }
        });

        mAddFriend = (TextView)findViewById(R.id.my_info_addFirend);
        if(isFriend){
            mAddFriend.setVisibility(View.GONE);
        }else{
            mAddFriend.setVisibility(View.VISIBLE);
            mAddFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(mUserInfo.getId()+"", Const.EVENT_BUS_TAG_ADD_FRIEND);//添加盆友
                }
            });
        }
    }

    @Override
    protected void initData(){
        Const.getUserInfo(mUserId, new CallBack<UserData, Void>() {
            @Override
            public Void run(UserData userData) {
                mUserInfo = userData;
                mNickName.setText(mUserInfo.getNickName());
                mUserName.setText(mUserInfo.getUserName());
                String createTime = SystemUtils.getTimestampStringListView(SystemUtils.TIME_FORMAT_yyyy_MM_dd_HH_mm_ss, new Date(mUserInfo.getCreateDate()));
                mCreateTime.setText(createTime);
                String loginTime = SystemUtils.getTimestampStringListView(SystemUtils.TIME_FORMAT_yyyy_MM_dd_HH_mm_ss, new Date(mUserInfo.getLastLoginDate()));
                mLastLoginTime.setText(loginTime);
                String offTime = SystemUtils.getTimestampStringListView(SystemUtils.TIME_FORMAT_yyyy_MM_dd_HH_mm_ss,new Date(mUserInfo.getLastOfflineTime()));
                mLastOffTime.setText(offTime);
                mHeadImage.setImageUrl(mUserInfo.getHeadPhoto(),new ImageLoader(VolleyUtils.getRequestQueue(),ImageCacheImpl.getInstance(mActivity)));
                return null;
            }
        },true);
    }

    /**
     * 弹出一个对话框，用来展示生成的二维码
     *
     * @param id
     */
    private void generateQRCode(long id) {
        boolean isFirst = PopupWindowUtils.isNeedInti(R.id.window_qr_code, mActivity, mMyQrCode.getRootView());
        final PopupWindowUtils showQRCodeWindow = PopupWindowUtils.getInstance(R.layout.window_qr_code, mActivity, mMyQrCode.getRootView());
        if(!isFriend) {//如果不是朋友
            showQRCodeWindow.setViewVisibility(R.id.window_qr_add_friend,View.VISIBLE);
            showQRCodeWindow.setOnClickForSpecialedView(R.id.window_qr_add_friend, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView imageView = (ImageView) showQRCodeWindow.getViewById(R.id.window_qr_code_image);
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                    if (bitmapDrawable != null) {
                        CameraUtils.scannerQRCodeForSpecialBitmap(bitmapDrawable.getBitmap(), new CallBack<String, Void>() {
                            @Override
                            public Void run(String s) {
                                EventBus.getDefault().post(s, Const.EVENT_BUS_TAG_ADD_FRIEND);//添加盆友
                                mActivity.finish();
                                return null;
                            }
                        });
                    } else {
                        Toast.makeText(mActivity, getResources().getString(R.string.can_not_scan_qrCode_tip_cn), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else{
            showQRCodeWindow.setViewVisibility(R.id.window_qr_add_friend,View.GONE);
        }

        //初始化相关值
        if (isFirst) {
            int parentWidth = mMyQrCode.getRootView().getRootView().getMeasuredWidth();
            int parentHeight = mMyQrCode.getRootView().getRootView().getMeasuredHeight();
            showQRCodeWindow.setStartLocationX((parentWidth - showQRCodeWindow.getPopupWindow().getWidth()) / 2);//开始X位置
            showQRCodeWindow.setStartLocationY((parentHeight - showQRCodeWindow.getPopupWindow().getHeight()) / 2);//开始Y位置
        }
        showQRCodeWindow.showAtLocation();
        //请求网络
        Object[] params = new Object[2];
        params[0] = id;
        params[1] = showQRCodeWindow.getViewById(R.id.window_qr_code_image);
        EventBus.getDefault().post(params, Const.EVENT_BUS_TAG_GENERATE_QR_CODE);
    }
}
