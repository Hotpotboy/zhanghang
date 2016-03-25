package com.zhanghang.self.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Adapter;
import android.widget.ListAdapter;

import java.util.ArrayList;

/**
 * 子视图类型只有一种
 * 子视图只有显示状态、活跃状态、废弃状态三种状态
 * 只有从左到右一种布局模式
 * Created by hangzhang209526 on 2015/8/24.
 */
public class HorizontalListView extends AdapterView<ListAdapter> {
    /**
     * 适配器观察者
     */
    AdapterView<ListAdapter>.AdapterDataSetObserver mAdapterDataSetObserver;
    /**
     * 适配器
     */
    private ListAdapter mAdapter;

    /**
     * 正常布局模式
     */
    private final int LAYOUT_NORMAL = 1;

    /**
     * 布局模式
     */
    private int mLayoutMode;
    /**未触摸*/
    private final int TOUCH_RESET = -1;
    /**按下*/
    private final int TOUCH_DOWN = 0;
    /**轻触*/
    private final int TOUCH_TAP = 1;
    /**按下等待*/
    private final int TOUCH_DOWN_WAITING = 2;
    /**滚动*/
    private final int TOUCH_SCROLL = 3;
    /**抛动*/
    private final int TOUCH_FILING = 4;
    /**滚动回弹*/
    private final int TOUCH_OVER_SCROLL = 5;
    /**抛动回弹*/
    private final int TOUCH_OVER_FILING = 6;
    /**
     * 触摸模式
     */
    private int mTouchMode = TOUCH_RESET;
    /**触摸事件中，按下时的item对应的位置*/
    private int mMotionPosition = INVALID_POSITION;
    /**
     * 被选中item距离该视图左边的距离
     */
    private int mSelectedLeft;
    /**
     * 被选中item的效果矩形
     */
//    Rect mSelectorRect = new Rect();
    /**视图池*/
    private RecycleBin mRescycleBin = new RecycleBin();
    /**子视图是否是来至于重用视图池*/
    private final boolean[] mIsScrap = new boolean[1];
    /**HorizontalListView高度布局*/
    private int mHeightMeasureSpec;
    /**
     * Optional callback to notify client when scroll position has changed
     */
    private OnScrollListener mOnScrollListener;
    /**
     * Determines speed during touch scrolling
     * 滚动速率
     */
    private VelocityTracker mVelocityTracker;
    /**触摸时，移动事件中的敏感宽度,即在进行滚动之前，用户能够移动的距离*/
    private int mTouchSlop;
    /**确定触摸点的矩形范围*/
    private Rect mTouchFrame;
    /**触摸手势中，按下事件的X坐标*/
    private int mMotionX;
    /**轻触事件执行Runnable*/
    private CheckTap mPendingCheckForTap;
    /**长按事件执行Runnable*/
    private CheckLongClick mPendingCheckForLongPress;
    /**执行点击事件*/
    private PerformClickRunnable mPerformClickRunnable;
    private int mLastScrollState;
    private int mMinimumVelocity;
    private int mMaximumVelocity;

    public HorizontalListView(Context context){
        this(context, null);
    }

