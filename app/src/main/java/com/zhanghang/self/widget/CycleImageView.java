package com.zhanghang.self.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;
import com.zhanghang.self.utils.SystemUtils;

/**
 * Created by hangzhang209526 on 2016/1/29.
 */
public class CycleImageView extends NetworkImageView {
    /**圆角半径*/
    private float mRadio;
    /**外围边框的宽度*/
    private float mBorderWidth = 0*getResources().getDisplayMetrics().density;
    /**头像边框画笔*/
    private Paint mStrokePaint;
    /**头像画笔*/
    private Paint mPaint;
    /**屏幕密度*/
    private int mDensity;
    public CycleImageView(Context context) {
        super(context);
        initView();
    }

    public CycleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView(){
        setScaleType(ScaleType.CENTER_CROP);
        //初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//设置边缘光滑，去掉锯齿
        mStrokePaint = new Paint();
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(mBorderWidth);
        mStrokePaint.setColor(Color.RED);

        mDensity = getResources().getDisplayMetrics().densityDpi;
    }

    @Override
    public void onDraw(Canvas canvas){
        Drawable imageDrawable = getDrawable();
        if (imageDrawable == null) {
            return; // couldn't resolve the URI
        }
        Bitmap bitmap = SystemUtils.getBitmapFromDrawable(imageDrawable);
        if(bitmap==null) return;
        int width =  getMeasuredWidth();
        int height = getMeasuredHeight();
        if (width == 0 || height == 0) {
            return;     // nothing to draw (empty bounds)
        }
        mRadio = width>=height?width/2:height/2;
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        shader.setLocalMatrix(getShaderMatrix(bitmap, (mRadio-mBorderWidth) * 2, (mRadio-mBorderWidth) * 2));
        mPaint.setShader(shader);
        canvas.drawCircle(mRadio,mRadio,mRadio-mBorderWidth,mPaint);
        //绘制边框
        canvas.drawCircle(mRadio,mRadio,mRadio-mBorderWidth,mStrokePaint);
    }

    private Matrix getShaderMatrix(Bitmap bitmap,float realWidth,float realHeight) {
        float scale;
        Matrix shaderMatrix = new Matrix();

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        if (bitmapWidth * realHeight> realWidth * bitmapHeight) {
            scale = realHeight / (float) bitmapHeight;
        } else {
            scale = realWidth / (float) bitmapWidth;
        }

        shaderMatrix.setScale(scale, scale);

        return shaderMatrix;
    }
}
