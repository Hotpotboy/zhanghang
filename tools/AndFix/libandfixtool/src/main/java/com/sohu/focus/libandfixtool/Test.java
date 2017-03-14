package com.sohu.focus.libandfixtool;

/**
 * Created by hangzhang209526 on 2016/3/10.
 */
public class Test extends Thread {
    private boolean istag = false;
    public static void main(String[] args){
        Test test = new Test();
        test.start();
        while (!test.istag){
            System.out.println("循环中……");
        }
        System.out.println("退出");
    }

    @Override
    public void run(){
        try {
            istag = true;
            System.out.println("子线程更改了布尔值");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
