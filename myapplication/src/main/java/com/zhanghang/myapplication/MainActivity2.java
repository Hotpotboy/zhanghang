package com.zhanghang.myapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity2 extends Activity  implements View.OnClickListener{
    private ArrayList<TestServiceConnection> connections = new ArrayList<>();
    private Button one,two,three,four,five;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        ((TextView)findViewById(R.id.tip)).setText("第三个activity");
        one = (Button) findViewById(R.id.one);
        two = (Button)findViewById(R.id.two);
        three = (Button) findViewById(R.id.three);
        four = (Button)findViewById(R.id.four);
        five = (Button)findViewById(R.id.five);
        one.setOnClickListener(this);
        two.setOnClickListener(this);
        three.setOnClickListener(this);
        four.setOnClickListener(this);
        five.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.one:
                bindServiceClick();
                break;
            case R.id.two:
                unBindServiceClick();
                break;
            case R.id.three:
                Intent serviceIntent = new Intent(this,TestService.class);
                startService(serviceIntent);
                break;
            case R.id.four:
                serviceIntent = new Intent(this,TestService.class);
                stopService(serviceIntent);
                break;
            case R.id.five:
                serviceIntent = new Intent(this, MainActivity.class);
                serviceIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(serviceIntent);
                break;
        }

    }

    private void bindServiceClick(){
        TestServiceConnection connection = new TestServiceConnection(connections.size()+1);
        connections.add(connection);
        Intent serviceIntent = new Intent(this,TestService.class);
        bindService(serviceIntent, connection, BIND_AUTO_CREATE);
    }

    private void unBindServiceClick(){
        if(connections.size()>0){
            ServiceConnection connection = connections.remove(0);
            unbindService(connection);
        }else{
            Toast.makeText(this,"还没有绑定服务",Toast.LENGTH_SHORT).show();
        }
    }

   private class TestServiceConnection implements ServiceConnection {
       private int index;

       private TestServiceConnection(int i) {
           index=i;
       }

       @Override
       public void onServiceConnected(ComponentName name, IBinder service) {
           Toast.makeText(MainActivity2.this,"连接成功【"+index+"】",Toast.LENGTH_SHORT).show();
           ((TestService.MyBinder)service).getString();
       }

       @Override
       public void onServiceDisconnected(ComponentName name) {
           Toast.makeText(MainActivity2.this,"连接断开",Toast.LENGTH_SHORT).show();
       }
   }


}
