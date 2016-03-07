package com.sohu.focus.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.BaseListener;
import com.android.volley.toolbox.UploadRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sohu.focus.chat.adapter.ChatAdapter;
import com.sohu.focus.chat.data.Content;
import com.sohu.focus.chat.data.MessageData;
import com.sohu.focus.chat.data.MessageType;
import com.sohu.focus.chat.data.StringResponseData;
import com.sohu.focus.chat.db.MessageTabeHelper;
import com.sohu.focus.eventbus.EventBus;
import com.sohu.focus.eventbus.subscribe.Subscriber;
import com.sohu.focus.eventbus.subscribe.ThreadMode;
import com.souhu.hangzhang209526.zhanghang.db.BaseSQLiteHelper;
import com.souhu.hangzhang209526.zhanghang.utils.CameraUtils;
import com.souhu.hangzhang209526.zhanghang.utils.DefaultWebSocketUtils;
import com.souhu.hangzhang209526.zhanghang.utils.VolleyUtils;
import com.souhu.hangzhang209526.zhanghang.utils.cache.ImageCacheImpl;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/2/26.
 */
public class ChatActivity extends Activity implements View.OnClickListener,AdapterView.OnItemClickListener,ViewTreeObserver.OnPreDrawListener,ViewTreeObserver.OnGlobalLayoutListener {
    /**当前屏幕可滑动的距离*/
    private int mCanMovingHeight;
    /**是否收起更多按钮*/
    private boolean isNoShowMoreButton = true;
    /**滚动器*/
    private Scroller mScroller;
    /**
     * 对话者的ID
     */
    private int otherId;
    /**
     * 会话者的Id
     */
    private long sessionId;
    /**总体布局*/
    private RelativeLayout mChatRelativelayout;
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
    /**更多按钮*/
    private Button mMoreButton;
    /**拍照按钮*/
    private Button mCameraButton;
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
    /**图片缓存类*/
    private ImageCacheImpl mImageCache;
    private ObjectMapper mObjectMapper = new ObjectMapper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        otherId = getIntent().getIntExtra(Const.INTENT_KEY_OTHER_ID, -1);
        sessionId = getIntent().getLongExtra(Const.INTENT_KEY_SESSION_ID, -1);

        mChatRelativelayout = (RelativeLayout)findViewById(R.id.chat_all_layout);
        mChatList = (ListView) findViewById(R.id.chat_list);
        mChatMsg = (EditText) findViewById(R.id.chat_msg);
        mSendButton = (Button) findViewById(R.id.chat_send_button);
        mMoreButton = (Button) findViewById(R.id.chat_more_button);
        mCameraButton = (Button) findViewById(R.id.chat_carmera_button);
        mMessageTableHelper = MessageTabeHelper.getInstance(this);//获取数据库帮助类
        getChatRecordFromDB();
        //初始化图片缓存类
        mImageCache = new ImageCacheImpl(this);
        mChatAdapter = new ChatAdapter(this, mDatas,mImageCache);

        mChatList.setAdapter(mChatAdapter);

