package com.paril.mlaclientapp.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.GetJoinConfirmationsModel;
import com.paril.mlaclientapp.util.SNPrefsManager;
import com.paril.mlaclientapp.webservice.Api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
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

import static com.paril.mlaclientapp.ui.activity.MLASocialNetwork.showSnackBar;

public class SNHomeActivity extends AppCompatActivity implements View.OnClickListener {

    TextView helloUserTxt;
    Button writePostBtn;
    Button viewPostsBtn;
    Button createGroupBtn;
    Button viewGroupsBtn;
    Button addFriendsBtn;
    Button addRequestsBtn;
    Button refreshBtn;

    SNPrefsManager prefsManager;

    ProgressDialog progressDialog;

    Intent currentIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snhome);

        currentIntent = getIntent();
        prefsManager = new SNPrefsManager(getApplicationContext(), currentIntent.getStringExtra("username"));

        helloUserTxt = (TextView) findViewById(R.id.HelloUserTV);

        // import main btns
        writePostBtn = (Button) findViewById(R.id.write_posts_btn);
        viewPostsBtn = (Button) findViewById(R.id.view_posts_btn);
        viewGroupsBtn = (Button) findViewById(R.id.view_groups_btn);
        createGroupBtn = (Button) findViewById(R.id.create_group_btn);
        addFriendsBtn = (Button) findViewById(R.id.add_friends_btn);
        addRequestsBtn = (Button) findViewById(R.id.add_req_btn);
        refreshBtn = (Button) findViewById(R.id.home_refreash_btn);

        writePostBtn.setOnClickListener(this);
        viewPostsBtn.setOnClickListener(this);
        viewGroupsBtn.setOnClickListener(this);
        createGroupBtn.setOnClickListener(this);
        addFriendsBtn.setOnClickListener(this);
        addRequestsBtn.setOnClickListener(this);
        refreshBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refreshPage();
                    }
                }
        );

        helloUserTxt.setText("Hello, " + currentIntent.getStringExtra("fullname")
                + "! (User ID: " + currentIntent.getStringExtra("user_id") + ")");

        // encryption test
        byte[] testBytes = new byte[16];
        try {
            testBytes = KeyHelper2.generateRandomBytes(); //
            String sampleGrpKey = "b0RTIXCSLb6pJ6CPHUeT4zyOOA4UlQQH5S2Rh2wja61M-6GxxUBdDwSb2-B6CbXtvIMo3tlg7-_X9KaON3c_hHtuLsr9tGoOwKZB8Hkt6vmCeloE7MRHkrP7k8mDWUUNOUAr1i6Hsnh3lvJLsaoBVH_Pgs3QM4_Gt-L-ZO84mPQha8ugW2l-B36cpKT7ZcItF8FIZBCrbbKVuP4sWX1sHmEmSyP_fVXatsgP_dDLABnbVU6aGRZGdz171A58jRShzR9OO45Zq-DcwlJqoIqomNzvdosbNqHbhDVv5dHXvzCxUv03zXaIlQ1x65hopp3DZkrI1oE-l0Xwy6p-aTl0xw";

//            System.out.println("TESTESTESTESTESTESTESTESTESTESTESTESTESTESTESTES");
//
//            String pubkey = prefsManager.getStringData("publicKey");
//
//            byte[] pubKeyBytes = KeyHelper2.decodeB64(pubkey);


//            PublicKey pubkey_key = (PublicKey) KeyHelper2.deserializeKey(pubKeyBytes);
//
//
//            byte[] encryptedWithPubKey = KeyHelper2.encryptGroupKeyUsingPubKey(pubkey_key, testBytes);
//
//            String encryptedGrpKey = KeyHelper2.encodeB64(encryptedWithPubKey);

            PrivateKey privKey = (PrivateKey) KeyHelper2.deserializeKey(
                    KeyHelper2.decodeB64(
                            prefsManager.getStringData("privateKey")
                    )
            );

            byte[] decryptedGrpKey = KeyHelper2.decryptGroupKey(
                    KeyHelper2.decodeB64(sampleGrpKey)
                    , privKey);

            String decdecgrpkey = KeyHelper2.encodeB64(decryptedGrpKey);

            System.out.println("decdecgrpkey " + decdecgrpkey);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.write_posts_btn:
                intent = new Intent(SNHomeActivity.this, WritePostActivity.class);
                break;
            case R.id.view_posts_btn:
                intent = new Intent(SNHomeActivity.this, ViewPostsActivity.class);
                break;

            case R.id.add_friends_btn:
                intent = new Intent(SNHomeActivity.this, AddFriendsActivity.class);
                break;

            case R.id.create_group_btn:
                intent = new Intent(SNHomeActivity.this, CreateGroupActivity.class);
                break;

            case R.id.view_groups_btn:
                intent = new Intent(SNHomeActivity.this, ViewGroupsActivity.class);
                break;

            case R.id.add_req_btn:
                intent = new Intent(SNHomeActivity.this, AddRequestsActivity.class);
                break;

        }

        Intent currentIntent = getIntent();

        intent.putExtra("user_id", currentIntent.getStringExtra("user_id"));
        intent.putExtra("fullname", currentIntent.getStringExtra("fullname"));
        intent.putExtra("publicKey", currentIntent.getStringExtra("publicKey"));
        intent.putExtra("username", currentIntent.getStringExtra("username"));
