package com.souhu.hangzhang209526.zhanghang;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.souhu.hangzhang209526.zhanghang.adpter.ImageAdapter;
import com.souhu.hangzhang209526.zhanghang.utils.ImageCache;

import java.util.ArrayList;

import static com.souhu.hangzhang209526.zhanghang.adpter.ImageAdapter.*;


public class MainActivity extends Activity {
//    NetworkImageView imageView;
    ListView imageList;
    ImageAdapter adapter;
    private static ArrayList<String> list;
    static{
        list = new ArrayList<>();
        list.add("http://p3.qhimg.com/t0167a9dbb748dbff77.jpg");
        list.add("http://img2.3lian.com/2014/f4/68/d/129.jpg");
        list.add("http://pic34.nipic.com/20131023/13946634_152224761000_2.jpg");
        list.add("http://p3.qhimg.com/t0167a9dbb748dbff77.jpg");
        list.add("http://img2.3lian.com/2014/f4/68/d/129.jpg");
        list.add("http://pic34.nipic.com/20131023/13946634_152224761000_2.jpg");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        imageView = (NetworkImageView) findViewById(R.id.net_work_image);
//        //³õÊ¼»¯Í¼Æ¬¼ÓÔØÆ÷
//        ImageLoader imageLoader = new ImageLoader(Volley.newRequestQueue(this), new ImageCache(8*1024*1024));
//        imageView.setDefaultImageResId(R.drawable.deafult);
//        imageView.setImageUrl("http://p3.qhimg.com/t0167a9dbb748dbff77.jpg11", imageLoader);
        imageList = (ListView)findViewById(R.id.imageList);
        adapter = new ImageAdapter(this,list);
        imageList.setAdapter(adapter);
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
