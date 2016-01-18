package com.sohu.focus.incoming;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.KeyEvent;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by hangzhang209526 on 2016/1/18.
 */
public class CallService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent,int flag,int startId){
        try {
            Toast.makeText(this, "发送了事件!", Toast.LENGTH_SHORT).show();
            Runtime.getRuntime().exec("input keyevent " + KeyEvent.KEYCODE_HEADSETHOOK);
        } catch (IOException e) {
            Toast.makeText(this, "异常!"+e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return super.onStartCommand(intent,flag,startId);
    }
}
