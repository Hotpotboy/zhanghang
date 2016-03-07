package com.android.volley.toolbox;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by hangzhang209526 on 2016/3/7.
 */
public class UploadRequest extends Request<String> {
    private final String boundaryValue = "-----------------------------7db372eb000e2";
    private final String endValue = "\r\n";
    private BaseListener mListener;
    /**需要上传的文件*/
    private File mFile;

    /**
     *
     * @param url      上传路径
     * @param listener 监听器
     * @param file     上传的文件
     */
    public UploadRequest(String url, BaseListener listener,File file) {
        super(Method.POST, url, listener);
        mListener = listener;
        mFile = file;
    }

    @Override
    public String getBodyContentType(){
        String result = "Content-Type:multipart/form-data;boundary="+boundaryValue+endValue;
        try {
            result += "Content-Length:"+getBody().length;
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }
        return result;
    }

    @Override
    public byte[] getBody() throws AuthFailureError{
        StringBuffer bodyStrBuff = new StringBuffer();
        bodyStrBuff.append("Content-Disposition: form-data;");
        bodyStrBuff.append("name=\"file\";");
        bodyStrBuff.append("filename=\"image.png\"").append(endValue);
        bodyStrBuff.append("Content-Type:image/png"+endValue);
        int fileSize = (int) mFile.length();
        byte[] fileContent = new byte[fileSize];
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(mFile);
            fileOutputStream.write(fileContent,0,fileSize);
            String contentStr = new String(fileContent,"UTF-8");
            bodyStrBuff.append(contentStr).append(endValue);
            bodyStrBuff.append(boundaryValue).append("--").append(endValue);
            return bodyStrBuff.toString().getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}
