package com.sohu.focus.chat.db;

import android.content.Context;

import com.sohu.focus.chat.ChatApplication;
import com.sohu.focus.chat.Const;
import com.sohu.focus.chat.data.MessageData;
import com.souhu.hangzhang209526.zhanghang.db.BaseSQLiteHelper;
import com.souhu.hangzhang209526.zhanghang.db.ComlueInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by hangzhang209526 on 2016/2/26.
 */
public class MessageTabeHelper extends BaseSQLiteHelper<MessageData> {
    private static MessageTabeHelper sInstance;
    private static final String TABLE_NAME = "message";

    public static ComlueInfo[] comlueInfos;

    static {
        Field[] fields = MessageData.class.getDeclaredFields();
        comlueInfos = new ComlueInfo[fields.length];
        for(int i=0;i<fields.length;i++){
            Field field = fields[i];
            int description = field.getModifiers();
            int mark = Modifier.STATIC|Modifier.FINAL;//非静态、非常量的属性
            if((description&mark)!=0) continue;
            ComlueInfo comlueInfo = new ComlueInfo();
            String name = field.getName();
            comlueInfo.setName(filterKeyWord(name));
            if(name.equals("id")) comlueInfo.setPrimaryKey(true);
            else comlueInfo.setPrimaryKey(false);
            comlueInfo.setType(field.getDeclaringClass());
            comlueInfos[i] = comlueInfo;
        }
    }

    public static MessageTabeHelper getInstance(Context context){
        synchronized (TABLE_NAME){
            if(sInstance==null){
                sInstance = new MessageTabeHelper(context, ChatApplication.getInstance().getVersionCode());
            }
        }
        return sInstance;
    }

    private MessageTabeHelper(Context context, int version) {
        super(context, Const.DB_NAME, TABLE_NAME, comlueInfos, version);
    }
}
