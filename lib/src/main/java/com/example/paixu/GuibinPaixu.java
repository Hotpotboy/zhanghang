package com.example.paixu;

/**
 * Created by hangzhang209526 on 2016/6/3.
 */
public class GuibinPaixu extends BasePaixu {
    public GuibinPaixu(int[] _valus) {
        super(_valus);
    }

    @Override
    public int[] paixu() {
        int subArrayNum = 1;//子数组的最大长度
        while (subArrayNum<valus.length){
            sort(valus,subArrayNum);
            subArrayNum*=2;
        }
        return valus;
    }

    private void sort(int[] array,int subArrayNum){
        for(int i=0;i<array.length;i+=(subArrayNum*2)){
            int rightLen = (i+subArrayNum*2)<array.length?subArrayNum:array.length-(i+subArrayNum);
            int[] left = new int[subArrayNum];
            int[] right = new int[rightLen];
            System.arraycopy(array,i,left,0,subArrayNum);
            System.arraycopy(array,i+subArrayNum,right,0,rightLen);
            int[] tmp = merga(left, right);
            System.arraycopy(tmp,0,array,i,tmp.length);
        }
    }

    private int[] merga(int[] array1,int[] array2){
        int mergaLen = array1.length+array2.length;//合并长度
        if(mergaLen==array1.length) return array1;
        if(mergaLen==array2.length) return array2;
        int[] result = new int[mergaLen];
        int oneStart = 0,twoStart = 0;
        for(int i=0;i<mergaLen;i++){
            if(oneStart<array1.length){
                int oneValue = array1[oneStart];
                if(twoStart<array2.length){
                    int twoValue = array2[twoStart];
                    if(oneValue<twoValue){
                        result[i] = oneValue;
                        oneStart++;
                    }else{
                        result[i] = twoValue;
                        twoStart++;
                    }
                }else{//第二个数组遍历完了
                    result[i] = oneValue;
                    oneStart++;
                }
            }else if(twoStart<array2.length){//第一个数组遍历完了
                int twoValue = array2[twoStart];
                result[i] = twoValue;
                twoStart++;
            }

        }
        return result;
    }
}
