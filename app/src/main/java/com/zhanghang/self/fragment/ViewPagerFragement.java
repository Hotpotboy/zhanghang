package com.zhanghang.self.fragment;

import android.support.v4.view.ViewPager;

import com.zhanghang.self.adpter.BaseFragmentPagerAdapter;
import com.zhanghang.self.base.BaseFragment;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/3/16.
 * <p>此类是用来实现ViewPager+Fragment效果的基本类</p>
 * <p>
 * 此类的子类必须重写{@link ViewPager.OnPageChangeListener#onPageScrolled(int, float, int)}、
 * {@link ViewPager.OnPageChangeListener#onPageScrollStateChanged(int)}、
 * {@link ViewPager.OnPageChangeListener#onPageScrolled(int, float, int)}这三个方法。
 * </p>
 */
public abstract class ViewPagerFragement extends BaseFragment implements ViewPager.OnPageChangeListener {
    /**Bundle对象中表示要展示的Fragment的ArrayList的Key*/
    public static final String INTENT_FRAGMENT_LIST_KEY = "intent_fragment_list_key";
    /**Fragment列表*/
    private ArrayList<BaseFragment> mFragmentList;
    /**Fragment适配器*/
    private BaseFragmentPagerAdapter mBaseFragmentPagerAdapter;
    /**ViewPager*/
    private ViewPager mViewPager;
    private int mCurrentItem = 0;

    /**指定用以切换的Fragment列表，非抽象子类必须继承*/
    protected abstract ArrayList<BaseFragment> specifyFragmentList();
    /**指定ViewPager,非抽象之类必须继承*/
    protected abstract ViewPager specifyViewPager();

    @Override
    protected void initView(){
        mViewPager = specifyViewPager();
    }

    @Override
    protected void initData() {
        mFragmentList = specifyFragmentList();
        if(mFragmentList!=null&&mFragmentList.size()>0){
            mBaseFragmentPagerAdapter = new BaseFragmentPagerAdapter(getFragmentManager(),mFragmentList);
            setBaseFragmentPagerAdapter(mBaseFragmentPagerAdapter);//设置适配器
        }
    }

    /**设置适配器*/
    public void setBaseFragmentPagerAdapter(BaseFragmentPagerAdapter adapter) {
        mBaseFragmentPagerAdapter = adapter;
        mViewPager.setAdapter(mBaseFragmentPagerAdapter);
        setCurrentFragment(0);
        mViewPager.addOnPageChangeListener(this);
    }

    /**
     * 将指定的Fragment作为当前显示的页面
     * @param index
     */
    protected void setCurrentFragment(int index){
        if(index>=0&&index<mBaseFragmentPagerAdapter.getCount()&&index!=mCurrentItem){
            mCurrentItem = index;
            mViewPager.setCurrentItem(index);
        }
    }

    protected BaseFragment getCurrentFragment(){
        return getFragmentInList(mCurrentItem);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mViewPager.removeOnPageChangeListener(this);
    }

    protected BaseFragment getFragmentInList(int index){
        if(index>=0&&index<mBaseFragmentPagerAdapter.getCount()){
            return (BaseFragment) mBaseFragmentPagerAdapter.getItem(index);
        }else{
            return null;
        }
    }

    @Override
    public void onPageSelected(int position) {
        mCurrentItem = position;
    }
}
