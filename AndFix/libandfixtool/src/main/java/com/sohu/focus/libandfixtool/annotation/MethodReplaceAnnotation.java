package com.sohu.focus.libandfixtool.annotation;

import org.jf.dexlib2.AnnotationVisibility;
import org.jf.dexlib2.base.BaseAnnotation;
import org.jf.dexlib2.base.BaseAnnotationElement;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableStringEncodedValue;

import java.util.HashSet;
import java.util.Set;

public class MethodReplaceAnnotation extends BaseAnnotation
{
    private static final String ANNOTATION = "Lcom/sohu/focus/libandfix/annotation/MethodReplace;";
    private Set<BaseAnnotationElement> mElements = new HashSet();

    public int getVisibility()
    {
        return AnnotationVisibility.getVisibility("runtime");
    }
    @Override
    public String getType()
    {
        return ANNOTATION;
    }

    public MethodReplaceAnnotation(final String clazz, final String method) {
        BaseAnnotationElement clazzElement = new BaseAnnotationElement(){
            public EncodedValue getValue()
            {
                String name = clazz.substring(1, clazz.length() - 1).replace('/', '.');
                return new ImmutableStringEncodedValue(name);
            }

            public String getName()
            {
                return "clazz";
            }
        };
        this.mElements.add(clazzElement);

        BaseAnnotationElement methodElement = new BaseAnnotationElement()
        {
            public EncodedValue getValue()
            {
                return new ImmutableStringEncodedValue(method);
            }

            public String getName()
            {
                return "method";
            }
        };
        this.mElements.add(methodElement);
    }
    @Override
    public Set<? extends AnnotationElement> getElements()
    {
        return this.mElements;
    }
}