package com.sohu.focus.chat.data.message;

import android.text.TextUtils;

import com.sohu.focus.chat.data.BaseData;

/**
 * Created by hangzhang209526 on 2016/2/25.
 */
public class TextContent extends BaseData {
    private String content;

    public TextContent(){

    }

    public TextContent(String _content){
            content = _content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString(){
        return content;
    }
}