        mSendButton.setOnClickListener(this);
        mMoreButton.setOnClickListener(this);
        mCameraButton.setOnClickListener(this);
        mChatList.setOnItemClickListener(this);
        mChatRelativelayout.getViewTreeObserver().addOnPreDrawListener(this);
        mChatRelativelayout.getViewTreeObserver().addOnGlobalLayoutListener(this);
        //初始化滚动器
        mScroller = new Scroller(this);
        mCanMovingHeight = (int) getResources().getDimension(R.dimen.button_height)*2;
    }

    /**
     * 从数据库中获取聊天记录
     */
    private void getChatRecordFromDB() {
        String whereCase = "(" + mMessageTableHelper.getComlueInfos()[3].getName() + "=? AND " + mMessageTableHelper.getComlueInfos()[8].getName() + "=?)";
        whereCase += " OR " + "(" + mMessageTableHelper.getComlueInfos()[3].getName() + "=? AND " + mMessageTableHelper.getComlueInfos()[8].getName() + "=?)";
        String[] whereArgs = {Const.currentId + "", otherId + "", otherId + "", Const.currentId + ""};
        String orderBy = mMessageTableHelper.getComlueInfos()[5].getName();
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
            } else if (messageData.getType() == MessageType.TEXT_MESSAGE.id()
                    ||messageData.getType() == MessageType.IMAGE_MESSAGE.id()) {//如果收到普通的文本/图片消息
                orgData = messageData;
                orgData.setId(BaseSQLiteHelper.getId());
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
                mMessageTableHelper.insertData(orgData);//新增数据库
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
            case R.id.chat_send_button://点击“发送”按钮
                Editable content = mChatMsg.getText();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(this, "不能发送空消息!", Toast.LENGTH_SHORT).show();
                    return;
                }
                mChatMsg.setText("");
                //隐藏输入法
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(mChatMsg.getWindowToken(), 0);
                try {
                    MessageData msgData = getMessageData(content.toString(),MessageType.TEXT_MESSAGE.id());
                    sendMsgToWebSocket(v.getHandler(),msgData);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "消息发送失败" + e.toString(), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.chat_more_button://点击“更多”按钮
                if(!mScroller.isFinished()) mScroller.abortAnimation();//停止动画
                scrollScreen();
                break;
            case R.id.chat_carmera_button://点击“拍照”按钮
                CameraUtils.startSystemCamera(this);
                break;
        }
    }

    /**
     * 发送消息到IM
     * @param handler
     * @param msgData
     * @throws Exception
     */
    private void sendMsgToWebSocket(Handler handler,MessageData msgData) throws Exception {
        //检查消息是否发送失败
        handler.postDelayed(new CheckMessageSendFailed(msgData), CheckMessageSendFailed.DELAYED_TIME);
        ChatApplication.getInstance().getDefaultWebSocketUtils().sendMessage(mObjectMapper.writeValueAsString(msgData));
    }

    /**
     * 滑动屏幕
     */
    private void scrollScreen(){
        if(isNoShowMoreButton){//显示更多按钮
            isNoShowMoreButton = false;
            mScroller.startScroll(0,0,0, mCanMovingHeight);
        }else{//隐藏更多按钮
            isNoShowMoreButton = true;
            mScroller.startScroll(0, mCanMovingHeight,0,-mCanMovingHeight);
        }
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==this.RESULT_OK){
            switch (requestCode){
                case CameraUtils.CAMERA_REQUEST_CODE://从相机页面返回的结果
                     Bundle bundle = data.getExtras();
                    //获取相机返回的数据，并转换为图片格式
                    Bitmap bitmap = (Bitmap)bundle.get("data");
                    sendImage(bitmap);
                    break;
                case CameraUtils.PICTURES_REQUEST_CODE://从相册返回结果
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();
                    Bitmap bitmap1 = BitmapFactory.decodeFile(picturePath);
                    sendImage(bitmap1);
                    break;
            }
        }
    }

    /**
     * 发送图片
     */
    private void sendImage(final Bitmap bitmap){
        //保存到文件系统之中
        String fileName = SystemClock.elapsedRealtime()+"";
        final File bitmapFile = mImageCache.putFile(fileName,bitmap);
        //发送图片
        VolleyUtils.requestNet(new UploadRequest(Const.URL_UPLOAD_IMAGE, new BaseListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(Object response) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    StringResponseData data = objectMapper.readValue(response.toString(),StringResponseData.class);
                    String imageUrl = data.getData();//获取图片的路径
                    imageUrl = URLEncoder.encode(imageUrl,"UTF-8");
                    bitmapFile.delete();//删除临时文件
                    //更新缓存
                    mImageCache.putBitmap(imageUrl,bitmap,true);
                    MessageData imageMessageData = getMessageData(imageUrl, MessageType.IMAGE_MESSAGE.id());
                    sendMsgToWebSocket(mCameraButton.getHandler(),imageMessageData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, bitmapFile));
    }

    /**
     * 根据消息内容，生成一个消息数据对象，并将其保存在数据库中
     * @param msgContent
     * @param type       生成消息的类型
     * @return
     * @throws Exception
     */
    private MessageData getMessageData(String msgContent,int type) throws Exception {
        Content content = new Content();
        content.setContent(msgContent);
        MessageData result = new MessageData();
        result.setId(BaseSQLiteHelper.getId());
        result.setContent(content);
        result.setFrom(Const.currentId);
        result.setTo(otherId);
        result.setSessionId(sessionId);
        long time = System.currentTimeMillis();
        result.setCreateTime(time);
        result.setServerTime(time);
        result.setType(type);
        result.setClientType(3);
        result.setStatue(MessageData.STATUE_SENDING);
        updateDBorListViewIfNeed(result);
        return result;
    }

    @Override
    public boolean onPreDraw() {
        if(mScroller.computeScrollOffset()){//如果滚动没有完成
            int currentScrollY = mScroller.getCurrY();
            mChatRelativelayout.setScrollY(currentScrollY);//更新滚动高度的值
            mChatRelativelayout.requestLayout();
        }
        return true;
    }

    @Override
    public void onGlobalLayout() {
        //整体布局的高度随着滚动的增加而增加
        int currentHeight = mChatRelativelayout.getMeasuredHeight();//整个布局的当前高度
        currentHeight += (mCanMovingHeight-mChatRelativelayout.getScrollY());//显示相关按钮后的高度；
        ViewGroup.LayoutParams params = mChatRelativelayout.getLayoutParams();
        params.height = currentHeight;
        //列表的高度一致不变
        int currentListViewHeight = mChatList.getMeasuredHeight();
        params = mChatList.getLayoutParams();
        params.height = currentListViewHeight;
        mChatRelativelayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(!isNoShowMoreButton)
            scrollScreen();
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
