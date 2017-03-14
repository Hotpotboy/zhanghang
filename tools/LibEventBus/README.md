EventBus项目介绍:
<p><B>作用和流程概述：</B></p>
<p>此框架在AndroidEventBus的基础上进行了一定的修改和优化，主要以事件驱动模型来降低android中各个组件之间的耦合，简化了它们之间的引用关系。</p>
<p>可以通过以下示意图来进一步介绍此框架：</p>
![image](https://camo.githubusercontent.com/f1e9b092ec5fec24ce604efdc9e394dcd30f233e/687474703a2f2f696d672e626c6f672e6373646e2e6e65742f3230313530343236323233303430373839)
<p>不同的组件可以在任意一个地方（UI线程或者非UI线程之中）发送一个事件给EventBus，EventBus会将此事件发送给其对应的事件接受者（订阅者），每一个事件接受者都会指定一种线程模式，不同的线程模式会采用不同的线程来执行此事件接受者。
换而言之，可以指定UI线程或者非UI线程来处理此事件。<br>具体而言，每一个订阅者表示一个组件中的某个具体方法。
</p>
<p><B>功能：</B></p>
<ul>
<li>普通事件订阅者的注册，包括指定优先级，添加此订阅者所感兴趣的事件，指定此订阅者的线程模式;</li>
<li>普通事件的发送;</li>
<li>粘贴事件的发送;</li>
<li>粘贴事件订阅者的注册，包括指定优先级，添加此订阅者所感兴趣的事件，指定此订阅者的线程模式;</li>
</ul>
<p><B>普通事件使用步骤及例子：</B></p>
<p>发送普通事件的步骤分为三步：第三步发送普通事件；具体的例子如下：</p>
<ol>
<li>
获取EventBus实例;一般而言,EventBus在一个应用中只需要一个实例足够了;所以可以使用以下代码获取EventBus的默认实例：<br><code>EventBus.getDefault();</code>
</li>
<li>
注册对发送事件感兴趣的订阅者;一般而言，注册一个订阅者，就要同时考虑此订阅者的注销；订阅者的注册和注销可以结合android组件的生命周期来进行；例如，在Activity中的onStart方法中注册一个订阅者，那么就需要在其onStop方法中注销一个订阅者，
具体代码如下所示：
<code>
public class MainActivity extends Activity {
    /**订阅对象*/
    private TestSubscriber subscriber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ......
        //实例化订阅对象
        subscriber = new TestSubscriber(this, handler);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(subscriber);//注册订阅对象
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(subscriber);//注销订阅对象

    }
}
</code>
通过EventBus；注册订阅对象的前提是，此订阅对象中已经定义好了相关的事件处理方法（此方法才是订阅者），以上述代码中的TestSubscriber类为例子，其相关事件处理方法的代码如下：
<code>
public class TestSubscriber{

    @Subscriber(tag = "tag1", mode = ThreadMode.MAIN,proirity = 1)
    private boolean onMAINTag(EventPoJo pojo) {
        String msg = "收到事件，事件内容为:" + pojo.getMsg() + "!处理该事件的线程为:【" + Thread.currentThread().getName() + "】线程!";
        sendToast(msg);
        return false;
    }

    @Subscriber(tag = "tag1", mode = ThreadMode.MAIN,proirity = 2)
    private boolean onMAINTag2(EventPoJo pojo) {
        String msg = "收到事件，事件内容为:" + pojo.getMsg() + "!处理该事件的线程为:【" + Thread.currentThread().getName() + "】线程!";
        sendToast(msg);
        return false;
    }

    @Subscriber(tag = "tag2", mode = ThreadMode.BACKGROUND,proirity = 2)
    private boolean onBGTag(EventPoJo pojo) {
        String msg = "收到事件，事件内容为:" + pojo.getMsg() + "!处理该事件的线程为:【" + Thread.currentThread().getName() + "】线程!";
        sendToast(msg);
        return false;
    }

    @Subscriber(tag = "tag3", mode = ThreadMode.POST,proirity = 3)
    private boolean onPOSTTag(EventPoJo pojo) {
        String msg = "收到事件，事件内容为:" + pojo.getMsg() + "!处理该事件的线程为:【" + Thread.currentThread().getName() + "】线程!";
        sendToast(msg);
        return false;
    }

    @Subscriber(tag = "tag4", mode = ThreadMode.ASYNC,proirity = 4)
    private boolean onASYNCTag(EventPoJo pojo) {
        String msg = "收到事件，事件内容为:" + pojo.getMsg() + "!处理该事件的线程为:【" + Thread.currentThread().getName() + "】线程!";
        sendToast(msg);
        return false;
    }
    
    private void sendToast(String msg){
        ......
    }

}
</code>
在此订阅对象中一共定义了5个事件处理方法；也就是说，注册该订阅对象，就等同于注册了5个订阅者。5个事件处理方法都会返回一个boolean类型的值，并且其入参都是有且只有一个，同时5个方法都有@Subscriber标注；而5个事件处理方法的
不同点也十分明显，首先就是@Subscriber标注中的属性值不同；接着就是各个方法的方法名不同。下面重点介绍一个@Subscriber标注中的三个属性代表的意义：
<
</li>
</ol>