    public HorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    public ListAdapter getAdapter() {
        return null;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mAdapter != null && mAdapterDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mAdapterDataSetObserver);
            mAdapterDataSetObserver = null;
        }
        resetList();
        mAdapter = adapter;

        if (mAdapter != null) {
            mOldItemCount = mItemCount;
            mItemCount = mAdapter.getCount();

            mAdapterDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mAdapterDataSetObserver);//重新生成观察者

            int position = lookForSelectablePosition(0);//寻找可用的item
            setSelectedPositionInt(position);
            setNextSelectedPositionInt(position);

            checkSelectionChanged();
        }else{
            checkSelectionChanged();
        }
        requestLayout();
    }

    private int lookForSelectablePosition(int position) {
        final ListAdapter adapter = mAdapter;
        if (adapter == null || isInTouchMode()) {
            return INVALID_POSITION;
        }

        final int count = adapter.getCount();
        position = Math.max(0, position);
        while (position < count && !adapter.isEnabled(position)) {
            position++;
        }
        if (position < 0 || position >= count) {
            return INVALID_POSITION;
        }

        return position;
    }

    public void resetList() {
        removeAllViewsInLayout();
        mDataChanged = false;
        mFirstPosition = 0;
        mOldSelectedPosition = INVALID_POSITION;
        mOldSelectedRowId = INVALID_ROW_ID;
        mSelectedPosition = INVALID_POSITION;
        mSelectedRowId = INVALID_ROW_ID;
        mNextSelectedPosition = INVALID_POSITION;
        mNextSelectedRowId = INVALID_ROW_ID;
        mLayoutMode = LAYOUT_NORMAL;
        mSelectedLeft = 0;
//        mSelectorRect.setEmpty();
        mNeedSync = false;
        mRescycleBin.clear();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int childWidth = 0;
        int childHeight = 0;
        int childState = 0;

        mItemCount = mAdapter!=null?mAdapter.getCount():0;
        //如果HorizonlListView存在需要自己决定的宽度或者高度
        if(mItemCount>0){
            View child = obtainView(0,mIsScrap);
            //测量子视图
            measureChildView(child,0);
            childHeight = child.getMeasuredHeight();
            childWidth = child.getMeasuredWidth();
            childState = combineMeasuredStates(childState, child.getMeasuredState());
            //回收子视图
            mRescycleBin.addScrapViews(child,0);
        }

        if(widthMode==MeasureSpec.UNSPECIFIED){
            widthSize = getPaddingLeft()+childWidth+getPaddingRight();
        }else if(widthMode==MeasureSpec.AT_MOST){
            widthSize = measureWidthOfChildren(heightMeasureSpec, 0, mItemCount - 1, widthSize);
        }

        if(heightMode==MeasureSpec.UNSPECIFIED){
            heightSize = getPaddingTop()+childHeight+getPaddingBottom();
        }else if(heightMode==MeasureSpec.AT_MOST){//与子视图的高度一致
            heightSize = Math.min(heightSize,getPaddingTop()+childHeight+getPaddingBottom());
        }
        setMeasuredDimension(widthSize, heightSize);
        mHeightMeasureSpec = heightMeasureSpec;
    }

    /**测量子视图*/
    private void measureChildView(View child,int heightMeasureSpec){
        LayoutParams lp = getLayoutParamFromView(child);
        lp.forceAdd = true;//如果该子视图被重用，需要强制添加到父类之中

        int heightSpec = ViewGroup.getChildMeasureSpec(heightMeasureSpec,getPaddingTop()+getPaddingBottom(),lp.height);
        int widthSpec;
        //如果子视图指定了宽度的具体值，则子视图的宽度完全由HorizontalListView决定，否则完全由子视图自己决定
        if(lp.width>0) widthSpec = MeasureSpec.makeMeasureSpec(lp.width,MeasureSpec.EXACTLY);
        else widthSpec = MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
        child.measure(widthSpec, heightSpec);
    }

    /**
     * 根据子视图测量HorizontalListView的宽度
     * @param heightMeasureSpec  HorizontalListView测量时的分配高度，用来测量子视图
     * @param startItem   从何处item开始测量
     * @param endItem     从何处item结束测量
     * @param maxWidth    最大宽度
     */
    private int measureWidthOfChildren(int heightMeasureSpec, int startItem, int endItem, int maxWidth){
        int width = getPaddingLeft()+getPaddingRight();
        if(startItem>=endItem||maxWidth<=0) return width;
        for(int i=startItem;i<=endItem;i++){
            View child = obtainView(i,mIsScrap);
            measureChildView(child,heightMeasureSpec);
            int childWidth = child.getMeasuredWidth();
            width += childWidth;
            mRescycleBin.addScrapViews(child,i);
            if(width>=maxWidth) return maxWidth;
        }

        return width;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        mInLayout = true;

        final int childCount = getChildCount();
        if (changed) {
            for (int i = 0; i < childCount; i++) {
                getChildAt(i).forceLayout();
            }
            mRescycleBin.makeForceLayout();
        }

        layoutChildren();
        mInLayout = false;
    }

    protected void layoutChildren() {
        invalidate();

        if (mAdapter == null) {
            resetList();
            invokeOnItemScrollListener();
            return;
        }

        final int childrenTop = getPaddingTop();
        final int childrenBottom = getBottom() - getTop() - getPaddingBottom();
        final int childCount = getChildCount();

        int index = 0;//旧被选中的item对应的子视图在子视图数组中的index
        int delta = 0;//新旧被选中item的距离

//        View sel;
//        View oldSel = null;
//        View oldFirst = null;
        View newSel = null;

//        oldFirst = getChildAt(mFirstPosition);
        index = mSelectedPosition-mFirstPosition;
//        oldSel = getChildAt(index);//旧的被选中的item对应的子视图
        if(mNextSelectedPosition>=0){
            delta = mNextSelectedPosition-mSelectedPosition;
        }
        newSel = getChildAt(index+delta);//新的被选中的item对应的子视图

        if(mDataChanged){
            handleDataChanged();//处理数据改变
        }

        setSelectedPositionInt(mNextSelectedPosition);//更新被选中的item的位置

        if(!mDataChanged){
            mRescycleBin.fillActiveViews(mFirstPosition);//将所有显示状态的视图缓存到活跃视图池之中
        }else{
            for(int i=0;i<childCount;i++){
                View childView = getChildAt(i);
                mRescycleBin.addScrapViews(childView,mFirstPosition+i);
            }
        }

        //缓存子视图之后，就移除HorizontalListView之中所有的子视图
        detachAllViewsFromParent();

        //开始新的被选中的item为标准，左右布局
        int selectedLeft;
        if(newSel!=null) selectedLeft = newSel.getLeft();
        else selectedLeft = getPaddingLeft();
        fillSelected(mSelectedPosition,selectedLeft);

        setNextSelectedPositionInt(mSelectedPosition);
        mDataChanged = false;
        mNeedSync = false;
        invokeOnItemScrollListener();
    }

    /**
     * 从被选择的item指定的视图开始，向左右布局
     * @param selectedPosition
     */
    private void fillSelected(int selectedPosition,int left){
        View child = obtainView(selectedPosition, mIsScrap);
        int top = getPaddingTop();
        setupView(child, selectedPosition, left, top, true, mIsScrap[0], true);
        mSelectedLeft = left;
        //填充左边的子视图
        int startX = child.getLeft();//-child.getMeasuredWidth();
        fillLeftFromPosition(selectedPosition,startX);
        //填充右边的子视图
        startX = child.getRight();
        fillRightFromPosition(selectedPosition,startX);
    }

    /**
     * 从指定位置的左一个item开始往左填充子视图
     * @param position  指定item在适配器中的位置
     * @param startX  指定位置的item对应的子视图的右边坐标
     */
    private void fillLeftFromPosition(int position,int startX){
        position--;
        int left = getPaddingLeft();
        int top = getPaddingTop();
        while (startX>=left&&position>=0){
            View child = obtainView(position,mIsScrap);
            setupView(child,position,startX,top,false,mIsScrap[0],false);
            startX -= child.getMeasuredWidth();
            position--;
        }
        mFirstPosition = position+1;
    }

    /**
     * 从指定位置的右一个item开始往右填充子视图
     * @param position 指定item在适配器中的位置
     * @param startX 指定位置的item对应的子视图的左边坐标
     */
    private void fillRightFromPosition(int position,int startX){
        position++;
        int right = getMeasuredWidth()-getPaddingRight();
        int itemCount = mItemCount-1;
        int top = getPaddingTop();
        while (startX<=right&&position<=itemCount){
            View child = obtainView(position,mIsScrap);
            setupView(child,position,startX,top,false,mIsScrap[0],true);
            startX += child.getMeasuredWidth();
            position++;
        }
    }



    void invokeOnItemScrollListener() {
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(this, mFirstPosition, getChildCount(), mItemCount);
        }
        onScrollChanged(0, 0, 0, 0); // dummy values, View's implementation does not use these.
    }

    /**
     * 获取视图
     */
    private View obtainView(int position,boolean[] isScrap){
        isScrap[0] = false;//视图默认不是来自于重用视图池之中
        View child = null;
        RecycleBin bin = mRescycleBin;
        if(!mDataChanged){//如果数据没有改变则从活跃视图池中获取
            child = bin.getActiveView(position);
            if(child!=null){
                isScrap[0]=true;
                return child;
            }
        }
        View tmp = bin.getScrapView(position);//从废弃视图池中获取
        child = mAdapter.getView(position, tmp, this);//废弃状态的视图需要重新绑定数据
        if(tmp!=null){
            if(child!=tmp){//如果并未使用废弃视图池中的视图，则重新回收该视图
                bin.addScrapViews(tmp,position);
            }else{
                isScrap[0]=true;
                return child;
            }
        }
        return child;
    }

    /**
     * 布局指定子视图
     * @param child   指定子视图
     * @param position 该子视图对应的item在适配器中的位置
     * @param leftOrRight    isToRight为false，该子视图的右部位置
     * @param top     该子视图的顶部位置
     * @param isSelected  该子视图对应的item是否被选择
     * @param isScrap  该子视图是否来自于重用视图池
     * @param isToRight  是否朝右添加子视图
     */
    private void setupView(View child,int position,int leftOrRight,int top,boolean isSelected,boolean isScrap,boolean isToRight){
        boolean needSelected = isSelected!=child.isSelected();
        int mod = mTouchMode;
        boolean isPressed = mod>TOUCH_DOWN&&mod<TOUCH_SCROLL&&mMotionPosition==position;//该子视图是否需要显示按下状态
        boolean needPressed = isPressed!=child.isPressed();
        //如果子视图正在布局、或者不是来至于重用视图池或者需要修改选择状态，则需要重新测量及布局
        boolean needToMasure = child.isLayoutRequested()||!isScrap||needSelected;

        LayoutParams lp=getLayoutParamFromView(child);
        //如果已经被回收了同时布局参数中不需要强制添加（focusAdd为false）则无需粘合在ViewGroup之中
        if(!lp.forceAdd&&isScrap){
            attachViewToParent(child,isToRight?-1:0,lp);
        }else{
            lp.forceAdd = false;
            addViewInLayout(child,isToRight?-1:0,lp);
        }

        if(needSelected) child.setSelected(isSelected);
        if(needPressed) child.setPressed(true);

        //如果需要重新测量
        if(needToMasure){
            int heightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec,getPaddingTop()+getPaddingBottom(),lp.height);
            int widthSpec;
            //如果子视图指定了宽度的具体值，则子视图的宽度完全由HorizontalListView决定，否则完全由子视图自己决定
            if(lp.width>0) widthSpec = MeasureSpec.makeMeasureSpec(lp.width,MeasureSpec.EXACTLY);
            else widthSpec = MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
            child.measure(widthSpec,heightSpec);
        }else{
            cleanupLayoutState(child);
        }

        int w = child.getMeasuredWidth();
        int h = child.getMeasuredHeight();
        //重新布局
        if(needToMasure){
            int left,right;
            if(!isToRight){
                right = leftOrRight;
                left = leftOrRight-w;
            }else{
                right = leftOrRight+w;
                left = leftOrRight;
            }
            int bottom = top+h;
            child.layout(left,top,right,bottom);
        }else{
            int offset;
            if(isToRight) offset = leftOrRight-child.getLeft();
            else offset = leftOrRight-w-child.getLeft();

            child.offsetLeftAndRight(offset);
            child.offsetTopAndBottom(top-child.getTop());
        }
    }

    @Override
    public View getSelectedView() {
        return null;
    }

    @Override
    public void setSelection(int position) {

    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private LayoutParams getLayoutParamFromView(View view){
        if(view==null) return null;
        LayoutParams lp;
        ViewGroup.LayoutParams lpTmp =  view.getLayoutParams();
        if(lpTmp==null){
            lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT,false);
        }else if(!(lpTmp instanceof LayoutParams)){
            lp = new LayoutParams(lpTmp);
        }else{
            lp = (LayoutParams)lpTmp;
        }
        return lp;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(event);//收集触摸点，计算速率
        int actionMaskted = event.getActionMasked();//获取触摸手势中的具体事件
        switch (actionMaskted){
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                onTouchUp(event);
                break;
        }
        return true;
    }

    /***
     * 判断是否进行滚动
     * 对于慢速滑动，执行点击事件；对于快速滑动，执行滚动效果
     * @param event
     */
    private void onTouchUp(MotionEvent event){
        switch (mTouchMode){
            case TOUCH_DOWN:
            case TOUCH_TAP:
            case TOUCH_DOWN_WAITING:
                View motionView = getChildAt(mMotionPosition-mFirstPosition);
                if(motionView!=null){
                    setSelectedPositionInt(mMotionPosition);
                    layoutChildren();
                    motionView.setPressed(false);
                    if(mPerformClickRunnable!=null){//如果不为空则表示上一个点击事件未被执行
                        removeCallbacks(mPendingCheckForTap);
                        mPerformClickRunnable=null;
                    }
                    mPerformClickRunnable = new PerformClickRunnable();
                    mPerformClickRunnable.mClickPostion = mMotionPosition;
                    postDelayed(mPerformClickRunnable, ViewConfiguration.getPressedStateDuration());
                }
                mTouchMode = TOUCH_RESET;
                break;
            case TOUCH_SCROLL:
                int childCount = getChildCount();
                if(childCount>0){
                    mVelocityTracker.computeCurrentVelocity(1000,mMaximumVelocity);
                    int velocityX = (int) mVelocityTracker.getXVelocity();
                    boolean isFiling = velocityX>mMinimumVelocity;//是否需要滚动
                    if(isFiling){

                    }else{
                        mTouchMode=TOUCH_RESET;
                        reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                    }

                }else{
                    mTouchMode=TOUCH_RESET;
                    reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                }
                break;
        }
        //删除轻触或者长按事件
        removeCallbacks(mTouchMode == TOUCH_DOWN ? mPendingCheckForTap : mPendingCheckForLongPress);

        setPressed(false);
        recycleVelocityTracker();

    }

    /**判断触摸点是否移动出HorizontalListView*/
    private void onTouchMove(MotionEvent event){
        int x = (int) event.getX();
        switch (mTouchMode){
            case TOUCH_DOWN:
            case TOUCH_TAP:
            case TOUCH_DOWN_WAITING:
                if(startScrollIfNeed(x)){//如果滚动则直接退出
                    break;
                }
                if(!pointInView(x,event.getY(),mTouchSlop)){
                    setPressed(false);
                    View child = getChildAt(mMotionPosition-mFirstPosition);
                    if(child!=null) child.setPressed(false);
                    //删除相应的回调方法
                    removeCallbacks(mTouchMode==TOUCH_DOWN?mPendingCheckForTap:mPendingCheckForLongPress);
                }
                mTouchMode = TOUCH_DOWN_WAITING;
                break;
            case TOUCH_SCROLL:
                int delat = x-mMotionX;//滑动的总体距离
                scrollIfNeed(x, delat);
                break;
        }
    }

    /**
     * 判断是否需要滑动
     * @param x   当前移动点的x坐标
     * @return true表示开始滑动过了，false表示不需滑动
     */
    private boolean startScrollIfNeed(int x){
        int delat = x-mMotionX;//滑动的总体距离
        boolean isOverScroll = getScrollX()!=0;
        if(isOverScroll||Math.abs(delat)>mTouchSlop){
            if(isOverScroll){//如果HorizontalListView在水平方向发生了滚动，则触摸模式变为滚动回弹模式
               mTouchMode = TOUCH_OVER_SCROLL;
            }else{
               mTouchMode = TOUCH_SCROLL;
            }
            setPressed(false);
            View child = getChildAt(mMotionPosition-mFirstPosition);
            if(child!=null) child.setPressed(false);

            removeCallbacks(mPendingCheckForLongPress);

            //因为即将滚动，所有不许父视图拦截触摸事件
            ViewParent parent = getParent();
            if(parent!=null){
                parent.requestDisallowInterceptTouchEvent(true);
            }
            boolean isScroll = scrollIfNeed(x,delat);
            if(isScroll) reportScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
            else reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
            return isScroll;
        }
        return false;
    }

    private void reportScrollStateChange(int newState) {
        if (newState != mLastScrollState) {
            if (mOnScrollListener != null) {
                mLastScrollState = newState;
                mOnScrollListener.onScrollStateChanged(this, newState);
            }
        }
    }

    /**
     * 滑动HorizontalListView
     * @param totalDistance
     */
    private boolean scrollIfNeed(int x,int totalDistance){
        if(mAdapter==null||mAdapter.getCount()<=0) return false;

        if(mTouchMode==TOUCH_SCROLL) {
            //判断是否到左部或者右部
            boolean noCanScrollFromLeft = mFirstPosition==0&&getPaddingLeft()<=getChildAt(0).getLeft()&&totalDistance>=0;
            boolean noCanScrollFromRight = (mFirstPosition+getChildCount())==mItemCount&&getRight()>=getChildAt(getChildCount()-1).getRight()&&totalDistance<=0;
            if(noCanScrollFromLeft||noCanScrollFromRight) return false;

            int count=0;//需要回收的子视图个数
            int start=0;//从那个位置开始回收子视图
            boolean isLeft = totalDistance > 0;//是否从左往右
            int childCount = getChildCount();
            if(isLeft){
                int distance = getRight()-Math.abs(totalDistance);
                for(int i=childCount-1;i>=0;i--){
                    View child = getChildAt(i);
                    int left = child.getLeft();//子视图右部再父视图中的坐标
                    if(left>distance){//需要回收
                        start = i;
                        count++;
                        mRescycleBin.addScrapViews(child,mFirstPosition+i);
                    }else{
                        break;
                    }
                }
            }else{
                int distance = Math.abs(totalDistance);
                for(int i=0;i<childCount-1;i++){
                    View child = getChildAt(i);
                    int right = child.getRight();
                    if(right>=distance){
                        break;
                    }else{
                        start = i;
                        count++;
                        mRescycleBin.addScrapViews(child,mFirstPosition+i);
                    }
                }
            }

            if(count>0) {
                removeViews(start, count);//将丢入重用池中的所有子视图，从HorizontalListView之中删除
            }
            childCount = getChildCount();
            //将所有还存在的子视图进行移动
            for(int i=0;i<childCount;i++){
                View child = getChildAt(i);
//                child.offsetLeftAndRight(totalDistance);
                int mLeft = child.getLeft();
                int mRight = child.getRight();
                mLeft += totalDistance;
                mRight += totalDistance;
                child.setLeft(mLeft);
                child.setRight(mRight);
            }

            if(count>0){//填充余下的子视图
                if(!isLeft){
                    mFirstPosition += count;
                    View rightView = getChildAt(childCount-1);//当前最右边的子视图
                    fillRightFromPosition(getPositionForView(rightView),rightView.getRight());
                    setSelectedPositionInt(mFirstPosition);
                    setNextSelectedPositionInt(mFirstPosition);
                }else{
                    View leftView = getChildAt(0);//当前最边的子视图
                    fillLeftFromPosition(mFirstPosition, leftView.getLeft());
                    setSelectedPositionInt(mFirstPosition);
                    setNextSelectedPositionInt(mFirstPosition);
                }
            }
            mMotionX = x;
        }
        return true;
    }



    private void onTouchDown(MotionEvent event){
        int x = (int) event.getX();
        int y = (int) event.getY();

        mTouchMode = TOUCH_DOWN;//更新触摸状态

        if (mPendingCheckForTap == null) {
            mPendingCheckForTap = new CheckTap();
        }

        postDelayed(mPendingCheckForTap,ViewConfiguration.getTapTimeout());

        mMotionPosition = pointToPosition(x,y);
        mMotionX = x;
    }

    /**
     * Maps a point to a position in the list.
     *
     * @param x X in local coordinate
     * @param y Y in local coordinate
     * @return The position of the item which contains the specified point, or
     *         {@link #INVALID_POSITION} if the point does not intersect an item.
     */
    public int pointToPosition(int x, int y) {
        Rect frame = mTouchFrame;
        if (frame == null) {
            mTouchFrame = new Rect();
            frame = mTouchFrame;
        }

        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return mFirstPosition + i;
                }
            }
        }
        return INVALID_POSITION;
    }

    /**
     * 判断触摸点是否已经移除当前视图
     * @param localX
     * @param localY
     * @param slop
     * @return
     */
    private boolean pointInView(float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < ((getRight() - getLeft()) + slop) &&
                localY < ((getBottom() - getTop()) + slop);
    }

    /**轻触状态监测Runnable*/
    class CheckTap implements Runnable{

        @Override
        public void run() {
            if(mTouchMode==TOUCH_DOWN) {
                mTouchMode = TOUCH_TAP;
                View motionView = getChildAt(mMotionPosition-mFirstPosition);
                if(motionView!=null) {
                    motionView.setPressed(true);
                    setPressed(true);
                }
                boolean longClickable = isLongClickable();
                if (longClickable) {
                    if (mPendingCheckForLongPress == null) {
                        mPendingCheckForLongPress = new CheckLongClick();
                    }
                    //发起长按事件监听
                    postDelayed(mPendingCheckForLongPress, ViewConfiguration.getLongPressTimeout());
                } else {
                    mTouchMode = TOUCH_DOWN_WAITING;
                }
            }
        }
    }

    /**长按状态监测Runnable*/
    class CheckLongClick implements Runnable{

        @Override
        public void run() {
            View motionView = getChildAt(mMotionPosition-mFirstPosition);
            if(motionView!=null){
                boolean handle = motionView.performLongClick();
                if(handle){
                    motionView.setPressed(false);
                    setPressed(false);
                    mTouchMode = TOUCH_RESET;
                    return;
                }
            }
            mTouchMode = TOUCH_DOWN_WAITING;
        }
    }

    /**
     * AbsListView extends LayoutParams to provide a place to hold the view type.
     */
    public static class LayoutParams extends ViewGroup.LayoutParams {

        /**
         * When an AbsListView is measured with an AT_MOST measure spec, it needs
         * to obtain children views to measure itself. When doing so, the children
         * are not attached to the window, but put in the recycler which assumes
         * they've been attached before. Setting this flag will force the reused
         * view to be attached to the window rather than just attached to the
         * parent.
         */
        @ViewDebug.ExportedProperty(category = "list")
        boolean forceAdd;

        /**
         * The position the view was removed from when pulled out of the
         * scrap heap.
         *
         * @hide
         */
        int scrappedFromPosition;

        /**
         * The ID the view represents
         */
        long itemId = -1;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h,boolean _forceAdd) {
            super(w, h);
            forceAdd = _forceAdd;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    /**
     * 视图池类
     */
    class RecycleBin {
        /**
         * 存储在活跃状态视图池{@link #activeViews}之中的第一个视图，对应的item再适配器中的位置
         */
        private int mFirstActiveViewPostition;
        /**
         * 活跃状态视图池
         */
        private View[] activeViews;
        /**
         * 废弃状态视图池
         */
        private ArrayList<View> scrapViews;

        public RecycleBin() {
            scrapViews = new ArrayList<View>();
            activeViews = new View[0];
        }

        /**
         * 将显示状态的视图缓存到活跃视图池中
         *
         * @param firstViewPostition 当前列表第一个子视图对应item在适配器中的位置
         */
        public void fillActiveViews(int firstViewPostition) {
            int count = getChildCount();
            if (count <= 0) return;

            mFirstActiveViewPostition = firstViewPostition;

            if (activeViews == null || count > activeViews.length) activeViews = new View[count];
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                ViewGroup.LayoutParams lp = getLayoutParamFromView(child);
                if (lp != null && lp instanceof LayoutParams) {
                    activeViews[i] = child;
                    ((LayoutParams) lp).scrappedFromPosition = mFirstActiveViewPostition + i;
                }
            }
        }

        /**
         * 将显示状态的视图缓存到废弃视图池中
         *
         * @param scrapView 需要缓存的子视图
         * @param position  需要缓存子视图对应的item再适配器中的位置
         */
        public void addScrapViews(View scrapView, int position) {
            if (scrapView == null) return;
            LayoutParams lp = getLayoutParamFromView(scrapView);
            if (lp == null) return;

            lp.scrappedFromPosition = position;

            scrapViews.add(scrapView);
        }

        /**
         * 重用活跃视图池中的视图
         *
         * @param position 重用视图的位置
         */
        public View getActiveView(int position) {
            int index = position - mFirstActiveViewPostition;
            if (index >= 0 && index < activeViews.length) {
                View child = activeViews[index];
                activeViews[index] = null;
                return child;
            }
            return null;
        }

        /**
         * 重用废弃视图池中的视图
         *
         * @param position 重用视图的位置
         */
        public View getScrapView(int position) {
            int size = scrapViews.size();
            if (size > 0) {
                for (View view : scrapViews) {
                    LayoutParams lp = getLayoutParamFromView(view);
                    if (lp.scrappedFromPosition == position) {
                        scrapViews.remove(view);
                        return view;
                    }
                }
                View scrap = scrapViews.remove(size - 1);
                return scrap;
            } else return null;
        }

        /**
         * 将活跃状态视图池中的剩余视图全部添加到废弃视图池表之中
         */
        public void removeActivesToScrap() {
            if (activeViews != null && activeViews.length > 0) {
                for (int i = 0; i < activeViews.length; i++) {
                    View view = activeViews[i];
                    if (view != null) {
                        scrapViews.add(view);
                        LayoutParams lp = getLayoutParamFromView(view);
                        lp.scrappedFromPosition = mFirstActiveViewPostition + i;
                        activeViews[i] = null;//将该子视图从废弃视图列表之中清除
                    }
                }
                pruneScrapViews();
            }
        }

        /**
         * 确保废弃视图池的长度不超过活跃视图池
         */
        private void pruneScrapViews() {
            int maxLen = activeViews.length;
            int size = scrapViews.size();
            if (size > maxLen) {//如果废弃视图池超过了活跃视图池，则对废弃视图池进行裁剪
                int delat = size - maxLen;
                while (delat > 0) {
                    View view = scrapViews.remove(--size);
                    HorizontalListView.this.removeDetachedView(view, false);//从视图容器之中删除该子视图
                    delat--;
                }
            }
        }

        /**
         * 强制布局
         */
        public void makeForceLayout() {
            if (scrapViews != null && scrapViews.size() > 0) {
                for (View view : scrapViews)
                    view.forceLayout();
            }
        }

        /***
         * 清除所有的视图池
         */
        public void clear(){
            activeViews = new View[0];
            scrapViews.clear();
        }
    }

    public interface OnScrollListener {

        /**
         * The view is not scrolling. Note navigating the list using the trackball counts as
         * being in the idle state since these transitions are not animated.
         */
        public static int SCROLL_STATE_IDLE = 0;

        /**
         * The user is scrolling using touch, and their finger is still on the screen
         */
        public static int SCROLL_STATE_TOUCH_SCROLL = 1;

        /**
         * The user had previously been scrolling using touch and had performed a fling. The
         * animation is now coasting to a stop
         */
        public static int SCROLL_STATE_FLING = 2;

        /**
         * Callback method to be invoked while the list view or grid view is being scrolled. If the
         * view is being scrolled, this method will be called before the next frame of the scroll is
         * rendered. In particular, it will be called before any calls to
         * {@link Adapter#getView(int, View, ViewGroup)}.
         *
         * @param view The view whose scroll state is being reported
         *
         * @param scrollState The current scroll state. One of
         * {@link #SCROLL_STATE_TOUCH_SCROLL} or {@link #SCROLL_STATE_IDLE}.
         */
        public void onScrollStateChanged(HorizontalListView view, int scrollState);

        /**
         * Callback method to be invoked when the list or grid has been scrolled. This will be
         * called after the scroll has completed
         * @param view The view whose scroll state is being reported
         * @param firstVisibleItem the index of the first visible cell (ignore if
         *        visibleItemCount == 0)
         * @param visibleItemCount the number of visible cells
         * @param totalItemCount the number of items in the list adaptor
         */
        public void onScroll(HorizontalListView view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount);
    }

    /**HorizontalListView执行点击时间的方法*/
    class PerformClickRunnable implements Runnable{
        private int mClickPostion;//执行点击事件的位置
        @Override
        public void run() {
            //如果数据发生改变，则不执行点击事件
            if(mDataChanged||mAdapter==null){
                mPerformClickRunnable=null;
                return;
            }
            int firstPosition = mFirstPosition;
            int endPosition = firstPosition+getChildCount()-1;
            if(mClickPostion>=0&&mClickPostion<=endPosition&&mClickPostion>=mFirstPosition){
                View child = getChildAt(mClickPostion-firstPosition);
                if(child!=null){
                    performItemClick(child,mClickPostion,mAdapter.getItemId(mClickPostion));
                }
            }
            mPerformClickRunnable=null;
        }
    }
}
