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
     * @param bigIndex   较大值的索引
     * @param smallIndex 较小值的索引
     */
    protected void swap(int[] array,int bigIndex,int smallIndex){
        if(array[smallIndex]<array[bigIndex]){
            int tmp = array[smallIndex];
            array[smallIndex] = array[bigIndex];
            array[bigIndex] = tmp;
        }
    }

    public abstract int[] paixu();
}
