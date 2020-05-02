package com.paril.mlaclientapp.ui.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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
import java.util.Calendar;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.security.auth.x500.X500Principal;

public class KeyHelper {

    @TargetApi(Build.VERSION_CODES.M)
    static KeyPair generateKey(String alias) {
        try {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 30);

            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    ANDROID_KEY_STORE,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setCertificateSubject(new X500Principal("CN=" + alias))
                    .setCertificateSerialNumber(BigInteger.TEN)
                    .setCertificateNotBefore(start.getTime())
                    .setCertificateNotAfter(end.getTime())
                    .build();

            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            kpg.initialize(spec);

            KeyPair keyPair = kpg.genKeyPair();
            return keyPair;

        } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException exception) {
            throw new RuntimeException("Unable to generate KeyPair");
        }
    }

    public static Keys retrieveKey(String alias) {
        try {

            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if (keyStore.containsAlias(alias)) {
                Log.d(alias + " Keys", " found........");
                KeyStore.Entry entry = keyStore.getEntry(alias, null);
                PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
                PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();
                return new Keys(privateKey, publicKey);
            } else {
                Log.d("Generating", " new keys..................");
                KeyPair kp = generateKey(alias);
                PrivateKey privateKey = kp.getPrivate();
                PublicKey publicKey = kp.getPublic();


//                System.out.println(privateKey);

//                System.out.println(new String(
//                        Base64.encodeToString(
//                                privateKey.getEncoded(),
//                                Base64.NO_WRAP)
//                ));
                return new Keys(privateKey, publicKey);
            }


        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableEntryException | IOException e) {
            throw new RuntimeException("Unable to retrieve keys");
        }
    }

    public static class Keys {
        private PrivateKey privKey;
        private PublicKey pubKey;



        public Keys(PrivateKey _privKey, PublicKey _pubKey) {
            this.privKey = _privKey;
            this.pubKey = _pubKey;
        }

// Note - you cannot get private key encoded. Only keystore can. Reference is the proof that key exists
        public String getPubKeyBytesString() {
            return new String(Base64.encodeToString(this.pubKey.getEncoded(), Base64.NO_WRAP));


        }
//
//        public String getPrivKeyBytesString() {
//            return new String(Base64.encodeToString(this.privKey.getEncoded(), Base64.NO_WRAP));
//        }

        public PrivateKey getPrivateKey() {
            return privKey;
        }

        public PublicKey getPublicKey() {
            return pubKey;
        }
    }

//    ==========================
    // Symmetric Encryption using secret key

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    private static byte[] encryption;
    private static byte[] iv;

    public static byte[] encryptText(final String alias, final String textToEncrypt)
            throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException,
            NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IOException,
            InvalidAlgorithmParameterException, SignatureException, BadPaddingException,
            IllegalBlockSizeException {

        final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(alias));

        iv = cipher.getIV();

        return (encryption = cipher.doFinal(textToEncrypt.getBytes("UTF-8")));
    }

    public static byte[] encryptTextWithGivenKey(String alias, String textToEncrypt, SecretKey givenkey)
            throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException,
            NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IOException,
            InvalidAlgorithmParameterException, SignatureException, BadPaddingException,
            IllegalBlockSizeException {

        final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, givenkey);

        iv = cipher.getIV();

        return (encryption = cipher.doFinal(textToEncrypt.getBytes("UTF-8")));
    }

    @NonNull
    public static SecretKey getSecretKey(String alias) throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException {

        final KeyGenerator keyGenerator = KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);

        keyGenerator.init(new KeyGenParameterSpec.Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());

        return keyGenerator.generateKey();
    }

    byte[] getEncryption() {
        return encryption;
    }

    byte[] getIv() {
        return iv;
    }

//    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
//    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    private KeyStore keyStore;

    void DeCryptor() throws CertificateException, NoSuchAlgorithmException, KeyStoreException,
            IOException {
        initKeyStore();
    }

    private void initKeyStore() throws KeyStoreException, CertificateException,
            NoSuchAlgorithmException, IOException {
        keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
    }

    String decryptData(final String alias, final byte[] encryptedData, final byte[] encryptionIv)
            throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException,
            NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IOException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        final GCMParameterSpec spec = new GCMParameterSpec(128, encryptionIv);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKeyDecryptor(alias), spec);

        return new String(cipher.doFinal(encryptedData), "UTF-8");
    }

    private SecretKey getSecretKeyDecryptor(final String alias) throws NoSuchAlgorithmException,
            UnrecoverableEntryException, KeyStoreException {
        return ((KeyStore.SecretKeyEntry) keyStore.getEntry(alias, null)).getSecretKey();
    }

    public static String generateRandomString() {
        byte[] array = new byte[64]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = new String(Base64.encodeToString(array, Base64.NO_WRAP));

        System.out.println(generatedString);
        return generatedString;
    }
}
