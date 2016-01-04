package com.souhu.hangzhang209526.pulllistview;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.souhu.hangzhang209526.pulllistview.deafult.DeafultPullFooter;
import com.souhu.hangzhang209526.pulllistview.deafult.DeafultPullHeader;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {
    private ArrayList<String> datas = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //添加数据
        addItem(20);

        final PullListView pullListView = (PullListView) findViewById(R.id.pull_listview);

        //添加页眉、页脚视图
        DeafultPullHeader header = (DeafultPullHeader) PullLinearView.getInstance(this,R.layout.header);
        DeafultPullFooter footer = (DeafultPullFooter) PullLinearView.getInstance(this, R.layout.footer);
        pullListView.addHeaderAndFooter(0,header, -1,footer);
        //添加适配器
        ArrayAdapter adapter = new ArrayAdapter(this,R.layout.layout_item,R.id.text,datas);
        pullListView.setAdapter(adapter);
    }

    private void addItem(int num){
        if(num>0){
            for(int i=0;i<num;i++)
                datas.add("数据"+i);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
