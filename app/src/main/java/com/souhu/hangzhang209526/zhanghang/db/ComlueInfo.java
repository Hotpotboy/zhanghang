package com.souhu.hangzhang209526.zhanghang.db;

/**
 * Created by hangzhang209526 on 2016/2/25.
 */
public class ComlueInfo {
    public static final int INT_TYPE = 0;
    public static final int STRING_TYPE = 1;
    public static final int LONG_TYPE = 2;
    public static final int DOUBLE_TYPE = 3;
    private String name;
    private int type;
    private boolean primaryKey;

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setType(Class clazz) {
        if(clazz.equals(Integer.class)){
            type = INT_TYPE;
        }else if(clazz.equals(Long.class)){
            type = LONG_TYPE;
        }else if(clazz.equals(Double.class)){
            type = DOUBLE_TYPE;
        }else{
            type = STRING_TYPE;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getClassType(){
        switch (type){
            case INT_TYPE:
                return Integer.class;
            case LONG_TYPE:
                return Long.class;
            case DOUBLE_TYPE:
                return Double.class;
            case STRING_TYPE:
            default:
                return String.class;
        }
    }

    public String getTableType(){
        switch (type){
            case INT_TYPE:
                return "integer";
            case LONG_TYPE:
                return "bigint";
            case DOUBLE_TYPE:
                return "double";
            case STRING_TYPE:
            default:
                return "varchar";
        }
    }
}
