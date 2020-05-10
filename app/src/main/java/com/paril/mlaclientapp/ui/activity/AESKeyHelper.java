package com.paril.mlaclientapp.ui.activity;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import android.util.Base64;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESKeyHelper {

//    private static SecretKeySpec secretKey;
//    private static byte[] key;

//    public static void setKey(byte[] myKey)
//    {
//        MessageDigest sha = null;
//        try {
//
//            sha = MessageDigest.getInstance("SHA-1");
//            key = sha.digest(myKey);
//            System.out.println("Key byte : " + Arrays.toString(key));
//            key = Arrays.copyOf(key, 16);
//            secretKey = new SecretKeySpec(key, "AES");
//        }
//        catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static byte[] encrypt(byte[] strDataToEncrypt, byte[] secret)
//    {
//        try
//        {
//            setKey(secret);
//            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//            return cipher.doFinal(strDataToEncrypt);
//        }
//        catch (Exception e)
//        {
//            System.out.println("Error while encrypting: " + e.toString());
//        }
//        return null;
//    }
//
//    public static byte[] decrypt(byte[] strToDecrypt, byte[] secret)
//    {
//        try
//        {
//            setKey(secret);
//            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
//            cipher.init(Cipher.DECRYPT_MODE, secretKey);
//            return cipher.doFinal(strToDecrypt);
//        }
//        catch (Exception e)
//        {
//            System.out.println("Error while decrypting: " + e.toString());
//        }
//        return null;
//    }

    public static byte[] encrypt(byte[] dataToBeEncrypted, byte[] secret) {
        try {
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secret, "AES"), new IvParameterSpec(iv));
            byte[] cipherText = cipher.doFinal(dataToBeEncrypted);
            byte[] ivAndCipherText = getCombinedArray(iv, cipherText);

            System.out.println("Tag " + Arrays.toString(ivAndCipherText));

            return ivAndCipherText;
        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }
    }

    public static byte[] decrypt(byte[] decodedBytesToBeDecrypted, byte[] secret ) {
        try {
            byte[] ivAndCipherText = decodedBytesToBeDecrypted;
            byte[] iv = Arrays.copyOfRange(ivAndCipherText, 0, 16);
            byte[] cipherText = Arrays.copyOfRange(ivAndCipherText, 16, ivAndCipherText.length);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secret, "AES"), new IvParameterSpec(iv));
            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }
    }

    private static byte[] getCombinedArray(byte[] one, byte[] two) {
        byte[] combined = new byte[one.length + two.length];
        for (int i = 0; i < combined.length; ++i) {
            combined[i] = i < one.length ? one[i] : two[i - one.length];
        }
        return combined;
    }
}