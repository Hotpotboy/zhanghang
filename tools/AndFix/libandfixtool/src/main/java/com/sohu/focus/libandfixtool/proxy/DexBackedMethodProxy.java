package com.sohu.focus.libandfixtool.proxy;

import com.sohu.focus.libandfixtool.annotation.MethodReplaceAnnotation;

import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.reference.MethodReference;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hangzhang209526 on 2016/1/27.
 */
public class DexBackedMethodProxy implements Method {
    private DexBackedMethod mMethod;
    /**方法替换注释*/
    private Annotation mMethodReplaceAnnotation;
    public DexBackedMethodProxy(DexBackedMethod method,MethodReplaceAnnotation annotation){
        mMethod = method;
        mMethodReplaceAnnotation = annotation;
    }
    @Override
    public String getDefiningClass() {
        return mMethod.getDefiningClass();
    }

    @Override
    public String getName() {
        return mMethod.getName();
    }

    @Override
    public List<? extends CharSequence> getParameterTypes() {
        return mMethod.getParameterTypes();
    }

    @Override
    public List<? extends MethodParameter> getParameters() {
        return mMethod.getParameters();
    }

    @Override
    public String getReturnType() {
        return mMethod.getReturnType();
    }

    @Override
    public int compareTo(MethodReference methodReference) {
        return mMethod.compareTo(methodReference);
    }

    @Override
    public int getAccessFlags() {
        return mMethod.getAccessFlags();
    }

    @Override
    public Set<? extends Annotation> getAnnotations() {
        Set<? extends  Annotation> annotations = mMethod.getAnnotations();
        if(mMethodReplaceAnnotation!=null)  {
            HashSet<Annotation> result = new HashSet();
            result.addAll((Collection) annotations);
            result.add(mMethodReplaceAnnotation);
            return result;
        }
        return annotations;
    }

    @Override
    public MethodImplementation getImplementation() {
        return mMethod.getImplementation();
    }
}
