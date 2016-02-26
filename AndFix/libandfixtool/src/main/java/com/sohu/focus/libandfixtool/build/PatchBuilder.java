package com.sohu.focus.libandfixtool.build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;

public class PatchBuilder
{
    private SignedJarBuilder mBuilder;

    public PatchBuilder(File outFile, File dexFile, PrivateKeyEntry key, PrintStream verboseStream)
    {
        try
        {
            this.mBuilder =
                    new SignedJarBuilder(new FileOutputStream(outFile, false), key.getPrivateKey(),
                            (X509Certificate)key.getCertificate());
            this.mBuilder.writeFile(dexFile, "classes.dex");//写入dex文件
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeJarEntry(InputStream input,JarEntry entry) throws IOException {
        mBuilder.writeEntry(input,entry);
    }

    public void writeMeta(Manifest manifest) {
        try {
            this.mBuilder.getOutputStream().putNextEntry(
                    new JarEntry("META-INF/PATCH.MF"));
            manifest.write(this.mBuilder.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sealPatch() {
        try {
            this.mBuilder.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}