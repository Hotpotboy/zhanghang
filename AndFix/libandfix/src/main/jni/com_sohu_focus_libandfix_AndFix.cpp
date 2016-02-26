#include <stdio.h>
#include <cassert>
#include "com_sohu_focus_libandfix_AndFix.h"
#include "common.h"

//dalvik
extern jboolean dalvik_setup(JNIEnv* env, int apilevel);
extern void dalvik_replaceMethod(JNIEnv* env, jobject src, jobject dest);
extern void dalvik_replaceClass(JNIEnv* env, jobject src, jobject dest);
extern void dalvik_setFieldFlag(JNIEnv* env, jobject field);
//art
extern jboolean art_setup(JNIEnv* env, int apilevel);
extern void art_replaceMethod(JNIEnv* env, jobject src, jobject dest);
extern void art_setFieldFlag(JNIEnv* env, jobject field);

static bool isart;//是否是ART虚拟机
/*
 * Class:     com_sohu_focus_libandfix_AndFix
 * Method:    setup
 * Signature: (ZI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_sohu_focus_libandfix_AndFix_setup
  (JNIEnv * env, jclass clazz, jboolean isArt, jint apilevel){
    isart = isArt;
    LOGD("vm is: %s , apilevel is: %i", (isArt ? "art" : "dalvik"),(int )apilevel);
    if(!isart){
        return dalvik_setup(env,apilevel);
    }else{
        return art_setup(env,apilevel);
    }
}

/*
 * Class:     com_sohu_focus_libandfix_AndFix
 * Method:    replaceMethod
 * Signature: (Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;)V
 */
JNIEXPORT void JNICALL Java_com_sohu_focus_libandfix_AndFix_replaceMethod
  (JNIEnv * env, jclass clazz, jobject src, jobject dest){
  if(!isart){
      dalvik_replaceMethod(env,src,dest);
  }else{
      art_replaceMethod(env,src,dest);
  }
}

/*
 * Class:     com_sohu_focus_libandfix_AndFix
 * Method:    replaceClass
 * Signature: (Ljava/lang/Class;Ljava/lang/Class;)V
 */
JNIEXPORT void JNICALL Java_com_sohu_focus_libandfix_AndFix_replaceClass
  (JNIEnv * env, jclass clazz, jclass src, jclass dest){
  if(!isart){
      dalvik_replaceClass(env,src,dest);
  }else{
//      art_replaceMethod(env,src,dest);
  }
}

/*
 * Class:     com_sohu_focus_libandfix_AndFix
 * Method:    setFieldFlag
 * Signature: (Ljava/lang/reflect/Field;)V
 */
JNIEXPORT void JNICALL Java_com_sohu_focus_libandfix_AndFix_setFieldFlag
  (JNIEnv * env, jclass clazz, jobject object){
  if(!isart){
      dalvik_setFieldFlag(env,object);
  }else{
      art_setFieldFlag(env,object);
  }
}

