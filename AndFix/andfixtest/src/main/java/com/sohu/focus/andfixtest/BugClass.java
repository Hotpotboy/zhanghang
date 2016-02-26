package com.sohu.focus.andfixtest;

/**
 * Created by hangzhang209526 on 2016/2/23.
 */
public class BugClass {

    private static String getInfo(){
        return "发现一个bug！";
    }

    public static String getMsg(){
        return getInfo();
    }

}
