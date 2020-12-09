package com.teambald.cse442_project_team_bald.Encryption;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AudioEncryptionUtils {
    public static AudioEncryptionUtils instance = null;
    private static PrefUtils prefUtils;
    private static String KEY_ALGORITHM = "AES";
    public static final String LogInEmail = "LogInEmail";

    public static AudioEncryptionUtils getInstance(Context context) {

        if (null == instance)
            instance = new AudioEncryptionUtils();

        if (null == prefUtils)
            prefUtils = PrefUtils.getInstance(context);

        return instance;
    }

    public static byte[] encode(SecretKey yourKey, byte[] fileData)
            throws Exception {
        byte[] data = yourKey.getEncoded();
        SecretKeySpec skeySpec = new SecretKeySpec(data, 0, data.length, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        return cipher.doFinal(fileData);
    }

    public static byte[] decode(SecretKey yourKey, byte[] fileData)
            throws Exception {
        byte[] decrypted;
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, yourKey, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        decrypted = cipher.doFinal(fileData);
        return decrypted;
    }

    public void saveSecretKey(SecretKey secretKey) {
        String encodedKey = Base64.encodeToString(secretKey.getEncoded(), Base64.NO_WRAP);
        prefUtils.saveSecretKey(encodedKey);
    }

    public SecretKey getSecretKey(Context context) {
        final int outputKeyLength = 256;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String email = sharedPref.getString(LogInEmail, "");
        //Unique to email
        String encodedKey = "";
        try{
            encodedKey = SHA256(email);
        }catch (NoSuchAlgorithmException e){
            Log.e("AudioEncryptionUtils getSecretKey: ", e.toString());
        }

        if (null == encodedKey || encodedKey.isEmpty()) {
            SecureRandom secureRandom = new SecureRandom();
            KeyGenerator keyGenerator = null;
            try {
                keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            keyGenerator.init(outputKeyLength, secureRandom);
            SecretKey secretKey = keyGenerator.generateKey();
            saveSecretKey(secretKey);
            return secretKey;
        }

        byte[] decodedKey = Base64.decode(encodedKey, Base64.NO_WRAP);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, KEY_ALGORITHM);
        return originalKey;
    }

    public static String SHA256 (String text) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(text.getBytes());
        byte[] digest = md.digest();

        return Base64.encodeToString(digest, Base64.DEFAULT);
    }
}
