package com.souhu.hangzhang209526.zhanghang.utils;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;

/**
 * Created by hangzhang209526 on 2016/3/4.
 */
public class CameraUtils {
    public static final int CAMERA_REQUEST_CODE = 1;
    //调用系统相机
    public static void startSystemCamera(Activity activity){
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        activity.startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }
}
