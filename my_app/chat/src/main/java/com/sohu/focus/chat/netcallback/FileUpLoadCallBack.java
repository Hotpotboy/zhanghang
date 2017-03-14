package com.sohu.focus.chat.netcallback;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.BaseListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sohu.focus.chat.data.FileResponseData;
import com.sohu.focus.chat.data.StringResponseData;

import java.io.File;
import java.util.HashMap;

/**
 * Created by hangzhang209526 on 2016/3/25.
 */
public class FileUpLoadCallBack extends BaseListener<String> {
    /**上传图片
     * 其子类型为上传图片的绝对路径
     **/
    public static final int NET_UPLOAD_IMAGE = 9;
    /**
     * 生成上传图片的网络参数
     * @param bimMapFile  上传图片文件
     * @return
     */

    public static Object[] generateQRCodeNetParams(File bimMapFile, boolean isFocusGetDataFromNet){
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("fid","false");
        Object[] result = new Object[4];
        result[0] = NET_UPLOAD_IMAGE;
        result[1] = bimMapFile;
        result[2] = result;
        result[3] = isFocusGetDataFromNet;
        return result;
    }

    /**
     * FileUpLoadCallBack
     * @param type   主类型
     * @param key    子类型
     * @return
     */
    public static FileUpLoadCallBack getFileUpLoadCallBack(int type,String key){
        FileUpLoadCallBack result  = null;
        synchronized (sInstance){
            result = (FileUpLoadCallBack) sInstance.get(type);
            if(result==null){
                result = new FileUpLoadCallBack(type);
                sInstance.put(type,result);
            }
        }
        result.setKey(key);
        return result;
    }

    protected FileUpLoadCallBack(int type) {
        super(type);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            FileResponseData fileResponseData = objectMapper.readValue(response, FileResponseData.class);
            if (fileResponseData != null && fileResponseData.getErrorCode() == 0) {
                String data = fileResponseData.getData().getUrl();
                mDatas.put(mKey,data);
                invokeDatasRefresh();
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
