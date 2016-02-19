package com.sohu.focus.hotfixtest;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BugClass bugClass = new BugClass();
        ((TextView)findViewById(R.id.bugMsg)).setText(bugClass.getBugMsg());
    }
}
