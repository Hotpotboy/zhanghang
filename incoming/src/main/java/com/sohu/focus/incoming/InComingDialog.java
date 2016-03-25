package com.sohu.focus.incoming;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by hangzhang209526 on 2016/1/14.
 */
public class InComingDialog{
    public static final int CAN_ANSWER_CALL = 0;
    public static final int CAN_NOT_ANSWER_CALL = 1;

    public static int type = CAN_NOT_ANSWER_CALL;

    private Context mAPPContext;
    /**
     * 窗口管理者
     */
    private WindowManager mWindowManager;
    /**
     * 窗口布局参数
     */
    private WindowManager.LayoutParams mLayoutParams;
    /**顶级视图*/
    private FrameLayout mDecor;
    private LayoutInflater inflater;

    private boolean isShow = false;

    public static void setType(int t) {
        type = t;
    }

    public InComingDialog(Context context, String num) {
        mAPPContext = context.getApplicationContext();
        mWindowManager = (WindowManager) mAPPContext.getSystemService(Context.WINDOW_SERVICE);
        mDecor = new FrameLayout(mAPPContext);

        inflater = (LayoutInflater) mAPPContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //窗口布局参数
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.format = PixelFormat.TRANSLUCENT;
//        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        if (type == CAN_ANSWER_CALL) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            setupViewCanAnswer(num);
        } else if (type == CAN_NOT_ANSWER_CALL) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            //该标志表示即便窗口是可获取焦点的，那么任何在此窗口外的触摸事件都将被发送给它的后面的窗口；如果此标志不被设置，则任何在此窗口外的触摸事件依然会发送给此窗口
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            layoutParams.height = 1200;
            layoutParams.gravity = Gravity.TOP;
            setupViewCanNotAnswer(num);
        }

        mLayoutParams = layoutParams;
    }

    private void setupViewCanNotAnswer(String num) {
        mDecor = (FrameLayout) inflater.inflate(R.layout.incoming_dialog, mDecor);
        TextView phoneNum = (TextView) mDecor.findViewById(R.id.incoming_dialog_num);
        phoneNum.setText(num);
        WebView webView = (WebView) mDecor.findViewById(R.id.net_info);
        webView.loadUrl("http://www.sohu.com");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
    }

    private void setupViewCanAnswer(String num) {
        mDecor = (FrameLayout) inflater.inflate(R.layout.incoming, mDecor);
        Button refuse = (Button) mDecor.findViewById(R.id.incoming_refuse);
        refuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                dismiss();
                endCall();
            }
        });

        Button answer = (Button) mDecor.findViewById(R.id.incoming_answer);
        answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 21) {
                    Intent intent = new Intent(mAPPContext,CallService.class);
                    mAPPContext.startService(intent);
                }else{
                    answerRingingCall();
                }
            }
        });

        TextView phoneNum = (TextView) mDecor.findViewById(R.id.incoming_num);
        phoneNum.setText(num);

    }

    public void show() {
        if (isShow) return;//已经显示了
        mWindowManager.addView(mDecor, mLayoutParams);
        isShow = true;
    }

    public void dismiss() {
        if (!isShow) return;//已经取消了
        mWindowManager.removeView(mDecor);
        isShow = false;
    }


    private void answerRingingCall() {

        //据说该方法只能用于Android2.3及2.3以上的版本上，但本人在2.2上测试可以使用
        try {
            Intent localIntent1 = new Intent(Intent.ACTION_HEADSET_PLUG);
            localIntent1.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            localIntent1.putExtra("state", 1);
            localIntent1.putExtra("microphone", 1);
            localIntent1.putExtra("name", "Headset");
            mAPPContext.sendOrderedBroadcast(localIntent1, "android.permission.CALL_PRIVILEGED");
            //按下耳机接听电话键
            Intent localIntent2 = new Intent(Intent.ACTION_MEDIA_BUTTON);
            KeyEvent localKeyEvent1 = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK);
            localIntent2.putExtra("android.intent.extra.KEY_EVENT", localKeyEvent1);
            mAPPContext.sendOrderedBroadcast(localIntent2, "android.permission.CALL_PRIVILEGED");
            //松开耳机接听电话键
            Intent localIntent3 = new Intent(Intent.ACTION_MEDIA_BUTTON);
            KeyEvent localKeyEvent2 = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);
            localIntent3.putExtra("android.intent.extra.KEY_EVENT", localKeyEvent2);
            mAPPContext.sendOrderedBroadcast(localIntent3, "android.permission.CALL_PRIVILEGED");
            //拔出耳机
            Intent localIntent4 = new Intent(Intent.ACTION_HEADSET_PLUG);
            localIntent4.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            localIntent4.putExtra("state", 0);
            localIntent4.putExtra("microphone", 1);
            localIntent4.putExtra("name", "Headset");
            mAPPContext.sendOrderedBroadcast(localIntent4, "android.permission.CALL_PRIVILEGED");
            Toast.makeText(mAPPContext, "没有出现了异常!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            String enforcedPerm = "android.permission.CALL_PRIVILEGED";
            Intent btnDown = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                    Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_HEADSETHOOK));
            Intent btnUp = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                    Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,
                            KeyEvent.KEYCODE_HEADSETHOOK));

            mAPPContext.sendOrderedBroadcast(btnDown, enforcedPerm);
            mAPPContext.sendOrderedBroadcast(btnUp, enforcedPerm);
        }
    }


    private void printErro(Throwable e){
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);//打印异常
        Throwable cause = e.getCause();//打印异常的引起异常
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        SharedPreferences preferences = mAPPContext.getSharedPreferences("reson", mAPPContext.MODE_PRIVATE);
        SharedPreferences.Editor commit = preferences.edit();
        commit.remove("reson");
        commit.putString("reson",result);
        commit.commit();
    }

//    private void answerCall() {
//        // 初始化iTelephony
//        TelephonyManager telephonyManager = (TelephonyManager) mAPPContext.getSystemService(Context.TELEPHONY_SERVICE);
//        Class<TelephonyManager> c = (Class<TelephonyManager>) telephonyManager.getClass();
//        try {
//            Method getTelMethod = c.getDeclaredMethod("getITelephony");
//            getTelMethod.setAccessible(true);
//            //通过反射，获取ITelephony的IBinder代理对象
//            Object telephonyObject = getTelMethod.invoke(telephonyManager);
//            if (telephonyObject != null) {
//                Class telephonyClass = telephonyObject.getClass();
//                Method answerRingingCallMethod = telephonyClass.getMethod("answerRingingCall");
//                answerRingingCallMethod.setAccessible(true);
//                answerRingingCallMethod.invoke(telephonyObject);
//            }
//        } catch (Exception e) {
//
//        }
//    }

    /**
     * 挂断电话
     */
    public void endCall() {
        try {
            // 初始化iTelephony
            TelephonyManager telephonyManager = (TelephonyManager) mAPPContext.getSystemService(Context.TELEPHONY_SERVICE);
            // Will be used to invoke hidden methods with reflection
            // Get the current object implementing ITelephony interface
            Class telManager = telephonyManager.getClass();
            Method getITelephony = telManager.getDeclaredMethod("getITelephony");
            getITelephony.setAccessible(true);
            //通过反射，获取ITelephony的IBinder代理对象
            Object telephonyObject = getITelephony.invoke(telephonyManager);
            if (null != telephonyObject) {
                Class telephonyClass = telephonyObject.getClass();

                Method endCallMethod = telephonyClass.getMethod("endCall");
                endCallMethod.setAccessible(true);

                endCallMethod.invoke(telephonyObject);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }
}
