package com.paril.mlaclientapp.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.CreateGroupModel;
import com.paril.mlaclientapp.model.GetGroupsModel;
import com.paril.mlaclientapp.util.CommonUtils;
import com.paril.mlaclientapp.util.SNPrefsManager;
import com.paril.mlaclientapp.webservice.Api;
import com.sinch.gson.JsonParser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import retrofit2.Call;
import retrofit2.Response;

import static com.paril.mlaclientapp.ui.activity.KeyHelper.createEncryptedKey;
import static com.paril.mlaclientapp.ui.activity.KeyHelper.keyStore;
import static com.paril.mlaclientapp.ui.activity.MLASocialNetwork.showSnackBar;

public class CreateGroupActivity extends AppCompatActivity {

    Button createGroupBtn;
    EditText createGroupET;
    TextView myGroupsTV;
    Intent currentIntent;

    ProgressDialog progressDialog;
    SNPrefsManager prefsManager;

    PublicKey pubKey;

    String alias;

    List<GetGroupsModel> joinedGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        setToolbarTitle("Create Group");

        currentIntent = getIntent();

        prefsManager = new SNPrefsManager(getApplicationContext(), currentIntent.getStringExtra("username"));

        alias = prefsManager.getStringData("key_alias");

        Button createGroupBtn = (Button) findViewById(R.id.enter_grp_name_btn);
        final EditText createGroupET = (EditText) findViewById(R.id.enter_grp_name_ET);
        myGroupsTV = (TextView) findViewById(R.id.my_groups_TV);

        // Get groups this user is part of
        getGroups groupCall = new getGroups();
        groupCall.execute();

        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                if (TextUtils.isEmpty(createGroupET.getText().toString())) {
                    showSnackBar("Please enter a group name", findViewById(R.id.activity_create_group_main));
                } else {
                    // send a create group request
                    if (CommonUtils.checkInternetConnection(CreateGroupActivity.this)) {
                        CreateGroupAPI createGroupCall = new CreateGroupAPI(getApplicationContext());

                        try {

                            // generate a random string
                            byte[] newGroupKeyBytes = KeyHelper2.generateRandomBytes();

                            PublicKey myPubKey = (PublicKey) KeyHelper2.deserializeKey(
                                    KeyHelper2.decodeB64(
                                            prefsManager.getStringData("publicKey")
                                    )
                            );

                            // encrypt group key using my public key
                            byte[] encNewGrpKey = KeyHelper2.encryptGroupKeyUsingPubKey(myPubKey,
                                    newGroupKeyBytes);

                            PrivateKey myPrivKey = (PrivateKey) KeyHelper2.deserializeKey(
                                    KeyHelper2.decodeB64(
                                            prefsManager.getStringData("privateKey")
                                    )
                            );

                            // sign encrypted group key
                            String newSignature = KeyHelper2.signData(
                                    KeyHelper2.encodeB64(encNewGrpKey),
                                    myPrivKey);

                            String owner_id = prefsManager.getStringData("user_id");

                            createGroupCall.execute(
                                    owner_id,
                                    createGroupET.getText().toString(),
                                    KeyHelper2.encodeB64(encNewGrpKey),
                                    newSignature
                            );

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        showSnackBar(getString(R.string.check_connection), findViewById(R.id.activity_sn__login));
                    }
                }
            }
        });

    }

    class CreateGroupAPI extends AsyncTask<String, Void, Void> {
        Context appContext;

        public CreateGroupAPI(Context context) {
            appContext = context;
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog("Creating Group");
        }

        @Override
        protected Void doInBackground(String... params) {

            System.out.println("params ============== " + params[0] + " " + params[1]);
            Call<String> createGroupApiCall = Api.getClient().createNewGroup(params[0], params[1], params[2], params[3]);
            try {
                Response<String> response = createGroupApiCall.execute();

                hideProgressDialog();

                if (response != null && response.body() != null && response.isSuccessful()) {
                    showSnackBar(response.body().toString(), findViewById(R.id.activity_create_group_main));
                } else {
                    if (response.code() != 302) {
                        showSnackBar("Response Code : " + response.code() + " " + response.toString(), findViewById(R.id.activity_create_group_main));
                        return null;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    class getGroups extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... voids) {

            currentIntent = getIntent();

            prefsManager = new SNPrefsManager(getApplicationContext(), currentIntent.getStringExtra("username"));


            String userId = prefsManager.getStringData("user_id");
            System.out.println("Sending Request for userId : " + userId);
            Call<List<GetGroupsModel>> callAuth = Api.getClient().GetGroupsByMemberId(userId);
            try {
                Response<List<GetGroupsModel>> respAuth = callAuth.execute();
                if (respAuth != null && respAuth.isSuccessful() & respAuth.body() != null && respAuth.body().size() > 0) {
                    joinedGroups = respAuth.body();
                    System.out.println("joinedGroups " + Arrays.toString(joinedGroups.toArray()));

                    String groupItems = new String();

                    for (int i = 0; i < joinedGroups.size(); i++) {
                        groupItems += joinedGroups.get(i).group_name + "\n";
                    }

                    System.out.println(groupItems);
                    final String finalGroupItems = groupItems;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myGroupsTV.setText(finalGroupItems);
                        }
                    });


                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new String[0];
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

    public void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void setToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
