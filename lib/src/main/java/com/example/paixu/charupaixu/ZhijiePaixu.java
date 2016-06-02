package com.example.paixu.charupaixu;

import com.example.paixu.BasePaixu;

/**
 * Created by hangzhang209526 on 2016/6/2.
 * 直接排序
 */
public class ZhijiePaixu extends BasePaixu {
    /**每隔多少个元素,默认为0*/
    int baseSpace;

    public ZhijiePaixu(int[] _valus) {
        super(_valus);
        baseSpace = 0;
    }

    @Override
    public int[] paixu() {
        return zhijiepaixu(1);
    }

    /**
     *
     * @param start  还未排序的第一个索引
     * @return
     */
    int[] zhijiepaixu(int start){
        int startIndex = start-baseSpace-1;
        for(;start<valus.length;start+= (baseSpace+1)){
            addEleumeToSortedArray(startIndex,start,valus);
        }
        return valus;
    }

    /**
     * 将一个内含排序子集的数组的排序子集扩大1
     * @param start       数组已排序子集中的开始位置
     * @param end         数组已排序子集中的结束位置+（{@link #baseSpace}+1）
     * @param array       数组，内含一个已排序的子集
     * @return
     */
    void addEleumeToSortedArray(int start,int end,int[] array){
        if(end==array.length) return;
        for(;start<=end-(baseSpace+1);start+=(baseSpace+1)){
            swap(array,start,end);
        }
        return;
    }

}
