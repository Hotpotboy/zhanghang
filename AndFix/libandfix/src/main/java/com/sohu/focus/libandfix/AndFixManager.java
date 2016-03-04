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
import android.os.Debug;
import android.util.Log;

import com.sohu.focus.libandfix.annotation.MethodReplace;
import com.sohu.focus.libandfix.security.SecurityChecker;
import com.sohu.focus.libandfix.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

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
			mSecurityChecker = new SecurityChecker(mContext);
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
	public synchronized void fix(String patchPath) {
		fix(new File(patchPath), mContext.getClassLoader(), null);
	}

	/**
	 * fix
	 * 
	 * @param file
	 *            patch file
	 * @param classLoader
	 *            classloader of class that will be fixed
	 * @param classes
	 *            classes will be fixed  类名添加了_CF后缀
	 */
	public synchronized void fix(File file, ClassLoader classLoader,
			List<String> classes) {
		if (!mSupport) {
			return;
		}

		if (!mSecurityChecker.verifyApk(file)) {// security check fail验证补丁文件的签名证书与安装当前应用的apk文件的签名证书是否一致
			return;
		}

		try {
			File optfile = new File(mOptDir, file.getName());
			boolean saveFingerprint = true;
			if (optfile.exists()) {
				// need to verify fingerprint（指纹） when the optimize file exist,
				// prevent someone attack（攻击） on jailbreak（越狱） device with
				// Vulnerability-Parasyte.
				// btw:exaggerated android Vulnerability-Parasyte
				// http://secauo.com/Exaggerated-Android-Vulnerability-Parasyte.html
				if (mSecurityChecker.verifyOpt(optfile)) {//如果优化后的补丁文件存在，那么它的内容摘要与上一次保存的内容摘要是否一致
					saveFingerprint = false;
				} else if (!optfile.delete()) {
					return;
				}
			}

			if (saveFingerprint) {
				//会遍历整个优化后的aptach文件的内容，根据整个文件内容进行MD5加密，生成一个MD5指纹，
				//按照"键,值对"为："文件名-md5,MD5指纹字符串"的格式保存到SharedPreferences之中
				mSecurityChecker.saveOptSig(optfile);//保存指纹
			}

			//统一将补丁文件的后缀名转换为apk
			String fileName = file.getName().substring(0,file.getName().indexOf("."))+".apk";
			File apkFile = new File(file.getParentFile().getAbsolutePath(),fileName);
			FileUtil.copyFile(file, apkFile);

			DexClassLoader dexClassLoader = new DexClassLoader(apkFile.getAbsolutePath(),mOptDir.getAbsolutePath(),null,classLoader);

			if(classes!=null&&classes.size()>0){
				Class<?> clazz = null;
				for(String item:classes){
//					clazz = dexClassLoader.loadClass(item);
//					clazz = Class.forName(item,true,dexClassLoader);
					Class classLoaderclazz = BaseDexClassLoader.class;
					Method method = classLoaderclazz.getDeclaredMethod("findClass",String.class);
					method.setAccessible(true);
					clazz = (Class<?>) method.invoke(dexClassLoader,item);
					if (clazz != null) {
						fixClass(clazz, classLoader);
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
			Log.e(TAG, "pacth", e);
		}
	}

	/**
	 * fix class
	 * 遍历指定类的方法
	 * @param clazz
	 *            class
	 */
	private void fixClass(Class<?> clazz, ClassLoader classLoader) {
		Method[] methods = clazz.getDeclaredMethods();
		MethodReplace methodReplace;
		String clz;
		String meth;
		for (Method method : methods) {
			//获取方法的注释，如果一个方法被修改过，那么在apktool阶段，apktool命令就会自动为每一个修改过的方法添加@MethodReplace注释
			//@MethodReplace注释的clazz属性表示产生bug的类；
			//@MethodReplace注释的method属性表示产生bug的方法。
			methodReplace = method.getAnnotation(MethodReplace.class);
			if (methodReplace == null)
				continue;
			clz = methodReplace.clazz();
			meth = methodReplace.method();
			if (!isEmpty(clz) && !isEmpty(meth)) {
				replaceMethod(classLoader, clz, meth, method);
			}
		}
	}

	/**
	 * replace method
	 * 
	 * @param classLoader classloader    原始类的类加载器
	 * @param clz class                  原始类的类名
	 * @param meth name of target method 原始类的原始方法名，即有bug的方法
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
				Method src = clazz.getDeclaredMethod(meth,
						method.getParameterTypes());//获取原始方法
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
