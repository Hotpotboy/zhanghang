package com.zhanghang.self.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.zhanghang.self.base.BaseApplication;
import com.zhanghang.self.utils.PreferenceUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by hangzhang209526 on 2016/2/25.
 * 模板T表示对应的数据POJO类
 */
public class BaseSQLiteHelper<T> extends SQLiteOpenHelper {
    /**所有表名*/
    public static ArrayList<String> sAllTableName;
    /**
     * 替换关键字的前缀
     */
    private static final String KEY_WORD_SUFFIX = "_sufffix_key_word";
    /**
     * 当前id值保存在SharePreference中的key
     */
    private static final String KEY_DB_ID = "key_db_id";
    protected String mTableName;
    protected ComlueInfo[] mComlueInfos;
    private String TAG = getClass().getSimpleName();

    /**
     * 获取主键ID
     */
    public static synchronized long getId() {
        long currentId = PreferenceUtil.getLongInPreferce(BaseApplication.getInstance(), BaseApplication.getInstance().getVersionName(), KEY_DB_ID, 0);
        long result = ++currentId;
        PreferenceUtil.updateLongInPreferce(BaseApplication.getInstance(), BaseApplication.getInstance().getVersionName(), KEY_DB_ID, result);
        return result;
    }

    /**
     * 过滤关键字
     *
     * @return
     */
    protected static String filterKeyWord(String keyWord) {
        if (TextUtils.isEmpty(keyWord)) return keyWord;
        if ("from".equals(keyWord.toLowerCase())
                || "to".equals(keyWord.toLowerCase())
                || "type".equals(keyWord.toLowerCase())
                ) {
            return keyWord + KEY_WORD_SUFFIX;
        } else {
            return keyWord;
        }
    }

    private static String converKeyWord(String keyWord) {
        if (TextUtils.isEmpty(keyWord)) {
            return keyWord;
        } else {
            int index = keyWord.indexOf(KEY_WORD_SUFFIX);
            if (index == -1) return keyWord;
            else return keyWord.substring(0, index);
        }
    }

