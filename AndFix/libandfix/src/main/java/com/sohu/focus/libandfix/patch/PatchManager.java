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
import android.content.SharedPreferences;
import android.util.Log;


import com.sohu.focus.libandfix.AndFixManager;
import com.sohu.focus.libandfix.util.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	private static final String TAG = "PatchManager";
	// patch extension
	private static final String SUFFIX = ".apatch";
	private static final String DIR = "apatch";
	private static final String SP_NAME = "_andfix_";
	private static final String SP_VERSION = "version";

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
	 * 补丁目录
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
	private final Map<String, ClassLoader> mLoaders;

	/**
	 * @param context
	 *            context
	 */
	public PatchManager(Context context) {
		mContext = context;
		mAndFixManager = new AndFixManager(mContext);
		mPatchDir = new File(mContext.getFilesDir(), DIR);
		mPatchs = new ConcurrentSkipListSet<Patch>();
		mLoaders = new ConcurrentHashMap<String, ClassLoader>();
	}

	/**
	 * initialize
	 * 
	 * @param appVersion
	 *            App version
	 */
	public void init(String appVersion) {
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
		if (ver == null || !ver.equalsIgnoreCase(appVersion)) {
			cleanPatch();//清空文件夹
			sp.edit().putString(SP_VERSION, appVersion).commit();
		} else {
			initPatchs();//加载补丁目录下的所有后缀名为'.apatch'的文件
		}
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
	 * @param file
	 * @return patch
	 */
	private Patch addPatch(File file) {
		Patch patch = null;
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
	public void loadPatch(String patchName, ClassLoader classLoader) {
		mLoaders.put(patchName, classLoader);
		Set<String> patchNames;
		List<String> classes;
		for (Patch patch : mPatchs) {
			patchNames = patch.getPatchNames();
			if (patchNames.contains(patchName)) {
				classes = patch.getClasses(patchName);
				mAndFixManager.fix(patch.getFile(), classLoader, classes);
			}
		}
	}

	/**
	 * load patch,call when application start
	 * 
	 */
	public void loadPatch() {
		mLoaders.put("*", mContext.getClassLoader());// wildcard 通配符
		Set<String> patchNames;
		List<String> classes;
		for (Patch patch : mPatchs) {
			patchNames = patch.getPatchNames();
			for (String patchName : patchNames) {
				classes = patch.getClasses(patchName);
				mAndFixManager.fix(patch.getFile(), mContext.getClassLoader(),
						classes);
			}
		}
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
			if (mLoaders.containsKey("*")) {
				cl = mContext.getClassLoader();
			} else {
				cl = mLoaders.get(patchName);
			}
			if (cl != null) {
				classes = patch.getClasses(patchName);
				mAndFixManager.fix(patch.getFile(), cl, classes);
			}
		}
	}

}
