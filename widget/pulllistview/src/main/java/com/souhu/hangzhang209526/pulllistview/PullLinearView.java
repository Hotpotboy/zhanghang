package com.souhu.hangzhang209526.pulllistview;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * 整个页眉、页脚视图的公共父视图；
 * 可以通过静态方法{@link PullLinearView#getInstance(Context, int)}，指定一个布局文件，来获取一个具体的实例，
 * 布局文件的根视图必须为PullLinearView及其子类。
 * {@link PullLinearView#mAllHeight}变量表示该视图的最大高度，其值是由此视图
 * 所有的子视图共同决定的（决定方式与LinearLayout一致）；此外此视图的高度是可变的，
 * 调用{@link PullLinearView#updateHeaderVisiblity(int)}方法来设置视图的当前时刻的可视高度。
 * {@link PullLinearView#updateHeaderVisiblity(int)}只是简单的改变视图的高度，如果想要实现
 * 一种随可视高度变化而变化的效果，则需要在子类中覆盖{@link PullLinearView#processViewUpdateByTouch(float, PullLinearView)}
 * 方法，该方法的第一个入参表示当前可视高度与总高度的比值（小于等于1）。
 * 此视图执行加载数据的方法是{@link PullLinearView#dealLoadData()},该方法使用一个{@link AsyncTask}对象，
 * 通过该对象来实现，加载数据前、加载数据后的视图变化效果；使用时，只需在子类中分别覆盖
 * {@link PullLinearView#processViewUpdateByOnclick(PullLinearView)}方法和
 * {@link PullLinearView#processViewUpdateAfterLoadedData(PullLinearView, Object[])}方法
 * 来实现加载数据前、加载数据后的视图变化效果；同时，此{@link AsyncTask}对象，也开辟了一个新的
 * 线程来执行加载数据的操作，加载数据的操作通过{@link PullLinearView#loadDataByASync(PullLinearView)}方法实现。
 * Created by hangzhang209526 on 2015/11/16.
 */
public class PullLinearView extends LinearLayout {
    protected Context mContext;
    /**
     * PullLinearView中所有子视图的总高度
     */
    private int mAllHeight = 0;
    /**
     * 滚动辅助类
     */
    private Scroller mScroller;

    /**粘合本视图的PullListView*/
    protected PullListView mAttchedListView;

    public PullLinearView(Context context) {
        super(context);
        init(context);
    }

    public PullLinearView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        mContext = context;
        mScroller = new Scroller(context);
    }

    /**
     * 获取所有子视图的总高度
     */
    public void initChildrenAllHeight() {
        //获取每一个子视图的高度
        int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View child = getChildAt(i);
            int childHeight = child.getMeasuredHeight();
            if (childHeight <= 0) {
                child.measure(0, 0);
                childHeight = child.getMeasuredHeight();
            }
            mAllHeight += childHeight;
        }
    }


    /**
     * 更新视图的可视高度
     *
     * @param visiblityHeight
     */
    public void updateHeaderVisiblity(int visiblityHeight) {
        int height = visiblityHeight;
        boolean isZero = height == 0;//可见高度是否为0
        if (mAllHeight <= visiblityHeight) {
            height = mAllHeight;
        } else if (isZero) {
            // PullLinearView为ListView的子视图，ListView在对子视图测量之前，会做一个判断，
            // 对于layout_height为0的子视图，ListView默认将其当做MeasureSpec.UNSPECIFIED模式
            height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
        }
        float percent = (float) (isZero ? 0 : height) / (float) mAllHeight;
        //执行通过触摸带来的视图变化
//        processViewUpdateByTouch(percent, this);

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if(layoutParams==null){
            layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT, 0);
        }
        layoutParams.height = height;
        setLayoutParams(layoutParams);
    }

    public int getAllHeight() {
        if (mAllHeight <= 0) {
            initChildrenAllHeight();
        }
        return mAllHeight;
    }

    /**
     * 是否还能加载更多
     *
     * @return
     */
    public boolean canLoadMore() {
        int currentHeight = getLayoutParams().height;//当前高度
        return currentHeight < getAllHeight();
    }

    /**
     * 开始回滚
     */
    public void startScrollBack() {
        int currentHeight = getLayoutParams().height;//当前高度
//        if(!mScroller.isFinished()) mScroller.forceFinished(false);
        mScroller.startScroll(0, currentHeight, 0, -currentHeight);
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            int height = mScroller.getCurrY();
            updateHeaderVisiblity(height);
        }
    }

    public void dealLoadData() {
        AsyncTask task = new AsyncTask<Object, Void, Object[]>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                processViewUpdateByOnclick(PullLinearView.this);
            }

            @Override
            protected Object[] doInBackground(Object[] params) {
                 return loadDataByASync(PullLinearView.this);
            }

            @Override
            protected void onPostExecute(Object[] result) {
                super.onPostExecute(result);
                processViewUpdateAfterLoadedData(PullLinearView.this, result);
            }
        };
        task.execute();
    }

    /**
     * 根据布局文件获取一个实例
     *
     * @param context
     * @param layoutId 布局文件，此布局文件的根标签必须是一个PullLinearView，否则将返回空
     * @return
     */
    public static PullLinearView getInstance(Context context, int layoutId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View result = inflater.inflate(layoutId, null);
        if (result instanceof PullLinearView) {
            return (PullLinearView) result;
        } else {
            return null;
        }
    }

    /**
     * 通过触摸事件更新PullLinearView的高度（调用{@link PullLinearView#updateHeaderVisiblity(int)}）连带产生的视图变化
     * @param percent 此时时刻，在整个变化所耗总时间所占的百分比
     */
    protected void processViewUpdateByTouch(float percent, PullLinearView pullLinearView) {
    }

    /**
     * 通过点击执行视图更新，一般而言点击事件是在触摸事件UP阶段产生，因此会在{@link PullListView}
     * 的触摸UP阶段调用此方法
     */
    protected void processViewUpdateByOnclick(PullLinearView pullLinearView) {
    }

    /**
     * 加载数据,非UI线程执行
     * @return 是否执行
     */
    protected Object[] loadDataByASync(PullLinearView pullLinearView) {
        return null;
    }

    /**
     * 加载数据完成后，更新视图的方法，在UI线程之中运行
     * @param result 加载数据后的结果，此结果必须在UI线程中执行{@link PullLinearView#loadDataByASync(PullLinearView)}
     *               方法的返回值
     */
    protected void processViewUpdateAfterLoadedData(PullLinearView pullLinearView,Object[] result) {
    }

    public void setAttchedListView(PullListView attchedListView) {
        mAttchedListView = attchedListView;
    }
}

