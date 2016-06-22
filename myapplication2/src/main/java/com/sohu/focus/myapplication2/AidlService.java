package com.sohu.focus.myapplication2;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.test.IGetString;

/**
 * Created by hangzhang209526 on 2016/6/13.
 */
public class AidlService extends Service {
    private Binder localBinder = new IGetString.Stub(){

        @Override
        public void getString() throws RemoteException {
            Toast.makeText(AidlService.this, "调用binder对象中的接口方法", Toast.LENGTH_SHORT).show();
        }
    };
    @Override
    public void onCreate(){
        super.onCreate();
        Toast.makeText(this, "调用onCreate方法", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent,int flag,int startId){
        Toast.makeText(this,"调用onStartCommand方法",Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent,flag,startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(this,"调用onBind方法",Toast.LENGTH_SHORT).show();
        return localBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        Toast.makeText(this,"调用onUnbind方法",Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Toast.makeText(this,"onRebind",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this,"onDestroy",Toast.LENGTH_SHORT).show();
    }
}