    /**
     * 解析此表对应的POJO类，
     * @param clazz  POJO类
     * @return   返回POJO类非静态且非常量的属性集合，以26个子母的顺序排列
     */
    public static ArrayList<ComlueInfo> getComlueInfos(Class clazz) {
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

    public BaseSQLiteHelper(Context context, String name, String tableName, ComlueInfo[] comlueNames, int version) {
        super(context, name, null, version);
        mTableName = tableName;
        mComlueInfos = comlueNames;
        if(sAllTableName==null){
            sAllTableName = getAllTableName(getReadableDatabase());
        }
        if(!sAllTableName.contains(mTableName)){//创建表
            SQLiteDatabase db = getWritableDatabase();
            createTable(db);
            sAllTableName = getAllTableName(db);//刷新
        }
    }

    private ArrayList<String> getAllTableName(SQLiteDatabase db){
        ArrayList<String> result = new ArrayList<>();
        StringBuffer sqlStrBuffer = new StringBuffer();
        sqlStrBuffer.append("select name from sqlite_master where type='table' order by name;");
        Cursor cursor = db.rawQuery(sqlStrBuffer.toString(), null);
        cursor.moveToFirst();
        while(cursor.moveToNext()){
            //遍历出表名
            String name = cursor.getString(0);
            result.add(name);
        }
        return result;
    }
    private void createTable(SQLiteDatabase db){
        StringBuffer sqlStrBuffer = new StringBuffer();
        sqlStrBuffer.append("create table if not exists ");
        sqlStrBuffer.append(mTableName);
        sqlStrBuffer.append("(");
        for (ComlueInfo item : mComlueInfos) {
            if (item == null) continue;
            sqlStrBuffer.append(item.getName()).append(" ").append(item.getTableType());
            if (item.isPrimaryKey())
                sqlStrBuffer.append(" ").append("primary key");
            sqlStrBuffer.append(",");
        }
        String sqlString = sqlStrBuffer.substring(0, sqlStrBuffer.length() - 1);
        sqlString += ")";
        db.execSQL(sqlString);
    }

    /**
     * 清空当前表
     */
    public void deleteTable(){
        SQLiteDatabase db = getWritableDatabase();
        StringBuffer sqlStrBuffer = new StringBuffer();
        sqlStrBuffer.append("delete from"+" "+mTableName);
        db.execSQL(sqlStrBuffer.toString());
        sAllTableName.remove(mTableName);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 获取列属性数组
     *
     * @return
     */
    public ComlueInfo[] getComlueInfos() {
        return mComlueInfos;
    }

    public long insertData(T data) throws Exception {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues result = converObjectToContentValues(data);
        return db.insert(mTableName, null, result);
    }

    public void deleteData(String whereCase, String[] whereArg) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(mTableName, whereCase, whereArg);
        db.close();
    }

    /**
     * 获取主键
     *
     * @return
     */
    private String getKeyCol() {
        String keyCol = null;
        for (ComlueInfo item : mComlueInfos) {
            if (item.isPrimaryKey()) {
                keyCol = item.getName();
                break;
            }
        }
        return keyCol;
    }

    public T selectData(long id, Class<? extends T> clazz) throws Exception {
        String keyCol = getKeyCol();//获取主键
        if (TextUtils.isEmpty(keyCol)) throw new Exception("没有指定主键对应的属性!");
        String[] args = {id + ""};
        String whereCase = keyCol + "=?";
        ArrayList result = selectDatas(whereCase, args, null, null, null, clazz);
        if (result == null || result.size() == 0) {
            return null;
        }
        if (result.size() != 1) throw new Exception("同一主键值下有多条记录!");
        return (T) result.get(0);
    }

    public ArrayList selectAllDatas(Class<? extends T> clazz) throws Exception {
        return selectDatas("",null,"","","",clazz);
    }

    public ArrayList selectDatas(String selection, String[] selectionArgs, String groupBy, String having, String orderBy, Class<? extends T> clazz) throws Exception {
        SQLiteDatabase db = getWritableDatabase();
        String[] colNames = new String[mComlueInfos.length];
        int i = 0;
        for (ComlueInfo item : mComlueInfos) {
            colNames[i] = item.getName();
            i++;
        }
        Cursor cursor = db.query(mTableName, colNames, selection, selectionArgs, groupBy, having, orderBy);
        if (cursor != null) {
            ArrayList result = new ArrayList();
            int resultLen = cursor.getCount();
            if (resultLen > 0) {
                cursor.moveToFirst();
                do {
                    T instance = clazz.newInstance();
                    for (i = 0; i < mComlueInfos.length; i++) {
                        fillDataFromDB(i, cursor, instance);
                    }
                    result.add(instance);
                } while (cursor.moveToNext());
            }
            db.close();
            return result;
        }
        db.close();
        return null;
    }

    /**
     * 将数据库中的每一行具体某一列的记录填充到指定的对象之中
     *
     * @return
     */
    private void fillDataFromDB(int colIndex, Cursor cursor, T data) throws Exception {
        Object valueInDB;
        if (mComlueInfos[colIndex].getType() == ComlueInfo.INT_TYPE) {
            valueInDB = cursor.getInt(colIndex);
        } else if (mComlueInfos[colIndex].getType() == ComlueInfo.LONG_TYPE) {
            valueInDB = cursor.getLong(colIndex);
        } else if (mComlueInfos[colIndex].getType() == ComlueInfo.DOUBLE_TYPE) {
            valueInDB = cursor.getDouble(colIndex);
        } else {
            valueInDB = cursor.getString(colIndex);
        }
        String oldName = converKeyWord(mComlueInfos[colIndex].getName());
        Class dataClazz = mComlueInfos[colIndex].getDecClass();
        Field field = dataClazz.getDeclaredField(oldName);
        field.setAccessible(true);
        if (mComlueInfos[colIndex].isOjbect()) {
            Class fieldClazz = field.getType();
            //获取该列对应的类的一个构造器，此构造器只有一个String类型的入参
            try {
                Constructor constructor = fieldClazz.getConstructor(String.class);
                field.set(data, constructor.newInstance(valueInDB));
            }catch (NoSuchMethodException e){
                Log.e(TAG,"无法为类【"+data.getClass().getSimpleName()+"】的属性【"+field.getName()+"】填充数据库中的值,因为类型【"+field.getType()+"】没有只有一个String类型入参的构造器");
            }
        } else {
            field.set(data, valueInDB);
        }
    }

    public void updateData(T data, String whereCase, String[] whereArgs) throws Exception {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues result = converObjectToContentValues(data);//将数据转换为ContentValues
        String keyCol = getKeyCol();//获取主键
        String keyColValue = result.get(keyCol).toString();
        if (whereCase == null) {
            whereCase = keyCol + "=?";
        } else {
            whereCase += " AND " + keyCol + "=?";
        }
        if (whereArgs == null) {
            whereArgs = new String[1];
            whereArgs[0] = keyColValue;
        } else {
            String[] tmp = new String[whereArgs.length + 1];
            System.arraycopy(whereArgs, 0, tmp, 0, whereArgs.length);
            tmp[whereArgs.length] = keyColValue;
            whereArgs = tmp;
        }
        db.update(mTableName, result, whereCase, whereArgs);
        db.close();
    }

    private ContentValues converObjectToContentValues(T data) throws Exception {
        ContentValues result = new ContentValues();
        for (int i = 0; i < mComlueInfos.length; i++) {
            String tableColName = mComlueInfos[i].getName();//表中的列名
            String oldName = converKeyWord(tableColName);//对象中的属性名
            Class clazz = mComlueInfos[i].getDecClass();
            Field field = clazz.getDeclaredField(oldName);
            field.setAccessible(true);
            Object value = field.get(data);
            result.put(tableColName, String.valueOf(value));
        }
        return result;
    }

}
