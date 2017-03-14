package com.my.hangzhang.ebook.fragments;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.my.hangzhang.ebook.R;
import com.my.hangzhang.ebook.mode.Book;
import com.my.hangzhang.ebook.view.BookView;
import com.zhanghang.self.base.BaseFragment;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/1/19.
 */
public class BooksFragments extends BaseFragment implements ViewPager.PageTransformer {
    private static final String[] bookNames = {"雪山飞狐","连城诀","鹿鼎记","笑傲江湖","神雕侠侣","碧血剑","鸳鸯刀","侠客行","飞狐外传","倚天屠龙记"};
    /**书籍ViewPager*/
    private ViewPager mBooksPager;
    /**书籍ViewPager适配器*/
    private BooksAdapter mBooksAdapter;

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_books;
    }

    @Override
    protected void initView(){
        mBooksPager = (ViewPager) mRootView.findViewById(R.id.books_pager);
    }

    @Override
    protected void initData(){
        ArrayList<Book> datas = new ArrayList<Book>();
        for(int i=0;i<10;i++){
            Book item = new Book();
            item.setId(i);
            item.setAuthor("");
            item.setBookName(bookNames[i]);
            datas.add(item);
        }
        mBooksAdapter = new BooksAdapter(datas);//设置适配器
        mBooksPager.setAdapter(mBooksAdapter);
        mBooksPager.setPageTransformer(true,this);
    }

    /**
     *
     * @param page
     * @param position     position ==  0 ：当前界面位于屏幕中心的时候
                           position ==  1 ：当前Page刚好滑出屏幕右侧
                           position == -1 ：当前Page刚好滑出屏幕左侧
     */
    @Override
    public void transformPage(View page, float position) {
        if(position>-1&&position<=0){
            float percentage = Math.abs(position);
            page.setRotationY(percentage*90);//围绕Y轴旋转
        }else if(position>0&&position<=1){
            float percentage = Math.abs(1-position);
            page.setRotationY(-90+percentage*90);//围绕Y轴旋转
        }
    }


    private class BooksAdapter extends PagerAdapter{
        /**书籍视图数组，只需要3个*/
        private RelativeLayout[] bookViews = new RelativeLayout[9];
        /**书籍列表*/
        private ArrayList<Book> bookList;

        public BooksAdapter(ArrayList<Book> list){
            bookList = list;
            bookViews[0] = (RelativeLayout) mInflater.inflate(R.layout.view_book, null);
            bookViews[1] = (RelativeLayout) mInflater.inflate(R.layout.view_book, null);
            bookViews[2] = (RelativeLayout) mInflater.inflate(R.layout.view_book, null);
            bookViews[3] = (RelativeLayout) mInflater.inflate(R.layout.view_book, null);
            bookViews[4] = (RelativeLayout) mInflater.inflate(R.layout.view_book, null);
            bookViews[5] = (RelativeLayout) mInflater.inflate(R.layout.view_book, null);
            bookViews[6] = (RelativeLayout) mInflater.inflate(R.layout.view_book, null);
            bookViews[7] = (RelativeLayout) mInflater.inflate(R.layout.view_book, null);
            bookViews[8] = (RelativeLayout) mInflater.inflate(R.layout.view_book, null);
        }


        @Override
        public int getCount() {
            return bookList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            if(object instanceof Book && view!=null && view instanceof ViewGroup){
                View bookView = ((ViewGroup) view).getChildAt(0);
                if(bookView!=null && bookView instanceof BookView) {
                    boolean result = TextUtils.equals(((Book) object).getBookName(), ((BookView) bookView).getBookName());//判断标准为视图显示的数据名称是否和对应的书籍对象的名称是否一致
                    Log.e("book","is true:"+((Book) object).getBookName()+","+((BookView) bookView).getBookName());
                    return result;
                }
            }
            return false;
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            int index = position%bookViews.length;
            if(view.getChildCount()<=index){
                view.addView( bookViews[index]);//添加视图
            }
            Book item = bookList.get(position);
            ((BookView)bookViews[index].getChildAt(0)).setBookName(item.getBookName());//更新数据名称
            return item;
        }

        @Override
        public void destroyItem(ViewGroup container, int position,Object object) {
        }

        @Override
        public float getPageWidth(int position){
            return 0.3f;
        }
    }
}
