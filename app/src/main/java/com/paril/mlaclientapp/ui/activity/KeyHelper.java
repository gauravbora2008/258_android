package com.paril.mlaclientapp.ui.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.paril.mlaclientapp.util.PrefsManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;
import javax.security.cert.X509Certificate;


public class KeyHelper {

    public static KeyStore keyStore;

    public static void initKeyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
    }

    // used during registration
    public static NewUserKeys getGroupKey(Context ctx, String alias) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, CertificateException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, UnrecoverableEntryException, InvalidKeySpecException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {

        // 2) Setup KeyPairGenerator
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 30);

        Log.d("Setting ks entry: alias", alias);
        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(ctx)
                .setAlias(alias)
                .setSubject(new X500Principal("CN=" + alias))
                .setSerialNumber(BigInteger.TEN)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();

        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
        keyPairGen.initialize(spec);

        // 3) Generate the pair of keys, should save with alias
//        Keys pair = keyPairGen.generateKeyPair();
        Keys pair = retrieveKey(alias, ctx);
        PrivateKey privateKey = pair.getPrivateKey();
        PublicKey publicKey = pair.getPublicKey();

        Log.w("publicKeypublicKeypubli", Arrays.toString(publicKey.getEncoded()));
        System.out.println("publicKeypublicKeypublicKeypublicKey " + publicKey.getFormat());
        System.out.println("publicKeypublicKeypublicKeypublicKey " + publicKey.getAlgorithm());
        System.out.println("publicKeypublicKeypublicKeypublicKey " + publicKey.getClass());

        // 4) Check PublicKeyString that will be stored in db
        String pubKeyString = Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
//        String pubKeyString = publicKey.toString();
        Log.d("  Original pubKeyString", pubKeyString);
        Log.d("Original pubKeyStrBytes", Arrays.toString(pubKeyString.getBytes("utf-8")));

        // 5) Generate a new group key string
        String genGrpKey = generateRandomString();

        Log.d("          New genGrpKey", genGrpKey);
        Log.d("     New genGrpKeyBytes", Arrays.toString(genGrpKey.getBytes("utf-8")));
        byte[] genGrpKeyBytes = genGrpKey.getBytes("utf-8");

        // 6) Creating a Cipher object and encrypt the group key


        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        cipher.update(genGrpKeyBytes);
        Log.e("cipher blk size getKeys", "" + cipher.getBlockSize() );
        Log.e("cipher blk size getKeys", "" + cipher.getAlgorithm() );
        Log.e("cipher blk size getKeys", "" + cipher.getParameters() );
        byte[] cipherTextGroupKey = cipher.doFinal();

        // 7) encode and test new encrypted group key
        String encodedCipherTextGroupKey = Base64.encodeToString(cipherTextGroupKey, Base64.DEFAULT);
        Log.d("GrpKeyEncry with pubkey", encodedCipherTextGroupKey);

        // test
        String testDecryptedKey = decryptData(encodedCipherTextGroupKey, ctx, alias);
        Log.d("     Testing decryption", testDecryptedKey);

        // test two recreate key and decrypt
        String grpKeyEncryptedWithRecreatedPubKey = encryptGroupKeyUsingGivePubKeyString(pubKeyString, genGrpKey);
        System.out.println("grpKeyEncryptedWithRecreatedPubKey ======== " + grpKeyEncryptedWithRecreatedPubKey);

        String testDecryptedKey2 = decryptData(encodedCipherTextGroupKey, ctx, alias);
        Log.d("   Testing decryption 2", testDecryptedKey2);

        return new NewUserKeys(encodedCipherTextGroupKey, pubKeyString);
    }

    // generates a random grp/post key string
    // encrypts it with public key from the keystore
    // returns the encrypted group key string
    // assumes there is a public key stored in the keystore
    public static String createEncryptedKey(String publicKeyString) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, CertificateException, InvalidAlgorithmParameterException {


        System.out.println("pubKeyString in createEncryptedGrpKey method: " + publicKeyString);

        // Creating a Cipher object
        Cipher createEncryptedKeyCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        byte[] publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT);
        KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever
        PublicKey regeneratedPublicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));


        //Initializing a Cipher object
