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
import com.paril.mlaclientapp.util.SNPrefsManager;
import com.paril.mlaclientapp.webservice.APIInterface;
import com.paril.mlaclientapp.webservice.Api;

import android.util.Base64;
import android.widget.Toast;

import org.bouncycastle.jce.provider.symmetric.AES;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.security.auth.x500.X500Principal;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.paril.mlaclientapp.ui.activity.KeyHelper.decryptData;
import static com.paril.mlaclientapp.ui.activity.KeyHelper.getGroupKey;

public class MLASocialNetwork extends AppCompatActivity {

    Button openLoginScreen;
    Button registerNewUserBtn;
    TextView registerScreenPostResponse;
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

        try {
            KeyHelper.initKeyStore();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        String a = "Hellow World!";
//        System.out.println(Arrays.toString(a.getBytes()));
//        System.out.println(Arrays.toString(new String(a.getBytes()).getBytes()));
//        System.out.println(Arrays.toString(Base64.decode(a.getBytes(), Base64.NO_PADDING)));

        registerNewUserBtn = (Button) findViewById(R.id.register_screen_register_btn);

        registerScreenFullName = (EditText) findViewById(R.id.register_screen_fullname);
        registerScreenEmail = (EditText) findViewById(R.id.register_screen_email);
        registerScreenPassword = (EditText) findViewById(R.id.register_screen_password);

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

        hideKeyboard();

    }


    public void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void showSnackBar(String message, View view) {
        Snackbar snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_LONG);

        snackbar.show();
    }

    private void createUser() throws KeyStoreException, IOException, CertificateException, NoSuchProviderException, InvalidAlgorithmParameterException, UnrecoverableEntryException {

        String fullname = registerScreenFullName.getText().toString();
        String email = registerScreenEmail.getText().toString();
        String password = registerScreenPassword.getText().toString();

        try {

            // saving different prefsManagers by the username
            SNPrefsManager prefsManager = new SNPrefsManager(getApplicationContext(), email);
            prefsManager.saveData("key_alias", email);
            String alias = prefsManager.getStringData("key_alias");

            Log.d("Getting alias from prfs", alias);
            KeyHelper.NewUserKeys newKeys = getGroupKey(getApplicationContext(), alias);
            publicKeyString = newKeys.getPubKey();
            encryptedGroupKey = newKeys.getCipheredGroupKey();

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

        Call<SNRegisterNewUser> registrationCall = Api.getClient().registerNewUser(
                email,
                password,
                publicKeyString,
                fullname,
                encryptedGroupKey
        );

        registrationCall.enqueue(new Callback<SNRegisterNewUser>() {
            @Override
            public void onResponse(Call<SNRegisterNewUser> call, Response<SNRegisterNewUser> response) {

                BufferedReader reader = null;
                StringBuilder sb = new StringBuilder();
                try {
                    reader = new BufferedReader(new InputStreamReader(response.errorBody().byteStream()));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                String finallyError = sb.toString();

                if (finallyError.contains("Already Exists")) {
                    showSnackBar("User Already Exists!", findViewById(R.id.activity_social_main));
                }

                if (response.code() != 302) {
                    showSnackBar("Response Code : " + response.code() + " " + response.toString(), findViewById(R.id.activity_social_main));
                    return;
                }

                showSnackBar("User Registered Successfully !", findViewById(R.id.activity_social_main));

            }

            @Override
            public void onFailure(Call<SNRegisterNewUser> call, Throwable t) {
                showSnackBar(t.getMessage(), findViewById(R.id.activity_social_main));
            }
        });


    }

    public class ModelError {
        private String Message;

        public String getMessage() {
            return Message;
        }

        public void setMessage(String Message) {
            this.Message = Message;
        }
    }

}
