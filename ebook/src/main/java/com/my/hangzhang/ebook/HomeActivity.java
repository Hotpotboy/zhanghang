package com.my.hangzhang.ebook;

import android.app.Activity;
import android.os.Bundle;

import com.my.hangzhang.ebook.view.BookView;
import com.souhu.hangzhang209526.zhanghang.base.BaseFragmentActivity;

public class HomeActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_book);
        BookView book = (BookView)findViewById(R.id.book);
        book.setRotationY(180);
    }
}
