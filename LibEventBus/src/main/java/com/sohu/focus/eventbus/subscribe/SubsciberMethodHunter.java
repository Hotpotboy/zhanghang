/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Umeng, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.sohu.focus.eventbus.subscribe;

import android.util.Log;

import com.sohu.focus.eventbus.EventType;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * the subscriber method hunter, find all of the subscriber's methods which
 * annotated with @Subcriber.
 * 订阅方法获取者，寻找所有订阅对象的方法，这些方法注释了@Subcriber
 * @author mrsimple
 */
public class SubsciberMethodHunter {

    /**
     * the event bus's subscriber's map
     */
    Map<EventType, CopyOnWriteArrayList<Subscription>> mSubcriberMap;
    /**是否遍历父类的相关方法*/
    private boolean isHunterSuper = false;

    public void setHunterSuper(boolean is){
        isHunterSuper = is;
    }

    /**
     * @param subscriberMap
     */
    public SubsciberMethodHunter(Map<EventType, CopyOnWriteArrayList<Subscription>> subscriberMap) {
        mSubcriberMap = subscriberMap;
    }

    /**
     * 查找订阅对象中的所有订阅函数,订阅函数的参数只能有一个.找到订阅函数之后构建Subscription存储到Map中
     * 
     * @param subscriber 订阅对象
     * @return
     */
    public void findSubcribeMethods(Object subscriber) {
        if (mSubcriberMap == null) {
            throw new NullPointerException("the mSubcriberMap is null. ");
        }
        Class<?> clazz = subscriber.getClass();
        // 查找类中符合要求的注册方法,直到Object类
        while (clazz != null && !isSystemCalss(clazz.getName())) {
            final Method[] allMethods = clazz.getDeclaredMethods();
            for (int i = 0; i < allMethods.length; i++) {
                Method method = allMethods[i];
                //判断方法的返回值,如果返回值不是布尔类型，则不是订阅方法
                Class returnType = method.getReturnType();
                if(returnType!=boolean.class&&returnType!=Boolean.class){
                    continue;
                }
                // 根据注解来解析函数
                Subscriber annotation = method.getAnnotation(Subscriber.class);
                if (annotation != null) {
                    // 获取方法参数
                    Class<?>[] paramsTypeClass = method.getParameterTypes();
                    // 订阅函数只支持一个参数
                    if (paramsTypeClass != null && paramsTypeClass.length == 1) {
                        Class<?> paramType = convertType(paramsTypeClass[0]);
                        EventType eventType = new EventType(paramType, annotation.tag());
                        int prority = annotation.proirity();//获取优先级默认为0
                        Subscription newSubscription = new Subscription(subscriber, method,annotation.mode(),eventType,prority);
                        subscibe(newSubscription);
                    }
                }
            } // end for
            if(!isHunterSuper) break;
              // 获取父类,以继续查找父类中符合要求的方法
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * 按照EventType存储订阅者列表,这里的EventType就是事件类型,一个事件对应0到多个订阅者.
     * 
     * @param subscription 订阅者
     */
    private void subscibe(Subscription subscription) {
        CopyOnWriteArrayList<Subscription> subscriptionLists = mSubcriberMap.get(subscription.eventType);
        if (subscriptionLists == null) {
            subscriptionLists = new CopyOnWriteArrayList<Subscription>();
        }

        if (subscriptionLists.contains(subscription)) {
            return;
        }
        int num = subscriptionLists.size();
        if(num==0) subscriptionLists.add(subscription);
        else{
            //根据优先级早到合适的位置
            int i=0;
            for(;i<num;i++){
                Subscription item = subscriptionLists.get(i);
                if(item.priority<=subscription.priority){
                    subscriptionLists.add(i,subscription);
                    break;
                }
            }
            if(i==num) subscriptionLists.add(subscription);
        }
        // 将事件类型key和订阅者信息存储到map中
        mSubcriberMap.put(subscription.eventType, subscriptionLists);
    }

    /**
     * remove subscriber methods from map
     * 
     * @param subscriber
     */
    public void removeMethodsFromMap(Object subscriber) {
        Iterator<CopyOnWriteArrayList<Subscription>> iterator = mSubcriberMap
                .values().iterator();
        while (iterator.hasNext()) {
            CopyOnWriteArrayList<Subscription> subscriptions = iterator.next();
            if (subscriptions != null) {
                List<Subscription> foundSubscriptions = new
                        LinkedList<Subscription>();
                Iterator<Subscription> subIterator = subscriptions.iterator();
                while (subIterator.hasNext()) {
                    Subscription subscription = subIterator.next();
                    // 获取引用
                    Object cacheObject = subscription.subscriber.get();
                    if (isObjectsEqual(cacheObject, subscriber)
                            || cacheObject == null) {
                        Log.d("", "### 移除订阅 " + subscriber.getClass().getName());
                        foundSubscriptions.add(subscription);
                    }
                }

                // 移除该subscriber的相关的Subscription
                subscriptions.removeAll(foundSubscriptions);
            }

            // 如果针对某个Event的订阅者数量为空了,那么需要从map中清除
            if (subscriptions == null || subscriptions.size() == 0) {
                iterator.remove();
            }
        }
    }

    private boolean isObjectsEqual(Object cachedObj, Object subscriber) {
        return cachedObj != null
                && cachedObj.equals(subscriber);
    }

    /**
     * if the subscriber method's type is primitive, convert it to corresponding
     * Object type. for example, int to Integer.
     * 
     * @param eventType origin Event Type
     * @return
     */
    private Class<?> convertType(Class<?> eventType) {
        Class<?> returnClass = eventType;
        if (eventType.equals(boolean.class)) {
            returnClass = Boolean.class;
        } else if (eventType.equals(int.class)) {
            returnClass = Integer.class;
        } else if (eventType.equals(float.class)) {
            returnClass = Float.class;
        } else if (eventType.equals(double.class)) {
            returnClass = Double.class;
        }

        return returnClass;
    }

    private boolean isSystemCalss(String name) {
        return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.");
    }

}