//        intent.putExtra("signature", currentIntent.getStringExtra("signature"));


        startActivity(intent);
    }

    private void refreshPage() {

        showProgressDialog("Refreshing...");

        // get join confirmations
        getJoinConfirmations getJoinConfirmationsAsyncCall = new getJoinConfirmations();
        getJoinConfirmationsAsyncCall.execute();
    }

    class getJoinConfirmations extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... voids) {

            Log.d("Is is working?", "????");

            String user_id = prefsManager.getStringData("user_id");
            System.out.println("Sending Request for notJoinedMemberId : " + user_id);
            Call<List<GetJoinConfirmationsModel>> getJoinConfirmationsCall = Api.getClient().GetJoinConfirmations(user_id);
            try {
                final Response<List<GetJoinConfirmationsModel>> response = getJoinConfirmationsCall.execute();
                if (response != null && response.isSuccessful() & response.body() != null && response.body().size() > 0) {

                    hideProgressDialog();

                    List<GetJoinConfirmationsModel> responseConfirmationsList = response.body();
//                    GetJoinConfirmationsModel responseConfirmationsItem = new GetJoinConfirmationsModel();

                    System.out.println("responseConfirmations.size() " + response.body().size());

//                    // for each confirmation verify and send finalize request
//                    for (int i = 0; i < responseConfirmationsList.size(); i++) {
//
//                        responseConfirmations2.add(new GetJoinConfirmationsModel(
//                                responseConfirmations1.get(i).group_owner_id,
//                                responseConfirmations1.get(i).requester_id,
//                                responseConfirmations1.get(i).encryptedGroupKey,
//                                responseConfirmations1.get(i).group_id,
//                                responseConfirmations1.get(i).signature,
//                                responseConfirmations1.get(i).public_key
//                        ));
//                    }

                    if (response.body().size() > 0) {
                        checkAndFinalize(response.body());
                    }


                } else {
                    hideProgressDialog();
                }
            } catch (IOException | NoSuchProviderException | KeyStoreException | InvalidKeyException | CertificateException | NoSuchAlgorithmException | ClassNotFoundException | InvalidKeySpecException | SignatureException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void checkAndFinalize(List<GetJoinConfirmationsModel> responseConfirmations) throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException, KeyStoreException, InvalidKeySpecException, ClassNotFoundException {

        for (int i = 0; i < responseConfirmations.size(); i++) {

            Boolean verifyJoinConfirmation = KeyHelper2.verifyData(
                    responseConfirmations.get(i).signature,
                    responseConfirmations.get(i).encryptedGroupKey,
                    responseConfirmations.get(i).public_key
            );

            if (verifyJoinConfirmation) {
                Log.d("Join confirmation : ", "verified!");

                // send finalize request
                finalizeConfirmation finalizeConfirmationApi = new finalizeConfirmation();
                finalizeConfirmationApi.execute(
                        responseConfirmations.get(i).requester_id,
                        responseConfirmations.get(i).group_id,
                        responseConfirmations.get(i).group_owner_id,
                        responseConfirmations.get(i).encryptedGroupKey,
                        responseConfirmations.get(i).signature
                );
            } else {
                Log.d("Join confirmation : ", "not verified");
            }

        }
    }

    class finalizeConfirmation extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... joinConfirmation) {

            hideProgressDialog();

            Call<String> registerCallAuth = Api.getClient().FinalizeJoin(
                    joinConfirmation[0],
                    joinConfirmation[1],
                    joinConfirmation[2],
                    joinConfirmation[3],
                    joinConfirmation[4]
            );
            try {
                Response<String> respAuth = registerCallAuth.execute();
                if (respAuth != null && respAuth.isSuccessful() & respAuth.body() != null) {
                    String responseMsg = respAuth.body().toString();
                    showSnackBar(responseMsg, findViewById(R.id.activity_sn_home_main));
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
