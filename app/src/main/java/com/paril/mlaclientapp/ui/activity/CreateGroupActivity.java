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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.CreateGroupModel;
import com.paril.mlaclientapp.model.GetGroupsModel;
import com.paril.mlaclientapp.util.CommonUtils;
import com.paril.mlaclientapp.util.PrefsManager;
import com.paril.mlaclientapp.webservice.Api;
import com.sinch.gson.JsonParser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import retrofit2.Call;
import retrofit2.Response;

import static com.paril.mlaclientapp.ui.activity.KeyHelper.createEncryptedGrpKey;
import static com.paril.mlaclientapp.ui.activity.MLASocialNetwork.showSnackBar;

public class CreateGroupActivity extends AppCompatActivity {

    Button createGroupBtn;
    EditText createGroupET;
    TextView myGroupsTV;

    ProgressDialog progressDialog;
    PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        setToolbarTitle("Create Group");

        prefsManager = new PrefsManager(getApplicationContext());

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

                            String pubKey = prefsManager.getStringData("publicKey");
                            System.out.println("publicKey retrieved is: " + pubKey);

                            String newGroupKeyStr = createEncryptedGrpKey(pubKey);
                            String owner_id = prefsManager.getStringData("user_id");

                            System.out.println("owner_id for creating new group: " + owner_id);

                            System.out.println("createGroupET...................." + createGroupET.getText().toString());
                            createGroupCall.execute(owner_id, createGroupET.getText().toString(), newGroupKeyStr);
                        } catch (UnrecoverableEntryException e) {
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (KeyStoreException e) {
                            e.printStackTrace();
                        } catch (NoSuchPaddingException e) {
                            e.printStackTrace();
                        } catch (InvalidKeySpecException e) {
                            e.printStackTrace();
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                        } catch (BadPaddingException e) {
                            e.printStackTrace();
                        } catch (IllegalBlockSizeException e) {
                            e.printStackTrace();
                        } catch (CertificateException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
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
        protected void onPostExecute(Void aVoid) {
            hideProgressDialog();
        }

        @Override
        protected Void doInBackground(String... params) {

            System.out.println("params============== " + params[0] + " " + params[1]);
            Call<String> createGroupApiCall = Api.getClient().createNewGroup(params[0], params[1], params[2]);
            try {
                Response<String> response = createGroupApiCall.execute();

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

                if (finallyError.contains("Group created")) {
                    showSnackBar(finallyError, findViewById(R.id.activity_create_group_main));
                }

                if (response.code() != 302) {
                    showSnackBar("Response Code : " + response.code() + " " + response.toString(), findViewById(R.id.activity_create_group_main));
                    return null;
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

            prefsManager = new PrefsManager(getApplicationContext());

            List<GetGroupsModel> joinedGroups;
            String userId = prefsManager.getStringData("userId");
            System.out.println("Sending Request for userId : " + userId);
            Call<List<GetGroupsModel>> callAuth = Api.getClient().getJoinedGroups(userId);
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
