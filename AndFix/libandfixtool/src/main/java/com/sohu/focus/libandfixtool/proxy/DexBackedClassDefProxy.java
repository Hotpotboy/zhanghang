package com.sohu.focus.libandfixtool.proxy;

import com.sohu.focus.libandfixtool.utils.TypeGenUtil;

import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;

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
        return mInstance.getDirectMethods();
    }

    @Override
    public Iterable<? extends Method> getVirtualMethods() {
        return mInstance.getVirtualMethods();
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
