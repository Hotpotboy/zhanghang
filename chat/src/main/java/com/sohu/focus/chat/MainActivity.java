package com.sohu.focus.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
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
import com.sohu.focus.chat.adapter.FriendListAdapter;
import com.sohu.focus.chat.data.StringResponseData;
import com.sohu.focus.chat.data.user.UserData;
import com.sohu.focus.chat.data.user.UserListData;
import com.sohu.focus.chat.data.LongResponseData;
import com.sohu.focus.chat.data.user.UserResponseData;
import com.souhu.hangzhang209526.zhanghang.utils.LocationUtil;
import com.souhu.hangzhang209526.zhanghang.utils.PopupWindowUtils;
import com.souhu.hangzhang209526.zhanghang.utils.VolleyUtils;
import com.souhu.hangzhang209526.zhanghang.utils.cache.ImageCacheImpl;
import com.souhu.hangzhang209526.zhanghang.utils.camera.CameraUtils;

import java.util.ArrayList;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener,AMapLocationListener,View.OnClickListener {
    private static final String TAG = "MainActivity.this";
    /**添加按钮*/
    private TextView addFriend;
    private ListView mFriendList;
    /**好友列表适配器*/
    private FriendListAdapter mFriendListAdapter;
    /**用户与好友的数据列表*/
    private ArrayList<UserData> mDatas = new ArrayList<UserData>();
    /**弹出窗口的辅助类*/
    private PopupWindowUtils mAddFriendWindow,mShowQRCodeWindow;
    private int mStartHeight;
    private int mStartWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFriendList = (ListView)findViewById(R.id.friend_list);
        addFriend = (TextView)findViewById(R.id.friend_add);

        addFriend.setOnClickListener(this);
        mFriendList.setOnItemClickListener(this);

        getUserInfo();
        LocationUtil.setAMapLocationListener(this);
        try {
            LocationUtil.requestLocation(-1);//请求定位
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        LocationUtil.stopLocation();
    }

    /**
     * 获取自己的信息
     */
    private void getUserInfo(){
        String url = Const.URL_GET_USER_INFO+"?userId="+Const.currentId;
        StringRequest userInfoRequest = new StringRequest(url, new BaseListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(String response) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    UserResponseData userData = objectMapper.readValue(response.toString(),UserResponseData.class);
                    if (userData != null && userData.getErrorCode()==0) {
                        UserData friendDatas = userData.getData();
                        mDatas.add(friendDatas);
                        if(mFriendListAdapter==null){
                            mFriendListAdapter = new FriendListAdapter(MainActivity.this,mDatas);
                            mFriendList.setAdapter(mFriendListAdapter);
                        }else{
                            mFriendListAdapter.setDatas(mDatas);
                        }
                        getFriendList();//获取好友列表
                    }else{

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        VolleyUtils.requestNet(userInfoRequest);
    }

    /**
     * 获取好友列表
     */
    private void getFriendList(){
        String url = Const.URL_GET_FRIENDS+"?userId="+Const.currentId;
        StringRequest friendListRequest = new StringRequest(url, new BaseListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(String response) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    UserListData friendList = objectMapper.readValue(response.toString(),UserListData.class);
                    if (friendList != null && friendList.getErrorCode()==0) {
                        ArrayList<UserData> friendDatas = friendList.getData();
                        mDatas.addAll(friendDatas);
                        mFriendListAdapter.notifyDataSetChanged();
                    }else{

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        VolleyUtils.requestNet(friendListRequest);
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserData userInfo = (UserData) mFriendListAdapter.getItem(position);
        final long userId = userInfo.getId();
        if(userId!=Const.currentId) {//如果不是自己，则进入聊天界面
            String url = Const.URL_GET_SESSION_ID + "?userId=" + Const.currentId + "&friendId=" + userId;
            StringRequest friendListRequest = new StringRequest(url, new BaseListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }

                @Override
                public void onResponse(String response) {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        LongResponseData sessionIdData = objectMapper.readValue(response, LongResponseData.class);
                        if (sessionIdData != null && sessionIdData.getErrorCode() == 0) {
                            long sessionId = sessionIdData.getData();
                            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                            intent.putExtra(Const.INTENT_KEY_OTHER_ID, userId);
                            intent.putExtra(Const.INTENT_KEY_SESSION_ID, sessionId);
                            startActivity(intent);
                        } else {

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            VolleyUtils.requestNet(friendListRequest);
        }else{//如果是自己，则进入详情页面
            Intent intent = new Intent(MainActivity.this,UserInfoActivity.class);
            intent.putExtra(Const.INTENT_KEY_USER_INFO,userInfo);
            MainActivity.this.startActivity(intent);
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation.getErrorCode() == 0) {
            //获取位置信息
            Double geoLat = aMapLocation.getLatitude();
            Double geoLng = aMapLocation.getLongitude();
            String url = Const.URL_SEND_LOCATION+"?userId="+Const.currentId+"&lat="+(geoLat*10000)+"&lng="+(geoLng*10000);
            StringRequest sendLocationRequest = new StringRequest(url,new BaseListener(){
                @Override
                public void onErrorResponse(VolleyError error) {

                }

                @Override
                public void onResponse(String response) {

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
        switch (v.getId()){
            case R.id.friend_add:
                if(mAddFriendWindow ==null) {
                    int width = (int) getResources().getDimension(R.dimen.one_hundred_fifty_dp);
                    int height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    mAddFriendWindow = new PopupWindowUtils(this, R.layout.window_add_friend,width,height);
                }
                mAddFriendWindow.setOnClickForSpecialedView(R.id.window_add_friend, this);
                mAddFriendWindow.setOnClickForSpecialedView(R.id.window_add_ScanQR, this);
                mAddFriendWindow.setOnClickForSpecialedView(R.id.window_add_myQR, this);
                mAddFriendWindow.show(v);
                break;
            case R.id.window_add_friend://添加盆友
                break;
            case R.id.window_add_myQR://我的二维码
                if(mAddFriendWindow!=null) mAddFriendWindow.getPopupWindow().dismiss();
                genrateQRCode(Const.currentId);
                break;
            case R.id.window_add_ScanQR://扫描二维码
                CameraUtils.scannerQRCode(this);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            switch (requestCode){
                case CameraUtils.SCANNER_QR_CODE_REQUEST_CODE:
                    String result = data.getStringExtra(Intents.Scan.RESULT);
                    Toast.makeText(this,result,Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    /**生成二维码*/
    private void genrateQRCode(long id){
        if(mShowQRCodeWindow==null){
            ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.window_qr_code,null);
            contentView.measure(0,0);
            int width = contentView.getMeasuredWidth();
            int height = contentView.getMeasuredHeight();
            mShowQRCodeWindow = new PopupWindowUtils(this,R.layout.window_qr_code,width,height);

            //初始化相关值
            int parentWidth = mFriendList.getRootView().getMeasuredWidth();
            int parentHeight = mFriendList.getRootView().getMeasuredHeight();
            mStartWidth = (parentWidth-width)/2;//开始X位置
            mStartHeight = (parentHeight-height)/2;//开始Y位置

            NetworkImageView networkImageView = (NetworkImageView) mShowQRCodeWindow.getViewById(R.id.window_qr_code_image);
            networkImageView.setDefaultImageResId(R.drawable.default_img);
            networkImageView.setImageDrawable(getResources().getDrawable(R.drawable.default_img));
        }
        mShowQRCodeWindow.showAtLocation(mFriendList, mStartWidth, mStartHeight);
        //请求网络
        final String url = Const.URL_GET_QR_CODE+"?userId=" + id;
        StringRequest genrateQRCodeRequest = new StringRequest(url, new BaseListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,error.toString());
            }

            @Override
            public void onResponse(String response) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    StringResponseData qrCodeUrlData = objectMapper.readValue(response, StringResponseData.class);
                    if (qrCodeUrlData != null && qrCodeUrlData.getErrorCode() == 0) {
                        String imageUrl = qrCodeUrlData.getData();
                        NetworkImageView networkImageView = (NetworkImageView) mShowQRCodeWindow.getViewById(R.id.window_qr_code_image);
                        networkImageView.setImageUrl(imageUrl,new ImageLoader(VolleyUtils.getRequestQueue(), ImageCacheImpl.getInstance(MainActivity.this)));
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        VolleyUtils.requestNet(genrateQRCodeRequest);
    }
}
