package com.paril.mlaclientapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.SNUser;
import com.paril.mlaclientapp.webservice.APIInterface;
import com.paril.mlaclientapp.webservice.Api;

import android.util.Base64;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.*;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Calendar;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MLASocialNetwork extends AppCompatActivity {

    Button openLoginScreen;
    Button registerNewUserBtn;
    TextView registerScreenPostResponse;
    TextView pubKeyTV;
    TextView privKeyTV;
    TextView groupKeyTV;
    TextView secretKeyTV;
    TextView groupKeyEncryptedTV;
//    TextView pubKeyTV;
//    TextView pubKeyTV;
//
    EditText registerScreenFullName;
    EditText registerScreenEmail;
    EditText registerScreenPassword;

    String publicKey, privateKey;
    String TAG = "StoreInKeyStore";

    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String AES_MODE = "AES/GCM/NoPadding";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mlasocial_network);

        registerScreenPostResponse = (TextView) findViewById(R.id.register_screen_post_response);
        pubKeyTV = (TextView) findViewById(R.id.register_screen_public_Key);
        privKeyTV = (TextView) findViewById(R.id.register_screen_private_Key);
        secretKeyTV = (TextView) findViewById(R.id.register_screen_secret_key);
        groupKeyTV = (TextView) findViewById(R.id.register_screen_group_Key);
        groupKeyEncryptedTV = (TextView) findViewById(R.id.register_screen_group_key_enc);

        registerNewUserBtn = (Button) findViewById(R.id.register_screen_register_btn);

        registerScreenFullName = (EditText) findViewById(R.id.register_screen_fullname);
        registerScreenEmail = (EditText) findViewById(R.id.register_screen_email);
        registerScreenPassword = (EditText) findViewById(R.id.register_screen_password);

        // Keys test
        KeyHelper.Keys keys1 = KeyHelper.retrieveKey("key1");
        pubKeyTV.setText("Public Key: \n" + keys1.getPubKeyBytesString());
        privKeyTV.setText("Private Key Ref: \n" + keys1.getPrivateKey().toString());

        try {
            SecretKey key2 = KeyHelper.getSecretKey("key2");
            SecretKey groupkey1 = KeyHelper.getSecretKey("groupkey1");
            System.out.println(key2.toString());
            secretKeyTV.setText("Secret Key:\n" + key2.toString());
            groupKeyTV.setText("Group Key:\n" + groupkey1.toString());

            encryptGroupKey("encryptedGroupKey1", keys1.getPubKeyBytesString(), groupkey1);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

//        System.out.println("Public key.................." + keys1.getPubKeyBytesString());
//        System.out.println("Private key.................." + keys1.getPrivKeyBytesString());

//         open login activity
        openLoginScreen = (Button) findViewById(R.id.register_screen_login_btn);
        openLoginScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openLoginIntent = new Intent(MLASocialNetwork.this, SN_Login.class);
                openLoginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(openLoginIntent);
                overridePendingTransition(0, 0);
                MLASocialNetwork.this.finish();
            }
        });

        registerNewUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();

                try {
                    createUser();
                } catch (KeyStoreException | CertificateException | IOException | NoSuchProviderException | InvalidAlgorithmParameterException | UnrecoverableEntryException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }

            }
        });

    }

    public void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showSnackBar(String message, View view) {
        Snackbar snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_LONG);

        snackbar.show();
    }

    void encryptGroupKey(String alias, String textToEncrypt, SecretKey givenkey) throws IOException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnrecoverableEntryException, InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchProviderException, SignatureException, KeyStoreException, IllegalBlockSizeException {
        byte[] groupKeyEncrypted = KeyHelper.encryptTextWithGivenKey(alias, textToEncrypt, givenkey);
        String encryptedGroupKeyString = Base64.encodeToString(groupKeyEncrypted, Base64.NO_WRAP);
        System.out.println("encryptedGroupKeyString: " + encryptedGroupKeyString);

        String TestPhrase = "Hello World!";
        byte[] encTestPhrase = KeyHelper.encryptTextWithGivenKey("encTestPhrase", TestPhrase, givenkey);


        // encrypt testphrase with original groupkey
        // encrypt testphrase with decrypted groupkey
        KeyHelper.generateRandomString();


        groupKeyEncryptedTV.setText("Encrypted Group Key: \n" + encryptedGroupKeyString);
    }



    private void createUser() throws KeyStoreException, IOException, CertificateException, NoSuchProviderException, InvalidAlgorithmParameterException, UnrecoverableEntryException {

        String fullname = registerScreenFullName.getText().toString();
        String email = registerScreenEmail.getText().toString();
        String password = registerScreenPassword.getText().toString();


        // generate key pair
//        try {

//            Calendar start = Calendar.getInstance();
//            Calendar end = Calendar.getInstance();
//            end.add(Calendar.YEAR, 30);


//                privateKey = keys1.getPrivKeyBytesString();
//                publicKey = keys1.getPublicKey().toString();


//            KeyPairGenerator sr = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA);
//            SecureRandom random = new SecureRandom();
//            sr.initialize(1024, random);
//            KeyPair kp = sr.generateKeyPair();
//
//            String algo = sr.getAlgorithm();
//            System.out.println("Algorithm : " + algo);
//
//            byte[] privateKeyEnc = kp.getPrivate().getEncoded(); //store private key as byte stream
//            byte[] publicKeyEnc = kp.getPublic().getEncoded(); //store public key as byte stream
//
//            publicKey = new String(Base64.encodeToString(publicKeyEnc, Base64.NO_WRAP));
//            privateKey = new String(Base64.encodeToString(privateKeyEnc, Base64.NO_WRAP)); //encode to base64 format and store it as string
//
//            Toast.makeText(MLASocialNetwork.this, privateKey, Toast.LENGTH_LONG).show();

        System.out.println("private key .............. " + privateKey);
        System.out.println("public key .............. " + publicKey);


        // store private key in keystore
//            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
//            keyStore.load(null);
//
//            String alias = "key3";
//
//            int nBefore = keyStore.size();

        // Create the keys if necessary
//            if (!keyStore.containsAlias(alias)) {
//
//                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(MLASocialNetwork.this)
//                        .setAlias(alias)
//                        .setKeyType(KeyProperties.KEY_ALGORITHM_RSA)
//                        .setKeySize(2048)
//                        .setSubject(new X500Principal("CN=test"))
//                        .setSerialNumber(BigInteger.TEN)
//                        .setStartDate(start.getTime())
//                        .setEndDate(end.getTime())
//                        .build();
//                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
//                generator.initialize(spec);
//
//                KeyPair keyPair = generator.generateKeyPair();
//            }
//            int nAfter = keyStore.size();
//            Log.v(TAG, "Before = " + nBefore + " After = " + nAfter);
//
//            // Retrieve the keys
//            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
//            PrivateKey privateKey2 = privateKeyEntry.getPrivateKey();
//            PublicKey publicKey2 = privateKeyEntry.getCertificate().getPublicKey();
//
//            Log.v(TAG, "private key = " + privateKey2.toString());
//            Log.v(TAG, "public key = " + publicKey2.toString());

        Call<SNUser> call = Api.getClient().registerNewUser(
                email,
                password,
                publicKey,
                fullname
        );

        call.enqueue(new Callback<SNUser>() {
            @Override
            public void onResponse(Call<SNUser> call, Response<SNUser> response) {
                if (response.code() != 302) {
                    registerScreenPostResponse.setText("Response Code : " + response.code() + " " + response.toString());
                    return;
                }

                showSnackBar("User Registered Successfully !", findViewById(R.id.activity_social_main));

//                    KeyHelper.retrieveKey("key2");

//                    registerScreenPostResponse.setText("User Registered Successfully !");

//                    registerScreenPostResponse.setText(response.toString());
            }

            @Override
            public void onFailure(Call<SNUser> call, Throwable t) {
                registerScreenPostResponse.setText(t.getMessage());
            }
        });
//        }
//        catch (NoSuchAlgorithmException e) {
//
//            System.out.println("Exception thrown : " + e);
//        } catch (ProviderException e) {
//
//            System.out.println("Exception thrown : " + e);
//        } catch (KeyStoreException | CertificateException | IOException | NoSuchProviderException | InvalidAlgorithmParameterException | UnrecoverableEntryException e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//        }

    }

}
