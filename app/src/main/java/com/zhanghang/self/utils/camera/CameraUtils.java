package com.zhanghang.self.utils.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.CaptureActivity;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Intents;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.zhanghang.self.base.BaseApplication;
import com.zhanghang.self.base.BaseFragment;
import com.zhanghang.self.base.BaseFragmentActivity;
import com.zhanghang.zhanghang.R;
import com.zhanghang.self.utils.CallBack;

import java.util.Hashtable;

/**
 * Created by hangzhang209526 on 2016/3/4.
 */
public class CameraUtils {
    public static final int CAMERA_REQUEST_CODE = 1;
    public static final int PICTURES_REQUEST_CODE = 2;
    /**进入二维码扫描页面*/
    public static final int SCANNER_QR_CODE_REQUEST_CODE=3;

    private static Context mContext = BaseApplication.getInstance();

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
    public static void scannerQRCode(BaseFragmentActivity activity,BaseFragment fragment){
        Intent intent = new Intent(activity,CaptureActivity.class);
        intent.setAction(Intents.Scan.ACTION);
        intent.putExtra(Intents.Scan.RESULT_DISPLAY_DURATION_MS, -1L);
        activity.startActivityFromFragment(fragment,intent,SCANNER_QR_CODE_REQUEST_CODE);
    }
    /**进入二维码扫描页面*/
    public static void scannerQRCode(Activity activity){
        Intent intent = new Intent(activity,CaptureActivity.class);
        intent.setAction(Intents.Scan.ACTION);
        intent.putExtra(Intents.Scan.RESULT_DISPLAY_DURATION_MS, -1L);
        activity.startActivityForResult(intent,SCANNER_QR_CODE_REQUEST_CODE);
    }

    /**指定二维码图片*/
    public static void scannerQRCodeForSpecialBitmap(final Bitmap bitmap,final CallBack<String,Void> callback){
        AsyncTask<Bitmap,Void,Result> task = new AsyncTask<Bitmap, Void, Result>() {
            @Override
            protected Result doInBackground(Bitmap... params) {
                Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
                hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码
                Bitmap bitmap1 = params[0];
                int width = bitmap1.getWidth();
                int height = bitmap1.getHeight();
                int[] pixs = new int[width*height];
                for(int i=0;i<width*height;i++){
                    pixs[i] = bitmap1.getPixel(i%width,i/width);
                }
                RGBLuminanceSource source = new RGBLuminanceSource(width,height,pixs);
                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                QRCodeReader reader = new QRCodeReader();
                try {
                    return reader.decode(binaryBitmap, hints);
                } catch (NotFoundException e) {
                    e.printStackTrace();
                } catch (ChecksumException e) {
                    e.printStackTrace();
                } catch (com.google.zxing.FormatException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Result result){
                if (result == null) {
                    Toast.makeText(mContext, mContext.getString(R.string.can_not_scan_qrCode_tip_cn), Toast.LENGTH_LONG).show();
                } else {
                    String recode = result.toString();
                    if(callback!=null){
                        callback.run(recode);
                    }
                }
            }
        };
        task.execute(bitmap);
    }
}