//        cipher.init(Cipher.ENCRYPT_MODE, (RSAPublicKey) getPublicKeyFromKeyStore(keyStore, alias));
        createEncryptedKeyCipher.init(Cipher.ENCRYPT_MODE, regeneratedPublicKey);


        //Add data to the cipher
        String genGrpKey = generateRandomString();
        System.out.println("genGrpKey: " + genGrpKey);

        byte[] genGrpKeyBytes = genGrpKey.getBytes("utf-8");

//        System.out.println("cipher: " + cipher);
        createEncryptedKeyCipher.update(genGrpKeyBytes);

        //encrypting the data
        byte[] cipherTextGroupKey = createEncryptedKeyCipher.doFinal();

        String encodedCipherTextGroupKey = Base64.encodeToString(cipherTextGroupKey, Base64.DEFAULT);
        System.out.println("encodedCipherTextGroupKey: " + encodedCipherTextGroupKey);

        return encodedCipherTextGroupKey;
    }



    // generates a random grp/post key string
    // encrypts it with public key from the keystore
    // returns the encrypted groupkeystring
    // assumes there is a public key stored in the keystore

    public static String encryptGroupKeyUsingGivePubKeyString(String publicKeyString, String decryptedGroupKey) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, CertificateException, InvalidAlgorithmParameterException {


        System.out.println("pubKeyString in createEncryptedGrpKey method: " + publicKeyString);

        // Creating a Cipher object
        Cipher createEncryptedKeyCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        byte[] publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT);
        KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever
        PublicKey regeneratedPublicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        // how to check if this key is same as original key?
        System.out.println("regeneratedPublicKey" + regeneratedPublicKey);
        System.out.println("regeneratedPublicKey" + regeneratedPublicKey.getClass());
        System.out.println("regeneratedPublicKey" + regeneratedPublicKey.getAlgorithm());
        System.out.println("regeneratedPublicKey" + regeneratedPublicKey.getFormat());
        Log.w("regeneratedPublicKey", Arrays.toString(regeneratedPublicKey.getEncoded()));
        //Initializing a Cipher object
//        cipher.init(Cipher.ENCRYPT_MODE, (RSAPublicKey) getPublicKeyFromKeyStore(keyStore, alias));
        createEncryptedKeyCipher.init(Cipher.ENCRYPT_MODE, regeneratedPublicKey);

        byte[] DecGrpKeyBytes = decryptedGroupKey.getBytes("utf-8");

//        System.out.println("cipher: " + cipher);
        createEncryptedKeyCipher.update(DecGrpKeyBytes);

        //encrypting the data
        byte[] cipherTextGroupKey = createEncryptedKeyCipher.doFinal();

        String encodedCipherTextGroupKey = Base64.encodeToString(cipherTextGroupKey, Base64.DEFAULT);
        System.out.println("encodedCipherTextGroupKey: " + encodedCipherTextGroupKey);

        return encodedCipherTextGroupKey;
    }

    @NonNull
    public static String decryptData(String encryptedGrpKey, Context ctx, String alias)
            throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException,
            NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IOException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, CertificateException {

//        Enumeration<String> aliases = keyStore.aliases();
//        System.out.println(aliases);
//        Log.d("Getting ks entry: alias", alias);

        Keys pair = retrieveKey(alias, ctx);

        PrivateKey privateKey = pair.getPrivateKey();

        // 3) encryptedGrpKey was Base64 encoded so it needs to be decoded first
//        byte[] decodedBytesOfGroupKey = Base64.decode(encryptedGrpKey, Base64.DEFAULT);
        byte[] decodedBytesOfGroupKey = Base64.decode(encryptedGrpKey, Base64.DEFAULT);
        Log.d(" decodedBytesOfGroupKey", Arrays.toString(decodedBytesOfGroupKey)); // correct
        Log.d(" decodedBytesOfGrpK len", "" + decodedBytesOfGroupKey.length);

        Cipher decryptDataCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        decryptDataCipher.init(Cipher.DECRYPT_MODE, privateKey);
        Log.w("DataCipher.getBlockSiz", "" + decryptDataCipher.getBlockSize());
        decryptDataCipher.update(decodedBytesOfGroupKey);
        byte[] decipheredTextBytes = decryptDataCipher.doFinal();
        Log.d("    decipheredTextBytes", Arrays.toString(decipheredTextBytes)); // correct
        Log.d("decipheredTextBytes Str", new String(decipheredTextBytes));

        return new String(decipheredTextBytes);
    }

