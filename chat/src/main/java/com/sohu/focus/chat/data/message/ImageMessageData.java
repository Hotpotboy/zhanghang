package com.sohu.focus.chat.data.message;

/**
 * Created by hangzhang209526 on 2016/1/29.
 */
public class ImageMessageData extends MessageData {

    /**消息内容*/
    private ImageContent content;

    public ImageContent getContent() {
        return content;
    }

    public void setContent(ImageContent content) {
        this.content = content;
    }
}
