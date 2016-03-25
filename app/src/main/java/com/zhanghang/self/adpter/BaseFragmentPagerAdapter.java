package com.zhanghang.self.adpter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.zhanghang.self.base.BaseFragment;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/3/16.
 */
public class BaseFragmentPagerAdapter extends FragmentPagerAdapter {
    /**Fragment列表*/
    protected ArrayList<BaseFragment> mFragments;

    public BaseFragmentPagerAdapter(FragmentManager fm,ArrayList<BaseFragment> baseFragments) {
        super(fm);
        mFragments = baseFragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}