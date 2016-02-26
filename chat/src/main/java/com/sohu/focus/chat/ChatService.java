package com.sohu.focus.chat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.android.websocket.WebSocketException;
import com.souhu.hangzhang209526.zhanghang.utils.DefaultWebSocketUtils;

/**
 * Created by hangzhang209526 on 2016/2/26.
 */
public class ChatService extends Service {
    private DefaultWebSocketUtils defaultWebSocketUtils;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        defaultWebSocketUtils = ChatApplication.getInstance().getDefaultWebSocketUtils();
    }
    /**
     * Called by the system every time a client explicitly(明确的) starts the service by calling
     * {@link android.content.Context#startService}, providing the arguments it supplied and a
     * unique integer token representing the start request.  Do not call this method directly.
     *
     * <p>For backwards compatibility, the default implementation calls
     * {@link #onStart} and returns either {@link #START_STICKY}
     * or {@link #START_STICKY_COMPATIBILITY}.
     *
     * <p>If you need your application to run on platform versions prior to API
     * level 5, you can use the following model to handle the older {@link #onStart}
     * callback in that case.  The <code>handleCommand</code> method is implemented by
     * you as appropriate:
     *
     * {@sample development/samples/ApiDemos/src/com/example/android/apis/app/ForegroundService.java
     *   start_compatibility}
     *
     * <p class="caution">Note that the system calls this on your
     * service's main thread.  A service's main thread is the same
     * thread where UI operations take place for Activities running in the
     * same process.  You should always avoid stalling（拖延） the main
     * thread's event loop.  When doing long-running operations,
     * network calls, or heavy disk I/O, you should kick off a new
     * thread, or use {@link android.os.AsyncTask}.</p>
     *
     * @param intent The Intent supplied to {@link android.content.Context#startService},
     * as given.  This may be null if the service is being restarted after
     * its process has gone away, and it had previously returned anything
     * except {@link #START_STICKY_COMPATIBILITY}.
     * <p>
     * 如果在进程消失后，此服务进行重启的时候，该参数可能为空
     * <p/>
     * @param flags Additional data about this start request.  Currently either
     * 0, {@link #START_FLAG_REDELIVERY}, or {@link #START_FLAG_RETRY}.
     * START_FLAG_RETRY：表示服务之前被设为START_STICKY，则会被传入这个标记
     * @param startId A unique integer representing this specific request to
     * start.  Use with {@link #stopSelfResult(int)}.
     *
     * @return The return value indicates what semantics the system should
     * use for the service's current started state.  It may be one of the
     * constants associated with the {@link #START_CONTINUATION_MASK} bits.
     *
     * @see #stopSelfResult(int)
     */
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        try {
            defaultWebSocketUtils.connection();
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }
}
