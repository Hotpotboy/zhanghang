package com.example;

import com.example.paixu.GuibinPaixu;
import com.example.paixu.JisuPaixu;
import com.example.paixu.charupaixu.XierPaixu;
import com.example.paixu.charupaixu.ZhijiePaixu;
import com.example.paixu.jiaohuan.KuaisuPaixu;
import com.example.paixu.jiaohuan.MaoPaoPaixu;
import com.example.paixu.xuanzepaixu.DuiPaixu;
import com.example.paixu.xuanzepaixu.JiandanXuanzePaixu;
import com.example.soucang.ShanchuYuanQuan;

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
        //堆排序
        int[] valus3 = {13,37,25,18,14,6,9,17};
        DuiPaixu duiPaixu = new DuiPaixu(valus3);
        //冒泡排序
        int[] valus4 = {13,37,25,18,14,6,9,17,59};
        MaoPaoPaixu maoPaoPaixu = new MaoPaoPaixu(valus4);
        //快速排序
        int[] valus5 = {13,37,25,18,14,6,9,17,59,30};
        KuaisuPaixu kuaisuPaixu = new KuaisuPaixu(valus5);
        //基数排序
        int[] valus6 = {13,37,25,18,214,6,9,17,59,30,27};
        JisuPaixu jisuPaixu = new JisuPaixu(valus6,3);
        //归并排序
        int[] valus7 = {13,37,25,108,214,6,9,17,59,30,27,55};
//        GuibinPaixu guibinPaixu = new GuibinPaixu(valus7);
//        System.out.println("    直接排序:"+getIntArrayString(zhijiePaixu.paixu()));
//        System.out.println("    希尔排序:"+getIntArrayString(xierPaixu.paixu()));
//        System.out.println("简单选择排序:"+getIntArrayString(jiandanXuanzePaixu.paixu()));
//        System.out.println("      堆排序:"+getIntArrayString(duiPaixu.paixu()));
//        System.out.println("    冒泡排序:"+getIntArrayString(maoPaoPaixu.paixu()));
//        System.out.println("    快速排序:"+getIntArrayString(kuaisuPaixu.paixu()));
//        System.out.println("    基数排序:"+getIntArrayString(jisuPaixu.paixu()));
//        System.out.println("    归并排序:"+getIntArrayString(guibinPaixu.paixu()));
        ITest test = new ShanchuYuanQuan(7,3);
        test.test();
    }

    public static String getIntArrayString(int[] array){
        String result="";
        for(int item:array){
            result+=item+",";
        }
        return result;
    }
}