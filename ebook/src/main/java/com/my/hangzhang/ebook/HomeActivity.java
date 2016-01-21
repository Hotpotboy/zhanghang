package com.my.hangzhang.ebook;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.my.hangzhang.ebook.fragments.BooksFragments;
import com.souhu.hangzhang209526.zhanghang.base.BaseFragment;
import com.souhu.hangzhang209526.zhanghang.base.BaseFragmentActivity;

public class HomeActivity extends BaseFragmentActivity {
    private FrameLayout mFragmentParent;
    /**Fragment数组*/
    private BaseFragment[] mFragments = {new BooksFragments()};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initFragments(mFragments,R.id.fragment_content);
    }
}
