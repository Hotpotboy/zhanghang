package com.sohu.focus.hotfixtest;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.souhu.hangzhang209526.zhanghang.base.BaseApplication;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BaseApplication.getInstance().exeHotFix();
        ((TextView)findViewById(R.id.bugMsg)).setText(new InnerClass().getMsg());
    }
}
