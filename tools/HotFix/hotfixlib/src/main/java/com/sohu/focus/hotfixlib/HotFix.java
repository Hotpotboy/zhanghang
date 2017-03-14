/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.sohu.focus.hotfixlib;

import android.annotation.TargetApi;
import android.content.Context;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/* compiled from: ProGuard */
public final class HotFix {
    /**
     * @param context
     * @param patchDexFile   补丁dex文件
     * @param patchClassName 补丁类名
     */
    public static void patch(Context context, String patchDexFile, String patchClassName) {
        if (patchDexFile != null && new File(patchDexFile).exists()) {
            try {
                if (hasDexClassLoader()) {//android4.0以上
                    injectAboveEqualApiLevel14(context, patchDexFile, patchClassName);
                } else {//android4.0以下
                    injectBelowApiLevel14(context, patchDexFile, patchClassName);
                }
            } catch (Throwable th) {
            }
        }
    }

    private static boolean hasDexClassLoader() {
        try {
            Class.forName("dalvik.system.BaseDexClassLoader");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @TargetApi(14)
    /**
     * android4.0以下的补丁修复方法
     */
    private static void injectBelowApiLevel14(Context context, String dexFilePath, String patchClassName) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        PathClassLoader defualtClassLoader = (PathClassLoader) context.getClassLoader();
        DexClassLoader dexClassLoader = new DexClassLoader(dexFilePath, context.getDir("dex", Context.MODE_PRIVATE).getAbsolutePath(), dexFilePath, defualtClassLoader);
        dexClassLoader.loadClass(patchClassName);
        setField(defualtClassLoader, PathClassLoader.class, "mPaths",
                appendArray(getField(defualtClassLoader, PathClassLoader.class, "mPaths"), getField(dexClassLoader, DexClassLoader.class,
                                "mRawDexPath")
                ));
        setField(defualtClassLoader, PathClassLoader.class, "mFiles",
                combineArray(getField(defualtClassLoader, PathClassLoader.class, "mFiles"), getField(dexClassLoader, DexClassLoader.class,
                                "mFiles")
                ));
        setField(defualtClassLoader, PathClassLoader.class, "mZips",
                combineArray(getField(defualtClassLoader, PathClassLoader.class, "mZips"), getField(dexClassLoader, DexClassLoader.class,
                        "mZips")));
        setField(defualtClassLoader, PathClassLoader.class, "mDexs",
                combineArray(getField(defualtClassLoader, PathClassLoader.class, "mDexs"), getField(dexClassLoader, DexClassLoader.class,
                        "mDexs")));
        defualtClassLoader.loadClass(patchClassName);
    }

    /**
     * android4.0及其以上的版本的补丁修复方法
     * @param context
     * @param dexFilePath
     * @param patchClassName
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static void injectAboveEqualApiLevel14(Context context, String dexFilePath, String patchClassName)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        PathClassLoader defualtClassLoader = (PathClassLoader) context.getClassLoader();
        DexClassLoader patchClassLoader = new DexClassLoader(dexFilePath, context.getDir("dex", Context.MODE_PRIVATE).getAbsolutePath(), dexFilePath, defualtClassLoader);
        Object defualtDexPathList = getPathList(defualtClassLoader);//获取默认类加载器的DexPathList对象
        Object patchDexPathList = getPathList(patchClassLoader);//获取补丁类加载器的DexPathList对象
        Object a = combineArray(getDexElements(defualtDexPathList),getDexElements(patchDexPathList));
        Object a2 = getPathList(defualtClassLoader);
        setField(a2, a2.getClass(), "dexElements", a);
        defualtClassLoader.loadClass(patchClassName);
    }

    /**
     * 返回一个DexPathList对象
     * @param obj
     * @return
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static Object getPathList(Object obj) throws ClassNotFoundException, NoSuchFieldException,
            IllegalAccessException {
        return getField(obj, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    private static Object getDexElements(Object obj) throws NoSuchFieldException, IllegalAccessException {
        return getField(obj, obj.getClass(), "dexElements");
    }

    private static Object getField(Object obj, Class cls, String str)
            throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cls.getDeclaredField(str);
        declaredField.setAccessible(true);
        return declaredField.get(obj);
    }

    /**
     *
     * @param reflectObj        反射对象
     * @param reflectClass      反射对象对应的类
     * @param reflectFieldName  需要修改的属性名称
     * @param reflectFieldValue 需要修改的属性值
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static void setField(Object reflectObj, Class reflectClass, String reflectFieldName, Object reflectFieldValue)
            throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = reflectClass.getDeclaredField(reflectFieldName);
        declaredField.setAccessible(true);
        declaredField.set(reflectObj, reflectFieldValue);
    }

    /**
     * 合并两个数组
     * @param obj
     * @param obj2
     * @return
     */
    private static Object combineArray(Object obj, Object obj2) {
        Class componentType = obj2.getClass().getComponentType();
        int length = Array.getLength(obj2);
        int length2 = Array.getLength(obj) + length;
        Object newInstance = Array.newInstance(componentType, length2);
        for (int i = 0; i < length2; i++) {
            if (i < length) {
                Array.set(newInstance, i, Array.get(obj2, i));
            } else {
                Array.set(newInstance, i, Array.get(obj, i - length));
            }
        }
        return newInstance;
    }

    /**
     * 将元素添加到数组中（添加到第一位）
     * @param arrayObj
     * @param elementObj
     * @return
     */
    private static Object appendArray(Object arrayObj, Object elementObj) {
        Class componentType = arrayObj.getClass().getComponentType();
        int length = Array.getLength(arrayObj);
        Object newInstance = Array.newInstance(componentType, length + 1);
        Array.set(newInstance, 0, elementObj);
        for (int i = 1; i < length + 1; i++) {
            Array.set(newInstance, i, Array.get(arrayObj, i - 1));
        }
        return newInstance;
    }
}