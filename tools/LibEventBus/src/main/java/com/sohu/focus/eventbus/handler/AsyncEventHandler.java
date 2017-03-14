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

import com.sohu.focus.eventbus.subscribe.Subscription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 耗时事件的异步处理,每一个事件将在一个单独的线程中处理
 * 
 * @author mrsimple
 */
public class AsyncEventHandler implements EventHandler {

    /**
     * 事件分发线程
     */
    private final static ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 事件处理器
     */
    EventHandler mEventHandler = new DefaultEventHandler();

    public AsyncEventHandler() {
    }

    /**
     * 将订阅的函数执行在异步线程中
     * 
     * @param subscription
     * @param event
     */
    public void handleEvent(final Subscription subscription, final Object event) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                mEventHandler.handleEvent(subscription, event);
            }
        });
    }
}
