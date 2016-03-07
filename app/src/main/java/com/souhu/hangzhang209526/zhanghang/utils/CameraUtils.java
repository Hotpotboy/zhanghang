package com.souhu.hangzhang209526.zhanghang.utils;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;

/**
 * Created by hangzhang209526 on 2016/3/4.
 */
public class CameraUtils {
    public static final int CAMERA_REQUEST_CODE = 1;
    public static final int PICTURES_REQUEST_CODE = 2;
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
        intent.setData(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(intent, PICTURES_REQUEST_CODE);
    }
}