//    @NonNull
//    public static String decryptDataUsingGivenPublicKeyStr(String encryptedGrpKey, String pubkeyStr, Context ctx, String alias)
//            throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException,
//            NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IOException,
//            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, CertificateException {
//
////        Enumeration<String> aliases = keyStore.aliases();
////        System.out.println(aliases);
////        Log.d("Getting ks entry: alias", alias);
//
//        Keys pair = retrieveKey(alias, ctx);
//        PublicKey publicKey = pair.getPublicKey();
//        PrivateKey privateKey = pair.getPrivateKey();
//
//        // 2) Creating and init Cipher object
//        Cipher decryptDataCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//        decryptDataCipher.init(Cipher.DECRYPT_MODE, privateKey);
//
//        // 3) encryptedGrpKey was Base64 encoded so it needs to be decoded first
//        byte[] decodedBytesOfGroupKey = Base64.decode(encryptedGrpKey.trim(), Base64.DEFAULT);
//        Log.d(" decodedBytesOfGroupKey", Arrays.toString(decodedBytesOfGroupKey)); // correct
//        Log.d(" decodedBytesOfGrpK len", "" + decodedBytesOfGroupKey.length);
//
//        // Decrypting the test using decodedBytesOfGroupKey
//        decryptDataCipher.update(decodedBytesOfGroupKey);
//        byte[] decipheredTextBytes = decryptDataCipher.doFinal();
//        Log.d("    decipheredTextBytes", Arrays.toString(decipheredTextBytes)); // correct
//        Log.d("decipheredTextBytes Str", new String(decipheredTextBytes));
//
//        return new String(decipheredTextBytes);
//    }

    public static String generateRandomString() {
        byte[] array = new byte[16];
        new Random().nextBytes(array);
        String generatedString = new String(Base64.encodeToString(array, Base64.DEFAULT)).substring(0, 16);
        generatedString.replace("\n", "$");

        Log.d("        generatedString", generatedString);
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

    @TargetApi(Build.VERSION_CODES.M)
    static KeyPair generateKey(String alias, Context ctx) {
        try {
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
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            kpg.initialize(spec);

            KeyPair keyPair = kpg.generateKeyPair();
            return keyPair;

        } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException exception) {
            throw new RuntimeException("Unable to generate KeyPair");
        }
    }

    public static Keys retrieveKey(String alias, Context ctx) {
        try {

            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            if (keyStore.containsAlias(alias)) {
                Log.d("new Keys", " not generated");
                KeyStore.Entry entry = keyStore.getEntry(alias, null);
                PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
                PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();
                return new Keys(privateKey, publicKey);
            } else {
                KeyPair kp = generateKey(alias, ctx);
                PrivateKey privateKey = kp.getPrivate();
                PublicKey publicKey = kp.getPublic();
                return new Keys(privateKey, publicKey);
            }


        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableEntryException | IOException e) {
            throw new RuntimeException("Unable to retrieve keys");
        }
    }

    public static class Keys {
        private PrivateKey privKey;
        private PublicKey pubKey;

        public Keys(PrivateKey privKey, PublicKey pubKey) {
            this.privKey = privKey;
            this.pubKey = pubKey;
        }

        public PrivateKey getPrivateKey() {
            return privKey;
        }

        public PublicKey getPublicKey() {
            return pubKey;
        }
    }

}
