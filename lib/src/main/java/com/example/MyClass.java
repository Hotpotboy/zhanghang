package com.example;

import com.example.paixu.charupaixu.XierPaixu;
import com.example.paixu.charupaixu.ZhijiePaixu;
import com.example.paixu.xuanzepaixu.JiandanXuanzePaixu;

public class MyClass {
    public static void main(String[] arrays){
        int[] valus = {13,7,5,18,14,6,9};

        //直接排序
        ZhijiePaixu zhijiePaixu = new ZhijiePaixu(valus);
        //希尔排序
        int[] valus1 = {13,7,5,18,14,6,9};
        XierPaixu xierPaixu = new XierPaixu(valus1);
        //简单选择排序
        int[] valus2 = {13,37,5,18,14,6,9};
        JiandanXuanzePaixu jiandanXuanzePaixu = new JiandanXuanzePaixu(valus2);
        System.out.println("    直接排序:"+getIntArrayString(zhijiePaixu.paixu()));
        System.out.println("    希尔排序:"+getIntArrayString(xierPaixu.paixu()));
        System.out.println("简单选择排序:"+getIntArrayString(jiandanXuanzePaixu.paixu()));
    }

    private static String getIntArrayString(int[] array){
        String result="";
        for(int item:array){
            result+=item+",";
        }
        return result;
    }
}