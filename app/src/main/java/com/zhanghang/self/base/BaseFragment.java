package com.zhanghang.self.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by hangzhang209526 on 2016/1/4.
 */
public abstract class BaseFragment extends Fragment {
    /**Fragment的布局文件*/
    protected int mRootLayout;
    /**Fragment的根视图*/
    protected View mRootView;

    protected LayoutInflater mInflater;
    /**Fragment对应的Activity*/
    protected Activity mActivity;

    /**指定布局文件的资源ID，非抽象子类必须重写*/
    protected abstract int specifyRootLayoutId();

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle onSavedInstance){
        mActivity = getActivity();
        mInflater = inflater;
        mRootLayout = specifyRootLayoutId();
        mRootView = inflater.inflate(mRootLayout,null);
        Bundle arguments = getArguments();
        if(arguments==null){
            arguments = mActivity.getIntent().getExtras();
        }
        initDataFromArguments(arguments);
        initView();
        initData();
        return mRootView;
    }

    /**
     * 通过Bundle初始化相关数据
     * @param arguments    首先来自与方法{@link #getArguments()}，如果为空，则由其宿主Activity（{@link #mActivity}）
     *                     的方法{@link Activity#getIntent()}得来。
     */
    protected void initDataFromArguments(Bundle arguments){

    }

    /**初始化视图*/
    protected void initView(){

    }

    /**初始化数据*/
    protected void initData(){

    }

    protected View findViewById(int viewId){
        return mRootView.findViewById(viewId);
    }
}
