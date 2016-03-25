package com.zhanghang.self.utils;

/**
 * Created by hangzhang209526 on 2016/3/14.
 */
public interface CallBack<T,V> {
    public V run(T t);
}
