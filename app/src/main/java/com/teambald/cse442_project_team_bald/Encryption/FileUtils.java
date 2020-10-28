package com.teambald.cse442_project_team_bald.Encryption;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

    public static void saveFile(byte[] encodedBytes, String path) {
        try {
            File file = new File(path);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(encodedBytes);
            bos.flush();
            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static byte[] readFile(String filePath) {
        byte[] contents;
        File file = new File(filePath);
        int size = (int) file.length();
        contents = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(
                    new FileInputStream(file));
            try {
                buf.read(contents);
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return contents;
    }

    @NonNull
    public static File createTempFile(Context context, byte[] decrypted) throws IOException {
        File tempFile = File.createTempFile("PLAYING_TEMP", ".mp4", context.getCacheDir());
        tempFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(decrypted);
        fos.close();
        return tempFile;
    }

    public static FileDescriptor getTempFileDescriptor(Context context, byte[] decrypted) throws IOException {
        File tempFile = FileUtils.createTempFile(context, decrypted);
        FileInputStream fis = new FileInputStream(tempFile);
        return fis.getFD();
    }
}