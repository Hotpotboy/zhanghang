package com.souhu.hangzhang209526.zhanghang;

import android.os.SystemClock;

/**
 * Created by hangzhang209526 on 2015/11/4.
 */
public class Test {



    /**减速速率，此表达式计算后的结果为2.3582017*/
    private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
    /**弯曲率*/
    private static final float INFLEXION = 0.35f; // Tension lines cross at (INFLEXION, 1)
    /**开始的拉力*/
    private static final float START_TENSION = 0.5f;
    /**结束的拉力*/
    private static final float END_TENSION = 1.0f;
    private static final float P1 = START_TENSION * INFLEXION;
    private static final float P2 = 1.0f - END_TENSION * (1.0f - INFLEXION);

    private static final int NB_SAMPLES = 100;//样本点的个数
    private static final float[] SPLINE_POSITION = new float[NB_SAMPLES + 1];//样本点数组
    private static final float[] SPLINE_TIME = new float[NB_SAMPLES + 1];//样本点对应的时间数组


    static {
        float x_min = 0.0f;
        float y_min = 0.0f;
        for (int i = 0; i < NB_SAMPLES; i++) {
            final float alpha = (float) i / NB_SAMPLES;

            float x_max = 1.0f;
            float x, tx, coef;
            while (true) {
                x = x_min + (x_max - x_min) / 2.0f;
                coef = 3.0f * x * (1.0f - x);
                tx = coef * ((1.0f - x) * P1 + x * P2) + x * x * x;
                if (Math.abs(tx - alpha) < 1E-5) break;
                if (tx > alpha) x_max = x;
                else x_min = x;
            }
            SPLINE_POSITION[i] = coef * ((1.0f - x) * START_TENSION + x) + x * x * x;

            float y_max = 1.0f;
            float y, dy;
            while (true) {
                y = y_min + (y_max - y_min) / 2.0f;
                coef = 3.0f * y * (1.0f - y);
                dy = coef * ((1.0f - y) * START_TENSION + y) + y * y * y;
                if (Math.abs(dy - alpha) < 1E-5) break;
                if (dy > alpha) y_max = y;
                else y_min = y;
            }
            SPLINE_TIME[i] = coef * ((1.0f - y) * P1 + y * P2) + y * y * y;
        }
        SPLINE_POSITION[NB_SAMPLES] = SPLINE_TIME[NB_SAMPLES] = 1.0f;
    }


    public static  void main(String[] args){
        float jj = (float) (Math.log(0.78) / Math.log(0.9));
        System.out.println("值："+jj+"");
    }
}
