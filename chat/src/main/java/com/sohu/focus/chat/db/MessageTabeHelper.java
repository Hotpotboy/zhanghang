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

    public static ArrayList<ComlueInfo> getTextComlueInfos(Class clazz) {
        ArrayList<ComlueInfo> comlueInfos = new ArrayList<ComlueInfo>();
        //遍历继承树中的声明属性
        do{
            Field[] fields = clazz.getDeclaredFields();
            if(fields!=null&&fields.length>0){
                for(int i=0;i<fields.length;i++){
                    Field field = fields[i];
                    int description = field.getModifiers();
                    int mark = Modifier.STATIC|Modifier.FINAL;//非静态、非常量的属性
                    if((description&mark)!=0) continue;//如果是静态的或者是常量则跳过
                    //生成ComlueInfo对象
                    ComlueInfo comlueInfo = new ComlueInfo();
                    //属性名字
                    String name = field.getName();
                    comlueInfo.setName(filterKeyWord(name));
                    //是否是主键
                    if(name.equals("id")) comlueInfo.setPrimaryKey(true);
                    else comlueInfo.setPrimaryKey(false);
                    //属性的类型
                    comlueInfo.setType(field.getType());
                    //属性所属的类
                    comlueInfo.setDecClass(clazz);
                    comlueInfos.add(comlueInfo);
                }
            }
            clazz = clazz.getSuperclass();
        }while (!clazz.getName().endsWith(".BaseData"));

        Collections.sort(comlueInfos, new Comparator<ComlueInfo>() {
            @Override
            public int compare(ComlueInfo lhs, ComlueInfo rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        return comlueInfos;
    }

    public static MessageTabeHelper getImageMessageInstance(Context context){
        synchronized (TABLE_NAME){
            if(sImageMessageInstance ==null){
                sImageMessageInstance = new MessageTabeHelper(context, ChatApplication.getInstance().getVersionCode(),getTextComlueInfos(ImageMessageData.class));
            }
        }
        return sImageMessageInstance;
    }

    public static MessageTabeHelper getTextMessageInstance(Context context){
        synchronized (TABLE_NAME){
            if(sTextMessageInstance ==null){
                sTextMessageInstance = new MessageTabeHelper(context, ChatApplication.getInstance().getVersionCode(),getTextComlueInfos(TextMessageData.class));
            }
        }
        return sTextMessageInstance;
    }

    private MessageTabeHelper(Context context, int version,ArrayList<ComlueInfo> comlueInfos) {
        super(context, Const.DB_NAME, TABLE_NAME, comlueInfos.toArray(new ComlueInfo[comlueInfos.size()]), version);
    }
}
