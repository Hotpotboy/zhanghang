/*
 * Copyright (C) 2015 Mr.Simple <bboyfeiyu@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sohu.focus.eventbus.handler;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.sohu.focus.eventbus.subscribe.Subscription;

/**
 * 事件处理在UI线程,通过Handler将事件处理post到UI线程的消息队列
 *
 * @author mrsimple
 */
public class UIorBGThreadEventHandler implements EventHandler {
    private static final int MSG_WHAT = 1;

    /**
     * ui handler
     */
    private Handler mHandler;
    /**
     * 事件分发线程
     */
    DispatcherThread mDispatcherThread;
    /**
     *
     */
    private DefaultEventHandler mEventHandler;

    /**是否继续向指定的队列发送事件*/
    private boolean  isGoOnSendEvent;

    /**
     * 获取有UI线程处理的事件处理实例
     * @return
     */
    public static UIorBGThreadEventHandler getUIThreadEventHandler(){
        return new UIorBGThreadEventHandler(Looper.getMainLooper());
    }
    /**
     * 获取有后台线程（HandlerThread的一个子类）处理的事件处理实例
     * @return
     */
    public static UIorBGThreadEventHandler getBGThreadEventHandler(){
        return new UIorBGThreadEventHandler(null);
    }

    private UIorBGThreadEventHandler(Looper looper) {
        mEventHandler = new DefaultEventHandler();
        if (looper == Looper.getMainLooper()) {//生成由UI线程处理的Handler
            mHandler = new Handler(Looper.getMainLooper());
        } else {//生成由后台线程处理的Handler
            mDispatcherThread = new DispatcherThread(UIorBGThreadEventHandler.class.getSimpleName());//只启动一个线程来处理事件队列
            mDispatcherThread.start();
        }
    }

    /**
     * @param subscription
     * @param event
     */
    public void handleEvent(final Subscription subscription, final Object event) {
        if (mHandler == null) {
            throw new NullPointerException("mHandler == null, hava some System erro.");
        }
        Message postMsg = Message.obtain(mHandler, new Runnable() {
            @Override
            public void run() {
                mEventHandler.handleEvent(subscription, event);
                if (!subscription.isGoOn) {
                    setIsGoOnSendEvent(false);//停止继续发送事件
                    mHandler.removeMessages(MSG_WHAT);//删除所有已添加的队列
                }
            }
        });
        postMsg.what = MSG_WHAT;
        postMsg.sendToTarget();
    }

    public synchronized boolean isGoOnSendEvent() {
        return isGoOnSendEvent;
    }

    public synchronized void setIsGoOnSendEvent(boolean is) {
        isGoOnSendEvent = is;
    }

    /**
     * @author mrsimple
     */
    private class DispatcherThread extends HandlerThread {

        /**
         * @param name
         */
        public DispatcherThread(String name) {
            super(name);
        }

        @Override
        public synchronized void start() {
            super.start();
            mHandler = new Handler(this.getLooper());
        }

    }

}
