package com.paril.mlaclientapp.ui.activity;

import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;


import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
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
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import static junit.framework.Assert.assertTrue;

public class KeyHelper2 {

    public static KeyStore keyStore;
    public static PrivateKey privateKey;
    public static PublicKey publicKey;
    public static PublicKey regeneratedPublicKey;
    public static OAEPParameterSpec oaepSpec;
    public static String cipherInstanceString = "RSA/ECB/NoPadding";;

    public static void initKeyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        oaepSpec = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-1"), PSource.PSpecified.DEFAULT);

//        cipherInstanceString = "RSA/ECB/OAEPwithSHA-1andMGF1Padding";
//        cipherInstanceString = "RSA/ECB/PKCS1Padding";
        cipherInstanceString = "RSA/ECB/NoPadding";


    }

    // without android key store
    public static KeyPair genKeyPairAndGetPubKey2(String alias) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException, UnrecoverableKeyException, CertificateException, SignatureException, KeyStoreException, IOException, InvalidKeySpecException {

        // We are creating the key pair with sign and verify purposes
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "BC");

        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);

        byte[] array = new byte[8];
        new Random().nextBytes(array);

        kpg.initialize(spec, new SecureRandom(array));

        KeyPair keyPair = kpg.generateKeyPair();

        // Check keypair
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        System.out.println("privateKey NEW : " + privateKey);
        System.out.println("publicKey NEW : " + publicKey);

        System.out.println("publicKeyLength Reg " + Arrays.toString(publicKey.getEncoded()));
        System.out.println("privateKey NEW : " + Arrays.toString(privateKey.getEncoded()));

        return keyPair;

    }


    // 1b) Recreate Public Key From String

    public static PublicKey getPublicKeyFromString(String pubKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {

        // decode Base64 to get byte[]
        byte[] pubKeyBytesB64Decoded = decodeB64(pubKeyString);

        // Create a public key from this
        X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(pubKeyBytesB64Decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PublicKey regeneratedPublicKey = keyFactory.generatePublic(bobPubKeySpec);

        System.out.println("regeneratedPublicKey.getFormat() : " + regeneratedPublicKey.getFormat());

        System.out.println(regeneratedPublicKey.getEncoded().toString());

        return regeneratedPublicKey;
    }

    // 2) encrypt GroupKey Using Received PubKey
    public static byte[] encryptGroupKeyUsingPubKey(PublicKey PubKey, byte[] groupKey) throws Exception {

        Log.w("groupKey length", groupKey.length + "");
        // encrypt using this byte[]
        Cipher newCipher = Cipher.getInstance(cipherInstanceString, "BC");
        newCipher.init(Cipher.ENCRYPT_MODE, PubKey);
        return newCipher.doFinal(groupKey);

    }

    public static byte[] decryptGroupKey(byte[] encGrpKey, PrivateKey privateKeyPara) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, IOException, CertificateException, KeyStoreException, UnrecoverableEntryException {

        Cipher decryptCipher = Cipher.getInstance(cipherInstanceString, "BC");
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKeyPara);
        return decryptCipher.doFinal(encGrpKey);

    }

    public static String generateRandomString() throws UnsupportedEncodingException {
        byte[] array = new byte[16];
        new SecureRandom().nextBytes(array);
        String generatedString = encodeB64(array).substring(0, 16);

        Log.d("generatedString", generatedString);
        return generatedString;
    }

    public static byte[] generateRandomBytes() throws UnsupportedEncodingException {
        byte[] array = new byte[16];
        new SecureRandom().nextBytes(array);

        Log.d("generatedBytes", Arrays.toString(array));
        return array;
    }

    public static String signData(String dataToBeSigned, PrivateKey privKey) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, InvalidKeyException, SignatureException {

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privKey);
        signature.update(dataToBeSigned.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = signature.sign();

        String signatureResult = "";

        System.out.println("signature.toString() : " + signature.toString());

        // We encode and store in a variable the value of the signature
        if (signature != null)
            signatureResult = encodeB64(signatureBytes);

        System.out.println("signatureResult ====== " + signatureResult);
        return signatureResult;
    }

    public static boolean verifyData(String signatureResult, String dataToBeVerified, String sendersPubKey) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, InvalidKeyException, SignatureException, InvalidKeySpecException, NoSuchProviderException, ClassNotFoundException {

        byte[] B64DecSendersPubKey = decodeB64(sendersPubKey);

//        Certificate cert = keyStore.getCertificate(alias);
        PublicKey tempPubKey = (PublicKey) deserializeKey(B64DecSendersPubKey);

//        System.out.println("Certificate ==>> " + cert.toString());

        System.out.println("signature.toString() : " + signatureResult.toString());
        System.out.println("dataToBeVerified : " + dataToBeVerified);

        //We decode the signature value
        byte[] signature = decodeB64(signatureResult);

        //We check if the signature is valid. We use RSA algorithm along SHA-256 digest algorithm
        Signature isVali = Signature.getInstance("SHA256withRSA");
        isVali.initVerify(tempPubKey);
        isVali.update(dataToBeVerified.getBytes(StandardCharsets.UTF_8));

        return isVali.verify(signature);


    }


    public static String getStringFromPublicKey(PublicKey pubKey) throws NoSuchAlgorithmException, InvalidKeySpecException {

        // get byte [] from pubKey
        byte[] pubKeyBytes = pubKey.getEncoded();

        // Convert it to string
        String pubKeyBytesString = encodeB64(pubKeyBytes);

        return pubKeyBytesString;

    }

    public static String encodeB64(byte[] stringBytes) {
        return Base64.encodeToString(stringBytes, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_CLOSE | Base64.NO_WRAP);
    }

    public static byte[] decodeB64(String B64EncodedString) {
        return Base64.decode(B64EncodedString, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_CLOSE | Base64.NO_WRAP);
    }

    public static Boolean comparePublicKeys(PublicKey temp, PublicKey publicKey) throws KeyStoreException {

        byte[] pubKey1Bytes = Hex.encode(temp.getEncoded());
        byte[] pubKey2Bytes = Hex.encode(publicKey.getEncoded());

        Boolean testPubKeys = pubKey1Bytes.equals(pubKey2Bytes);

        Log.d("comparing PubKeys", testPubKeys.toString());

        return testPubKeys;
    }

    public static PublicKey getPubKeyFromKeyStore(String alias) throws KeyStoreException {
        return keyStore.getCertificate(alias).getPublicKey();
    }

    public static PrivateKey getPrivateKeyFromString(String privateKeyEnc) {

        byte[] sigBytes = decodeB64(privateKeyEnc);
//        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(sigBytes);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(sigBytes);
        KeyFactory keyFact = null;
        try {
            keyFact = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            return keyFact.generatePrivate(pkcs8EncodedKeySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static byte[] serializeKey(Key kp) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(kp);
        byte[] res = b.toByteArray();

        o.close();
        b.close();

        return res;
    }

    public static Key deserializeKey(byte[] serKey) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bi = new ByteArrayInputStream(serKey);
        ObjectInputStream oi = new ObjectInputStream(bi);
        Object obj = oi.readObject();
        assertTrue(obj instanceof Key);

        oi.close();
        bi.close();

        return (Key) obj;
    }
}

