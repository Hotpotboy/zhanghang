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

package com.sohu.focus.eventbus;

import android.os.Looper;
import android.util.Log;

import com.sohu.focus.eventbus.handler.DefaultEventHandler;
import com.sohu.focus.eventbus.handler.EventHandler;
import com.sohu.focus.eventbus.handler.AsyncEventHandler;
import com.sohu.focus.eventbus.handler.UIorBGThreadEventHandler;
import com.sohu.focus.eventbus.matchpolicy.MatchPolicy;
import com.sohu.focus.eventbus.matchpolicy.SimpleMatchPolicy;
import com.sohu.focus.eventbus.subscribe.SubsciberMethodHunter;
import com.sohu.focus.eventbus.subscribe.Subscription;
import com.sohu.focus.eventbus.subscribe.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p/>
 * EventBus是AndroidEventBus框架的核心类,也是用户的入口类.它存储了用户注册的订阅者信息和方法,
 * 事件类型和该事件对应的tag标识一个种类的事件{@link EventType},每一种事件对应有一个或者多个订阅者{@link Subscription}
 * ,订阅者中的订阅函数通过{@link com.sohu.focus.eventbus.subscribe.Subscriber}注解来标识tag和线程模型,这样使得用户体检较为友好,代码也更加整洁.
 * <p/>
 * 用户需要在发布事件前通过@{@link #register(Object)}方法将订阅者注册到EventBus中,EventBus会解析该订阅者中使用了
 * {@see Subcriber}标识的函数,并且将它们以{@link EventType}为key,以{@link Subscription}
 * 列表为value存储在map中. 当用户post一个事件时通过事件到map中找到对应的订阅者,然后按照订阅函数的线程模型将函数执行在对应的线程中.
 * EvetnBus默认不会遍历订阅对象的父类中的方法，如果要使其遍历父类中的方法，你可以在初始化一个EventBus对象之后，调用其
 * {@link #initConfig(MatchPolicy, boolean)}方法，通过第二个入参设置
 * <p/>
 * 最后在不在需要订阅事件时,应该调用{@link #unregister(Object)}函数注销该对象,避免内存泄露!
 * 例如在Activity或者Fragment的onDestory函数中注销对Activity或者Fragment的订阅.
 * <p/>
 * 注意 : 如果发布的事件的参数类型是订阅的事件参数的子类,订阅函数默认也不会被执行。例如你在订阅函数中订阅的是List<String>类型的事件,
 * 但是在发布时发布的是ArrayList<String>的事件,
 * 因此List<String>是一个泛型抽象,而ArrayList<String>才是具体的实现
 * ,因此这种情况下订阅函数是不会被执行，而如果想要订阅函数执行，在必须发布一个List<String>事件</>。
 * 如果你需要订阅函数能够接收到的事件类型无须严格匹配 ,你可以在初始化一个EventBus对象之后，调用其
 * {@link #initConfig(MatchPolicy)}方法
 *
 * @author mrsimple
 */
public final class EventBus {
    private static final String TAG = "EventBus";

    /**
     * default descriptor
     */
    private static final String DESCRIPTOR = EventBus.class.getSimpleName();

    /**
     * 事件总线描述符描述符
     */
    private String mDesc = DESCRIPTOR;

    /**
     * EventType-Subcriptions map
     */
    private final Map<EventType, CopyOnWriteArrayList<Subscription>> mSubcriberMap = new ConcurrentHashMap<EventType, CopyOnWriteArrayList<Subscription>>();
    /**
     *
     */
    private List<EventType> mStickyEvents = Collections.synchronizedList(new LinkedList<EventType>());
    /**
     * the thread local event queue, every single thread has it's own queue.
     */
//    ThreadLocal<Queue<EventType>> mLocalEvents = new ThreadLocal<Queue<EventType>>() {
//        protected java.util.Queue<EventType> initialValue() {
//            return new ConcurrentLinkedQueue<EventType>();
//        };
//    };

    /**
     * the event dispatcher
     */
    EventDispatcher mDispatcher = new EventDispatcher();

    /**
     * the subscriber method hunter, find all of the subscriber's methods
     * annotated with @Subcriber
     */
    SubsciberMethodHunter mMethodHunter = new SubsciberMethodHunter(mSubcriberMap);

    /**
     * The Default EventBus instance
     */
    private static EventBus sDefaultBus;

    /**
     * private Constructor
     */
    private EventBus() {
        this(DESCRIPTOR);
    }

    /**
     * constructor with desc
     *
     * @param desc the descriptor of eventbus
     */
    public EventBus(String desc) {
        mDesc = desc;
    }

    /**
     * @return
     */
    public static EventBus getDefault() {
        if (sDefaultBus == null) {
            synchronized (EventBus.class) {
                if (sDefaultBus == null) {
                    sDefaultBus = new EventBus();
                }
            }
        }
        return sDefaultBus;
    }

    /**
     * register a subscriber into the mSubcriberMap, the key is subscriber's
     * method's name and tag which annotated with {@see Subcriber}, the value is
     * a list of Subscription.
     *
     * @param subscriber the target subscriber
     */
    public void register(Object subscriber) {
        if (subscriber == null) {
            return;
        }

        synchronized (this) {
            mMethodHunter.findSubcribeMethods(subscriber);
        }
    }

    /**
     * 以sticky的形式注册,则会在注册成功之后迭代所有的sticky事件
     *
     * @param subscriber 订阅对象
     */
    public void registerSticky(Object subscriber) {
        this.register(subscriber);
        // 处理sticky事件
        mDispatcher.dispatchStickyEvents(subscriber);
    }

    /**
     * @param subscriber
     */
    public void unregister(Object subscriber) {
        if (subscriber == null) {
            return;
        }
        synchronized (this) {
            mMethodHunter.removeMethodsFromMap(subscriber);
        }
    }

    /**
     * post a event
     *
     * @param event
     */
    public void post(Object event) {
        post(event, EventType.DEFAULT_TAG);
    }

    /**
     * 发布事件
     *
     * @param event 要发布的事件，一个POJO类
     * @param tag   事件的tag, 类似于BroadcastReceiver的action
     */
    public void post(Object event, String tag) {
        if (event == null) {
            Log.e(TAG, "The event object is null");
            return;
        }
        //将本次事件添加到线程局部的事件队列之中
//        mLocalEvents.get().offer(new EventType(event.getClass(), tag));
//        Log.e("test","线程【"+Thread.currentThread().getName()+"】此次一共发送了"+mLocalEvents.get().size()+"个事件");
        //分配要发布的事件
//        mDispatcher.dispatchEvents(new EventType(event.getClass(), tag),event);
        mDispatcher.deliveryEvent(new EventType(event.getClass(), tag), event);
    }

    /**
     * 发布Sticky事件,tag为EventType.DEFAULT_TAG
     *
     * @param event
     */
    public void postSticky(Object event) {
        postSticky(event, EventType.DEFAULT_TAG);
    }

    /**
     * 发布含有tag的Sticky事件
     *
     * @param event 事件
     * @param tag   事件tag
     */
    public void postSticky(Object event, String tag) {
        if (event == null) {
            Log.e(TAG,this.getClass().getSimpleName()+"The event object is null");
            return;
        }
        EventType eventType = new EventType(event.getClass(), tag);
        eventType.event = event;
        mStickyEvents.add(eventType);
    }

    public void removeStickyEvent(Class<?> eventClass) {
        removeStickyEvent(eventClass, EventType.DEFAULT_TAG);
    }

    /**
     * 移除Sticky事件
     *
     * @param eventClass
     */
    public void removeStickyEvent(Class<?> eventClass, String tag) {
        Iterator<EventType> iterator = mStickyEvents.iterator();
        while (iterator.hasNext()) {
            EventType eventType = iterator.next();
            if (eventType.paramClass.equals(eventClass)
                    && eventType.tag.equals(tag)) {
                iterator.remove();
            }
        }
    }

    public List<EventType> getStickyEvents() {
        return mStickyEvents;
    }

    /**
     * 设置订阅函数匹配策略
     *
     * @param policy 匹配策略
     */
    public void setMatchPolicy(MatchPolicy policy) {
        mDispatcher.mMatchPolicy = policy;
    }

    /**
     * 设置执行在UI线程的事件处理器
     *
     * @param handler
     */
    public void setUIThreadEventHandler(UIorBGThreadEventHandler handler) {
        mDispatcher.mUIThreadEventHandler = handler;
    }

    /**
     * 设置执行在post线程的事件处理器
     *
     * @param handler
     */
    public void setPostThreadHandler(EventHandler handler) {
        mDispatcher.mPostThreadHandler = handler;
    }

    /**
     * 设置执行在异步线程的事件处理器
     *
     * @param handler
     */
    public void setAsyncEventHandler(UIorBGThreadEventHandler handler) {
        mDispatcher.mBGThreadEventHandler = handler;
    }

    /**
     * 返回订阅map
     *
     * @return
     */
    public Map<EventType, CopyOnWriteArrayList<Subscription>> getSubscriberMap() {
        return mSubcriberMap;
    }

    /**
     * 获取等待处理的事件队列
     *
     * @return
     */
//    public Queue<EventType> getEventQueue() {
//        return mLocalEvents.get();
//    }

    /**
     * clear the events and subcribers map
     */
    public synchronized void clear() {
//        mLocalEvents.get().clear();
        mSubcriberMap.clear();
    }

    /**
     * get the descriptor of EventBus
     *
     * @return the descriptor of EventBus
     */
    public String getDescriptor() {
        return mDesc;
    }

    public EventDispatcher getDispatcher() {
        return mDispatcher;
    }
    
    /**
     * 更新配置
     * 是否遍历订阅对象的父类的方法
     *
     * @param isHunterSuperInRegister 在注册订阅对象时，是否遍历订阅对象对应的父类的方法
     *                                true表示需要遍历，否则不需要
     */
    public void initConfig(boolean isHunterSuperInRegister) {
        initConfig(null,false);
    }

    /**
     * 更新配置
     * 更新事件的合成策略
     *
     * @param updateMatchPolicy    事件的合成策略
     * @param isHunterSuperInRegister 在注册订阅对象时，是否遍历订阅对象对应的父类的方法
     *                                true表示需要遍历，否则不需要
     */
    public void initConfig(MatchPolicy updateMatchPolicy, boolean isHunterSuperInRegister) {
        if (mMethodHunter != null) {
            mMethodHunter.setHunterSuper(isHunterSuperInRegister);
        }
        if(updateMatchPolicy!=null){
            initConfig(updateMatchPolicy);
        }
    }

    /**
     * 更新配置
     * 更新事件的合成策略
     *
     * @param updateMatchPolicy    事件的合成策略
     */
    public void initConfig(MatchPolicy updateMatchPolicy) {
        if (updateMatchPolicy != null&&mDispatcher!=null) {
            mDispatcher.mMatchPolicy = updateMatchPolicy;
            if(mDispatcher.mCacheEventTypes!=null&&mDispatcher.mCacheEventTypes.size()>0){
                mDispatcher.mCacheEventTypes.clear();//清空缓存
            }
        }
    }

    /**
     * 事件分发器
     *
     * @author mrsimple
     */
    private class EventDispatcher {

        /**
         * 将接收方法执行在UI线程
         */
        UIorBGThreadEventHandler mUIThreadEventHandler = UIorBGThreadEventHandler.getUIThreadEventHandler();

        /**
         * 哪个线程执行的post,接收方法就执行在哪个线程
         */
        EventHandler mPostThreadHandler = new DefaultEventHandler();

        /**
         * 异步线程中执行订阅方法
         */
        UIorBGThreadEventHandler mBGThreadEventHandler = UIorBGThreadEventHandler.getBGThreadEventHandler();
        /**
         * 订阅方法由每一个单独的异步线程执行
         */
        EventHandler mAsyncEventHandler = new AsyncEventHandler();

        /**
         * 缓存一个事件类型对应的可EventType列表
         */
        private Map<EventType, List<EventType>> mCacheEventTypes = new ConcurrentHashMap<EventType, List<EventType>>();
        /**
         * 事件匹配策略,根据策略来查找对应的EventType集合
         */
//        MatchPolicy mMatchPolicy = new DefaultMatchPolicy();
        MatchPolicy mMatchPolicy = new SimpleMatchPolicy();
        /**
         * @param aEvent
         */
//        void dispatchEvents(EventType eventType,Object aEvent) {
//            Queue<EventType> eventsQueue = mLocalEvents.get();
//            Log.e("test","线程【"+Thread.currentThread().getName()+"】一共要分配"+eventsQueue.size()+"个事件");
//            while (eventsQueue.size() > 0) {
//                deliveryEvent(eventsQueue.poll(), aEvent);
//            }
//            deliveryEvent(eventType, aEvent);
//        }

        /**
         * 根据aEvent查找到所有匹配的集合,然后处理事件
         *
         * @param type
         * @param aEvent
         */
        private void deliveryEvent(EventType type, Object aEvent) {
            // 如果有缓存则直接从缓存中取
            List<EventType> eventTypes = getMatchedEventTypes(type, aEvent);
            Log.d(TAG, "事件【" + type.toString() + "】一共合成了" + eventTypes.size() + "个新的事件");
            // 迭代所有匹配的事件并且分发给订阅者
            for (EventType eventType : eventTypes) {
                Log.d(TAG, "合成事件【" + eventType.toString() + "】");
                handleEvent(eventType, aEvent);
            }
        }

        /**
         * 处理单个事件
         *
         * @param eventType
         * @param aEvent
         */
        private void handleEvent(EventType eventType, Object aEvent) {
            CopyOnWriteArrayList<Subscription> subscriptions = mSubcriberMap.get(eventType);
            if (subscriptions == null) {
                return;
            }
            //初始化分配器的相关值
            ((UIorBGThreadEventHandler) mBGThreadEventHandler).setIsGoOnSendEvent(true);
            ((UIorBGThreadEventHandler) mUIThreadEventHandler).setIsGoOnSendEvent(true);

            //遍历引用
            //需要删除的列表，用以自动注销
            ArrayList<Subscription> needDelete = new ArrayList<Subscription>();
            for (Subscription subscription : subscriptions) {
                if (subscription == null
                        || subscription.subscriber.get() == null) {
                    needDelete.add(subscription);
                    continue;
                }
                final ThreadMode mode = subscription.threadMode;
                //根据不同的模式进行不同的处理
                if(!realHandleEvent(mode,subscription,aEvent)) break;
            }
            if (needDelete.size() > 0) {
                subscriptions.removeAll(needDelete);//删除所有订阅对象已被GC回收的订阅者
                mSubcriberMap.put(eventType, subscriptions);
            }
        }

        private boolean realHandleEvent(ThreadMode mode, Subscription subscription,Object aEvent){
            //根据处理结果判断是否需要结束事件处理
            if (mode == ThreadMode.POST
                    ||(mode==ThreadMode.MAIN&& Looper.getMainLooper()==Looper.myLooper())
                    ||(mode==ThreadMode.BACKGROUND&&Looper.getMainLooper()!=Looper.myLooper())) {
                // 处理事件
                mPostThreadHandler.handleEvent(subscription, aEvent);
                if (!subscription.isGoOn) return false;//停止事件处理
            } else if (mode == ThreadMode.MAIN || mode == ThreadMode.BACKGROUND) {
                // 处理事件
                boolean isGoOn;
                if(mode == ThreadMode.MAIN) {
                    mUIThreadEventHandler.handleEvent(subscription,aEvent);
                    isGoOn = mUIThreadEventHandler.isGoOnSendEvent();
                }else  {
                    mBGThreadEventHandler.handleEvent(subscription, aEvent);
                    isGoOn = mBGThreadEventHandler.isGoOnSendEvent();
                }
                if (!isGoOn) return false;//停止事件处理
                Log.d(TAG,"执行方法:"+subscription.targetMethod.toString()+"【"+isGoOn+"】");
            } else{
                // 处理事件
                mAsyncEventHandler.handleEvent(subscription, aEvent);
            }
            return true;
        }

        private List<EventType> getMatchedEventTypes(EventType type, Object aEvent) {
            List<EventType> eventTypes = null;
            // 如果有缓存则直接从缓存中取
            if (mCacheEventTypes.containsKey(type)) {
                eventTypes = mCacheEventTypes.get(type);
            } else {
                eventTypes = mMatchPolicy.findMatchEventTypes(type, aEvent);
                mCacheEventTypes.put(type, eventTypes);
            }

            return eventTypes != null ? eventTypes : new ArrayList<EventType>();
        }

        void dispatchStickyEvents(Object subscriber) {
            for (EventType eventType : mStickyEvents) {
                handleStickyEvent(eventType, subscriber);
            }
        }

        /**
         * 处理单个Sticky事件
         *
         * @param eventType  遍历粘贴事件列表中的某一个粘贴事件
         * @param subscriber 订阅对象（已注册）
         */
        private void handleStickyEvent(EventType eventType, Object subscriber) {
            List<EventType> eventTypes = getMatchedEventTypes(eventType, eventType.event);
            // 事件
            Object event = eventType.event;
            for (EventType foundEventType : eventTypes) {
                Log.d(TAG, "### 找到的类型 : " + foundEventType.paramClass.getSimpleName()+ ", event class : " + event.getClass().getSimpleName());
                final List<Subscription> subscriptions = mSubcriberMap.get(foundEventType);
                if (subscriptions == null) {
                    continue;
                }
                for (Subscription subItem : subscriptions) {
                    final ThreadMode mode = subItem.threadMode;
                    // 如果订阅者为空,那么该sticky事件分发给所有订阅者.否则只分发给该订阅者
                    //订阅者的订阅对象等于注册对象或者注册对象为空
                    if (isTarget(subItem, subscriber)) {
                        //根据不同的模式进行不同的处理
                        if(!realHandleEvent(mode,subItem,event)) break;
                    }
                }
            }
        }

        /**
         * 如果传递进来的订阅者不为空,那么该Sticky事件只传递给该订阅者(注册时),否则所有订阅者都传递(发布时).
         *
         * @param item
         * @param subscriber
         * @return
         */
        private boolean isTarget(Subscription item, Object subscriber) {
            Object cacheObject = item.subscriber != null ? item.subscriber.get() : null;
            return subscriber == null || (subscriber != null
                    && cacheObject != null && cacheObject.equals(subscriber));
        }
    } // end of EventDispatcher

}