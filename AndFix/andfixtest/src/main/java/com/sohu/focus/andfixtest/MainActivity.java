package com.sohu.focus.andfixtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.StyleRes;
import android.view.ContextThemeWrapper;
import android.widget.TextView;

import com.sohu.focus.libandfix.annotation.MethodReplace;
import com.sohu.focus.libandfix.patch.PatchManager;
import com.souhu.hangzhang209526.zhanghang.base.BaseApplication;
import com.souhu.hangzhang209526.zhanghang.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends Activity {
    private Resources.Theme mTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ((TextView)findViewById(R.id.text_view)).setText(BugClass.getMsg());
//        ((TextView)findViewById(R.id.text_view)).setText(providerString());
//        TextView text_view = (TextView)findViewById(R.id.text_view);
//        String jj = getResources().getString(R.string.hello_world);
//        text_view.setText("123");
//        searchAndUpdatePatch();
        searchAndUpdatePatch();
    }

    @Override
    public AssetManager getAssets() {
       Resources resources = BaseApplication.getInstance().getPatchManager().getCanReplaceResource();
        return resources == null ? super.getAssets() : resources.getAssets();
    }

    @Override
    public Resources getResources() {
        Resources resources = BaseApplication.getInstance().getPatchManager().getCanReplaceResource();
        return resources == null ? super.getResources() : resources;
    }

    private String providerString(){
        return "修复bug前，提供的字符串";
    }

    /**
     * 搜索并更新补丁
     */
    private void searchAndUpdatePatch() {
        final BaseApplication baseApplication = BaseApplication.getInstance();
        AsyncTask<String, Void, ArrayList<File>> patchTask = new AsyncTask<String, Void, ArrayList<File>>() {
            @Override
            protected ArrayList<File> doInBackground(String... params) {
                File patchsFile = new File(params[0]);
                if (!patchsFile.exists()) {//如果不存在
                    patchsFile.mkdirs();
                    return null;
                } else if (patchsFile.isFile()) {//如果是文件
                    patchsFile.delete();
                    patchsFile.mkdirs();
                    return null;
                } else if (patchsFile.isDirectory()) {//遍历此目录
                    return FileUtils.traversalFiles(patchsFile);
                }
                return null;
            }

            @Override
            protected void onPostExecute(final ArrayList<File> files) {
                if (files != null && files.size() > 0) {//如果有补丁文件
                    Context baseContext = getBaseContext();
                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                    dialog.setMessage("找到补丁，是否立即更新之?");
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                for (File item : files) {
                                    if (item != null && item.getName().endsWith(PatchManager.SUFFIX) && baseApplication.getPatchManager() != null) {
                                        final String path = item.getAbsolutePath();
                                        baseApplication.getPatchManager().addPatch(path);//立即更新补丁
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    dialog.show();
                }
            }
        };
        String patch = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + PatchManager.SD_DIR;
        patchTask.execute(patch);
    }

    /**
     * 搜索并更新资源补丁
     */
    private void searchAndUpdateResPatch() {
        final BaseApplication baseApplication = BaseApplication.getInstance();
        AsyncTask<String, Void, ArrayList<File>> patchTask = new AsyncTask<String, Void, ArrayList<File>>() {
            @Override
            protected ArrayList<File> doInBackground(String... params) {
                File patchsFile = new File(params[0]);
                if (!patchsFile.exists()) {//如果不存在
                    patchsFile.mkdirs();
                    return null;
                } else if (patchsFile.isFile()) {//如果是文件
                    patchsFile.delete();
                    patchsFile.mkdirs();
                    return null;
                } else if (patchsFile.isDirectory()) {//遍历此目录
                    return FileUtils.traversalFiles(patchsFile);
                }
                return null;
            }

            @Override
            protected void onPostExecute(final ArrayList<File> files) {
                if (files != null && files.size() > 0) {//如果有补丁文件
                    Context baseContext = getBaseContext();
                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                    dialog.setMessage("找到补丁，是否立即更新之?");
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                for (File item : files) {
                                    if (item != null && baseApplication.getPatchManager() != null) {
                                        baseApplication.getPatchManager().addResourcePathc(item);//立即更新补丁
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    dialog.show();
                }
            }
        };
        String patch = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + PatchManager.SD_DIR;
        patchTask.execute(patch);
    }
}
