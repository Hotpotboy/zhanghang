package com.sohu.focus.libandfixtool.proxy;

import com.sohu.focus.libandfixtool.annotation.MethodReplaceAnnotation;
import com.sohu.focus.libandfixtool.diff.DiffInfo;
import com.sohu.focus.libandfixtool.utils.TypeGenUtil;

import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hangzhang209526 on 2016/1/22.
 */
public class DexBackedClassDefProxy implements ClassDef{
    private ClassDef mInstance;
    public DexBackedClassDefProxy(ClassDef dexBackedClassDef) {
        mInstance = dexBackedClassDef;
    }

    @Override
    public String getType() {
        return TypeGenUtil.newType(mInstance.getType());
    }

    @Override
    public int getAccessFlags() {
        return mInstance.getAccessFlags();
    }

    @Override
    public String getSuperclass() {
        return mInstance.getSuperclass();
    }

    @Override
    public List<String> getInterfaces() {
        return mInstance.getInterfaces();
    }

    @Override
    public String getSourceFile() {
        return mInstance.getSourceFile();
    }

    @Override
    public Set<? extends Annotation> getAnnotations() {
        return mInstance.getAnnotations();
    }

    @Override
    public Iterable<? extends Field> getStaticFields() {
        return mInstance.getStaticFields();
    }

    @Override
    public Iterable<? extends Field> getInstanceFields() {
        return mInstance.getInstanceFields();
    }

    @Override
    public Iterable<? extends Method> getDirectMethods() {
        Iterable<? extends Method> directMethods = mInstance.getDirectMethods();
        Set modifieds =  DiffInfo.getInstance().getModifiedMethods();
        return addAnnotationForModifyMethod(directMethods,modifieds);
    }

    @Override
    public Iterable<? extends Method> getVirtualMethods() {
        Iterable<? extends Method> directMethods = mInstance.getVirtualMethods();
        Set modifieds =  DiffInfo.getInstance().getModifiedMethods();
        return addAnnotationForModifyMethod(directMethods,modifieds);
    }

    /**
     * 自动为发生了修改的方法添加@MethodReplace注释
     * 原理：发生了修改的方法引用了一个DexBackedMethodProxy代理对象，否则不变
     * @param srcMethods      所有的原始方法集合
     * @param modifiesMehods  所有的发生了修改的方法集合
     * @return
     */
    private Set<Method>  addAnnotationForModifyMethod(Iterable<? extends Method> srcMethods,Set<Method> modifiesMehods){
        Set<Method> result = new HashSet<Method>();
        for (Method method:srcMethods){
            if(modifiesMehods!=null&&modifiesMehods.contains(method)){//如果是修改的方法
                MethodReplaceAnnotation annotation = new MethodReplaceAnnotation(method.getDefiningClass(),method.getName());
                Method tmp = new DexBackedMethodProxy((DexBackedMethod)method,annotation);//使用代理模式处理注释获取的情况
                result.add(tmp);
            }else{
                result.add(method);
            }
        }
        return result;
    }

    @Override
    public Iterable<? extends Method> getMethods() {
        return mInstance.getMethods();
    }

    @Override
    public int length() {
        return mInstance.length();
    }

    @Override
    public char charAt(int index) {
        return mInstance.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return mInstance.subSequence(start, end);
    }

    @Override
    public int compareTo(CharSequence o) {
        return mInstance.compareTo(o);
    }
}
