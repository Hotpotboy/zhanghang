package com.sohu.focus.chat;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.BaseListener;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sohu.focus.chat.data.user.UserData;
import com.sohu.focus.chat.data.user.UserResponseData;
import com.sohu.focus.chat.fragment.UserFragment;
import com.sohu.focus.chat.netcallback.StringDataCallBack;
import com.sohu.focus.eventbus.EventBus;
import com.zhanghang.self.base.BaseFragment;
import com.zhanghang.self.utils.CallBack;
import com.zhanghang.self.utils.VolleyUtils;

import java.io.File;

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
    /**用户类型在intent之中的key值*/
    public static final String INTENT_KEY_USER_TYPE = "itent_key_user_type";
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
    public static final String URL_ADD_FRIEND = HTTP_HOST + File.separator + "friend/add";
    /**获取陌生人列表*/
    public static final String URL_GET_STRANGER_LIST = HTTP_HOST + File.separator + "friend/strangerList";
    /**创建web socket*/
    public static final String WS_CREATE_WEB_SOCKET = WS_HOST + File.separator + "ws/connect";


   /**************************EventBus的TAG常量***********************************************************************/
    /**执行上传文件的网络请求*/
    public static final String EVENT_BUS_TAG_UP_LOAD_FILE = "com.sohu.focus.chat.NetRequestInterfaceUtil.upLoadFile";
    /**执行结果是字符串类型的网络请求*/
    public static final String EVENT_BUS_TAG_GET_STRING_DATA = "com.sohu.focus.chat.NetRequestInterfaceUtil.getStringDataFromNet";
    /**执行结果是{@link UserData}类型的网络请求*/
    public static final String EVENT_BUS_TAG_GET_USER_DATAS = "com.sohu.focus.chat.NetRequestInterfaceUtil.getUsersInfoList";
    /**打开DrawerLayout的抽屉，其入参表示打开左抽屉还是右抽屉*/
    public static final String EVENT_BUS_TAG_OPEN_DRAWER = "com.sohu.focus.chat.MainActivity.openLeftDrawer";

    /************************相关公用的静态方法***************************************************************************/
    /**
     * 添加该用户为好友
     *
     * @param id 添加的用户的ID
     * @param baseFragment 刷新界面
     */
    public static void addFriendFromScanQRCode(long id,final UserFragment baseFragment) {
        Object[] params = StringDataCallBack.generateAddFriendNetParams(id);
        EventBus.getDefault().post(params,Const.EVENT_BUS_TAG_GET_STRING_DATA);
        StringDataCallBack.addOnDataRefreshListeners(StringDataCallBack.NET_ADD_FRIEND, new BaseListener.OnDataRefreshListener() {
            @Override
            public void OnDataRefresh(Object stringData) {
                //重新刷新界面
                if(baseFragment!=null) {
                    baseFragment.initData();
                }
                StringDataCallBack.removeOnDataRefreshListeners(StringDataCallBack.NET_ADD_FRIEND, this);
            }
        });
    }
}
