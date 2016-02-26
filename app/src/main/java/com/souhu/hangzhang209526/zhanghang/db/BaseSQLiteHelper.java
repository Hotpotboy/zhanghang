package com.souhu.hangzhang209526.zhanghang.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2016/2/25.
 */
public class BaseSQLiteHelper<T> extends SQLiteOpenHelper {
    /**
     * 替换关键字的前缀
     */
    private static final String KEY_WORD_SUFFIX = "_sufffix_key_word";
    protected String mTableName;
    protected ComlueInfo[] mComlueInfos;

    /**
     * 过滤关键字
     * @return
     */
    protected static String filterKeyWord(String keyWord){
        if(TextUtils.isEmpty(keyWord)) return keyWord;
        if("from".equals(keyWord.toLowerCase())
                ||"to".equals(keyWord.toLowerCase())
                ||"type".equals(keyWord.toLowerCase())
                ){
            return keyWord+KEY_WORD_SUFFIX;
        }else{
            return keyWord;
        }
    }

    private static String converKeyWord(String keyWord){
        if(TextUtils.isEmpty(keyWord)){
            return keyWord;
        }else{
            int index = keyWord.indexOf(KEY_WORD_SUFFIX);
            if(index==-1) return keyWord;
            else return keyWord.substring(0,index);
        }
    }

    public BaseSQLiteHelper(Context context, String name, String tableName,ComlueInfo[] comlueNames,int version) {
        super(context, name, null, version);
        mTableName = tableName;
        mComlueInfos = comlueNames;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuffer sqlStrBuffer = new StringBuffer();
        sqlStrBuffer.append("create table if not exists ");
        sqlStrBuffer.append(mTableName);
        sqlStrBuffer.append("(");
        for(ComlueInfo item: mComlueInfos){
            if(item==null) continue;
            sqlStrBuffer.append(item.getName()).append(" ").append(item.getType());
            if(item.isPrimaryKey())
                sqlStrBuffer.append(" ").append("primary key");
            sqlStrBuffer.append(",");
        }
        sqlStrBuffer.substring(0,sqlStrBuffer.length()-1);
        sqlStrBuffer.append(")");
        db.execSQL(sqlStrBuffer.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long  insertData(T data) throws Exception {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues result = new ContentValues();
        for(ComlueInfo item: mComlueInfos){
            String oldName = converKeyWord(item.getName());
            Class clazz = data.getClass();
            Method method = clazz.getDeclaredMethod(columeNameToMethodName("get", oldName));
            Object value = method.invoke(data, null);
            result.put(oldName,value.toString());
        }
        return db.insert(mTableName,null,result);
    }

    /**
     * 将列名转换为方法名
     * @param qianzui    方法名前缀
     * @param columeName
     * @return
     */
    private String columeNameToMethodName(String qianzui,String columeName){
        return qianzui+columeName.substring(0, 1).toUpperCase()+columeName.substring(1);
    }

    public void deleteData(String whereCase,String[] whereArg){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(mTableName, whereCase, whereArg);
        db.close();
    }

    public T selectData(long id,Class<? extends T> clazz) throws Exception{
        SQLiteDatabase db = getWritableDatabase();
        String keyCol=null;
        for(ComlueInfo item:mComlueInfos) {
            if(item.isPrimaryKey()){
                keyCol = item.getName();
                break;
            }
        }
        if(TextUtils.isEmpty(keyCol)) throw new Exception("没有指定主键对应的属性!");
        String[] args = {id+""};
        Cursor cursor = db.rawQuery("select * from " + mTableName + "where " + keyCol + "=?", args);
        if(cursor.getCount()==0){
            db.close();
            return null;
        }
        if(cursor.getCount()!=1) throw new Exception("同一主键值下有多条记录!");
        T data = clazz.newInstance();
        for(int i=0;i<mComlueInfos.length;i++) {
            fillDataFromDB(i,cursor,data);
        }
        db.close();
        return data;
    }

    public ArrayList selectDatas(String selection, String[] selectionArgs, String groupBy, String having, String orderBy, Class<? extends T> clazz) throws Exception{
        SQLiteDatabase db = getWritableDatabase();
        String[] colNames = new String[mComlueInfos.length];
        int i = 0;
        for(ComlueInfo item:mComlueInfos) {
            colNames[i] = item.getName();
            i++;
        }
        Cursor cursor = db.query(mTableName, colNames, selection, selectionArgs, groupBy, having, orderBy);
        if(cursor!=null) {
            ArrayList result = new ArrayList();
            cursor.moveToFirst();
            do{
                T instance = clazz.newInstance();
                for(i=0;i<mComlueInfos.length;i++) {
                    fillDataFromDB(i,cursor,instance);
                }
                result.add(instance);
            }while (cursor.moveToNext());
            db.close();
            return result;
        }
        db.close();
        return null;
    }

    /**
     * 将数据库中的每一行具体某一列的记录填充到指定的对象之中
     * @return
     */
    private void fillDataFromDB(int colIndex,Cursor cursor,T data) throws Exception {
        Object valueInDB;
        Class paramClazz;
        if(mComlueInfos[colIndex].getType()==ComlueInfo.INT_TYPE) {
            valueInDB = cursor.getInt(colIndex);
            paramClazz = Integer.class;
        }else if(mComlueInfos[colIndex].getType()==ComlueInfo.LONG_TYPE) {
            valueInDB = cursor.getLong(colIndex);
            paramClazz = Long.class;
        }else if(mComlueInfos[colIndex].getType()==ComlueInfo.DOUBLE_TYPE) {
            valueInDB = cursor.getDouble(colIndex);
            paramClazz = Double.class;
        }else {
            valueInDB = cursor.getString(colIndex);
            paramClazz = String.class;
        }
        String oldName = converKeyWord(mComlueInfos[colIndex].getName());
        Method method = data.getClass().getDeclaredMethod(columeNameToMethodName("set", oldName),paramClazz);
        method.invoke(data, valueInDB);
    }

    public void updateData(T data,String whereCase,String[] whereArgs) throws Exception{
        SQLiteDatabase db = getWritableDatabase();
        ContentValues result = new ContentValues();
        int keyColIndex = -1;
        for(int i=0;i<mComlueInfos.length;i++){
            String oldName = converKeyWord(mComlueInfos[i].getName());
            Class clazz = data.getClass();
            Method method = clazz.getDeclaredMethod(columeNameToMethodName("get", oldName));
            Object value = method.invoke(data, null);
            result.put(oldName,value.toString());
            if(mComlueInfos[i].isPrimaryKey()) keyColIndex = i;
        }
        if(keyColIndex>=0){
            String keyCol = mComlueInfos[keyColIndex].getName();
            String keyColValue = result.get(keyCol).toString();
            if(whereCase==null) {
                whereCase = keyCol+"=?";
            }else{
                whereCase += "," + keyCol+"=?";
            }
            if(whereArgs==null) {
                whereArgs = new String[1];
                whereArgs[0] = keyColValue;
            }else{
                String[] tmp = new String[whereArgs.length+1];
                System.arraycopy(whereArgs,0,tmp,0,whereArgs.length);
                tmp[whereArgs.length] = keyColValue;
                whereArgs = tmp;
            }
        }
        db.update(mTableName,result,whereCase,whereArgs);
        db.close();
    }

}
