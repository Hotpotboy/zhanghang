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

package com.sohu.focus.libandfix;

import android.content.Context;
import android.util.Log;

import com.sohu.focus.libandfix.annotation.MethodReplace;
import com.sohu.focus.libandfix.security.SecurityChecker;
import com.sohu.focus.libandfix.util.FileUtil;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * AndFix Manager
 * 
 * @author sanping.li@alipay.com
 * 
 */
public class AndFixManager {
	private static final String TAG = "AndFixManager";

	private static final String DIR = "apatch_opt";

	/**
	 * context
	 */
	private final Context mContext;

	/**
	 * classes will be fixed
	 */
	private static Map<String, Class<?>> mFixedClass = new ConcurrentHashMap<String, Class<?>>();

	/**
	 * whether support AndFix
	 */
	private boolean mSupport = false;

	/**
	 * security check
	 */
	private SecurityChecker mSecurityChecker;

	/**
	 * optimize directory  优化后的dex文件所在的目录/data/data/[package-name]/files/apatch_opt
	 */
	private File mOptDir;

	public AndFixManager(Context context) {
		mContext = context;
		mSupport = Compat.isSupport();
		if (mSupport) {
			mSecurityChecker = SecurityChecker.getInstance(mContext);
			mOptDir = new File(mContext.getFilesDir(), DIR);
			if (!mOptDir.exists() && !mOptDir.mkdirs()) {// make directory fail
				mSupport = false;
				Log.e(TAG, "opt dir create error.");
			} else if (!mOptDir.isDirectory()) {// not directory
				mOptDir.delete();
				mSupport = false;
			}
		}
	}

	/**
	 * delete optimize file of patch file
	 * 
	 * @param file
	 *            patch file
	 */
	public synchronized void removeOptFile(File file) {
		File optfile = new File(mOptDir, file.getName());
		if (optfile.exists() && !optfile.delete()) {
			Log.e(TAG, optfile.getName() + " delete error.");
		}
	}

	/**
	 * fix
	 * 
	 * @param patchPath
	 *            patch path
	 */
//	public synchronized void fix(String patchPath) {
//		fix(new File(patchPath), mContext.getClassLoader(), null);
//	}

