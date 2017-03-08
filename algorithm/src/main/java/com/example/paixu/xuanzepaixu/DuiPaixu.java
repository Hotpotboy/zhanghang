package com.example.paixu.xuanzepaixu;

import com.example.paixu.BasePaixu;

/**
 * Created by hangzhang209526 on 2016/6/2.
 * 堆排序
 */
public class DuiPaixu extends BasePaixu {
    public DuiPaixu(int[] _valus) {
        super(_valus);
    }

    @Override
    public int[] paixu() {
       for(int i=0;i<valus.length;i++){
           sortedByStack(valus,i);
       }
        return valus;
    }

    /**
     * 以堆的方式进行排序
     * @param array
     * @param start   开始位置
     */
    private void sortedByStack(int[] array,int start){
        if(start==array.length-1) return;
        for(int i=array.length-1;i>=start;i--){
            int value = array[i];
            int rootValue = array[(start+i-1)/2];//父节点
            if(value<rootValue){//如果子节点小于父节点
                swap(array,(start+i-1)/2,i);
            }
        }
    }
}
