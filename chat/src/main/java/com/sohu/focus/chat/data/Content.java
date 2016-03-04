package com.sohu.focus.chat.data;

/**
 * Created by hangzhang209526 on 2016/2/25.
 */
public class Content extends BaseData {
    private String content;

    public Content(){

    }

    public Content(String _content){
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
