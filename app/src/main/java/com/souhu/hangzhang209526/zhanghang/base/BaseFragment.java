package com.souhu.hangzhang209526.zhanghang.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by hangzhang209526 on 2016/1/4.
 */
public class BaseFragment extends Fragment {
    /**Fragment的布局文件*/
    protected int mRootLayout;
    /**Fragment的根视图*/
    protected View mRootView;

    protected LayoutInflater mInflater;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle onSavedInstance){
        mInflater = inflater;
        mRootView = inflater.inflate(mRootLayout,null);
        initView();
        initData();
        return mRootView;
    }

    protected void initView(){

    }

    protected void initData(){

    }
}
