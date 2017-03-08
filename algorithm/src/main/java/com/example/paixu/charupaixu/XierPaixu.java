package com.example.paixu.charupaixu;

/**
 * Created by hangzhang209526 on 2016/6/2.
 * 希尔排序
 */
public class XierPaixu extends com.example.paixu.charupaixu.ZhijiePaixu {
    /**增量因子*/
    private int adds = 2;
    public XierPaixu(int[] _valus) {
        super(_valus);
    }

    @Override
    public int[] paixu() {
        baseSpace = valus.length/2;
        while (baseSpace >= 0){
            for(int i=0;i<baseSpace+1;i++){
                zhijiepaixu(i+baseSpace+1);
            }
            if(baseSpace==0){//退出循环
                break;
            }
            baseSpace /=2;
        }
        return valus;
    }
}
