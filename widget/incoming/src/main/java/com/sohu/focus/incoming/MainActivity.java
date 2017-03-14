package com.sohu.focus.incoming;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;


public class MainActivity extends Activity {
    private InComingReceiver inComingReceiver = new InComingReceiver();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences = getSharedPreferences("reson", MODE_PRIVATE);
        String reson = preferences.getString("reson","");
        if (!TextUtils.isEmpty(reson)) {
            ((TextView) findViewById(R.id.text_id)).setText(reson);
        }
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                InComingDialog.setType(InComingDialog.CAN_ANSWER_CALL);
                InComingDialog dialog = new InComingDialog(MainActivity.this,"5555");
                dialog.show();//打开来电悬浮窗界面
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InComingDialog.setType(InComingDialog.CAN_NOT_ANSWER_CALL);
            }
        });
//
        IntentFilter intent = new IntentFilter();
        intent.addAction("android.intent.action.PHONE_STATE");
        registerReceiver(inComingReceiver,intent);
    }

    @Override
    public void onDestroy(){
        unregisterReceiver(inComingReceiver);
        super.onDestroy();
    }
}
