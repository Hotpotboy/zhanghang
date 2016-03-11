package com.sohu.focus.chat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.sohu.focus.chat.data.user.UserData;
import com.souhu.hangzhang209526.zhanghang.utils.SystemUtils;
import com.souhu.hangzhang209526.zhanghang.utils.VolleyUtils;
import com.souhu.hangzhang209526.zhanghang.utils.cache.ImageCacheImpl;
import com.souhu.hangzhang209526.zhanghang.widget.CycleImageView;

import java.net.URLEncoder;
import java.util.Date;

/**
 * Created by hangzhang209526 on 2016/3/9.
 */
public class UserInfoActivity extends Activity {
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);
        UserData userData = (UserData) getIntent().getSerializableExtra(Const.INTENT_KEY_USER_INFO);
        initView(userData);
        getHeadImage(userData);
    }

    private void initView(UserData userData){
        mHeadImage = (CycleImageView) findViewById(R.id.my_info_headImage);

        mNickName = (TextView)findViewById(R.id.my_info_niceName);
        mNickName.setText(userData.getNickName());

        mUserName = (TextView)findViewById(R.id.my_info_userName);
        mUserName.setText(userData.getUserName());

        mCreateTime = (TextView)findViewById(R.id.my_info_createTime);
        String createTime = SystemUtils.getTimestampStringListView(SystemUtils.TIME_FORMAT_yyyy_MM_dd_HH_mm_ss,new Date(userData.getCreateDate()));
        mCreateTime.setText(createTime);

        mLastLoginTime = (TextView)findViewById(R.id.my_info_loginTime);
        String loginTime = SystemUtils.getTimestampStringListView(SystemUtils.TIME_FORMAT_yyyy_MM_dd_HH_mm_ss,new Date(userData.getLastLoginDate()));
        mLastLoginTime.setText(loginTime);

        mLastOffTime = (TextView)findViewById(R.id.my_info_offTime);
        String offTime = SystemUtils.getTimestampStringListView(SystemUtils.TIME_FORMAT_yyyy_MM_dd_HH_mm_ss,new Date(userData.getLastOfflineTime()));
        mLastOffTime.setText(offTime);
    }

    /**获取头像*/
    public void getHeadImage(UserData userData){
        final String url = userData.getHeadPhoto();
        Bitmap bitmap = ImageCacheImpl.getInstance(this).getBitmap(URLEncoder.encode(userData.getHeadPhoto()));
        if(bitmap!=null){
            mHeadImage.setImageBitmap(bitmap);
            return;
        }
        int size = (int) getResources().getDimension(R.dimen.forty_dp);
        ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                String encodeUrl = URLEncoder.encode(url);
                ImageCacheImpl.getInstance(UserInfoActivity.this).putFile(encodeUrl, response);
                mHeadImage.setImageBitmap(response);
            }
        }, size, size, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        VolleyUtils.requestNet(imageRequest);
    }
}
