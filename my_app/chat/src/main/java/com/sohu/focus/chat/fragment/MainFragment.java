package com.sohu.focus.chat.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.android.volley.toolbox.BaseListener;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.zxing.Intents;
import com.sohu.focus.chat.ChatApplication;
import com.sohu.focus.chat.Const;
import com.sohu.focus.chat.R;
import com.sohu.focus.chat.UserInfoActivity;
import com.sohu.focus.chat.adapter.FriendListAdapter;
import com.sohu.focus.chat.data.user.UserData;
import com.sohu.focus.chat.netcallback.StringDataCallBack;
import com.sohu.focus.chat.netcallback.UserDataCallBack;
import com.sohu.focus.eventbus.EventBus;
import com.zhanghang.self.base.BaseFragment;
import com.zhanghang.self.fragment.ViewPagerFragement;
import com.zhanghang.self.utils.LocationUtil;
import com.zhanghang.self.utils.PopupWindowUtils;
import com.zhanghang.self.utils.PreferenceUtil;
import com.zhanghang.self.utils.VolleyUtils;
import com.zhanghang.self.utils.cache.ImageCacheImpl;
import com.zhanghang.self.utils.camera.CameraUtils;
import com.zhanghang.self.widget.CycleImageView;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/3/16.
 */
public class MainFragment extends ViewPagerFragement implements  AMapLocationListener, View.OnClickListener {
    private static final String TAG = "MainFragment.class";
    private UserDataCallBack mUserDataCallBack;
    /**我的好友标签*/
    private TextView mFriendsCheckBox;
    /**附近的人标签*/
    private TextView mNearCheckBox;
    /**
     * 添加按钮
     */
    private TextView addFriend;

