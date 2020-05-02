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
import com.paril.mlaclientapp.model.SNRegisterNewUser;
import com.paril.mlaclientapp.model.SNUser;
import com.paril.mlaclientapp.webservice.APIInterface;
import com.paril.mlaclientapp.webservice.Api;

import android.util.Base64;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import java.security.spec.InvalidKeySpecException;
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

import static com.paril.mlaclientapp.ui.activity.KeyHelper.decryptData;
import static com.paril.mlaclientapp.ui.activity.KeyHelper.getGroupKey;

public class MLASocialNetwork extends AppCompatActivity {

    Button openLoginScreen;
    Button registerNewUserBtn;
    TextView registerScreenPostResponse;
    TextView pubKeyTV;
    TextView privKeyTV;
    TextView groupKeyTV;
    TextView secretKeyTV;
    TextView groupKeyEncryptedTV;
    TextView groupKeyDecryptedTV;
    //    TextView pubKeyTV;
//    TextView pubKeyTV;
//
    EditText registerScreenFullName;
    EditText registerScreenEmail;
    EditText registerScreenPassword;

    String publicKeyString, encryptedGroupKey;

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
        groupKeyDecryptedTV = (TextView) findViewById(R.id.register_screen_group_key_dec);


        registerNewUserBtn = (Button) findViewById(R.id.register_screen_register_btn);

        registerScreenFullName = (EditText) findViewById(R.id.register_screen_fullname);
        registerScreenEmail = (EditText) findViewById(R.id.register_screen_email);
        registerScreenPassword = (EditText) findViewById(R.id.register_screen_password);



        // group key encrypted using public key
//            byte[] encGrpKeyBytes = KeyHelper.encryptTextWithGivenKey(generateGroupKey, keys1.getPublicKey());
//            System.out.println("encGrpKeyBytes........ " + encGrpKeyBytes);
//        try {
//            String encGrpKey = KeyHelper.getGroupKey();
//
//            groupKeyEncryptedTV.setText(
//                    "Group Key (Encrypted with public key):\n"
//                            + encGrpKey);
//        } catch (NoSuchPaddingException
//                | NoSuchAlgorithmException
//                | InvalidKeyException
//                | BadPaddingException
//                | IllegalBlockSizeException
//                | UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }


        // decrypting the group key
//            String decryptedGroupKey = KeyHelper.decryptData("key1", encGrpKeyBytes);


//            System.out.println("decryptedGroupKey" + decryptedGroupKey);
//            groupKeyDecryptedTV.setText(
//                    "Group Key (Decrypted with private key):\n"
//                    + decryptedGroupKey
//            );


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

    private void createUser() throws KeyStoreException, IOException, CertificateException, NoSuchProviderException, InvalidAlgorithmParameterException, UnrecoverableEntryException {

        String fullname = registerScreenFullName.getText().toString();
        String email = registerScreenEmail.getText().toString();
        String password = registerScreenPassword.getText().toString();

        try {

            String alias = "OWNER_34_GRP_76";
            KeyHelper.NewUserKeys newKeys = getGroupKey(alias, getApplicationContext());
            publicKeyString = newKeys.getPubKey();
            encryptedGroupKey = newKeys.getCipheredGroupKey();

            decryptData(alias, encryptedGroupKey);

        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        System.out.println("encrypted public key .............. " + encryptedGroupKey);

        Call<SNRegisterNewUser> call = Api.getClient().registerNewUser(
                email,
                password,
                publicKeyString,
                fullname,
                encryptedGroupKey
        );

        call.enqueue(new Callback<SNRegisterNewUser>() {
            @Override
            public void onResponse(Call<SNRegisterNewUser> call, Response<SNRegisterNewUser> response) {
                if (response.code() != 302) {
                    registerScreenPostResponse.setText("Response Code : " + response.code() + " " + response.toString());
                    return;
                }

                showSnackBar("User Registered Successfully !", findViewById(R.id.activity_social_main));

            }

            @Override
            public void onFailure(Call<SNRegisterNewUser> call, Throwable t) {
                registerScreenPostResponse.setText(t.getMessage());
            }
        });


    }

}
