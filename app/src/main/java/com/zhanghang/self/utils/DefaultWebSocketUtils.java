package com.zhanghang.self.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.android.websocket.WebSocket;
import com.android.websocket.WebSocketConnection;
import com.android.websocket.WebSocketException;
import com.android.websocket.WebSocketOptions;
import com.sohu.focus.eventbus.EventBus;
import com.zhanghang.zhanghang.R;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by hangzhang209526 on 2016/1/28.
 */
public class DefaultWebSocketUtils{
    private static final String TAG = "DefaultWebSocketUtils";
    private static final int TIME_DEPLOYED = 2*1000;
    public static final String TAG_RECEIVE_TEXT = "tag_recevie_text";
    public static final String TAG_RECEIVE_RAW = "tag_recevie_raw";
    public static final String TAG_RECEIVE_BIN = "tag_recevie_bin";
    /**发送心跳包的线程池*/
    private ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static Object lock = new Object();
    /**WebSocket连接*/
    private WebSocket webSocket;
    /**WebSocket连接的回调接口*/
    private WebSocket.ConnectionHandler connectionHandler;
    /**上下文*/
    private Context context;
    /**服务端url*/
    private String mUrl;
    /**所有WebSocket实例映射表*/
    private static HashMap<String,DefaultWebSocketUtils> instanceMap = new HashMap<String,DefaultWebSocketUtils>();

    /**根据url获取相关实例*/
    public static DefaultWebSocketUtils getInstanceByUrl(Context context,String url){
        return getInstanceByUrl(context,url,null);
    }

    /**根据url获取相关实例*/
    public static DefaultWebSocketUtils getInstanceByUrl(Context context,String url,String heartMsg){
        DefaultWebSocketUtils instance;
        synchronized (lock){
            instance = instanceMap.get(url);
            if(instance==null){
                instance = new DefaultWebSocketUtils(context,url,heartMsg);
                instanceMap.put(url,instance);
            }
        }
        return instance;
    }

    private static void removeInstance(String url){
        synchronized (lock){
           instanceMap.remove(url);
        }
    }

    private DefaultWebSocketUtils(Context c,final String url, final String heartMsg){
        mUrl = url;
        context = c;
        webSocket = new WebSocketConnection();
        connectionHandler = new WebSocket.ConnectionHandler() {
            @Override
            public void onOpen() {
                Intent openBradCast = new Intent();
                openBradCast.setAction(context.getResources().getString(R.string.broadcast_webSocket_open));
                context.sendBroadcast(openBradCast);
                if(!TextUtils.isEmpty(heartMsg)) {
                    //开始发送心跳包
                    mExecutorService.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (webSocket != null && webSocket.isConnected()) {
                                    webSocket.sendPing();
                                    sendMessage(heartMsg);
                                }
                            } catch (WebSocketException e) {

                            }
                        }
                    }, 0, TIME_DEPLOYED, TimeUnit.MILLISECONDS);
                }
            }

            @Override
            public void onClose(int code, String reason) {
                Intent closeBradCast = new Intent();
                String action = context.getResources().getString(R.string.broadcast_webSocket_close);
                closeBradCast.setAction(action);
                closeBradCast.putExtra(action,reason);
                context.sendBroadcast(closeBradCast);
                //结束心跳包
                if(!TextUtils.isEmpty(heartMsg)) {
                    mExecutorService.shutdown();
                }
            }

            @Override
            public void onTextMessage(String payload) {
                Intent receiveTextMessageBradCast = new Intent();
                String action = context.getResources().getString(R.string.broadcast_webSocket_receive_text_message);
                receiveTextMessageBradCast.setAction(action);
                receiveTextMessageBradCast.putExtra(TAG_RECEIVE_TEXT, payload);
                context.sendBroadcast(receiveTextMessageBradCast);
                EventBus.getDefault().post(receiveTextMessageBradCast,TAG_RECEIVE_TEXT);
            }

            @Override
            public void onRawTextMessage(byte[] payload) {
                Intent receiveRawMessageBradCast = new Intent();
                String action = context.getResources().getString(R.string.broadcast_webSocket_receive_raw_message);
                receiveRawMessageBradCast.setAction(action);
                receiveRawMessageBradCast.putExtra(TAG_RECEIVE_RAW, payload);
                context.sendBroadcast(receiveRawMessageBradCast);
                EventBus.getDefault().post(receiveRawMessageBradCast, TAG_RECEIVE_RAW);
            }

            @Override
            public void onBinaryMessage(byte[] payload) {
                Intent receiveBinaryMessageBradCast = new Intent();
                String action = context.getResources().getString(R.string.broadcast_webSocket_receive_binary_message);
                receiveBinaryMessageBradCast.setAction(action);
                receiveBinaryMessageBradCast.putExtra(TAG_RECEIVE_BIN, payload);
                context.sendBroadcast(receiveBinaryMessageBradCast);
                EventBus.getDefault().post(receiveBinaryMessageBradCast, TAG_RECEIVE_BIN);
            }
        };
    }
    /**
     *建立连接
     */
    public synchronized void connection() throws WebSocketException {
        if(!webSocket.isConnected()) {
            WebSocketOptions option = new WebSocketOptions();
            option.setReconnectInterval(TIME_DEPLOYED*15);//每隔30秒进行重连
            webSocket.connect(mUrl, connectionHandler,option);
        }
    }

    /**发送消息*/
    public void sendMessage(String msg) throws WebSocketException {
        if(webSocket.isConnected()) {
            webSocket.sendTextMessage(msg);
            Log.d(TAG,"webSocket发送消息【"+msg+"】");
        }else{
            throw new WebSocketException("websocket失去连接!");
        }
    }
    /**断开连接*/
    public void disconnection(){
        webSocket.disconnect();
        removeInstance(mUrl);
    }
}