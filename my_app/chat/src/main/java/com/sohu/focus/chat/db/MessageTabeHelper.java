package com.sohu.focus.chat.db;

import android.content.Context;

import com.sohu.focus.chat.ChatApplication;
import com.sohu.focus.chat.Const;
import com.sohu.focus.chat.data.message.ImageMessageData;
import com.sohu.focus.chat.data.message.MessageData;
import com.sohu.focus.chat.data.message.TextMessageData;
import com.zhanghang.self.db.BaseSQLiteHelper;
import com.zhanghang.self.db.ComlueInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by hangzhang209526 on 2016/2/26.
 */
public class MessageTabeHelper extends BaseSQLiteHelper<MessageData> {
    private static final String TABLE_NAME = "message";
    private static MessageTabeHelper sTextMessageInstance;
    private static MessageTabeHelper sImageMessageInstance;

    public static MessageTabeHelper getImageMessageInstance(Context context){
        synchronized (TABLE_NAME){
            if(sImageMessageInstance ==null){
                sImageMessageInstance = new MessageTabeHelper(context, ChatApplication.getInstance().getVersionCode(),getComlueInfos(ImageMessageData.class));
            }
        }
        return sImageMessageInstance;
    }

    public static MessageTabeHelper getTextMessageInstance(Context context){
        synchronized (TABLE_NAME){
            if(sTextMessageInstance ==null){
                sTextMessageInstance = new MessageTabeHelper(context, ChatApplication.getInstance().getVersionCode(),getComlueInfos(TextMessageData.class));
            }
        }
        return sTextMessageInstance;
    }

    private MessageTabeHelper(Context context, int version,ArrayList<ComlueInfo> comlueInfos) {
        super(context, Const.DB_NAME, TABLE_NAME, comlueInfos.toArray(new ComlueInfo[comlueInfos.size()]), version);
    }
}
