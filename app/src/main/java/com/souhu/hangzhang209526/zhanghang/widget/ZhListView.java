package com.souhu.hangzhang209526.zhanghang.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.ListView;

import java.lang.reflect.Field;

/**
 * Created by hangzhang209526 on 2015/7/24.
 */
public class ZhListView extends ListView {
    public ZhListView(Context context) {
        super(context);
    }

    public ZhListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZhListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        Log.d("zhanghang", "执行绘制方法");
    }

    @Override
    public void onLayout(boolean changed,int l,int t,int r,int b){
        super.onLayout(changed, l, t, r, b);
        Log.d("zhanghang","执行布局方法");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        Log.d("zhanghang","【widthMode】="+widthMode+"【widthSize】="+widthSize+"【heightMode】="+heightMode+"【heightSize】"+heightSize);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d("zhanghang", getMeasuredWidthAndState() + "," + getMeasuredHeightAndState());
        Log.d("zhanghang","执行测量方法");
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        boolean result = super.onTouchEvent(event);
        if(event.getAction()==MotionEvent.ACTION_MOVE){
            try {
//                Class superClass = getClass().getSuperclass();
//                Class supersuperClass = superClass.getSuperclass();
//                Field file = supersuperClass.getDeclaredField("mScrollY");
//                file.setAccessible(true);
//                int value = file.getInt(this);
                Log.d("zhanghang1",getScrollY() + "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    protected boolean overScrollBy(int deltaX, int deltaY,
                                   int scrollX, int scrollY,
                                   int scrollRangeX, int scrollRangeY,
                                   int maxOverScrollX, int maxOverScrollY,
                                   boolean isTouchEvent) {
        final boolean canScrollVertical =
                computeVerticalScrollRange() > computeVerticalScrollExtent();
        final boolean overScrollVertical = getOverScrollMode() == OVER_SCROLL_ALWAYS ||
                (getOverScrollMode() == OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollVertical);
        boolean result = super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
        Log.e("zhanghang1",getScrollY() + "");
        return result;
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        Log.e("zhanghang1", getScrollY() + "");
    }
}
