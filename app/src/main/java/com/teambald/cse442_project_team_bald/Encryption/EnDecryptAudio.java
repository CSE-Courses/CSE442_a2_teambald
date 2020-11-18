package com.teambald.cse442_project_team_bald.Encryption;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class EnDecryptAudio {

    private static final String TAG = "EnDecrypt";

    /**
     * Encrypt and return the encoded bytes
     *
     * @return
     */
    public static byte[] encrypt(String filePath, Context context) {
        try {
            byte[] fileData = FileUtils.readFile(filePath);
            byte[] encodedBytes = AudioEncryptionUtils.encode(AudioEncryptionUtils.getInstance(context).getSecretKey(), fileData);
//            FileUtils.saveFile(encodedBytes, filePath);
            return encodedBytes;
        } catch (Exception e) {
            Log.e("Encryption", e.toString());
        }
        return null;
    }

    /**
     * Decrypt and return the decoded bytes
     *
     * @return
     */
    public static byte[] decrypt(File file, Context context) {
        String filePath = file.getPath();
        try {
            byte[] fileData = FileUtils.readFile(filePath);
            byte[] decryptedBytes = AudioEncryptionUtils.decode(AudioEncryptionUtils.getInstance(context).getSecretKey(), fileData);
            return decryptedBytes;
        } catch (Exception e) {
            Log.e("Decryption failed: ", e+"");
        }
        return null;
    }

    public static void writeByteToFile(byte[] bytes, String filePath)
    {
        try {

            // Initialize a pointer
            // in file using OutputStream
            OutputStream os = new FileOutputStream(filePath);

            // Starts writing the bytes in it
            os.write(bytes);
            System.out.println("Successfully" + " byte inserted");
            Log.d(TAG,"Successfully" + " byte inserted");
            // Close the file
            os.close();
        }

        catch (Exception e) {
            Log.d(TAG,"writeByteToFile Exception: " + e);
            e.printStackTrace();
        }
    }
}
