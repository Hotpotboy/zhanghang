package com.common;

import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor

public class HotFixTool {
    private static final int BUF_SIZE = 2048;
    public static final String DEX_FILE_NAME = "hotfix_dex.jar";
    public static final String JAR_FILE_NAME = "hotfix.jar"
    private static final String DECALRE_CLASS_PACKGE = "com.sohu.focus.hotfixlib.DecalreClass"
    private static ClassPool classPool
    /**
     * 将相关class文件转换为dex文件
     * @param paths class文件路径数组
     * @param outDir 生成文件路径
     */
    public static void toDex(String pathStr, String outDir, String packageDir) {
        if (isEmpty(outDir)) return;
        File outDirFile = new File(outDir);
        if (outDirFile.exists()) {
            try {
                outDirFile.delete();
            } catch (Exception e) {
                println(outDir + " is create ok?【" + e.toString() + "】111111111111111111111111111111111111111111111111111111111111111112");
            }
        }
        outDirFile.mkdirs();
        String[] paths = pathStr.split(",");
        String[] packageDirs = packageDir.split(",")
        if (paths != null && paths.length > 0) {
            for (int i=0;i<paths.length;i++) {
                String path = paths[i]
                if (!isEmpty(path)) {
                    File file = new File(path);
                    if (file.exists() && path.endsWith(".class")) {//如果是class文
                        File packageFile = new File(outDir + packageDirs[i])
                        if (!packageFile.exists()) packageFile.mkdirs();
                        copyFile(file, new File(outDir + packageDirs[i], file.getName()))
                        println("77777777777777777777777777"+file.exists())
                    }
                }
            }
        }
        File[] classFiles = outDirFile.listFiles();
        if (classFiles != null && classFiles.length > 0) {
            def proc = ["jar", "cf", JAR_FILE_NAME, "-C", outDir, "com"].execute();
            if (proc.waitFor() != 0) {
                println "[WARNING] ${proc.err.text.trim()}"
            }
            File jarFile = new File(JAR_FILE_NAME);
            copyFile(jarFile, new File(outDir, JAR_FILE_NAME))
            jarFile.delete()
            proc = ["D:\\android_studio\\sdk\\build-tools\\23.0.1\\dx.bat", "--dex", "--output=" + outDir + DEX_FILE_NAME, outDir + JAR_FILE_NAME].execute();
            if (proc.waitFor() != 0) {
                println "[WARNING] ${proc.err.text.trim()}"
            }
        }
    }

    private static boolean isEmpty(String str) {
        if (str == null) return true;
        if ("".equals(str)) return true;
        return false
    }

    private static boolean copyFile(File from, File to) {
        BufferedInputStream bis = null;
        OutputStream dexWriter = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(from));
            dexWriter = new BufferedOutputStream(new FileOutputStream(to));
            byte[] buf = new byte[BUF_SIZE];
            int len;
            while ((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            bis.close();
            return true;
        } catch (IOException e) {
            if (dexWriter != null) {
                try {
                    dexWriter.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            return false;
        }
    }

    public static void addConstructer(String classPath, String hotFixLib) {
        classPool = ClassPool.getDefault();
        classPool.appendClassPath(classPath)//指定classdex.dex中class文件对应的路径
        classPool.appendClassPath(hotFixLib)//指定非classdex.dex中class文件对应的路径
        File classDir = new File(classPath)
        if (classDir.exists() && !classDir.isFile()) {
            File[] classFiles = classDir.listFiles()
            realAddConstructer(classFiles, classPath)
        }
    }

    private static void realAddConstructer(File[] files, final String classPath) {
        if (files == null || files.length <= 0) return
        for (File file : files) {
            if (!file.exists()) continue
            if (file.isFile()) {
                String filePath = file.getAbsolutePath()
                if (filePath.endsWith(".class")) {
                    filePath = filePath.substring(classPath.length() + 1, filePath.length())
                    filePath = filePath.substring(0, filePath.indexOf(file.getName()) - 1)
                    String fileName = file.getName()
                    fileName = fileName.substring(0,fileName.indexOf("."))
                    filePath = filePath.replaceAll("\\" + File.separator, ".") + "." + fileName
                    println("1111111111111111111111111111111111" + filePath)
                    if (filePath.startsWith("com.sohu")
                         &&!fileName.startsWith("R\$")
                         &&!"R".equals(fileName)) {
                        CtClass ctClass = classPool.getCtClass(filePath)
                        if (ctClass.isFrozen()) {
                            ctClass.defrost()
                        }
                        CtConstructor constructor = ctClass.getConstructors()[0];
                        constructor.insertBefore("System.out.println("+DECALRE_CLASS_PACKGE+".class);")
                        ctClass.writeFile(classPath)
                        println("====添加构造方法===="+filePath)
                    }
                } else continue
            } else {
                File[] classFile = file.listFiles()
                realAddConstructer(classFile, classPath)
            }
        }
    }
}