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
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BaseListener;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.Intents;
import com.sohu.focus.chat.Const;
import com.sohu.focus.chat.R;
import com.sohu.focus.chat.UserInfoActivity;
import com.sohu.focus.chat.data.StringResponseData;
import com.sohu.focus.chat.data.user.UserData;
import com.sohu.focus.eventbus.EventBus;
import com.sohu.focus.eventbus.subscribe.Subscriber;
import com.sohu.focus.eventbus.subscribe.ThreadMode;
import com.souhu.hangzhang209526.zhanghang.base.BaseFragment;
import com.souhu.hangzhang209526.zhanghang.fragment.ViewPagerFragement;
import com.souhu.hangzhang209526.zhanghang.utils.CallBack;
import com.souhu.hangzhang209526.zhanghang.utils.LocationUtil;
import com.souhu.hangzhang209526.zhanghang.utils.PopupWindowUtils;
import com.souhu.hangzhang209526.zhanghang.utils.VolleyUtils;
import com.souhu.hangzhang209526.zhanghang.utils.cache.ImageCacheImpl;
import com.souhu.hangzhang209526.zhanghang.utils.camera.CameraUtils;
import com.souhu.hangzhang209526.zhanghang.widget.CycleImageView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/3/16.
 */
public class MainFragment extends ViewPagerFragement implements  AMapLocationListener, View.OnClickListener {
    private static final String TAG = "MainFragment.class";
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
        baseFragments.add(new UserFragment());
        baseFragments.add(new UserFragment());
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
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initData(){
        Const.getUserInfo(Const.currentId, new CallBack<UserData, Void>() {
            @Override
            public Void run(UserData userData) {
                mHeadImage.setImageUrl(userData.getHeadPhoto(),new ImageLoader(VolleyUtils.getRequestQueue(),ImageCacheImpl.getInstance(mActivity)));
                return null;
            }
        }, true);
        super.initData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocationUtil.stopLocation();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation.getErrorCode() == 0) {
            //获取位置信息
            Double geoLat = aMapLocation.getLatitude();
            Double geoLng = aMapLocation.getLongitude();
            String url = Const.URL_SEND_LOCATION + "?userId=" + Const.currentId + "&lat=" + ((int)(geoLat * 10000)) + "&lng=" + ((int)(geoLng * 10000));
            StringRequest sendLocationRequest = new StringRequest(url, new BaseListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, error.toString());
                }

                @Override
                public void onResponse(String response) {
                    Log.e(TAG,response);
                }
            });
            VolleyUtils.requestNet(sendLocationRequest);
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
        Intent intent = UserInfoFragment.getUserInfoFragmentIntent(id,((UserFragment)getFragmentInList(0)).isInList(id));
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
                    addFriendFromScanQRCode(result);
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
        getQRCodeFromNet(id,(NetworkImageView)showQRCodeWindow.getViewById(R.id.window_qr_code_image));
    }

    /***
     * EventBus事件（通过网络请求指定用户的二维码）的执行方法
     * @param params   Object数组，第一个元素表示指定用户的ID，第二个元素表示用来展示二维码图片的NetworkImageView
     */
    @Subscriber(tag = Const.EVENT_BUS_TAG_GENERATE_QR_CODE,mode = ThreadMode.MAIN)
    private boolean getQRCodeFromNet(Object[] params){
        getQRCodeFromNet((long) params[0], (NetworkImageView) params[1]);
        return false;
    }

    private void getQRCodeFromNet(long id,final NetworkImageView imageView){
        final String url = Const.URL_GET_QR_CODE + "?userId=" + id;
        StringRequest genrateQRCodeRequest = new StringRequest(url, new BaseListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }

            @Override
            public void onResponse(String response) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    StringResponseData qrCodeUrlData = objectMapper.readValue(response, StringResponseData.class);
                    if (qrCodeUrlData != null && qrCodeUrlData.getErrorCode() == 0) {
                        String imageUrl = qrCodeUrlData.getData();
                        imageView.setImageUrl(imageUrl, new ImageLoader(VolleyUtils.getRequestQueue(), ImageCacheImpl.getInstance(mActivity)));
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        VolleyUtils.requestNet(genrateQRCodeRequest);
    }

    /**
     * 添加该用户为好友
     *
     * @param id 添加的用户的ID
     * @return
     */
    @Subscriber(tag = Const.EVENT_BUS_TAG_ADD_FRIEND, mode = ThreadMode.MAIN)
    private boolean addFriendFromScanQRCode(String id) {
        String url = Const.URL_GET_ADD_FRIEND + "?userId="+Const.currentId+"&friendId="+id;
        StringRequest stringRequest = new StringRequest(url, new BaseListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,error.toString());
            }

            @Override
            public void onResponse(String response) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    StringResponseData responseData = objectMapper.readValue(response,StringResponseData.class);
                    if(responseData!=null&&responseData.getErrorCode()==0){
                        //重新刷新界面
                        EventBus.getDefault().post(Const.currentId,Const.EVENT_BUS_TAG_GET_FRIENDS);
                    }else{

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        VolleyUtils.requestNet(stringRequest);
        Toast.makeText(mActivity, id, Toast.LENGTH_LONG).show();
        return false;
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
