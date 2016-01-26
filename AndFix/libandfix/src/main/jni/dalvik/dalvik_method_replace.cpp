/*
 *
 * Copyright (c) 2015, alipay.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 	dalvik_method_replace.cpp
 *
 * @author : sanping.li@alipay.com
 *
 */
#include <time.h>
#include <stdlib.h>
#include <stddef.h>
#include <assert.h>

#include <stdbool.h>
#include <fcntl.h>
#include <dlfcn.h>

#include <sys/stat.h>
#include <dirent.h>
#include <unistd.h>
#include <ctype.h>
#include <errno.h>
#include <utime.h>
#include <sys/types.h>
#include <sys/wait.h>

#include "dalvik.h"
#include "common.h"

static JNIEnv* jni_env;
static ClassObject* classJavaLangObjectArray;
static jclass NPEClazz;
static jclass CastEClazz;
static jmethodID jInvokeMethod;
static jmethodID jClassMethod;


/*
*__attribute__ ((visibility ("hidden")))表示：让函数在本 share libs 之外不可见，就是说本库中应该还有其他文件（Unit）使用到这个函数
*dvmComputeMethodArgsSize_fnPtr    :计算方法参数大小；
*dvmCallMethod_fnPtr               :Dalvik虚拟机创建的线程调用解释器执行方法的方法；
*dexProtoGetParameterCount_fnPtr   :Dalvik虚拟机用来获取指定方法入参个数的方法；
*dvmAllocArrayByClass_fnPtr        :Dalvik虚拟机用来分配对象数组内存的方法；
*dvmBoxPrimitive_fnPtr             :Dalvik虚拟机来处理8个基本类型的方法；将JValue对象转换为8个基本类型；
*dvmFindPrimitiveClass_fnPtr       :Dalvik虚拟机通过基本类似的缩写找到对应的类的方法；
*dvmReleaseTrackedAlloc_fnPtr      :Dalvik虚拟机用来释放存储类信息内存的方法？
*dvmCheckException_fnPtr           :Dalvik虚拟机检查是否有异常产生的方法；
*dvmGetException_fnPtr             :Dalvik虚拟机获取异常的方法；
*dvmFindArrayClass_fnPtr           :Dalvik虚拟机获取元素为基本类型的数据的方法；
*dvmCreateReflectMethodObject_fnPtr:Dalvik虚拟机用来创建Method对象的方法；
*dvmGetBoxedReturnType_fnPtr       :Dalvik虚拟机用来获取某一java方法返回类型的方法；
*dvmUnboxPrimitive_fnPtr           :Dalvik虚拟机将某一类（包括基本类型和引用类型）的对象转换为JValue对象；
*dvmDecodeIndirectRef_fnPtr        :Dalvik虚拟机用以获取一个间接的引用的方法？
*dvmThreadSelf_fnPtr               :Dalvik虚拟机获取当前线程对象的方法；
*classJavaLangObjectArray          :一个对象数组的引用；
*jInvokeMethod                     :java反射中的Method类中invoke方法的引用；
*jClassMethod                      :java反射中的Method类中getDeclaringClass方法的引用；
*NPEClazz                          :java.lang.NullPointException类的引用；
*CastEClazz                        :java.lang.ClassCastException类的引用；
*/
extern jboolean __attribute__ ((visibility ("hidden"))) dalvik_setup(
		JNIEnv* env, int apilevel) {
	jni_env = env;
	//dlopen：打开指定的动态链接库文件
	//RTLD_NOW：需要在dlopen返回前，解析出所有未定义符号，如果解析不出来，在dlopen会返回NULL
	void* dvm_hand = dlopen("libdvm.so", RTLD_NOW);
	if (dvm_hand) {
		dvmComputeMethodArgsSize_fnPtr = (dvmComputeMethodArgsSize_func)dvm_dlsym(dvm_hand,
		         apilevel > 10 ?
						"_Z24dvmComputeMethodArgsSizePK6Method" :
						"dvmComputeMethodArgsSize");
		if (!dvmComputeMethodArgsSize_fnPtr) {
			throwNPE(env, "dvmComputeMethodArgsSize_fnPtr");
			return JNI_FALSE;
		}
		dvmCallMethod_fnPtr = (dvmCallMethod_func)dvm_dlsym(dvm_hand,
				apilevel > 10 ?
						"_Z13dvmCallMethodP6ThreadPK6MethodP6ObjectP6JValuez" :
						"dvmCallMethod");
		if (!dvmCallMethod_fnPtr) {
			throwNPE(env, "dvmCallMethod_fnPtr");
			return JNI_FALSE;
		}
		dexProtoGetParameterCount_fnPtr = (dexProtoGetParameterCount_func)dvm_dlsym(dvm_hand,
				apilevel > 10 ?
						"_Z25dexProtoGetParameterCountPK8DexProto" :
						"dexProtoGetParameterCount");
		if (!dexProtoGetParameterCount_fnPtr) {
			throwNPE(env, "dexProtoGetParameterCount_fnPtr");
			return JNI_FALSE;
		}

		dvmAllocArrayByClass_fnPtr = (dvmAllocArrayByClass_func)dvm_dlsym(dvm_hand,
				"dvmAllocArrayByClass");
		if (!dvmAllocArrayByClass_fnPtr) {
			throwNPE(env, "dvmAllocArrayByClass_fnPtr");
			return JNI_FALSE;
		}
		dvmBoxPrimitive_fnPtr = (dvmBoxPrimitive_func)dvm_dlsym(dvm_hand,
				apilevel > 10 ?
						"_Z15dvmBoxPrimitive6JValueP11ClassObject" :
						"dvmWrapPrimitive");
		if (!dvmBoxPrimitive_fnPtr) {
			throwNPE(env, "dvmBoxPrimitive_fnPtr");
			return JNI_FALSE;
		}
		dvmFindPrimitiveClass_fnPtr = (dvmFindPrimitiveClass_func)dvm_dlsym(dvm_hand,
				apilevel > 10 ?
						"_Z21dvmFindPrimitiveClassc" : "dvmFindPrimitiveClass");
		if (!dvmFindPrimitiveClass_fnPtr) {
			throwNPE(env, "dvmFindPrimitiveClass_fnPtr");
			return JNI_FALSE;
		}
		dvmReleaseTrackedAlloc_fnPtr = (dvmReleaseTrackedAlloc_func)dvm_dlsym(dvm_hand,
				"dvmReleaseTrackedAlloc");
		if (!dvmReleaseTrackedAlloc_fnPtr) {
			throwNPE(env, "dvmReleaseTrackedAlloc_fnPtr");
			return JNI_FALSE;
		}
		dvmCheckException_fnPtr = (dvmCheckException_func)dvm_dlsym(dvm_hand,
				apilevel > 10 ?
						"_Z17dvmCheckExceptionP6Thread" : "dvmCheckException");
		if (!dvmCheckException_fnPtr) {
			throwNPE(env, "dvmCheckException_fnPtr");
			return JNI_FALSE;
		}

		dvmGetException_fnPtr = (dvmGetException_func)dvm_dlsym(dvm_hand,
				apilevel > 10 ?
						"_Z15dvmGetExceptionP6Thread" : "dvmGetException");
		if (!dvmGetException_fnPtr) {
			throwNPE(env, "dvmGetException_fnPtr");
			return JNI_FALSE;
		}
		dvmFindArrayClass_fnPtr = (dvmFindArrayClass_func)dvm_dlsym(dvm_hand,
				apilevel > 10 ?
						"_Z17dvmFindArrayClassPKcP6Object" :
						"dvmFindArrayClass");
		if (!dvmFindArrayClass_fnPtr) {
			throwNPE(env, "dvmFindArrayClass_fnPtr");
			return JNI_FALSE;
		}
		dvmCreateReflectMethodObject_fnPtr = (dvmCreateReflectMethodObject_func)dvm_dlsym(dvm_hand,
				apilevel > 10 ?
						"_Z28dvmCreateReflectMethodObjectPK6Method" :
						"dvmCreateReflectMethodObject");
		if (!dvmCreateReflectMethodObject_fnPtr) {
			throwNPE(env, "dvmCreateReflectMethodObject_fnPtr");
			return JNI_FALSE;
		}

		dvmGetBoxedReturnType_fnPtr = (dvmGetBoxedReturnType_func)dvm_dlsym(dvm_hand,
				apilevel > 10 ?
						"_Z21dvmGetBoxedReturnTypePK6Method" :
						"dvmGetBoxedReturnType");
		if (!dvmGetBoxedReturnType_fnPtr) {
			throwNPE(env, "dvmGetBoxedReturnType_fnPtr");
			return JNI_FALSE;
		}
		dvmUnboxPrimitive_fnPtr = (dvmUnboxPrimitive_func)dvm_dlsym(dvm_hand,
				apilevel > 10 ?
						"_Z17dvmUnboxPrimitiveP6ObjectP11ClassObjectP6JValue" :
						"dvmUnwrapPrimitive");
		if (!dvmUnboxPrimitive_fnPtr) {
			throwNPE(env, "dvmUnboxPrimitive_fnPtr");
			return JNI_FALSE;
		}
		dvmDecodeIndirectRef_fnPtr = (dvmDecodeIndirectRef_func)dvm_dlsym(dvm_hand,
				apilevel > 10 ?
						"_Z20dvmDecodeIndirectRefP6ThreadP8_jobject" :
						"dvmDecodeIndirectRef");
		if (!dvmDecodeIndirectRef_fnPtr) {
			throwNPE(env, "dvmDecodeIndirectRef_fnPtr");
			return JNI_FALSE;
		}
		dvmThreadSelf_fnPtr = (dvmThreadSelf_func)dvm_dlsym(dvm_hand,
				apilevel > 10 ? "_Z13dvmThreadSelfv" : "dvmThreadSelf");
		if (!dvmThreadSelf_fnPtr) {
			throwNPE(env, "dvmThreadSelf_fnPtr");
			return JNI_FALSE;
		}

		classJavaLangObjectArray = dvmFindArrayClass_fnPtr(
				"[Ljava/lang/Object;", NULL);
		jclass clazz = env->FindClass("java/lang/reflect/Method");
		jInvokeMethod = env->GetMethodID(clazz, "invoke",
				"(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
		jClassMethod = env->GetMethodID(clazz, "getDeclaringClass",
				"()Ljava/lang/Class;");
		NPEClazz = env->FindClass("java/lang/NullPointerException");
		CastEClazz = env->FindClass("java/lang/ClassCastException");
		return JNI_TRUE;
	} else {
		return JNI_FALSE;
	}
}

static void throwNPE(JNIEnv* env, const char* msg) {
	LOGE("setup error: %s", msg);
//	env->ThrowNew(NPEClazz, msg);
}

static bool dvmIsStaticMethod(const Method* method) {
	return (method->accessFlags & ACC_STATIC) != 0;
}

/*
*src  表示补丁前的方法
*dest 表示补丁后的方法
*/
extern void __attribute__ ((visibility ("hidden"))) dalvik_replaceMethod(
		JNIEnv* env, jobject src, jobject dest) {
	jobject clazz = env->CallObjectMethod(dest, jClassMethod);//获取申明dest方法的类对象
	ClassObject* clz = (ClassObject*) dvmDecodeIndirectRef_fnPtr(
			dvmThreadSelf_fnPtr(), clazz);
	clz->status = CLASS_INITIALIZED;//将该类的状态设置为已初始化的

	Method* meth = (Method*) env->FromReflectedMethod(src);
	Method* target = (Method*) env->FromReflectedMethod(dest);
	LOGD("dalvikMethod: %s", meth->name);

	meth->jniArgInfo = 0x80000000;//如果jniArgInfo为1（即0x80000000），则Dalvik虚拟机会忽略后面的所有信息，强制在调用时实时计算
	meth->accessFlags |= ACC_NATIVE;//该方法是一个native方法；

	int argsSize = dvmComputeMethodArgsSize_fnPtr(meth);//源方法的参数个数
	if (!dvmIsStaticMethod(meth))
		argsSize++;
	meth->registersSize = meth->insSize = argsSize;//寄存器的个数以及指令的个数
	meth->insns = (u2*) target;//源函数执行的新入口

	meth->nativeFunc = (dalvik_dispatcher_func)dalvik_dispatcher;//本地方法的入口
}

extern void dalvik_setFieldFlag(JNIEnv* env, jobject field) {
	Field* dalvikField = (Field*) env->FromReflectedField(field);
	dalvikField->accessFlags = dalvikField->accessFlags & (~ACC_PRIVATE)
			| ACC_PUBLIC;
	LOGD("dalvik_setFieldFlag: %d ", dalvikField->accessFlags);
}

static bool dvmIsPrimitiveClass(const ClassObject* clazz) {
	return clazz->primitiveType != PRIM_NOT;
}

static void dalvik_dispatcher(const u4* args, jvalue* pResult,
		const Method* method, void* self) {
	ClassObject* returnType;
	jvalue result;
	ArrayObject* argArray;//参数数组

	LOGD("dalvik_dispatcher source method: %s %s", method->name,
			method->shorty);
	Method* meth = (Method*) method->insns;
	meth->accessFlags = meth->accessFlags | ACC_PUBLIC;
	LOGD("dalvik_dispatcher target method: %s %s", method->name,
			method->shorty);

	returnType = dvmGetBoxedReturnType_fnPtr(method);//获取该方法的返回类型
	if (returnType == NULL) {
		assert(dvmCheckException_fnPtr(self));
		goto bail;
	}
	LOGD("dalvik_dispatcher start call->");

	if (!dvmIsStaticMethod(meth)) {//如果不是静态方法
		Object* thisObj = (Object*) args[0];
		ClassObject* tmp = thisObj->clazz;
		thisObj->clazz = meth->clazz;
		argArray = boxMethodArgs(meth, args + 1);
		if (dvmCheckException_fnPtr(self))
			goto bail;

		dvmCallMethod_fnPtr(self, (Method*) jInvokeMethod,
				dvmCreateReflectMethodObject_fnPtr(meth), &result, thisObj,
				argArray);//执行方法

		thisObj->clazz = tmp;
	} else {
		argArray = boxMethodArgs(meth, args);
		if (dvmCheckException_fnPtr(self))
			goto bail;

		dvmCallMethod_fnPtr(self, (Method*) jInvokeMethod,
				dvmCreateReflectMethodObject_fnPtr(meth), &result, NULL,
				argArray);//执行方法
	}
	if (dvmCheckException_fnPtr(self)) {
		Object* excep = dvmGetException_fnPtr(self);
		jni_env->Throw((jthrowable) excep);
		goto bail;
	}

	if (returnType->primitiveType == PRIM_VOID) {
		LOGD("+++ ignoring return to void");
	} else if (result.l == NULL) {
		if (dvmIsPrimitiveClass(returnType)) {
			jni_env->ThrowNew(NPEClazz, "null result when primitive expected");
			goto bail;
		}
		pResult->l = NULL;
	} else {
		if (!dvmUnboxPrimitive_fnPtr(result.l, returnType, pResult)) {//方法的返回类型和实际的返回类型不符合
			char msg[1024] = { 0 };
			snprintf(msg, sizeof(msg) - 1, "%s!=%s\0",
					((Object*) result.l)->clazz->descriptor,
					returnType->descriptor);
			jni_env->ThrowNew(CastEClazz, msg);
			goto bail;
		}
	}

	bail: dvmReleaseTrackedAlloc_fnPtr((Object*) argArray, self);
}

static void* dvm_dlsym(void *hand, const char *name) {
	void* ret = dlsym(hand, name);
	char msg[1024] = { 0 };
	snprintf(msg, sizeof(msg) - 1, "0x%x", ret);
	LOGD("%s = %s\n", name, msg);
	return ret;
}

static s8 dvmGetArgLong(const u4* args, int elem) {
	s8 val;
	memcpy(&val, &args[elem], sizeof(val));
	return val;
}

/*
 * Return a new Object[] array with the contents of "args".  We determine
 * the number and types of values in "args" based on the method signature.
 * Primitive types are boxed.
 *
 * Returns NULL if the method takes no arguments.
 *
 * The caller must call dvmReleaseTrackedAlloc() on the return value.
 *
 * On failure, returns with an appropriate exception raised.
 */
static ArrayObject* boxMethodArgs(const Method* method, const u4* args) {
	const char* desc = &method->shorty[1]; // [0] is the return type.

	/* count args */
	size_t argCount = dexProtoGetParameterCount_fnPtr(&method->prototype);

	/* allocate storage */
	ArrayObject* argArray = dvmAllocArrayByClass_fnPtr(classJavaLangObjectArray,
			argCount, ALLOC_DEFAULT);
	if (argArray == NULL)
		return NULL;
	Object** argObjects = (Object**) (void*) argArray->contents;

	/*
	 * Fill in the array.
	 */

	size_t srcIndex = 0;
	size_t dstIndex = 0;
	while (*desc != '\0') {
		char descChar = *(desc++);
		jvalue value;

		switch (descChar) {
		case 'Z':
		case 'C':
		case 'F':
		case 'B':
		case 'S':
		case 'I':
			value.i = args[srcIndex++];
			argObjects[dstIndex] = (Object*) dvmBoxPrimitive_fnPtr(value,
					dvmFindPrimitiveClass_fnPtr(descChar));
			/* argObjects is tracked, don't need to hold this too */
			dvmReleaseTrackedAlloc_fnPtr(argObjects[dstIndex], NULL);
			dstIndex++;
			break;
		case 'D':
		case 'J':
			value.j = dvmGetArgLong(args, srcIndex);
			srcIndex += 2;
			argObjects[dstIndex] = (Object*) dvmBoxPrimitive_fnPtr(value,
					dvmFindPrimitiveClass_fnPtr(descChar));
			dvmReleaseTrackedAlloc_fnPtr(argObjects[dstIndex], NULL);
			dstIndex++;
			break;
		case '[':
		case 'L':
			argObjects[dstIndex++] = (Object*) args[srcIndex++];
			LOGD("boxMethodArgs object: index = %d", dstIndex - 1);
			break;
		}
	}

	return argArray;
}