    /**查询时，输入ID的文本框*/
    private EditText mSearchIdEditText;
    /**查询按钮*/
    private ImageView mSearchButton;
    private CycleImageView mHeadImage;

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater,ViewGroup viewGroup,Bundle savedInstanceState){
        mRootLayout = R.layout.fragment_main;
        return super.onCreateView(layoutInflater,viewGroup,savedInstanceState);
    }

    @Override
    protected ArrayList<BaseFragment> specifyFragmentList() {
        ArrayList<BaseFragment> baseFragments = new ArrayList<>();
        baseFragments.add(new UserFragment());//好友列表
        baseFragments.add(new StrangerFragment());//附近的人列表
        return baseFragments;
    }

    @Override
    protected ViewPager specifyViewPager() {
        ViewPager viewPager = (ViewPager) mRootView.findViewById(R.id.friend_view_pager);
        return viewPager;
    }

    @Override
    protected void initView(){
        super.initView();
        mSearchIdEditText = (EditText) mRootView.findViewById(R.id.friend_search_id);
        mSearchButton = (ImageView) mRootView.findViewById(R.id.friend_serach_button);
        addFriend = (TextView) mRootView.findViewById(R.id.friend_add);
        mFriendsCheckBox = (TextView) mRootView.findViewById(R.id.friend_check_friends);
        mNearCheckBox = (TextView) mRootView.findViewById(R.id.friend_check_near);
        mHeadImage = (CycleImageView)findViewById(R.id.friend_head_image);

        mFriendsCheckBox.setSelected(true);
        mNearCheckBox.setSelected(false);
        mFriendsCheckBox.setOnClickListener(this);
        mNearCheckBox.setOnClickListener(this);
        mSearchButton.setOnClickListener(this);
        addFriend.setOnClickListener(this);
        mHeadImage.setOnClickListener(this);

        LocationUtil.setAMapLocationListener(this);
        try {
            LocationUtil.requestLocation(-1);//请求定位
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initData(){
        boolean isFocusGetDataFromNet = !UserDataCallBack.getInstance(UserDataCallBack.SELF_OR_OTHER).isInCache(Const.currentId);//是否缓存了当前用户的信息
        Object[] params = UserDataCallBack.genrateParams(Const.currentId,UserDataCallBack.SELF_OR_OTHER,isFocusGetDataFromNet);
        UserDataCallBack.addOnDataRefreshListeners(UserDataCallBack.SELF_OR_OTHER, new BaseListener.OnDataRefreshListener() {
            @Override
            public void OnDataRefresh(Object data) {
                mHeadImage.setImageUrl(((ArrayList<UserData>)data).get(0).getHeadPhoto(), new ImageLoader(VolleyUtils.getRequestQueue(), ImageCacheImpl.getInstance(mActivity)));
                UserDataCallBack.removeOnDataRefreshListeners(UserDataCallBack.SELF_OR_OTHER, this);
            }
        });
        EventBus.getDefault().post(params, Const.EVENT_BUS_TAG_GET_USER_DATAS);
        super.initData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocationUtil.stopLocation();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation.getErrorCode() == 0) {
            //获取位置信息
            Double geoLat = aMapLocation.getLatitude();
            Double geoLng = aMapLocation.getLongitude();
            PreferenceUtil.updateLongInPreferce(ChatApplication.getInstance(),ChatApplication.getInstance().getVersionName(),Const.SHARE_LAT_KEY, (long) (geoLat*10000));
            PreferenceUtil.updateLongInPreferce(ChatApplication.getInstance(),ChatApplication.getInstance().getVersionName(),Const.SHARE_LON_KEY, (long) (geoLng*10000));
            EventBus.getDefault().post(StringDataCallBack.generateSendLocationNetParams(geoLng,geoLat),Const.EVENT_BUS_TAG_GET_STRING_DATA);
        } else if (aMapLocation != null) {
            Log.e("zhanghang", aMapLocation.getErrorCode() + "");
            Log.e("zhanghang", aMapLocation.getErrorInfo());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.friend_add:
                int width = (int) getResources().getDimension(R.dimen.one_hundred_fifty_dp);
                int height = ViewGroup.LayoutParams.WRAP_CONTENT;
                PopupWindowUtils menuPopupWindow = PopupWindowUtils.getInstance(R.layout.window_add_friend, mActivity, v, width, height);
                menuPopupWindow.setOnClickForSpecialedView(R.id.window_add_ScanQR, this);
                menuPopupWindow.setOnClickForSpecialedView(R.id.window_add_myQR, this);
                menuPopupWindow.show();
                break;
            case R.id.window_add_myQR://我的二维码
                generateQRCode(Const.currentId);
                break;
            case R.id.window_add_ScanQR://扫描二维码
                CameraUtils.scannerQRCode(mActivity);
                break;
            case R.id.friend_serach_button://查询用户
                Editable content = mSearchIdEditText.getText();
                if(TextUtils.isEmpty(content)){
                    Toast.makeText(mActivity, "请输入要查询的ID", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!TextUtils.isDigitsOnly(content)){
                    Toast.makeText(mActivity,"输入的ID必须为数字",Toast.LENGTH_LONG).show();
                    return;
                }
                long id = Long.valueOf(content.toString());
                gotoUserInfoActivity(id);
                mSearchIdEditText.setText("");
                mSearchIdEditText.clearFocus();
                break;
            case R.id.friend_check_friends://点击我的好友
                if(!mFriendsCheckBox.isSelected()){
                    setCurrentFragment(0);
                }
                break;
            case R.id.friend_check_near://点击附近的人
                if(!mNearCheckBox.isSelected()){
                    setCurrentFragment(1);
                }
                break;
            case R.id.friend_head_image://点击标题栏的头像图标
                EventBus.getDefault().post(Gravity.LEFT,Const.EVENT_BUS_TAG_OPEN_DRAWER);
                break;
            default:
                break;
        }
    }

    private void gotoUserInfoActivity(long id) {
        Intent intent = UserInfoFragment.getUserInfoFragmentIntent(id);
        intent.setClass(mActivity,UserInfoActivity.class);
        mActivity.startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == mActivity.RESULT_OK) {
            switch (requestCode) {
                case CameraUtils.SCANNER_QR_CODE_REQUEST_CODE:
                    String result = data.getStringExtra(Intents.Scan.RESULT);
                   long userId = Long.valueOf(result);
                    UserDataCallBack.addOnDataRefreshListeners(UserDataCallBack.SELF_OR_OTHER, new BaseListener.OnDataRefreshListener() {
                        @Override
                        public void OnDataRefresh(Object data) {
                            if(data!=null&&data instanceof ArrayList){
                                if(((ArrayList)data).size()>0) {
                                    Object userDataObj = ((ArrayList) data).get(0);
                                    if(userDataObj instanceof UserData){
                                        FriendListAdapter.operationForAllType(FriendListAdapter.OPERA_STRANGER_TO_FRIEND, (UserData) userDataObj);
                                    }
                                }
                            }
                            UserDataCallBack.removeOnDataRefreshListeners(UserDataCallBack.SELF_OR_OTHER,this);
                        }
                    });
                    EventBus.getDefault().post(UserDataCallBack.genrateParams(userId, UserDataCallBack.SELF_OR_OTHER, true), Const.EVENT_BUS_TAG_GET_USER_DATAS);
                    break;
            }
        }
    }

    /**
     * 弹出一个对话框，用来展示生成的二维码
     *
     * @param id
     */
    private void generateQRCode(long id) {
        boolean isFirst = PopupWindowUtils.isNeedInti(R.layout.window_qr_code, mActivity, mRootView);
        final PopupWindowUtils showQRCodeWindow = PopupWindowUtils.getInstance(R.layout.window_qr_code, mActivity, mRootView);
        showQRCodeWindow.setViewVisibility(R.id.window_qr_add_friend,View.GONE);

        //初始化相关值
        if (isFirst) {
            int parentWidth = mRootView.getRootView().getMeasuredWidth();
            int parentHeight = mRootView.getRootView().getMeasuredHeight();
            showQRCodeWindow.setStartLocationX((parentWidth - showQRCodeWindow.getPopupWindow().getWidth()) / 2);//开始X位置
            showQRCodeWindow.setStartLocationY((parentHeight - showQRCodeWindow.getPopupWindow().getHeight()) / 2);//开始Y位置

        }
        showQRCodeWindow.showAtLocation();
        //请求网络
        final NetworkImageView imageView = (NetworkImageView) showQRCodeWindow.getViewById(R.id.window_qr_code_image);
        final int type = StringDataCallBack.NET_GENERATE_QR_CODE;//请求网络接口的类型为生成二维码
        boolean isNeedFocus = !StringDataCallBack.getStringDataCallBack(type,id+"").isInCache();//是否强制从网络中获取数据
        Object[] params = StringDataCallBack.generateQRCodeNetParams(id, isNeedFocus);
        StringDataCallBack.addOnDataRefreshListeners(type, new BaseListener.OnDataRefreshListener() {
            @Override
            public void OnDataRefresh(Object stringData) {
                imageView.setImageUrl((String) stringData, new ImageLoader(VolleyUtils.getRequestQueue(), ImageCacheImpl.getInstance(mActivity)));
                StringDataCallBack.removeOnDataRefreshListeners(type, this);
            }
        });
        EventBus.getDefault().post(params, Const.EVENT_BUS_TAG_GET_STRING_DATA);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);
        if(position==0){
            mFriendsCheckBox.setSelected(true);
            mFriendsCheckBox.setTextColor(ContextCompat.getColor(mActivity,R.color.blue_bg_color));
            mNearCheckBox.setSelected(false);
            mNearCheckBox.setTextColor(ContextCompat.getColor(mActivity,android.R.color.white));
        }else if(position==1){
            mNearCheckBox.setSelected(true);
            mNearCheckBox.setTextColor(ContextCompat.getColor(mActivity,R.color.blue_bg_color));
            mFriendsCheckBox.setSelected(false);
            mFriendsCheckBox.setTextColor(ContextCompat.getColor(mActivity, android.R.color.white));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
