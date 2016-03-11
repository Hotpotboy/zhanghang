package com.android.volley.toolbox;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hangzhang209526 on 2016/3/7.
 */
public class UploadRequest extends Request<String> {
    private final String boundaryValue = "-----------------------------7db372eb000e2";
    private final String endValue = "\r\n";
    private BaseListener mListener;
    /**需要上传的文件*/
    private File mFile;
    private HashMap<String, String> mParams;

    /**
     *
     * @param url      上传路径
     * @param listener 监听器
     * @param file     上传的文件
     */
    public UploadRequest(String url, BaseListener listener,File file,HashMap<String, String> params) {
        super(Method.POST, url, listener);
        mListener = listener;
        mFile = file;
        mParams = params;
    }

    @Override
    public String getBodyContentType(){
        String result = "multipart/form-data;boundary="+boundaryValue+endValue;
        return result;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        StringBuilder sb_start = new StringBuilder();

        if(mParams!=null && !mParams.isEmpty()) {
            for (Map.Entry<String, String> entry : mParams.entrySet()) {
                sb_start.append("--" + boundaryValue+endValue);
                sb_start.append("Content-Disposition: form-data; name=\"" + entry.getKey()).append("\"").append(endValue).append(endValue);
                sb_start.append(entry.getValue());
                sb_start.append(endValue);
            }
        }

        sb_start.append("--" + boundaryValue + endValue);
        sb_start.append("Content-Disposition: form-data; name=\"file\";filename=\"image.jpg\"" + endValue);
        sb_start.append("Content-Type: image/jpeg" + endValue);
        sb_start.append(endValue);

        int size = (int) mFile.length();
        byte[] filedatabtye = new byte[size];
        try {
            new FileInputStream(mFile).read(filedatabtye, 0, size);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder sb_end = new StringBuilder();
        sb_end.append(endValue);
        sb_end.append("--" + boundaryValue+"--"+endValue);
        return mergeByteArray(mergeByteArray(sb_start.toString().getBytes(), filedatabtye), sb_end.toString()
                .getBytes());
    }

    private byte[] mergeByteArray(byte[] one,byte[] two){
        byte[] arrays = new byte[one.length+two.length];
        System.arraycopy(one,0,arrays,0,one.length);
        System.arraycopy(two,0,arrays,one.length,two.length);
        return arrays;
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
