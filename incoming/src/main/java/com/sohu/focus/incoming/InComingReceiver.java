package com.sohu.focus.incoming;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * Created by hangzhang209526 on 2016/1/14.
 */
public class InComingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
            //监听拨打电话
        }else if(intent.getAction().equals("zhanghanghanghanghanghanghanghang")){
            new InComingDialog(context,"137185624888").show();
        }else{
            //监听接听电话
            TelephonyManager phoneManager = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
            final Context finalContext = context;
            phoneManager.listen(new PhoneStateListener(){
                private InComingDialog dialog;
                @Override
                public void onCallStateChanged(int state, final String incomingNumber) {
                    super.onCallStateChanged(state, incomingNumber);
                    switch(state){
                        case TelephonyManager.CALL_STATE_IDLE://电话挂断状态
                            Intent i = new Intent();
                            i.setAction("com.likebamboo.phoneshow.ACTION_END_CALL");//发送电话处于挂断状态的广播
                            finalContext.sendBroadcast(i);
                            if(dialog!=null) dialog.dismiss();//关闭来电悬浮窗界面
                            break;
                        case TelephonyManager.CALL_STATE_OFFHOOK://电话接听状态
                            break;
                        case TelephonyManager.CALL_STATE_RINGING://电话铃响状态
                            if(dialog==null) dialog = new InComingDialog(finalContext,incomingNumber);
                            dialog.show();//打开来电悬浮窗界面
                            break;
                        default:

                            break;
                    }
                }
            }, PhoneStateListener.LISTEN_CALL_STATE);

        }
    }
}
