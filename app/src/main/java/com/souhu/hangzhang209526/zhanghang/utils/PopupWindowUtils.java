package com.souhu.hangzhang209526.zhanghang.utils;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

/**
 * Created by hangzhang209526 on 2016/3/11.
 */
public class PopupWindowUtils{
    /**上下文*/
    private Context mContext;
    /**内容视图*/
    private ViewGroup mContentView;
    /**弹出窗口*/
    private PopupWindow mPopupWindow;

    public PopupWindowUtils(Context context,int contentViewId,int width,int height){
        this(context,(ViewGroup) LayoutInflater.from(context).inflate(contentViewId,null),width,height);
    }

    public PopupWindowUtils(Context context,ViewGroup contentView,int width,int height){
        mContext = context;
        mPopupWindow = new PopupWindow(contentView,width,height,false);
        mContentView = contentView;
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setOutsideTouchable(true);
    }

    /**
     * 在指定的视图下方显示浮动窗口
     * @param view
     */
    public void show(View view){
        mPopupWindow.showAsDropDown(view);
    }


    public void showAtLocation(View view,int x,int y){
        mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, x, y);
    }

    public PopupWindow getPopupWindow(){
        return mPopupWindow;
    }

    /**
     * 为指定的视图设置点击监听事件
     * @param viewId 指定视图的ID
     */
    public void setOnClickForSpecialedView(int viewId,View.OnClickListener onClickListener){
        View view = mContentView.findViewById(viewId);
        setOnClickForSpecialedView(view,onClickListener);
    }

    /**
     * 为指定的视图设置点击监听事件
     * @param view 指定视图
     */
    public void setOnClickForSpecialedView(View view,View.OnClickListener onClickListener){
        view.setOnClickListener(onClickListener);
    }

    /**获取指定ID的视图*/
    public View getViewById(int id){
        return mContentView.findViewById(id);
    }
}
