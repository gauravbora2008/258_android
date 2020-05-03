package com.paril.mlaclientapp.ui.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.util.Base64;
import android.util.Log;

import com.paril.mlaclientapp.util.PrefsManager;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

public class KeyHelper {

    private static final String TRANSFORMATION = "RSA/ECB/NOPADDING";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    private static byte[] encryption;
    public static byte[] iv;

    private static Cipher cipher;


    public static KeyStore keyStore;

    PrefsManager prefsManager;

    // generates a random grpkey string
    // encrypts it with public key from the keystore
    // returns the encrypted groupkeystring
    // assumes there is a public key stored in the keystore
    public static String createEncryptedGrpKey (String publicKeyString) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, CertificateException {

        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        String alias = "OWNER_34_GRP_76";
//        KeyStore.Entry entry = keyStore.getEntry(alias, null);

//        PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();

//        String pubKeyString = new String (Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT));

        System.out.println("pubKeyString in createEncryptedGrpKey method: " + publicKeyString);

        // Creating a Cipher object
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        byte[] publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT);
        KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever
        PublicKey regeneratedPublicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        //Initializing a Cipher object
//        cipher.init(Cipher.ENCRYPT_MODE, (RSAPublicKey) getPublicKeyFromKeyStore(keyStore, alias));
        cipher.init(Cipher.ENCRYPT_MODE, regeneratedPublicKey);


        //Add data to the cipher
        String genGrpKey = generateRandomString();
        System.out.println("genGrpKey: " + genGrpKey);

        byte[] genGrpKeyBytes = genGrpKey.getBytes();

//        System.out.println("cipher: " + cipher);
        cipher.update(genGrpKeyBytes);

        //encrypting the data
        byte[] cipherTextGroupKey = cipher.doFinal();

        String encodedCipherTextGroupKey = new String(Base64.encodeToString(cipherTextGroupKey, Base64.DEFAULT));
        System.out.println("encodedCipherTextGroupKey: " + encodedCipherTextGroupKey);

        return encodedCipherTextGroupKey;
    }

    //
    public static NewUserKeys getGroupKey(String alias, Context ctx) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, CertificateException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, UnrecoverableEntryException, InvalidKeySpecException {

        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 30);

        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(ctx)
                .setAlias(alias)
                .setSubject(new X500Principal("CN=" + alias))
                .setSerialNumber(BigInteger.TEN)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();

        //Creating a Signature object
//        Signature sign = Signature.getInstance("SHA256withRSA");

        //Creating KeyPair generator object
//        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
        keyPairGen.initialize(spec);

        //Generate the pair of keys
        KeyPair pair = keyPairGen.generateKeyPair();


        // Getting the public key from the key pair
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        // publickey.getEncoded ==> byte[]
        // byte[] ==> Base64 encode
        // Base64 encode ==> new String

        String pubKeyString = new String (Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT));

        System.out.println("pubKeyString: " + pubKeyString + "\n");


        // Creating a Cipher object
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        byte[] publicKeyBytes = Base64.decode(pubKeyString, Base64.DEFAULT);
        KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever
        PublicKey regeneratedPublicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        //Initializing a Cipher object
//        cipher.init(Cipher.ENCRYPT_MODE, (RSAPublicKey) getPublicKeyFromKeyStore(keyStore, alias));
        cipher.init(Cipher.ENCRYPT_MODE, regeneratedPublicKey);


        //Add data to the cipher
        String genGrpKey = generateRandomString();
        System.out.println("genGrpKey: " + genGrpKey);

        byte[] genGrpKeyBytes = genGrpKey.getBytes();

//        System.out.println("cipher: " + cipher);
        cipher.update(genGrpKeyBytes);

        //encrypting the data
        byte[] cipherTextGroupKey = cipher.doFinal();

        String encodedCipherTextGroupKey = new String(Base64.encodeToString(cipherTextGroupKey, Base64.DEFAULT));
        System.out.println("encodedCipherTextGroupKey: " + encodedCipherTextGroupKey);

        return new NewUserKeys(encodedCipherTextGroupKey, pubKeyString);
    }


    // alias - used to retreive private key from keystore
    public static String decryptData(String alias, String encryptedGrpKey)
            throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException,
            NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IOException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        KeyStore.Entry entry = keyStore.getEntry(alias, null);

        PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
//        PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();
//
//        String pubKeyString = new String (Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT));
//
//        System.out.println("pubKeyString in decryt method: " + pubKeyString);

        // Creating a Cipher object
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        //Initializing the same cipher for decryption
        cipher.init(Cipher.DECRYPT_MODE, privateKey);


//
//        //Decrypting the text
        byte[] decipheredText = cipher.doFinal(Base64.decode(encryptedGrpKey, Base64.DEFAULT));
//
        System.out.println("decipheredText: " + new String(decipheredText));

        return new String(decipheredText);
    }

    public static String generateRandomString() {
        byte[] array = new byte[64];
        new Random().nextBytes(array);
        String generatedString = new String(Base64.encodeToString(array, Base64.NO_WRAP));

//        System.out.println(generatedString);
        return generatedString;
    }

    public static class NewUserKeys {
        private String cipheredGroupKey;
        private String pubKey;

        public NewUserKeys(String grpKey, String _pubKey) {
            this.cipheredGroupKey = grpKey;
            this.pubKey = _pubKey;
        }

        public String getCipheredGroupKey() {
            return cipheredGroupKey;
        }

        public String getPubKey() {
            return pubKey;
        }
    }

}
