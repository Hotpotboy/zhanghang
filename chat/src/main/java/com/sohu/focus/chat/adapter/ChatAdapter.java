package com.sohu.focus.chat.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.sohu.focus.chat.Const;
import com.sohu.focus.chat.R;
import com.sohu.focus.chat.data.MessageData;
import com.sohu.focus.chat.data.MessageType;
import com.souhu.hangzhang209526.zhanghang.adpter.BaseViewHolderAdapter;
import com.souhu.hangzhang209526.zhanghang.utils.VolleyUtils;
import com.souhu.hangzhang209526.zhanghang.widget.CycleImageView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hangzhang209526 on 2016/2/25.
 */
public class ChatAdapter extends BaseViewHolderAdapter {
    private final String KEY_ITEM_CHAT_OTHER_HEAD_IMG = "item_chat_other_head_img";
    private final String KEY_ITEM_CHAT_OTHER_MSG = "item_chat_other_msg";
    private final String KEY_ITEM_CHAT_OTHER_IMAGE = "item_chat_other_image";
    private final String KEY_ITEM_CHAT_SELF_HEAD_IMG = "item_chat_self_head_img";
    private final String KEY_ITEM_CHAT_SELF_MSG = "item_chat_self_msg";
    private final String KEY_ITEM_CHAT_SELF_PROGRESS = "item_chat_self_progress";
    private final String KEY_ITEM_CHAT_SELF_SEND_FAILD_TIP = "item_chat_send_self_fail_tip";
    private final String KEY_ITEM_CHAT_SELF_IMAGE = "item_chat_self_image";
    private int otherMsgFlag = 0;
    private int selfMsgFlag = 1;
    /**图片加载器*/
    private ImageLoader mImageLoader;
    public ChatAdapter(Context context, ArrayList<MessageData> list,ImageLoader.ImageCache cache) {
        super(context, list);
        mImageLoader = new ImageLoader(VolleyUtils.getRequestQueue(),cache);
    }

    @Override
    public int getItemViewType(int position){
        return ((MessageData)mDatas.get(position)).getFrom()!= Const.currentId?otherMsgFlag:selfMsgFlag;
    }

    @Override
    public int getViewTypeCount(){
        return 2;
    }

    @Override
    protected View inflaterView(int position) {
        if(getItemViewType(position)==otherMsgFlag)
            return mLayoutInflater.inflate(R.layout.item_chat,null);
        else if(getItemViewType(position)==selfMsgFlag)
            return mLayoutInflater.inflate(R.layout.item_chat_two,null);
        else return null;
    }

    @Override
    protected void reBindDataAndView(int position, HashMap<String, View> baseViewHolder, View convertView) {
        MessageData messageData = (MessageData) mDatas.get(position);

        TextView msgTextView = null;
        CycleImageView headImag = null;
        NetworkImageView msgImage;//图片消息
        if(messageData.getFrom()==Const.currentId){
            headImag = (CycleImageView) getViewByTag(R.id.item_chat_self_head_img, KEY_ITEM_CHAT_SELF_HEAD_IMG,baseViewHolder,convertView);
            msgTextView = (TextView) getViewByTag(R.id.item_chat_self_msg, KEY_ITEM_CHAT_SELF_MSG,baseViewHolder,convertView);
            TextView sendFailTextView = (TextView) getViewByTag(R.id.item_chat_self_send_fail_tip,KEY_ITEM_CHAT_SELF_SEND_FAILD_TIP,baseViewHolder,convertView);
            ProgressBar progressBar = (ProgressBar) getViewByTag(R.id.item_chat_self_progress,KEY_ITEM_CHAT_SELF_PROGRESS,baseViewHolder,convertView);
            if(messageData.getStatue()==MessageData.STATUE_SENDING){//正在发送中
                progressBar.setVisibility(View.VISIBLE);
                sendFailTextView.setVisibility(View.GONE);
            }else if(messageData.getStatue()==MessageData.STATUE_FAILED){//发送失败
                progressBar.setVisibility(View.GONE);
                sendFailTextView.setVisibility(View.VISIBLE);
            }else{
                progressBar.setVisibility(View.GONE);
                sendFailTextView.setVisibility(View.GONE);
            }
            msgImage = (NetworkImageView)getViewByTag(R.id.item_chat_self_image,KEY_ITEM_CHAT_SELF_IMAGE,baseViewHolder,convertView);
        }else{
            headImag = (CycleImageView) getViewByTag(R.id.item_chat_other_head_img, KEY_ITEM_CHAT_OTHER_HEAD_IMG,baseViewHolder,convertView);
            msgTextView = (TextView) getViewByTag(R.id.item_chat_other_msg, KEY_ITEM_CHAT_OTHER_MSG,baseViewHolder,convertView);
            msgImage = (NetworkImageView)getViewByTag(R.id.item_chat_other_image,KEY_ITEM_CHAT_OTHER_IMAGE,baseViewHolder,convertView);
        }
        if(messageData.getType()== MessageType.TEXT_MESSAGE.id()) {//文本消息
            msgTextView.setVisibility(View.VISIBLE);
            msgImage.setVisibility(View.GONE);
            msgTextView.setText(messageData.getContent().getContent());
        }else if(messageData.getType()== MessageType.IMAGE_MESSAGE.id()){//图片消息
            msgTextView.setVisibility(View.GONE);
            msgImage.setVisibility(View.VISIBLE);
            msgImage.setDefaultImageResId(R.drawable.default_img);
            msgImage.setImageUrl(messageData.getContent().getContent(),mImageLoader);
        }
    }
}
