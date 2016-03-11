package com.souhu.hangzhang209526.zhanghang.utils.camera;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;

import com.google.zxing.CaptureActivity;
import com.google.zxing.Intents;

/**
 * Created by hangzhang209526 on 2016/3/4.
 */
public class CameraUtils {
    public static final int CAMERA_REQUEST_CODE = 1;
    public static final int PICTURES_REQUEST_CODE = 2;
    /**进入二维码扫描页面*/
    public static final int SCANNER_QR_CODE_REQUEST_CODE=3;
    //调用系统相机
    public static void startSystemCamera(Activity activity){
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        activity.startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }
    //调用系统相册
    public static void startSystemPictures(Activity activity){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(intent, PICTURES_REQUEST_CODE);
    }
    /**进入二维码扫描页面*/
    public static void scannerQRCode(Activity activity){
        Intent intent = new Intent(activity,CaptureActivity.class);
        intent.setAction(Intents.Scan.ACTION);
        intent.putExtra(Intents.Scan.RESULT_DISPLAY_DURATION_MS,-1L);
        activity.startActivityForResult(intent,SCANNER_QR_CODE_REQUEST_CODE);
    }
}
