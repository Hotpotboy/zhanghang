package com.sohu.focus.chat;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BaseListener;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sohu.focus.chat.data.user.UserData;
import com.sohu.focus.chat.data.user.UserResponseData;
import com.souhu.hangzhang209526.zhanghang.utils.CallBack;
import com.souhu.hangzhang209526.zhanghang.utils.SystemUtils;
import com.souhu.hangzhang209526.zhanghang.utils.VolleyUtils;
import com.souhu.hangzhang209526.zhanghang.utils.cache.ImageCacheImpl;

import java.io.File;
import java.net.URLEncoder;
import java.util.Date;

/**
 * Created by hangzhang209526 on 2016/1/29.
 */
public class Const {
    public static long currentId = 2;
    public static int otherId = 1;
    /***********************Intent保存值的key**************************************************************/
    /**朋友Id在Intent之中的key值*/
    public static final String INTENT_KEY_USER_ID = "intent_key_user_id";
    /**session Id在Intent之中的key值*/
    public static final String INTENT_KEY_SESSION_ID = "intent_key_session_id";
    /**用户数据在intent之中的key值*/
    public static final String INTENT_KEY_USER_INFO = "intent_key_user_info";
    /**是否是好友在intent之中的key值*/
    public static final String INTENT_KEY_IS_FRIEND = "itent_key_is_friend";
    /***********************常用字符集**************************************************************/
    /**数据库名字*/
    public static final String DB_NAME = "im_chat.db";
    /**心跳包的字符串*/
    public static final String HEARTE_MSG_STR = "{\"type\":11}";
    /***********************接口地址**************************************************************/
    /**服务端地址*/
    private static final String HTTP_HOST = "http://iim.focustest.cn";
    /**ws地址*/
    private static final String WS_HOST = "ws://iim.focustest.cn";
    /**获取好友url*/
    public static final String URL_GET_FRIENDS = HTTP_HOST + File.separator + "friend/list";
    /**获取会话ID url*/
    public static final String URL_GET_SESSION_ID = HTTP_HOST + File.separator + "chat/session";
    /**上传图片url*/
    public static final String URL_UPLOAD_IMAGE = HTTP_HOST + File.separator + "image/upload";
    /**上传位置信息*/
    public static final String URL_SEND_LOCATION = HTTP_HOST + File.separator + "config/start";
    /**获取个人信息*/
    public static final String URL_GET_USER_INFO = HTTP_HOST + File.separator + "user/info";
    /**生成二维码*/
    public static final String URL_GET_QR_CODE = HTTP_HOST + File.separator + "user/qrcode";
    /**添加好友接口*/
    public static final String URL_GET_ADD_FRIEND = HTTP_HOST + File.separator + "friend/add";
    /**创建web socket*/
    public static final String WS_CREATE_WEB_SOCKET = WS_HOST + File.separator + "ws/connect";


   /**************************EventBus的TAG常量***********************************************************************/
   /**添加朋友点击事件的tag，其对应方法的入参为long类型，含义为添加朋友的UID*/
    public static final String EVENT_BUS_TAG_ADD_FRIEND = "com.sohu.focus.chat.MainActivity.addFriendFromScanQRCode";
    /**生成指定用户的二维码，<br>
     * 其对应方法的入参是一个Object数组类型，<br>
     * 该数组第一个类型为long类型，含义为需要生成二维码的用户的ID<br>
     * 该数组第二个类型为NetworkImageView类型，含义为展示此二维码图片的NetworkImageView*/
    public static final String EVENT_BUS_TAG_GENERATE_QR_CODE = "com.sohu.focus.chat.MainActivity.generateQRCode";
    /**刷新指定用户的好友列表
     * 其对应方法的入参类型为long类型，含义为指定用户的ID*/
    public static final String EVENT_BUS_TAG_GET_FRIENDS = "com.sohu.focus.chat.fragment.UserFragment.getFriendList";
    /**打开DrawerLayout的抽屉，其入参表示打开左抽屉还是右抽屉*/
    public static final String EVENT_BUS_TAG_OPEN_DRAWER = "com.sohu.focus.chat.MainActivity.openLeftDrawer";

    /************************请求网络的静态方法***************************************************************************/
    /**
     * 获取指定用户的信息
     */
    public static void getUserInfo(long userId, final CallBack<UserData,Void> callBack,boolean isCache) {
        String url = Const.URL_GET_USER_INFO + "?userId=" + userId;
        StringRequest userInfoRequest = new StringRequest(url, new BaseListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(String response) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    UserResponseData userData = objectMapper.readValue(response.toString(), UserResponseData.class);
                    if (userData != null && userData.getErrorCode() == 0) {
                        if(callBack!=null){
                            callBack.run(userData.getData());
                        }
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        userInfoRequest.setShouldCache(isCache);
        VolleyUtils.requestNet(userInfoRequest);
    }
//    /**获取头像*/
//    public static void getHeadImage(final String headImageUrl,final ImageView headImage){
//        Bitmap bitmap = ImageCacheImpl.getInstance(ChatApplication.getInstance()).getBitmap(URLEncoder.encode(headImageUrl));
//        if(bitmap!=null){
//            headImage.setImageBitmap(bitmap);
//            return;
//        }
//        int size = (int) ChatApplication.getInstance().getResources().getDimension(R.dimen.forty_dp);
//        ImageRequest imageRequest = new ImageRequest(headImageUrl, new Response.Listener<Bitmap>() {
//            @Override
//            public void onResponse(Bitmap response) {
//                String encodeUrl = URLEncoder.encode(headImageUrl);
//                ImageCacheImpl.getInstance(ChatApplication.getInstance()).putFile(encodeUrl, response);
//                headImage.setImageBitmap(response);
//            }
//        }, size, size, Bitmap.Config.RGB_565, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//            }
//        });
//        VolleyUtils.requestNet(imageRequest);
//    }
}
