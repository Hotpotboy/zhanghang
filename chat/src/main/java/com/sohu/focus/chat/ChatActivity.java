package com.sohu.focus.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sohu.focus.chat.adapter.ChatAdapter;
import com.sohu.focus.chat.data.Content;
import com.sohu.focus.chat.data.MessageData;
import com.sohu.focus.chat.data.MessageType;
import com.sohu.focus.chat.db.MessageTabeHelper;
import com.sohu.focus.eventbus.EventBus;
import com.sohu.focus.eventbus.subscribe.Subscriber;
import com.sohu.focus.eventbus.subscribe.ThreadMode;
import com.souhu.hangzhang209526.zhanghang.utils.DefaultWebSocketUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/2/26.
 */
public class ChatActivity extends Activity implements View.OnClickListener {
    /**
     * 对话者的ID
     */
    private int otherId;
    /**
     * 会话者的Id
     */
    private long sessionId;
    /**
     * 聊天记录列表
     */
    private ListView mChatList;
    /**
     * 发送聊天内容
     */
    private EditText mChatMsg;
    /**
     * 发送聊天内容按钮
     */
    private Button mSendButton;
    /**
     * 聊天内容适配器
     */
    private ChatAdapter mChatAdapter;
    /**
     * 聊天内容
     */
    private ArrayList<MessageData> mDatas;
    /**
     * 数据库帮助类
     */
    private MessageTabeHelper mMessageTableHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        otherId = getIntent().getIntExtra(Const.INTENT_KEY_OTHER_ID, -1);
        sessionId = getIntent().getLongExtra(Const.INTENT_KEY_SESSION_ID, -1);
        mChatList = (ListView) findViewById(R.id.chat_list);
        mChatMsg = (EditText) findViewById(R.id.chat_msg);
        mSendButton = (Button) findViewById(R.id.chat_send_button);
        mMessageTableHelper = MessageTabeHelper.getInstance(this);//获取数据库帮助类
        getChatRecordFromDB();
        mChatAdapter = new ChatAdapter(this, mDatas);

        mChatList.setAdapter(mChatAdapter);

        mSendButton.setOnClickListener(this);
    }

    /**
     * 从数据库中获取聊天记录
     */
    private void getChatRecordFromDB() {
        String whereCase = "(" + MessageTabeHelper.comlueInfos[4].getName() + "=?&&" + MessageTabeHelper.comlueInfos[5].getName() + "=?)";
        whereCase += "||" + "(" + MessageTabeHelper.comlueInfos[4].getName() + "=?&&" + MessageTabeHelper.comlueInfos[5].getName() + "=?)";
        String[] whereArgs = {Const.currentId + "", otherId + "", otherId + "", Const.currentId + ""};
        String orderBy = "order by " + MessageTabeHelper.comlueInfos[7];
        try {
            mDatas = mMessageTableHelper.selectDatas(whereCase, whereArgs, null, null, orderBy, MessageData.class);
        } catch (Exception e) {
            Toast.makeText(ChatActivity.this, "获取数据库失败" + e.toString(), Toast.LENGTH_SHORT).show();
            mDatas = new ArrayList<MessageData>();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 接收到消息时的处理方法
     *
     * @param intent
     * @return
     */
    @Subscriber(tag = DefaultWebSocketUtils.TAG_RECEIVE_TEXT, mode = ThreadMode.MAIN)
    public boolean receiveTextMessageBradCast(Intent intent) {
        String msg = intent.getStringExtra(DefaultWebSocketUtils.TAG_RECEIVE_TEXT);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            MessageData messageData = objectMapper.readValue(msg, MessageData.class);
            MessageData orgData = null;
            if (messageData.getType() == MessageType.ACK_MESSAGE.id()) {//如果是系统发送的确认接收消息
                int index = mChatAdapter.getPosistionForObject(messageData);
                if (index >= 0) {
                    orgData = (MessageData) mChatAdapter.getItem(index);
                    orgData.setServerTime(messageData.getServerTime());
                    if (orgData.getStatue() == MessageData.STATUE_SENDING) {
                        orgData.setStatue(MessageData.STATUE_SENDED);
                    }
                }
            } else if (messageData.getType() == MessageType.TEXT_MESSAGE.id()) {//如果收到普通的文本消息
                orgData = messageData;
            }
            updateDBorListViewIfNeed(orgData);//保存和展示满足条件的聊天记录
        } catch (IOException e) {
            Toast.makeText(this, "解析消息失败!" + e.toString(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(ChatActivity.this, "更新数据库失败" + e.toString(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * 按需更新数据库或者列表
     * @param orgData
     */
    private void updateDBorListViewIfNeed(MessageData orgData) throws Exception {
        if(orgData!=null) {
            boolean isNeedAdd = mMessageTableHelper.selectData(orgData.getId(),orgData.getClass())==null;
            if(isNeedAdd) {
                long id = mMessageTableHelper.insertData(orgData);//新增数据库
                orgData.setId(id);
            }else {
                mMessageTableHelper.updateData(orgData, null, null);//更新数据库；
            }
            if(orgData.getSessionId()==sessionId) {//属于当前会话
                if(isNeedAdd){
                    mChatAdapter.addData(orgData);
                }else{
                    mChatAdapter.notifyDataSetChanged();//更新列表
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chat_send_button:
                Editable content = mChatMsg.getText();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(this, "不能发送空消息!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    MessageData msgData = getMessageData(content.toString());
                    ObjectMapper objectMapper = new ObjectMapper();
                    ChatApplication.getInstance().getDefaultWebSocketUtils().sendMessage(objectMapper.writeValueAsString(msgData));
                    //检查消息是否发送失败
                    v.postDelayed(new CheckMessageSendFailed(msgData), CheckMessageSendFailed.DELAYED_TIME);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "消息发送失败" + e.toString(), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private MessageData getMessageData(String msgContent) throws Exception {
        Content content = new Content();
        content.setContent(msgContent);
        MessageData result = new MessageData();
        result.setContent(content);
        result.setFrom(Const.currentId);
        result.setTo(otherId);
        result.setSessionId(sessionId);
        result.setCreateTime(System.currentTimeMillis());
        result.setType(MessageType.TEXT_MESSAGE.id());
        result.setClientType(3);
        result.setStatue(MessageData.STATUE_SENDING);
        updateDBorListViewIfNeed(result);
        return result;
    }

    /**
     * 消息发送后，确认其发送是否成功的过程超时，执行的代码
     * 如果超时，则表示发送失败！
     */
    class CheckMessageSendFailed implements Runnable {
        public static final long DELAYED_TIME = 10 * 1000;
        private MessageData checkMessageData;

        CheckMessageSendFailed(MessageData _checkMessageData) {
            checkMessageData = _checkMessageData;
        }

        @Override
        public void run() {
            if (checkMessageData != null) {
                if (checkMessageData.getStatue() == MessageData.STATUE_SENDING) {
                    checkMessageData.setStatue(MessageData.STATUE_FAILED);
                    try {
                        updateDBorListViewIfNeed(checkMessageData);
                    } catch (Exception e) {
                        Toast.makeText(ChatActivity.this, "更新数据库失败" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}
