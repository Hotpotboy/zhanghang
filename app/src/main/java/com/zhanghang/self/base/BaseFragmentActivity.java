package com.zhanghang.self.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hangzhang209526 on 2016/1/4.
 */
public class BaseFragmentActivity extends FragmentActivity {
    /**
     * Fragment布局ID
     */
    private int mResourceId;
    /**
     * 当前显示的Fragment的索引
     */
    private int mShowIndex = -1;
    /**
     * 所有添加的frgment
     */
    private ArrayList<BaseFragment> mFragmentInstances = new ArrayList<BaseFragment>();
    protected FragmentManager mFragmentManager;

    /**
     * 显示Fragment监听器
     */
    private IBaseFragmentListener mIBaseFragmentListener;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        mFragmentManager = getSupportFragmentManager();
    }

    @Override
    protected void onStart(){
        super.onStart();
        List<Fragment> list = mFragmentManager.getFragments();
        if(list!=null&&list.size()>0){
            for(Fragment item:list){
                if(item instanceof BaseFragment){
                    mFragmentInstances.add((BaseFragment)item);
                }
            }
        }
    }

    /**
     * 清空此Activity之中所有的Fragments
     */
    private void resetFragments() {
        //删除所有的fragment
        int size = mFragmentInstances.size();
        if (size > 0) {
            FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
            for (int i = 0; i < size; i++)
                mFragmentTransaction.remove(mFragmentInstances.get(i));
            mFragmentTransaction.commit();
            mFragmentInstances.clear();//清空
        }
        //重置显示Fragment索引
        mShowIndex = -1;
    }

    /**
     * 初始化Fragments
     *
     * @param fragments
     * @param resouceId Fragment对应的布局ID
     */
    protected void initFragments(BaseFragment[] fragments, int resouceId) {
        initFragments(fragments, resouceId, -1);
    }

    /**
     * 初始化Fragments
     *
     * @param fragments
     * @param resouceId Fragment对应的布局ID
     */
    protected void initFragments(BaseFragment[] fragments, int resouceId, int showIndex) {
        resetFragments();
        mResourceId = resouceId;
        if (fragments == null) {
            return;
        }
        //添加frgment
        int size = fragments.length;
        if (size >= 1) {
            FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
            for (int i = 0; i < size; i++) {
                BaseFragment instance = fragments[i];
                mFragmentInstances.add(instance);//添加到相应的List集合之中
                mFragmentTransaction.add(mResourceId, instance);//添加到Activity之中
                mFragmentTransaction.hide(instance);//初始状态为隐藏
            }
            mFragmentTransaction.commit();//提交事务
            showFragment(showIndex < 0 || showIndex >= size ? 0 : showIndex);//如果没有指定显示的Fragment，则显示第一个Fragment
        }
    }

    /**
     * 添加Fragment
     *
     * @param fragment
     * @param isAddStack 此操作是否添加到回退栈之中
     */
    public void addFragment(BaseFragment fragment, boolean isAddStack) {
        //添加Fragment集合之中的对象
        mFragmentInstances.add(fragment);
        hideFragment(mShowIndex, null);
        //添加FragmentManager之中的对象
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.add(mResourceId, fragment);
        if (isAddStack) mFragmentTransaction.addToBackStack(null);
        mFragmentTransaction.commit();
        changeShowIndex(mFragmentInstances.size() - 1);
    }

    /**
     * 替换Fragment
     *
     * @param index      替换哪个位置的Fragment
     * @param fragment
     * @param isAddStack 此操作是否添加到回退栈之中
     */
    public void replaceFragment(int index, BaseFragment fragment, boolean isAddStack) {
        //替换Fragment集合之中的对象
        mFragmentInstances.remove(index);
        mFragmentInstances.add(index, fragment);
        //替换FragmentManager之中的对象
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(mResourceId, fragment);
        if (isAddStack) mFragmentTransaction.addToBackStack(null);
        mFragmentTransaction.commit();
        changeShowIndex(index);
    }

    /**
     * 删除指定的Fragment,此方法不会指定删除之后，显示的Fragment
     *
     * @param index 要删除的Fragment索引
     */
    public void removeFragment(int index) {
        //删除Fragment集合之中的对象
        BaseFragment fragment = mFragmentInstances.remove(index);
        //删除FragmentManager之中的对象
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.remove(fragment);
        mFragmentTransaction.commit();
    }

    /**
     * 指定显示的索引
     *
     * @param index
     */
    public void showFragment(int index) {
        if (index == mShowIndex) return;
        int size = mFragmentInstances.size();
        if (index >= 0 && index < size) {
            FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
            if (mShowIndex >= 0 && mShowIndex < size) {
                hideFragment(mShowIndex, mFragmentTransaction);//首先隐藏当前显示的索引对应的Fragment
            }
            BaseFragment fragment = mFragmentInstances.get(index);
            mFragmentTransaction.show(fragment);
            mFragmentTransaction.commit();
            changeShowIndex(index);
        }
    }

    private void changeShowIndex(int index) {
        mShowIndex = index;//更新
        if (mIBaseFragmentListener != null)
            mIBaseFragmentListener.onShowFragment(mFragmentInstances.get(index));
    }

    /**
     * 隐藏索引对应的Fragment
     *
     * @param index
     * @param transaction 提交事务
     */
    public void hideFragment(int index, FragmentTransaction transaction) {
        int size = mFragmentInstances.size();
        if (index >= 0 && index < size) {
            if(transaction!=null){
                transaction.hide(mFragmentInstances.get(index));
            }else{
                FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
                mFragmentTransaction.hide(mFragmentInstances.get(index));
                mFragmentTransaction.commit();
            }
        }
    }

    public void setmIBaseFragmentListener(IBaseFragmentListener listener) {
        mIBaseFragmentListener = listener;
    }

    public interface IBaseFragmentListener {
        public void onShowFragment(BaseFragment fragment);
    }

    @Override
    public void finish() {
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            mFragmentManager.popBackStack();
        } else {
            super.finish();
        }
    }
}
