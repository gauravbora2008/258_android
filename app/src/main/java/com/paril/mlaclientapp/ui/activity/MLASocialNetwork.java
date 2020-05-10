package com.paril.mlaclientapp.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.SNRegisterNewUser;
import com.paril.mlaclientapp.model.SNUser;
import com.paril.mlaclientapp.util.CommonUtils;
import com.paril.mlaclientapp.util.SNPrefsManager;
import com.paril.mlaclientapp.webservice.APIInterface;
import com.paril.mlaclientapp.webservice.Api;

import android.util.Base64;
import android.widget.Toast;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.symmetric.AES;
import org.bouncycastle.util.encoders.Hex;
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
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
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
import static com.paril.mlaclientapp.ui.activity.KeyHelper.keyStore;

public class MLASocialNetwork extends AppCompatActivity {

    Button openLoginScreen;
    Button registerNewUserBtn;
    TextView registerScreenPostResponse;
    EditText registerScreenFullName;
    EditText registerScreenEmail;
    EditText registerScreenPassword;
    private ProgressDialog progressDialog;

    String generatedGrpKeyString, registrationPubKeyStr, encryptedGrpKey;

    public int Base64EncDecScheme = Base64.NO_WRAP | Base64.NO_CLOSE | Base64.NO_PADDING | Base64.URL_SAFE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mlasocial_network);

        try {
            KeyHelper.initKeyStore();
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
        }

        System.out.println("Providers: " + Arrays.toString(Security.getProviders()));

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
                    Log.e("createUser() ", Log.getStackTraceString(e));
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
        String signedData = "";

        try {

            // saving different prefsManagers by the username
            SNPrefsManager prefsManager = new SNPrefsManager(getApplicationContext(), email);
            prefsManager.saveData("key_alias", email);
            String alias = prefsManager.getStringData("key_alias");
            Log.d("Getting alias from prfs", alias);

//            KeyHelper2.initKeyStore();

            // Encryption Starts ============================

            // 1) generate keypair
            KeyPair registrationKeyPair = KeyHelper2.genKeyPairAndGetPubKey2(alias);
            PublicKey registrationPubKey = registrationKeyPair.getPublic();
            PrivateKey registrationPrivKey = registrationKeyPair.getPrivate();

            // serialize private key
            byte[] privKeyStr = KeyHelper2.serializeKey(registrationPrivKey);

            // 2) save private key in sharedPrefs
            prefsManager.saveData("privateKey",
                    KeyHelper2.encodeB64(privKeyStr));

            // 3) serialize public key
            byte[] registrationPubKeyBytes = KeyHelper2.serializeKey(registrationPubKey);

            // 4) generate a string for grp key
            byte[] generatedGrpKeyBytes = KeyHelper2.generateRandomBytes();

            // 5) encrypt group key
            byte[] encryptedGrpKeyBytes = KeyHelper2.encryptGroupKeyUsingPubKey(registrationPubKey, generatedGrpKeyBytes);

            // 6) Sign encrypted group key
            signedData = KeyHelper2.signData(KeyHelper2.encodeB64(encryptedGrpKeyBytes), registrationPrivKey);

            // 7) B64 encode grp key and pub key bytes to be sent
            encryptedGrpKey = KeyHelper2.encodeB64(encryptedGrpKeyBytes);
            registrationPubKeyStr = KeyHelper2.encodeB64(registrationPubKeyBytes);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("encryptedGrpKey:", encryptedGrpKey);

        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(fullname) || TextUtils.isEmpty(email)) {

            showSnackBar(getString(R.string.enter_all_fields), findViewById(R.id.activity_sn__login));

        } else {
            if (CommonUtils.checkInternetConnection(MLASocialNetwork.this)) {
                RegistrationAPI registration = new RegistrationAPI(getApplicationContext());

                // 7) Send Data
                registration.execute(
                        email,
                        password,
                        registrationPubKeyStr,
                        fullname,
                        encryptedGrpKey,
                        signedData
                );
            } else {
                showSnackBar(getString(R.string.check_connection), findViewById(R.id.activity_sn__login));
            }
        }
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

    public class RegistrationAPI extends AsyncTask<String, Void, Void> {

        Context appContext;

        public RegistrationAPI(Context context) {
            appContext = context;
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog("Registering ...");
        }

        @Override
        protected Void doInBackground(String... newUserParams) {

            hideProgressDialog();

            Call<String> registerCallAuth = Api.getClient().registerNewUser(
                    newUserParams[0],
                    newUserParams[1],
                    newUserParams[2],
                    newUserParams[3],
                    newUserParams[4],
                    newUserParams[5]
            );
            try {
                Response<String> respAuth = registerCallAuth.execute();
                if (respAuth != null && respAuth.isSuccessful() & respAuth.body() != null) {
                    String responseMsg = respAuth.body().toString();
                    showSnackBar(responseMsg, findViewById(R.id.activity_social_main));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void showProgressDialog(String message) {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = ProgressDialog.show(this, getString(R.string.app_name), message, true, false);
        }
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();

        }
    }

}
