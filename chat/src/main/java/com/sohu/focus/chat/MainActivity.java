package com.sohu.focus.chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.sohu.focus.chat.data.user.UserData;
import com.sohu.focus.chat.fragment.MainFragment;
import com.sohu.focus.chat.fragment.UserInfoFragment;
import com.sohu.focus.eventbus.EventBus;
import com.sohu.focus.eventbus.subscribe.Subscriber;
import com.sohu.focus.eventbus.subscribe.ThreadMode;
import com.souhu.hangzhang209526.zhanghang.base.BaseFragment;
import com.souhu.hangzhang209526.zhanghang.base.BaseFragmentActivity;
import com.souhu.hangzhang209526.zhanghang.utils.VolleyUtils;
import com.souhu.hangzhang209526.zhanghang.utils.cache.ImageCacheImpl;

import java.net.URLEncoder;


public class MainActivity extends BaseFragmentActivity implements DrawerLayout.DrawerListener {
    private static final String TAG = "MainActivity.this";
    /**抽屉布局视图*/
    private DrawerLayout mDrawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            intent = UserInfoFragment.getUserInfoFragmentIntent(Const.currentId, true);
            setIntent(intent);
        } else {
            intent.putExtra(Const.INTENT_KEY_USER_ID, Const.currentId);
            intent.putExtra(Const.INTENT_KEY_IS_FRIEND, true);
        }
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_layout);
        mDrawerLayout.setDrawerListener(this);
        EventBus.getDefault().register(this);
    }

    @Subscriber(tag = Const.EVENT_BUS_TAG_OPEN_DRAWER,mode = ThreadMode.MAIN)
    private boolean openLeftDrawer(int gravity){
        mDrawerLayout.openDrawer(gravity);
        return false;
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        View content = mDrawerLayout.getChildAt(0);//获取内容视图
        int offsetWidth = (int) (drawerView.getMeasuredWidth()*slideOffset);
        content.scrollTo(-offsetWidth,0);
        content.invalidate();
    }

    @Override
    public void onDrawerOpened(View drawerView) {

    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
