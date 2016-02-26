package com.sohu.focus.hotfixtest;

/**
 * Created by hangzhang209526 on 2016/2/22.
 */
public class InnerClass {
    private String getmsgssss(){
        return BugClass.getBugMsg();
    }
    public String getMsg(){
        return getmsgssss();
    }
}
