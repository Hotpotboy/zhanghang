package com.example.paixu;

/**
 * Created by hangzhang209526 on 2016/6/2.
 */
public abstract class BasePaixu {
    protected int[] valus;
    public BasePaixu(int[] _valus){
        valus = _valus;
    }

    /**
     * 交互两个指定索引的值
     * @param array
     * @param first   较小索引
     * @param end     较大索引
     */
    protected void swap(int[] array,int first,int end){
        if(array[end]<array[first]){
            int tmp = array[end];
            array[end] = array[first];
            array[first] = tmp;
        }
    }

    public abstract int[] paixu();
}
