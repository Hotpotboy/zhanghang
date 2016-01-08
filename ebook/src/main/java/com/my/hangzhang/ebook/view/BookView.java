package com.my.hangzhang.ebook.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.my.hangzhang.ebook.R;

/**
 * Created by hangzhang209526 on 2016/1/7.
 */
public class BookView extends View {
    /**在密度为1的情况下的书名的上下边距*/
    private static final int PADDING = 20;
    /**书名的上下边距*/
    private int padding;
    /**书籍背景*/
    private Drawable mBookDrawable;
    /**书名背景*/
    private Drawable mTitleDrawable;
    /**书名背景的范围*/
    private Rect mTitleDrawableBounds;
    /**书名*/
    private CharSequence mBookName;
    /**书名中一个文字的宽度*/
    private int mOneTextWidth;
    /**书名中所有文字的高度*/
    private int mAllTextHeight;
    /**书名的字符布局*/
    private StaticLayout mLayout;
    /**书名的画笔*/
    private TextPaint mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);//参数表示抗锯齿

    public BookView(Context context,CharSequence bookName) throws Exception {
        super(context);
        mBookName = bookName;
        if(TextUtils.isEmpty(mBookName)){
            throw new Exception("必须在xml文件中，指定BookView:book_name的值!");
        }
    }

    public BookView(Context context, AttributeSet attrs) throws Exception {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BookView);
        //获取书名背景的资源ID
        int titleDrawableId = a.getResourceId(R.styleable.BookView_book_title_background,R.drawable.book_face_title_bg);
        mTitleDrawable = context.getDrawable(titleDrawableId);
        //其范围默认为其内在范围
        mTitleDrawableBounds = new Rect(0,0,mTitleDrawable.getIntrinsicWidth(),mTitleDrawable.getIntrinsicHeight());
        mTitleDrawable.setBounds(mTitleDrawableBounds);
        //获取书名
        mBookName = a.getString(R.styleable.BookView_book_name);
        if(TextUtils.isEmpty(mBookName)){
            throw new Exception("必须在xml文件中，指定BookView:book_name的值!");
        }
        a.recycle();
        //初始化画笔
        initTextPaint();
        //书籍背景
        mBookDrawable = getBackground();
    }

    /**
     * 初始化画笔
     */
    private void initTextPaint(){
        mPaint.density = getResources().getDisplayMetrics().density;
        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(25f*mPaint.density);
        Typeface xinshu = Typeface.createFromAsset(getContext().getAssets(), "fonts/YourCustomFont.ttf");
//        mPaint.setTypeface(xinshu);//设置字体
        padding = (int) (PADDING*mPaint.density);
    }

    private void makeLayout(){
        if(mTitleDrawable==null) return;
        //构件文字布局
        Rect textRect = new Rect();
        mPaint.getTextBounds(mBookName.toString(), 0, 1,textRect);//测量一个文字的尺寸
        if(textRect!=null){
            int maxTitleHeight = getMeasuredHeight()*2/3;//书籍背景的最大值
            mOneTextWidth = textRect.width();//一个文字的宽度
            //初始化一个布局
            mLayout = new StaticLayout(mBookName,mPaint,mOneTextWidth, Layout.Alignment.ALIGN_NORMAL,1.5f,0f,false);
            int num = mBookName.length();//文字个数
            mAllTextHeight = 0;
            int all = mLayout.getHeight();
            for(int i = 0;i<num;i++){
                mPaint.getTextBounds(mBookName.toString(), i, i+1,textRect);
                int oneTextHeight = textRect.height();//当前文字的高度
                if(mAllTextHeight+oneTextHeight+padding*2>maxTitleHeight){//如果超过书籍的背景高度则停止
                    CharSequence tmp = mBookName.subSequence(0,i++);
                    mBookName = tmp;
                    mTitleDrawableBounds.bottom = maxTitleHeight;
                    break;
                }
                mAllTextHeight+= oneTextHeight;
                if(i==num-1)
                    mTitleDrawableBounds.bottom = mAllTextHeight+padding*2;
            }
            mTitleDrawable.setBounds(mTitleDrawableBounds);//重新设置title背景的范围
        }
    }
    @Override
    public void onMeasure(int w,int h){
        super.onMeasure(w,h);
        //设置书名的布局
        makeLayout();
    }

    @Override
    protected void onDraw(Canvas c){
        //直接在视图的左上角绘制书名背景
        mTitleDrawable.draw(c);
        //绘制书名
        int titleBgWidth = mTitleDrawableBounds.width();//title背景的宽度
        int startX = (titleBgWidth-mOneTextWidth)/2;//开始绘制书名的x坐标
        if(startX>=0) {
            int titleBgHeight = mTitleDrawableBounds.height();//title背景的高度
            int contentHeight = titleBgHeight - padding*2;
            int startY = padding+(contentHeight - mAllTextHeight)/2;//开始绘制书名的Y坐标
            c.translate(startX, startY);
            mLayout.draw(c);
            c.translate(-startX, -startY);
        }
    }
}