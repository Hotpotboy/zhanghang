package com.android.volley.toolbox;

import android.util.SparseArray;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hangzhang209526 on 2016/3/7.
 */
public abstract class BaseListener<T> implements Response.ErrorListener, Response.Listener<String> {
    public static final String TAG = BaseListener.class.getSimpleName();
    protected static SparseArray<BaseListener> sInstance = new SparseArray<>();
    private static SparseArray<ArrayList<OnDataRefreshListener>> mOnDataRefreshListeners = new SparseArray<>();
    /**需要删除的监听器*/
    private static ArrayList<OnDataRefreshListener> mNeedRemoveOnDataRefreshListeners = new ArrayList<>();
    /**
     * 相关数据数据列表
     * key为子类型
     * value为主类型+子类型缓存的数据
     */
    protected HashMap<String, T> mDatas = new HashMap<>();
    /**
     * 主类型，与具体的某一个接口对应
     */
    protected int mType;
    /**
     * 子类型，与某一接口具体的参数对应
     */
    protected String mKey;

    /**
     * 为某一主类型对应的数据刷新监听器集合添加一个数据刷新监听器
     *
     * @param type                  网络接口对应的主类型
     * @param onDataRefreshListener 添加的数据刷新监听器
     */
    public static void addOnDataRefreshListeners(int type, OnDataRefreshListener onDataRefreshListener) {
        ArrayList<OnDataRefreshListener> mOnStringDataRefreshListeners;
        int index = mOnDataRefreshListeners.indexOfKey(type);
        if (index < 0) {
            mOnStringDataRefreshListeners = new ArrayList<>();
        } else {
            mOnStringDataRefreshListeners = mOnDataRefreshListeners.get(type);
        }
        mOnStringDataRefreshListeners.add(onDataRefreshListener);
        mOnDataRefreshListeners.put(type, mOnStringDataRefreshListeners);
    }

    public static void removeOnDataRefreshListeners(int type, OnDataRefreshListener listener) {
        if (mOnDataRefreshListeners.get(type) != null&&mOnDataRefreshListeners.get(type).contains(listener)) {
             mNeedRemoveOnDataRefreshListeners.add(listener);
        }
    }

    /**
     * 判断是否存在缓存,此方法适合网络结果为单一数据的情况
     * @return
     */
    public boolean isInCache() {
        Object cache = mDatas.get(mKey);
        return cache != null;
    }

    /**
     * 判断是否存在缓存,此方法适合网络结果为列表的情况，子类需重写
     *
     * @param object  子类型
     * @return
     */
    public boolean isInCache(Object object) {
        return false;
    }

    protected BaseListener(int type) {
        mType = type;
    }


    protected void setKey(String _key) {
        mKey = _key;
    }

    /**
     * 刷新数据
     */
    public void invokeDatasRefresh() {
        removeAllNeedRemoveListener();
        ArrayList<OnDataRefreshListener> dataRefreshListeners = mOnDataRefreshListeners.get(mType);
        if (dataRefreshListeners != null && dataRefreshListeners.size() > 0) {
            for (OnDataRefreshListener listener : dataRefreshListeners) {
                listener.OnDataRefresh(mDatas.get(mKey));
            }
        }
    }

    /**
     * 删掉所有需要被删掉的监听器
     */
    private static void removeAllNeedRemoveListener(){
        if(mNeedRemoveOnDataRefreshListeners.size()>0){
            for(OnDataRefreshListener item:mNeedRemoveOnDataRefreshListeners){
                for(int i=0;i<mOnDataRefreshListeners.size();i++){
                    ArrayList list = mOnDataRefreshListeners.valueAt(i);
                    if(list.contains(item)){
                        list.remove(item);
                    }
                }
            }
            mNeedRemoveOnDataRefreshListeners.clear();
        }
    }

    /**
     * 数据刷新监听器接口
     */
    public interface OnDataRefreshListener {
        public void OnDataRefresh(Object data);
    }
}
