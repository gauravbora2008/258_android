package com.paril.mlaclientapp.ui.activity;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Timestamp;
import java.util.Date;

/**
 * Created by dell on 5/8/2020.
 */

public class KeyHelper3 {

    public static String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
                    Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static void GetTimestamp(String info){
//        System.out.println(info + new Timestamp();
    }

    public static AsymmetricCipherKeyPair GenerateKeys() throws NoSuchAlgorithmException {
//	Source: http://stackoverflow.com/questions/3087049/bouncy-castle-rsa-keypair-generation-using-lightweight-api
        RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
        generator.init(new RSAKeyGenerationParameters
                (
                        new BigInteger("10001", 16),//publicExponent
                        SecureRandom.getInstance("SHA1PRNG"),//pseudorandom number generator
                        4096,//strength
                        80//certainty
                ));

        return generator.generateKeyPair();
    }

//    public static String getPubKeyAsyToStr(byte[] data, String pubKeyString) {
//
//    }
//
//    public static AsymmetricKeyParameter getPubKeyStrToAsy (byte[] data, String pubKeyString) {
//        byte[] decidedPubKeySte = hexStringToByteArray(pubKeyString);
//    }
//
//    public static String getPrivKeyAsyToStr(byte[] data, String pubKeyString) {
//
//    }
//
//    public static String getPrivKeyStrToAsy (byte[] data, String pubKeyString) {
//
//    }

    public static String Encrypt(byte[] data, AsymmetricKeyParameter publicKey) throws Exception{
//	Source: http://www.cs.berkeley.edu/~jonah/bc/org/bouncycastle/crypto/engines/RSAEngine.html
        Security.addProvider(new BouncyCastleProvider());

        RSAEngine engine = new RSAEngine();
        engine.init(true, publicKey); //true if encrypt

        byte[] hexEncodedCipher = engine.processBlock(data, 0, data.length);

        return getHexString(hexEncodedCipher);
    }

    public static String Decrypt(String encrypted, AsymmetricKeyParameter privateKey) throws InvalidCipherTextException {
//	Source: http://www.mysamplecode.com/2011/08/java-rsa-decrypt-string-using-bouncy.html

        Security.addProvider(new BouncyCastleProvider());

        AsymmetricBlockCipher engine = new RSAEngine();
        engine.init(false, privateKey); //false for decryption

        byte[] encryptedBytes = hexStringToByteArray(encrypted);
        byte[] hexEncodedCipher = engine.processBlock(encryptedBytes, 0, encryptedBytes.length);

        return new String (hexEncodedCipher);
    }

}
