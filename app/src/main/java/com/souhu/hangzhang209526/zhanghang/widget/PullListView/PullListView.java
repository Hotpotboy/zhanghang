package com.souhu.hangzhang209526.zhanghang.widget.PullListView;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by hangzhang209526 on 2015/11/16.
 */
public class PullListView extends ListView {
    /**列表此刻位于顶部*/
    private static final int IN_TOP = 0;
    /**列表此刻位于底部*/
    private static final int IN_BOTTOM = 1;
    /**列表此刻位于中部*/
    private static final int IN_MODDLE = 2;

    private Context mContext;
    /**是否正在滚动中*/
    private boolean isScrolling = false;
    /**ListView滚动监听器*/
    private PullListViewScrollListener pullListViewScrollListener = new PullListViewScrollListener();

    /**上一次触摸事件对应的Y坐标*/
    private int mLastY;
    /**滑动最小识别距离,10像素*/
//    private final int MIN_TOUCH_SLOP = 0;

    private PullLinearView mHeader;

    private PullLinearView mFooter;

    /**当前ListView的位置是否到达顶部或者底部*/
    private int isTopOrBottom = IN_TOP;

    public PullListView(Context context) {
        super(context);
        initListView(context);
    }

    public PullListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initListView(context);
    }

    private void initListView(Context context){
        mContext = context;
        super.setOnScrollListener(pullListViewScrollListener);
    }

    /**
     * 初始化页眉，页脚视图
     * @param initHeaderHeight   页眉视图的初始高度，如果是-1表示将视图全部展示出来
     * @param headerView         页眉视图
     * @param initFooterHeight   页脚视图的初始高度，如果是-1表示将视图全部展示出来
     * @param footerView         页脚视图
     */
    public void addHeaderAndFooter(final int initHeaderHeight,PullLinearView headerView,final int initFooterHeight, PullLinearView footerView){
        if(headerView!=null) {
            mHeader = headerView;
            addHeaderView(mHeader);
            mHeader.setAttchedListView(this);
        }

        if(footerView!=null) {
            mFooter = footerView;
            addFooterView(mFooter);
            mFooter.setAttchedListView(this);
        }
        //完成布局后，获取页眉、页脚视图的高度并设置初始高度
        post(new Runnable() {
            @Override
            public void run() {
                if (mHeader != null) {
                    mHeader.initChildrenAllHeight();
                    if (initHeaderHeight >= 0)
                        mHeader.updateHeaderVisiblity(initHeaderHeight);//初始的页眉视图的可视高度
                    else
                        mHeader.updateHeaderVisiblity(mHeader.getAllHeight());
                }
                if (mFooter != null) {
                    mFooter.initChildrenAllHeight();
                    if (initFooterHeight >= 0)
                        mFooter.updateHeaderVisiblity(initFooterHeight);//初始的页眉视图的可视高度
                    else
                        mFooter.updateHeaderVisiblity(mFooter.getAllHeight());
                }
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        int curY = (int) e.getY();
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastY = curY;
                break;
            case MotionEvent.ACTION_MOVE:
//                Debug.startMethodTracing();
                isTopOrBottom = isTopOrBottomInContent();//滑动后，ListView是否还处于顶部或者底部
                if(isTopOrBottom!=IN_MODDLE) {//当前list不在中间
                    int detal = curY - mLastY;
                    if (detal > 0) {//当前滑动距离超过了最小滑动识别距离
                        mHeader.updateHeaderVisiblity(detal);
                    } else {
                        mFooter.updateHeaderVisiblity(-detal);
                    }
                }
//                Debug.stopMethodTracing();
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                dealLoadData();
                break;
        }
        return super.onTouchEvent(e);
    }

    private void dealLoadData(){
        isTopOrBottom = isTopOrBottomInContent();//滑动后，ListView是否还处于顶部或者底部
        if(isTopOrBottom == IN_TOP&&mHeader!=null) {//位于顶部
            if (!mHeader.canLoadMore()) {//如果不能加载更多
                mHeader.dealLoadData();
            } else {//如果还能加载更多的时候释放了手指
                mHeader.startScrollBack();//用户的手指离开了屏幕，恢复至原状
            }
        }else if(isTopOrBottom == IN_BOTTOM&&mFooter!=null){//位于底部
            if (!mFooter.canLoadMore()) {//如果不能加载更多
                mFooter.dealLoadData();
            } else {//如果还能加载更多的时候释放了手指
                mFooter.startScrollBack();//用户的手指离开了屏幕，恢复至原状
            }
        }
    }


    /**listView滚动状态变化执行函数*/
    protected void scrollState(int scrollState){
        if(scrollState==OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
                ||scrollState == OnScrollListener.SCROLL_STATE_FLING){
             isScrolling = true;
        }else{
            if(isScrolling){
                dealLoadData();
            }
            isScrolling = false;
        }

    }

    protected void scrolling( int firstVisibleItem, int visibleItemCount, int totalItemCount){

    }

    @Override
    public final void setOnScrollListener(OnScrollListener l){
        Log.e("pull_list_view","此方法无效，请用scrollState方法和scrolling方法替换");
    }

    //判断是否到达了ListView内容的顶部或者底部
    private int isTopOrBottomInContent(){
        int childCount = getChildCount();//总体子视图个数
        int headViewCount = getHeaderViewsCount();
        int footViewCount = getFooterViewsCount();
        int listViewTop = getPaddingTop();//列表的顶部
        int listViewBottom = listViewTop+getHeight()-getPaddingBottom();//列表的底部
        //是否位于ListView内容的顶部
        int firstItemPosistion = getFirstVisiblePosition()+headViewCount;//第一个非页眉的子视图对应的数据在适配器中的位置
        int firstViewTop = getChildAt(0).getPaddingTop();
        boolean isFirstPosistion = firstItemPosistion==headViewCount&&firstViewTop>=listViewTop;
        if(isFirstPosistion) return IN_TOP;
        //是否位于ListView内容的底部
        int lastItemPosistion = getFirstVisiblePosition()+childCount-footViewCount;//最后一个非页脚的子视图对应的数据在适配器中的位置
        int lastViewBottom = getChildAt(childCount-1).getBottom();//
        boolean isLastPosition = (lastItemPosistion==getCount()-footViewCount)&&lastViewBottom<=listViewBottom;
        if(isLastPosition) return IN_BOTTOM;
        return IN_MODDLE;
    }

    private class PullListViewScrollListener implements OnScrollListener{

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            scrollState(scrollState);
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            scrolling(firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }
}
