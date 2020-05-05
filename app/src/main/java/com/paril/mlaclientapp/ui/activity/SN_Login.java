package com.paril.mlaclientapp.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.SNUserLogin;
import com.paril.mlaclientapp.util.CommonUtils;
import com.paril.mlaclientapp.util.SNPrefsManager;
import com.paril.mlaclientapp.webservice.Api;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static com.paril.mlaclientapp.ui.activity.MLASocialNetwork.showSnackBar;

public class SN_Login extends AppCompatActivity {

    Button openRegisterScreen;
    Button loginBtn;
    EditText usernameTxt;
    EditText passwordTxt;

    private ProgressDialog progressDialog;
    SNUserLogin login;
    private SNPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sn__login);

        loginBtn = (Button) findViewById(R.id.login_screen_login_btn);
        usernameTxt = (EditText) findViewById(R.id.login_screen_username);
        passwordTxt = (EditText) findViewById(R.id.login_screen_password);

        login = new SNUserLogin();

        // open register activity
        openRegisterScreen = (Button) findViewById(R.id.login_screen_register_btn);
        openRegisterScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openRegisterIntent = new Intent(SN_Login.this, MLASocialNetwork.class);
                openRegisterIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(openRegisterIntent);
                overridePendingTransition(0, 0);
                SN_Login.this.finish();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send login request
                hideKeyboard();
                if (TextUtils.isEmpty(passwordTxt.getText().toString()) || TextUtils.isEmpty(usernameTxt.getText().toString())) {

                    showSnackBar(getString(R.string.enter_all_fields), findViewById(R.id.activity_sn__login));

                } else {
                    if (CommonUtils.checkInternetConnection(SN_Login.this)) {
                        SNLoginAPI authenticationLogin = new SNLoginAPI(getApplicationContext());
                        System.out.println("...................." + usernameTxt.getText().toString() + " " + passwordTxt.getText().toString());
                        authenticationLogin.execute(usernameTxt.getText().toString(), passwordTxt.getText().toString());
                    } else {
                        showSnackBar(getString(R.string.check_connection), findViewById(R.id.activity_sn__login));
                    }
                }
            }
        });
    }

    class SNLoginAPI extends AsyncTask<String, Void, SNUserLogin> {
        Context appContext;

        public SNLoginAPI (Context context) {
            appContext = context;
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog("Verifying Credentials...");
        }

        @Override
        protected void onPostExecute(SNUserLogin loginArg) {
            hideProgressDialog();
            login = loginArg;

            System.out.println(login.toString());
            System.out.println("login.username========="+ login.username);
            prefsManager = new SNPrefsManager(getApplicationContext(), login.username);

            if (login.user_id != null) {
                Intent activity = new Intent();
                activity.setClass(SN_Login.this, SNHomeActivity.class);
                activity.putExtra("user_id", login.user_id);
                activity.putExtra("fullname", login.fullname);
                activity.putExtra("publicKey", login.publicKey);
                activity.putExtra("username", login.username);

                savingUserInformation();
                activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(activity);
                finish();
            } else {
                showSnackBar("User Name/Password is incorrect. Please enter correct credentials.", findViewById(R.id.activity_sn__login));
            }
        }

        @Override
        protected SNUserLogin doInBackground(String... params) {
            SNUserLogin login = new SNUserLogin();
            System.out.println("params============== "+ params[0] + " " + params[1]);
            Call<List<SNUserLogin>> loginCallAuth = Api.getClient().loginAuth(params[0], params[1]);
            try {
                Response<List<SNUserLogin>> respAuth = loginCallAuth.execute();
                if (respAuth != null && respAuth.isSuccessful() & respAuth.body() != null && respAuth.body().size() > 0) {
                    login = respAuth.body().get(0);
                    System.out.println(login.user_id + " ...... " + login.fullname);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return login;
        }
    }

    public void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

    void savingUserInformation() {
        prefsManager.saveData("user_id", login.user_id);
        prefsManager.saveData("fullname", login.fullname);
        prefsManager.saveData("publicKey", login.publicKey);
        prefsManager.saveData("username", login.username);
        prefsManager.saveData("key_alias", login.username);

        System.out.println("prefsManager ...........: " + prefsManager.getStringData("userId"));
//        prefsManager.saveData("userType", register.userType);
    }
}
