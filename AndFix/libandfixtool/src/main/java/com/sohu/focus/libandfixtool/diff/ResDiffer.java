package com.sohu.focus.libandfixtool.diff;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by hangzhang209526 on 2016/2/25.
 */
public class ResDiffer {
    public DiffInfo diff(File newFile, File oldFile)
            throws IOException {
        DiffInfo info = DiffInfo.getInstance();
        ZipFile newZipFile = new ZipFile(newFile);
        ZipInputStream newZipInput = new ZipInputStream(new FileInputStream(newFile));
        ZipFile oldZipFile = new ZipFile(oldFile);
        ZipEntry zipEntry;
        while ((zipEntry=newZipInput.getNextEntry())!=null){
            if(zipEntry.getName().endsWith(".xml")||zipEntry.getName().endsWith(".arsc")) {
                ZipEntry oldZipEntry = oldZipFile.getEntry(zipEntry.getName());
                if (oldZipEntry == null) {
                    byte[] content = getSpeacilFile(newZipFile, zipEntry);
                    info.modifiedRes.put(zipEntry.getName(), content);
                } else {
                    byte[] newContent = getSpeacilFile(newZipFile, zipEntry);
                    byte[] oldContent = getSpeacilFile(oldZipFile, oldZipEntry);
                    for (int i = 0; i < newContent.length; i++) {
                        if (newContent[i] != oldContent[i]) {
                            info.modifiedRes.put(zipEntry.getName(), newContent);
                            break;
                        }
                    }
                }
            }
        }
        newZipInput.close();
        return info;
    }


    private byte[] getSpeacilFile(ZipFile sourcFile,ZipEntry entry){
        ByteArrayOutputStream byteArrayOutputStream=null;
        BufferedReader inputStream=null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            inputStream = new BufferedReader(new InputStreamReader(sourcFile.getInputStream(entry)));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line=inputStream.readLine())!=null){
                result.append(line);
            }
            return result.toString().getBytes("gbk");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(byteArrayOutputStream!=null){
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ;
            }
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ;
            }
        }
        return null;
    }
}
