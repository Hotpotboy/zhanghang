package com.zhanghang.self.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/3/21.
 */
public class ViewPager3D extends ViewGroup{
    /**三个子视图之间的默认间距*/
    private final static int DEAFULT_SPACE_IN_CHILDREN = 10;
    private static int sChildrenNum = 3;
    /**是否正在布局*/
    private boolean mIsLayout = false;
    /**设备密度*/
    private float mDensity;
    /**当前位置*/
    private int mCurrPosition;
    /**当前所有被绑定的对象列表*/
    private ItemInfo[] mItems = new ItemInfo[sChildrenNum];
    /**适配器*/
    private Page3DAdapter mAdapter;

    /**观察者*/
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {

        }

        @Override
        public void onInvalidated() {

        }
    };

    public ViewPager3D(Context context) {
        super(context);
        initViewPager3D(context);
    }

    public ViewPager3D(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViewPager3D(context);
    }

    private void initViewPager3D(Context context){
        mDensity = context.getResources().getDisplayMetrics().density;
    }

    public void setAdapter(Page3DAdapter adapter){
        if(mAdapter!=null){
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
            mAdapter.startUpdate(this);
            for(int i=0;i<mItems.length;i++){
                if(mItems[i]!=null) {
                    mAdapter.destroyItem(this, i, mItems[i].object);
                    mItems[i] = null;
                }

            }
            mAdapter.finishUpdate(this);
        }
        mCurrPosition =0;
        scrollTo(0,0);
        mAdapter = adapter;
        if(mAdapter!=null){
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
        requestLayout();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mIsLayout = true;
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        float totalMarginWidthInChildren = DEAFULT_SPACE_IN_CHILDREN*mDensity*(sChildrenNum-1);//所有子视图之间的间隔的总和
        float resetWidthForChildren = measureWidth -totalMarginWidthInChildren;//出去间隔外的剩余宽度
        int centerChildWidth = (int) (resetWidthForChildren*2/5);//位于中间的子视图的宽度
        int noCenterChildWidth = (int) (resetWidthForChildren*3/10);//非中间的子视图的宽度
        int chilrenCount = getChildCount();
        if(chilrenCount>0){
            if(mCurrPosition ==0){//还未开始转动
                for(int i = 0;i<mItems.length;i++){
                    int childWidthiMeasureSpec = -1;
                    int childHeightMeasureSpec = ViewGroup.getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(), LayoutParams.MATCH_PARENT);
                    View childView=null;
                    ItemInfo item = mItems[i];
                    if(item!=null&&item.index==1){
                        childView = getChildAt(1);
                        childWidthiMeasureSpec = MeasureSpec.makeMeasureSpec(centerChildWidth, MeasureSpec.EXACTLY);
                    }else if(item!=null){
                        childView = getChildAt(item.index);
                        childWidthiMeasureSpec = MeasureSpec.makeMeasureSpec(noCenterChildWidth, MeasureSpec.EXACTLY);
                    }
                    if(childView!=null&&childHeightMeasureSpec!=-1&&childWidthiMeasureSpec!=-1) {
                        childView.measure(childWidthiMeasureSpec, childHeightMeasureSpec);//测量子视图
                    }
                }
            }else if(mCurrPosition >0&& mCurrPosition <mAdapter.getCount()){

            }
        }
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    }

    /**填充(添加)子视图*/
    private void populate(){

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft;
        childLeft = getPaddingLeft();
        for (ItemInfo itemInfo:mItems){
            View childView = getChildAt(itemInfo.index);
            int childWidth = childView.getMeasuredWidth();
            childView.layout(childLeft,t,childLeft+childWidth,b);
            childLeft+=childWidth;
            if(itemInfo.position!=mItems.length-1){
                childLeft+=DEAFULT_SPACE_IN_CHILDREN*mDensity;//添加间隔
            }
        }
        mIsLayout = false;
    }

    private class ItemInfo{
        /**用来标识每一Item的对象*/
        private Object object;
        /**此item在适配器中的位置*/
        private int position;
        /**此item对应的子视图的索引*/
        private int index;
    }

    public static abstract class Page3DAdapter<T> extends PagerAdapter {
        private Context mContext;
        private ArrayList<T> mData;
        /**此ViewPager3d的子视图数组*/
        private View[] children = new View[sChildrenNum];
        public Page3DAdapter(Context context,ArrayList<T> data,int layoutId){
            mContext = context;
            mData = data;
            for(int i=0;i<sChildrenNum;i++) {
                children[i] = LayoutInflater.from(mContext).inflate(layoutId, null);
            }
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            int index = position%children.length;
            if(view.getChildCount()<=index){
                view.addView(children[index]);//添加视图
            }
           return getObjectForSpecialView(position,children[index], (ViewPager3D) view);
        }

        protected abstract Object getObjectForSpecialView(int position,View ChildView,ViewPager3D parent);
    }
}
