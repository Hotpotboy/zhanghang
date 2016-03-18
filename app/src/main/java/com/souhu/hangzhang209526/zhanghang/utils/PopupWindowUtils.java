package com.souhu.hangzhang209526.zhanghang.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import java.util.HashMap;

/**
 * Created by hangzhang209526 on 2016/3/11.
 */
public class PopupWindowUtils {
    /**
     * 缓存的实例
     */
    private static HashMap<String, PopupWindowUtils> mInstances = new HashMap<String, PopupWindowUtils>();
    /**
     * window对应的activity
     */
    private Activity mActivity;
    /**
     * window对应的粘贴视图
     */
    private View mAttachedView;
    /**
     * 相对于粘贴视图的X位置
     */
    private int mStartLocationX;
    /**
     * 相对于粘贴视图的Y位置
     */
    private int mStartLocationY;
    /**
     * 内容视图
     */
    private ViewGroup mContentView;
    /**
     * 弹出窗口
     */
    private PopupWindow mPopupWindow;

    private static String getInstanceKey(int contentViewId, Activity activity, View attachedView) {
        return contentViewId + "#" + activity.toString() + "#" + attachedView;
    }

    public static boolean isNeedInti(int contentViewId, Activity activity, View attachedView) {
        String key = getInstanceKey(contentViewId, activity, attachedView);
        boolean result = true;
        synchronized (PopupWindowUtils.class) {
            result = !mInstances.containsKey(key);
        }
        return result;
    }

    /**
     * 获取指定资源ID的弹出窗口
     *
     * @param contentViewLayoutId 弹出窗口内容d的布局ID
     * @param activity            弹出窗口对应的activity
     * @param attachedView        弹出窗口对应的粘贴视图
     * @return
     */
    public static PopupWindowUtils getInstance(int contentViewLayoutId, Activity activity, View attachedView) {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(activity).inflate(contentViewLayoutId, null);
        return getInstance(contentView, activity, attachedView, 0, 0);
    }

    /**
     * 获取指定资源ID的弹出窗口
     *
     * @param contentViewLayoutId 弹出窗口内容的布局ID
     * @param activity            弹出窗口对应的activity
     * @param attachedView        弹出窗口对应的粘贴视图
     * @param width               指定弹出窗口的宽度
     * @param height              指定弹出窗口的高度
     * @return
     */
    public static PopupWindowUtils getInstance(int contentViewLayoutId, Activity activity, View attachedView, int width, int height) {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(activity).inflate(contentViewLayoutId, null);
        return getInstance(contentView, activity, attachedView, width, height);
    }

    /**
     * 获取指定资源ID的弹出窗口
     *
     * @param contentView  弹出窗口的内容对应的视图
     * @param activity     弹出窗口对应的activity
     * @param attachedView 弹出窗口对应的粘贴视图
     * @param width        弹出窗口的宽度
     * @param height       弹出窗口的高度
     * @return
     */
    public static PopupWindowUtils getInstance(ViewGroup contentView, Activity activity, View attachedView, int width, int height) {
        String key = getInstanceKey(contentView.getId(), activity, attachedView);
        PopupWindowUtils result = null;
        synchronized (PopupWindowUtils.class) {
            result = mInstances.get(key);
            if (result == null) {
                result = new PopupWindowUtils(activity, attachedView, contentView, width, height);
                mInstances.put(key, result);
                if (width == 0 || height == 0) {
                    contentView.measure(0, 0);
                    if (width == 0) width = contentView.getMeasuredWidth();
                    if (height == 0) height = contentView.getMeasuredHeight();
                }
            } else {
                if (width == 0) width = result.mPopupWindow.getWidth();
                if (height == 0) height = result.mPopupWindow.getWidth();
            }
        }
        //宽度和高度是否发生了改变
        int oldWidth = result.mPopupWindow.getWidth();
        int oldHeight = result.mPopupWindow.getHeight();
        if (oldWidth != width) {
            result.mPopupWindow.setWidth(width);
        }
        if (oldHeight != height) {
            result.mPopupWindow.setHeight(height);
        }
        return result;
    }

    private PopupWindowUtils(Activity activity, View attachedView, ViewGroup contentView, int width, int height) {
        mActivity = activity;
        mAttachedView = attachedView;
        mPopupWindow = new PopupWindow(contentView, width, height, false);
        mContentView = contentView;
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setOutsideTouchable(true);
    }

    /**
     * 在指定的视图下方显示浮动窗口
     */
    public void show() {
        mPopupWindow.showAsDropDown(mAttachedView);
    }


    public void showAtLocation() {
        mPopupWindow.showAtLocation(mAttachedView, Gravity.NO_GRAVITY, mStartLocationX, mStartLocationY);
    }

    public PopupWindow getPopupWindow() {
        return mPopupWindow;
    }

    /**
     * 为指定的视图设置点击监听事件
     *
     * @param viewId 指定视图的ID
     */
    public void setOnClickForSpecialedView(int viewId, View.OnClickListener onClickListener) {
        View view = mContentView.findViewById(viewId);
        setOnClickForSpecialedView(view, onClickListener);
    }

    /**
     * 为指定的视图设置点击监听事件
     *
     * @param view 指定视图
     */
    public void setOnClickForSpecialedView(View view, View.OnClickListener onClickListener) {
        view.setOnClickListener(onClickListener);
    }

    /**
     * 获取指定ID的视图
     */
    public View getViewById(int id) {
        return mContentView.findViewById(id);
    }

    public void setStartLocationX(int mStartLocationX) {
        this.mStartLocationX = mStartLocationX;
    }

    public void setStartLocationY(int mStartLocationY) {
        this.mStartLocationY = mStartLocationY;
    }

    /**
     * @param viewId
     * @param visibility
     */
    public void setViewVisibility(int viewId, int visibility) {
        View view = mContentView.findViewById(viewId);
        if (view.getVisibility() != visibility) {
            view.setVisibility(visibility);
            mContentView.measure(0, 0);
            int width = mContentView.getMeasuredWidth();
            int height = mContentView.getMeasuredHeight();
            mPopupWindow.setWidth(width);
            mPopupWindow.setHeight(height);
        }
    }
}
