package com.sohu.focus.chat.data.message;

/**
 * Created by hangzhang209526 on 2016/3/8.
 */
public class TextMessageData extends MessageData {
    /**消息内容*/
    private TextContent content;

    public TextContent getContent() {
        return content;
    }

    public void setContent(TextContent content) {
        this.content = content;
    }
}
