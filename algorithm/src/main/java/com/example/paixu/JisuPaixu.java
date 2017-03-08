package com.example.paixu;

/**
 * Created by hangzhang209526 on 2016/6/3.
 * 基数排序
 */
public class JisuPaixu extends BasePaixu {
    /**循环次数，数组中最大值的位数*/
    private int num;
    private int[][] baseNumArray;
    public JisuPaixu(int[] _valus,int _num) {
        super(_valus);
        num = _num;
    }

    @Override
    public int[] paixu() {
        int realNum = 1;//实际位数
        do{
            sortedByBaseNum(valus,realNum);
            realNum++;
        }while (realNum<=num);
        return valus;
    }

    private void sortedByBaseNum(int[] array,int baseNum){
        baseNumArray = new int[10][];
        for(int item:array){
            int base = dive(item,baseNum);
            int[] baseArray = baseNumArray[base];
            if(baseArray==null){
                baseArray = new int[1];
                baseArray[0] = item;
                baseNumArray[base] = baseArray;
            }else{
                int[] newArray = new int[baseArray.length+1];
                System.arraycopy(baseArray,0,newArray,0,baseArray.length);
                newArray[baseArray.length] = item;
                baseNumArray[base] = newArray;
            }
        }
        int start = 0;
        for(int i=0;i<10;i++){
            if(baseNumArray[i]!=null&&baseNumArray[i].length>0){
                System.arraycopy(baseNumArray[i],0,array,start,baseNumArray[i].length);
                start+=baseNumArray[i].length;
            }
        }
    }

    /**
     *
     * @param one  值
     * @param num  1表示个位，2表示十位
     * @return
     */
    private int dive(int one,int num){
       int weishu = 1;
        while (num>1){
            weishu*=10;
            num--;
        }
        return (one/weishu)%10;
    }
}