	/**
	 * fix
	 * 
	 * @param file patch file  apatch补丁文件
	 * @param classLoader PathClassDexLoader
	 * @param classes classes will be fixed  类名集合
	 * @param isFirst 是否刚启动应用,即加载补丁文件的时机是虚拟机已经加载了目标类还是没有
	 */
	public synchronized void fix(File file, ClassLoader classLoader,
			List<String> classes,boolean isFirst) {
		//当前系统是否支持:
		//设备运行的系统不是阿里巴巴的YunOS;
		//AndFix运行的JNI环境能够成功建立；
		//运行的android系统的API级别在8到23之间（包含8和23）；
		//能成功创建优化目录/data/data/[package-name]/files/apatch_opt。
		if (!mSupport) {
			return;
		}
		//验证补丁文件的签名证书与安装当前应用的apk文件的签名证书是否一致
		if (!mSecurityChecker.verifyApk(file)) {// security check fail
			return;
		}

		try {
			//如果优化后的补丁文件存在，那么它的内容摘要（指纹）与保存在SharePreference中的的内容摘要（指纹）是否一致
			File optfile = new File(mOptDir, file.getName());
			boolean saveFingerprint = true;
			if (optfile.exists()) {
				// need to verify fingerprint（指纹） when the optimize file exist,
				// prevent someone attack（攻击） on jailbreak（越狱） device with
				// Vulnerability-Parasyte.
				// btw:exaggerated android Vulnerability-Parasyte
				// http://secauo.com/Exaggerated-Android-Vulnerability-Parasyte.html
				if (mSecurityChecker.verifyOpt(optfile)) {
					saveFingerprint = false;
				} else if (!optfile.delete()) {//指纹不一致则需要删掉优化文件
					return;
				}
			}

			if (saveFingerprint) {
				//会遍历整个优化后的aptach文件的内容，根据整个文件内容进行MD5加密，生成一个MD5指纹，
				//按照"键,值对"为："文件名-md5,MD5指纹字符串"的格式保存到SharedPreferences之中
				mSecurityChecker.saveOptSig(optfile);//内容摘要（指纹）
			}

			//统一将补丁文件的后缀名转换为apk
			String fileName = file.getName().substring(0,file.getName().indexOf("."))+".apk";
			File apkFile = new File(file.getParentFile().getAbsolutePath(),fileName);
			FileUtil.copyFile(file,apkFile);

			DexClassLoader dexClassLoader = new DexClassLoader(apkFile.getAbsolutePath(),mOptDir.getAbsolutePath(),null,classLoader);

			if(classes!=null&&classes.size()>0){
				Class<?> clazz = null;
				for(String item:classes){
//					clazz = dexClassLoader.loadClass(item);
//					clazz = Class.forName(item,true,dexClassLoader);
					if(isFirst) {
						injectAboveEqualApiLevel14(mContext, dexClassLoader, item);
					}else{
						//通过反射的方式加载补丁类
					    Class classLoaderclazz = BaseDexClassLoader.class;
						Method method = classLoaderclazz.getDeclaredMethod("findClass",String.class);
						method.setAccessible(true);
					    clazz = (Class<?>) method.invoke(dexClassLoader,item);
						if (clazz != null) {
							//第二参数是系统默认的类加载器（PathClassLoader）
							fixClass(clazz, classLoader);
						}
					}
				}
			}
			/*Open a DEX file, specifying the file in which the optimized DEX data should be written.
			If the optimized form exists and appears to be current, it will be used;
			if not, the VM will attempt to regenerate it.
			This is intended for use by applications that wish to download and execute DEX files outside the usual application installation mechanism.
			This function should not be called directly by an application; instead, use a class loader such as dalvik.system.DexClassLoader.*/
			/**打开一个DEX文件，指定优化后的DEX数据应该写入的文件。如果指定的文件的优化形式存在且能够正确的呈现，则它将被使用；否则虚拟机将尝试重新生成它。
			 * 这一点意为被期望从通用应用安装机制之外的地方下载并执行DEX文件的应用使用
			 * loadDex方法不应该被应用直接调用；而是使用类似DexClassLoader这样的类加载器
			 * */
//			final DexFile dexFile = DexFile.loadDex(file.getAbsolutePath(),
//					optfile.getAbsolutePath(), Context.MODE_PRIVATE);
//
//
//
//			ClassLoader patchClassLoader = new ClassLoader(classLoader) {
//				@Override
//				protected Class<?> findClass(String className)
//						throws ClassNotFoundException {
//					Class<?> clazz = dexFile.loadClass(className, this);
//					if (clazz == null
//							&& className.startsWith("com.sohu.focus.andfix")) {
//						return Class.forName(className);// annotation’s class
//														// not found
//					}
//					if (clazz == null) {
//						throw new ClassNotFoundException(className);
//					}
//					return clazz;
//				}
//			};
//			Enumeration<String> entrys = dexFile.entries();//Dex文件中的所有类的名称
//			Class<?> clazz = null;
//			while (entrys.hasMoreElements()) {
//				String entry = entrys.nextElement();
//				if (classes != null && !classes.contains(entry)) {
//					continue;// skip, not need fix
//				}
//				clazz = dexFile.loadClass(entry, patchClassLoader);
//				if (clazz != null) {
//					fixClass(clazz, classLoader);
//				}
//			}
		}catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "pacth", e);
		}
	}


	/**
	 * android4.0及其以上的版本的补丁修复方法
	 * @param context
	 * @param patchClassLoader
	 * @param patchClassName
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	private static void injectAboveEqualApiLevel14(Context context, DexClassLoader patchClassLoader, String patchClassName)
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		PathClassLoader defualtClassLoader = (PathClassLoader) context.getClassLoader();
		Object defualtDexPathList = getField(defualtClassLoader,Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");//获取默认类加载器的DexPathList对象
		Object patchDexPathList = getField(patchClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");//获取补丁类加载器的DexPathList对象
		Object a = combineArray(getField(defualtDexPathList,defualtDexPathList.getClass(), "dexElements"),getField(patchDexPathList,patchDexPathList.getClass(), "dexElements"));
		Object a2 = getField(defualtClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
		setField(a2, a2.getClass(), "dexElements", a);
		defualtClassLoader.loadClass(patchClassName);
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
	 * fix class
	 * 遍历指定类的方法
	 * @param clazz
	 *            class
	 */
	private void fixClass(Class<?> clazz, ClassLoader classLoader) {
		Method[] methods = clazz.getDeclaredMethods();
		MethodReplace methodReplace;//被修改方法的注释，在补丁包生成期间主动添加上
		String clz;
		String meth;
		for (Method method : methods) {
			//获取方法的注释，如果一个方法被修改过，那么在补丁包生成期间，补丁生成工具就会自动为每一个修改过的方法添加@MethodReplace注释
			//@MethodReplace注释的clazz属性表示产生bug的类；
			//@MethodReplace注释的method属性表示产生bug的方法。
			methodReplace = method.getAnnotation(MethodReplace.class);
			if (methodReplace == null)
				continue;
			clz = methodReplace.clazz();//目标类
			meth = methodReplace.method();//目标方法
			if (!isEmpty(clz) && !isEmpty(meth)) {
				replaceMethod(classLoader, clz, meth, method);
			}
		}
	}

	/**
	 * replace method
	 * 
	 * @param classLoader classloader    目标类的类加载器
	 * @param clz class                  目标类的类名
	 * @param meth name of target method 目标类的目标方法名，即有bug的方法
	 * @param method source method       修复了bug的方法
	 */
	private void replaceMethod(ClassLoader classLoader, String clz,
			String meth, Method method) {
		try {
			String key = clz + "@" + classLoader.toString();
			Class<?> clazz = mFixedClass.get(key);
			if (clazz == null) {// class not load
				Class<?> clzz = classLoader.loadClass(clz);//导入原始类，即需要修复的类
				// initialize target class
				clazz = AndFix.initTargetClass(clzz);//原始类的所有的属性都变为可访问
			}
			if (clazz != null) {// initialize class OK
				mFixedClass.put(key, clazz);
				Method src = clazz.getDeclaredMethod(meth,method.getParameterTypes());//获取目标方法
				AndFix.addReplaceMethod(src, method);
			}
		} catch (Exception e) {
			Log.e(TAG, "replaceMethod", e);
		}
	}

	private static boolean isEmpty(String string) {
		return string == null || string.length() <= 0;
	}

}
