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

//    @TargetApi(Build.VERSION_CODES.M)
//    static KeyPair generateKey(String alias) {
//        try {
//            Calendar start = Calendar.getInstance();
//            Calendar end = Calendar.getInstance();
//            end.add(Calendar.YEAR, 30);
//
//            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
//                    ANDROID_KEY_STORE,
//                    KeyProperties.PURPOSE_ENCRYPT |
//                            KeyProperties.PURPOSE_DECRYPT)
//                    .setCertificateSubject(new X500Principal("CN=" + alias))
//                    .setCertificateSerialNumber(BigInteger.TEN)
//                    .setCertificateNotBefore(start.getTime())
//                    .setCertificateNotAfter(end.getTime())
//                    .build();
//
//            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
//            kpg.initialize(spec);
//
//            KeyPair keyPair = kpg.genKeyPair();
//            return keyPair;
//
//        } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException exception) {
//            throw new RuntimeException("Unable to generate KeyPair");
//        }
//    }

//    public static Keys retrieveKey(String alias) {
//        try {
//
//            keyStore = KeyStore.getInstance("AndroidKeyStore");
//            keyStore.load(null);
//
//            if (keyStore.containsAlias(alias)) {
//                Log.d(alias + " Keys", " found........");
//                KeyStore.Entry entry = keyStore.getEntry(alias, null);
//                PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
//                PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();
//                return new Keys(privateKey, publicKey);
//            } else {
//                Log.d("Generating", " new keys..................");
//                KeyPair kp = generateKey(alias);
//
//                PrivateKey privateKey = kp.getPrivate();
//                PublicKey publicKey = kp.getPublic();
//
//
////                System.out.println(privateKey);
//
////                System.out.println(new String(
////                        Base64.encodeToString(
////                                privateKey.getEncoded(),
////                                Base64.NO_WRAP)
////                ));
//                return new Keys(privateKey, publicKey);
//            }
//
//
//        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableEntryException | IOException e) {
//            throw new RuntimeException("Unable to retrieve keys");
//        }
//    }



//    ==========================
    // Symmetric Encryption using secret key


//    public static byte[] encryptText(final String alias, final String textToEncrypt)
//            throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException,
//            NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IOException,
//            InvalidAlgorithmParameterException, SignatureException, BadPaddingException,
//            IllegalBlockSizeException {
//
//        final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
//        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(alias));
//
//        iv = cipher.getIV();
//
//        return (encryption = cipher.doFinal(textToEncrypt.getBytes("UTF-8")));
//    }

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

        // Creating a Cipher object
//        Cipher cipher2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//
//        //Initializing the same cipher for decryption
//        cipher2.init(Cipher.DECRYPT_MODE, privateKey);
////
////        //Decrypting the text
//        byte[] decipheredText = cipher2.doFinal(Base64.decode(encodedCipherText, Base64.DEFAULT));
////
//        System.out.print("decipheredText: ");
//        System.out.println(new String(decipheredText));
//
//        return new String(decipheredText);
//        decryptData(alias, encodedCipherText);

        return new NewUserKeys(encodedCipherTextGroupKey, pubKeyString);
    }

    // generate and store key pair in AndroidKeyStore
//    private static KeyPair genStoreKeyPair(String alias) throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, NoSuchProviderException, InvalidAlgorithmParameterException, UnrecoverableEntryException {
//
//
//
//        return pair;
//
//
////        KeyPair keyPair = keyPairGen.genKeyPair();
////        return keyPair;
////
////        KeyPair kp = generateKey(alias);
//    }

//    public static PublicKey getPublicKeyFromKeyStore(KeyStore keystore, String alias) throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException, IOException, CertificateException {
//
//        // get keys from keystore
//        if (keyStore.containsAlias(alias)) {
//            Log.d(alias + " Keys", " found........");
//            KeyStore.Entry entry = keyStore.getEntry(alias, null);
////            PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
//            PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();
//
////            System.out.println("Private: "+ privateKey);
//            System.out.println("Pub:" + publicKey.getAlgorithm());
//            return publicKey;
//        }
//
//        return null;
//    }
//
//    public static PrivateKey getPrivateKeyFromKeyStore(KeyStore keyStore, String alias) throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException {
//        // get keys from keystore
//        if (keyStore.containsAlias(alias)) {
//            Log.d(alias + " Keys", " found........");
//            KeyStore.Entry entry = keyStore.getEntry(alias, null);
//            PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
////            PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();
//
//            System.out.println("Private: " + privateKey);
////            System.out.println("Pub:"+publicKey.getEncoded());
//            return privateKey;
//        }
//        return null;
//    }
//
//
//    public static byte[] encryptTextWithGivenKey(String textToEncrypt, PublicKey givenkey)
//            throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException,
//            NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IOException,
//            InvalidAlgorithmParameterException, SignatureException, BadPaddingException,
//            IllegalBlockSizeException {
//
//        cipher = Cipher.getInstance(TRANSFORMATION);
//        cipher.init(Cipher.ENCRYPT_MODE, givenkey);
//
//        iv = cipher.getIV();
//
//        return (encryption = cipher.doFinal(textToEncrypt.getBytes("UTF-8")));
//    }

//    @NonNull
//    public static SecretKey getSecretKey(String alias) throws NoSuchAlgorithmException,
//            NoSuchProviderException, InvalidAlgorithmParameterException {
//
//        final KeyGenerator keyGenerator = KeyGenerator
//                .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
//
//        keyGenerator.init(new KeyGenParameterSpec.Builder(alias,
//                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
//                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
//                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
//                .build());
//
//        return keyGenerator.generateKey();
//    }

//    byte[] getEncryption() {
//        return encryption;
//    }
//
//    public static byte[] getIv() {
//        return iv;
//    }

//    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
//    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";


//    void DeCryptor() throws CertificateException, NoSuchAlgorithmException, KeyStoreException,
//            IOException {
//        initKeyStore();
//    }

//    private void initKeyStore() throws KeyStoreException, CertificateException,
//            NoSuchAlgorithmException, IOException {
//        keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
//        keyStore.load(null);
//    }

    // alias - used to retreive private key from keystore
    public static String decryptData(String alias, String encryptedGrpKey)
            throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException,
            NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IOException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        KeyStore.Entry entry = keyStore.getEntry(alias, null);
        PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();

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

//    public static String decryptData(final String alias, final byte[] encryptedData)
//            throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException,
//            NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IOException,
//            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
//
//        final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
//        byte[] encryptionIv = getIv();
//        final GCMParameterSpec spec = new GCMParameterSpec(128, encryptionIv);
//        cipher.init(Cipher.DECRYPT_MODE, getSecretKeyDecryptor(alias), spec);
//
//        return new String(cipher.doFinal(encryptedData), "UTF-8");
//    }

//    private static SecretKey getSecretKeyDecryptor(final String alias) throws NoSuchAlgorithmException,
//            UnrecoverableEntryException, KeyStoreException {
//        return ((KeyStore.SecretKeyEntry) keyStore.getEntry(alias, null)).getSecretKey();
//    }

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
