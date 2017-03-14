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

package com.sohu.focus.libandfix.patch;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.Log;


import com.sohu.focus.libandfix.AndFixManager;
import com.sohu.focus.libandfix.util.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * patch manager
 * 补丁管理
 * @author sanping.li@alipay.com
 * 
 */
public class PatchManager {
	public static final String SUFFIX = ".apatch";
	public static final String SD_DIR = "patchs";
	private static final String TAG = "PatchManager";
	// patch extension
	private static final String DIR = "apatch";
	private static final String SP_NAME = "_andfix_";
	private static final String SP_VERSION = "version";
	/**是否是第一次加载补丁包*/
	private boolean isFirst = true;

	/**
	 * context
	 */
	private final Context mContext;
	/**
	 * AndFix manager
	 * 修复管理
	 */
	private final AndFixManager mAndFixManager;
	/**
	 * patch directory
	 * 补丁目录/data/data/[package-name]/files/apatch
	 */
	private final File mPatchDir;
	/**
	 * patchs
	 * 所有的补丁
	 */
	private final SortedSet<Patch> mPatchs;
	/**
	 * classloaders
	 * 类加载器
	 */
//	private final Map<String, ClassLoader> mLoaders;
	/**
	 * 替换资源
	 */
	private CanReplaceResource mCanRepalceResource;

//	public void replaceResource(Context context){
//		if(context!=null&&context instanceof ContextWrapper){
//			try {
//				ContextWrapper contextWrapper = (ContextWrapper) context;
//				Field field = contextWrapper.getBaseContext().getClass().getField("mResource");
//				field.setAccessible(true);
//				field.set(context,mCanRepalceResource);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}

	public void addResourcePathc(File file) throws IOException {
		if(mCanRepalceResource!=null) mCanRepalceResource.addOrgFile(file);
	}

	public Resources getCanReplaceResource(){
		return mCanRepalceResource;
	}

	/**
	 * @param context
	 *            context
	 */
	public PatchManager(Context context) {
		mContext = context;
		mAndFixManager = new AndFixManager(mContext);//负责代码补丁的修复(替换)
		mPatchDir = new File(mContext.getFilesDir(), DIR);//补丁包保存目录/data/data/[package-name]/files/apatch
		mPatchs = new ConcurrentSkipListSet<Patch>();//所有的代码补丁包集合
//		mLoaders = new ConcurrentHashMap<String, ClassLoader>();
	}

	/**
	 * initialize
	 * 
	 * @param appVersion App version
	 * @param isNeedReplaceRes  是否开启替换资源的功能
	 */
	public void init(String appVersion,boolean isNeedReplaceRes) {
		//确保代码补丁包目录存在
		if (!mPatchDir.exists() && !mPatchDir.mkdirs()) {// make directory fail
			Log.e(TAG, "patch dir create error.");
			return;
		} else if (!mPatchDir.isDirectory()) {// not directory 不是一个文件夹
			mPatchDir.delete();
			return;
		}
		SharedPreferences sp = mContext.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE);
		String ver = sp.getString(SP_VERSION, null);
		//版本号与保存的版本号是否一致
		if (ver == null || !ver.equalsIgnoreCase(appVersion)) {
			//清空补丁目录和优化目录的所有文件
			cleanPatch();
			sp.edit().putString(SP_VERSION, appVersion).commit();
		} else {
			//加载补丁目录下的所有后缀名为'.apatch'的文件
			initPatchs();
		}
		//初始化资源替换器
		if(isNeedReplaceRes)
			mCanRepalceResource = new CanReplaceResource(mContext);
	}

	public boolean canReplaceRes(){
		return mCanRepalceResource!=null;
	}

	private void initPatchs() {
		File[] files = mPatchDir.listFiles();
		for (File file : files) {
			addPatch(file);
		}
	}

	/**
	 * add patch file
	 * 
	 * @param file    .apatch格式的文件
	 * @return patch
	 */
	private Patch addPatch(File file) {
		Patch patch = null;
		//判断文件的后缀名
		if (file.getName().endsWith(SUFFIX)) {
			try {
				patch = new Patch(file);
				mPatchs.add(patch);
			} catch (IOException e) {
				Log.e(TAG, "addPatch", e);
			}
		}
		return patch;
	}

	private void cleanPatch() {
		File[] files = mPatchDir.listFiles();
		for (File file : files) {
			mAndFixManager.removeOptFile(file);
			if (!FileUtil.deleteFile(file)) {
				Log.e(TAG, file.getName() + " delete error.");
			}
		}
		if(mCanRepalceResource!=null)
		    mCanRepalceResource.cleanAllDir();
	}

	/**
	 * add patch at runtime
	 * 及时添加apatch文件
	 * @param path
	 *            patch path
	 * @throws IOException
	 */
	public void addPatch(String path) throws IOException {
		File src = new File(path);
		File dest = new File(mPatchDir, src.getName());
		if(!src.exists()){
			throw new FileNotFoundException(path);
		}
		if (dest.exists()) {
			Log.d(TAG, "patch [" + path + "] has be loaded.");
			return;
		}
		FileUtil.copyFile(src, dest);// copy to patch's directory
		Patch patch = addPatch(dest);
		if (patch != null) {
			loadPatch(patch);
		}
	}

	/**
	 * remove all patchs
	 */
	public void removeAllPatch() {
		cleanPatch();
		SharedPreferences sp = mContext.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE);
		sp.edit().clear().commit();
	}

	/**
	 * load patch,call when plugin be loaded. used for plugin architecture.</br>
	 * 
	 * need name and classloader of the plugin
	 * 加载补丁，当插件被加载时调用。被插件架构使用。需要插件的名字和其类加载器
	 * @param patchName
	 *            patch name
	 * @param classLoader
	 *            classloader
	 */
//	public void loadPatch(String patchName, ClassLoader classLoader) {
//		mLoaders.put(patchName, classLoader);
//		Set<String> patchNames;
//		List<String> classes;
//		for (Patch patch : mPatchs) {
//			patchNames = patch.getPatchNames();
//			if (patchNames.contains(patchName)) {
//				classes = patch.getClasses(patchName);
//				mAndFixManager.fix(patch.getFile(), classLoader, classes);
//			}
//		}
//	}

	/**
	 * load patch,call when application start
	 * 
	 */
	public void loadPatch() {
//		mLoaders.put("*", mContext.getClassLoader());// wildcard 通配符
		for (Patch patch : mPatchs) {
			Set<String> patchNames = patch.getPatchNames();
			for (String patchName : patchNames) {
				List<String> classes = patch.getClasses(patchName);//补丁包中所有的类的类名集合，这些类都是需要修复的
				mAndFixManager.fix(patch.getFile(), mContext.getClassLoader(),classes,isFirst);
			}
		}
		isFirst = false;
	}

	/**
	 * load specific patch
	 * 
	 * @param patch
	 *            patch
	 */
	private void loadPatch(Patch patch) {
		Set<String> patchNames = patch.getPatchNames();
		ClassLoader cl;
		List<String> classes;
		for (String patchName : patchNames) {
			cl = mContext.getClassLoader();
//			if (mLoaders.containsKey("*")) {
//				cl = mContext.getClassLoader();
//			} else {
//				cl = mLoaders.get(patchName);
//			}
			if (cl != null) {
				classes = patch.getClasses(patchName);
				mAndFixManager.fix(patch.getFile(), cl, classes,isFirst);
			}
		}
	}

}
