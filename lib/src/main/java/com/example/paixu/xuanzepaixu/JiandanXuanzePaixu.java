package com.example.paixu.xuanzepaixu;

import com.example.paixu.BasePaixu;

/**
 * Created by hangzhang209526 on 2016/6/2.
 * 简单选择排序
 */
public class JiandanXuanzePaixu extends BasePaixu {
    public JiandanXuanzePaixu(int[] _valus) {
        super(_valus);
    }

    @Override
    public int[] paixu() {
        int start = 0;
        for(;start<valus.length;start++){
            int smallest=valus[start];//最小值
            int smallestIndex = start;//最小值索引
            for(int i=start+1;i<valus.length;i++){
                if(smallest>valus[i]) {
                    smallest = valus[i];
                    smallestIndex = i;
                }
            }
            if(start!=smallestIndex)
               swap(valus,start,smallestIndex);//交换最小值

        }
        return valus;
    }
}
