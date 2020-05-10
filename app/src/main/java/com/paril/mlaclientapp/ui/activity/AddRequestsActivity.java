package com.paril.mlaclientapp.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Spinner;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.ViewPendingRequestsItem;
import com.paril.mlaclientapp.model.ViewUnjoinedGroupsItem;
import com.paril.mlaclientapp.ui.adapter.viewGroupsAdapter;
import com.paril.mlaclientapp.ui.adapter.viewPendingRequestsAdapter;
import com.paril.mlaclientapp.util.PrefsManager;
import com.paril.mlaclientapp.util.SNPrefsManager;
import com.paril.mlaclientapp.webservice.Api;

import java.io.IOException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import retrofit2.Call;
import retrofit2.Response;

import static com.paril.mlaclientapp.ui.activity.MLASocialNetwork.showSnackBar;

public class AddRequestsActivity extends AppCompatActivity {

    private RecyclerView addGroupsRecyclerView;
    private viewPendingRequestsAdapter addGroupsRVAdapter;
    private RecyclerView.LayoutManager addGroupsRVLayoutManager;

    private ProgressDialog progressDialog;
    ArrayList<ViewPendingRequestsItem> pendingRequestsList;

    List<ViewPendingRequestsItem> responseGroups;

    Intent currentIntent;

    Map<Integer, String[]> groupData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_requests);
        setToolbarTitle("Pending \"Add to Group\" Requests");

        getPendingAddRequests getPendingRequestsCall = new getPendingAddRequests();
        getPendingRequestsCall.execute();

        ArrayList<ViewPendingRequestsItem> groupsList = new ArrayList<>();

        pendingRequestsList = new ArrayList<>();

        addGroupsRecyclerView = (RecyclerView) findViewById(R.id.add_requests_recycler_view);
        addGroupsRecyclerView.setHasFixedSize(true);
        addGroupsRVLayoutManager = new LinearLayoutManager(AddRequestsActivity.this);
        addGroupsRVAdapter = new viewPendingRequestsAdapter(pendingRequestsList);
        addGroupsRecyclerView.setLayoutManager(addGroupsRVLayoutManager);
        addGroupsRecyclerView.setAdapter(addGroupsRVAdapter);

        addGroupsRVAdapter.setOnItemClickListener(
                new viewPendingRequestsAdapter.OnItemClickListener() {

                    @Override
                    public void onApproveBtnClick(int position) throws Exception {
                        sendApproveRequest(position);
                    }

                    @Override
                    public void onDenyBtnClick(int position) {
                        sendDenyRequest(position);
                    }
                });

        addGroupsRVAdapter.notifyDataSetChanged();

    }

    class getPendingAddRequests extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... voids) {
            final Spinner dropdown;

            currentIntent = getIntent();

            SNPrefsManager prefsManager = new SNPrefsManager(getApplicationContext(), currentIntent.getStringExtra("username"));

            String user_id = prefsManager.getStringData("user_id");

            Call<List<ViewPendingRequestsItem>> getPendingRequestsCall = Api.getClient().GetPendingAddRequests(user_id);
            try {
                final Response<List<ViewPendingRequestsItem>> response = getPendingRequestsCall.execute();
                if (response != null && response.isSuccessful() & response.body() != null && response.body().size() > 0) {


                    hideProgressDialog();

                    responseGroups = response.body();

                    for (int i = 0; i < responseGroups.size(); i++) {

                        pendingRequestsList.add(new ViewPendingRequestsItem(
                                responseGroups.get(i).getRequester_id(),
                                responseGroups.get(i).getRequester_fullname(),
                                responseGroups.get(i).getRequesters_pub_key(),
                                responseGroups.get(i).getGroup_id(),
                                responseGroups.get(i).getGroup_key(),
                                responseGroups.get(i).getGroup_owner_id(),
                                responseGroups.get(i).getGroup_name(),
                                responseGroups.get(i).getSignature(),
                                responseGroups.get(i).getJoin_request_hash(),
                                responseGroups.get(i).getGroup_owners_pub_key()

                        ));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            addGroupsRVAdapter.notifyDataSetChanged();
                        }
                    });

                } else {
                    hideProgressDialog();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


    }

    private void sendApproveRequest(int position) throws Exception {

        Intent currentIntent = getIntent();

        String user_id = currentIntent.getStringExtra("user_id");
        String username = currentIntent.getStringExtra("username");
        SNPrefsManager prefsManager = new SNPrefsManager(getApplicationContext(), username);
        String alias = prefsManager.getStringData("key_alias");

        String requester_id = responseGroups.get(position).getRequester_id();
        String requester_fullname = responseGroups.get(position).getRequester_fullname();
        String requesters_pub_key = responseGroups.get(position).getRequesters_pub_key();
        String group_id = responseGroups.get(position).getGroup_id();
        String group_key = responseGroups.get(position).getGroup_key();
        String group_name = responseGroups.get(position).getGroup_name();
        String group_owner_id = responseGroups.get(position).getGroup_owner_id();
        String join_request_hash = responseGroups.get(position).getJoin_request_hash();
        String group_owners_pub_key = responseGroups.get(position).getGroup_owners_pub_key(); // not needed
        String signature = responseGroups.get(position).getSignature();

        Log.d(" user_id frm currIntent", user_id);
        Log.d(" group_id from btnClick", group_id);
        Log.d(" group_owner_id   Click", group_owner_id);
        Log.d(" requesters_id    Click", requester_id);
        Log.d(" request action button", " sendApproveRequest");

        // 2) verify group key
        Boolean verificationResult = KeyHelper2.verifyData(signature, join_request_hash, requesters_pub_key);

        // 3) If verification passes, proceed

        if(verificationResult) {
            System.out.println("Join Request VERIFIED!");

            // 5) get my private key
            PrivateKey myPriv = (PrivateKey) KeyHelper2.deserializeKey(
                    KeyHelper2.decodeB64(
                            prefsManager.getStringData("privateKey")
                    )
            );

            byte[] decryptedGrpKey = KeyHelper2.decryptGroupKey(KeyHelper2.decodeB64(group_key), myPriv);

            PublicKey requestersPubKey = (PublicKey) KeyHelper2.deserializeKey(
                    KeyHelper2.decodeB64(requesters_pub_key)
            );

            // 6) encrypt this grp key with requester's public key
            byte[] encryptedGroupKeyWithReqKey = KeyHelper2.encryptGroupKeyUsingPubKey(requestersPubKey, decryptedGrpKey);

            // 7) encode the new group key
            String encoEncryGrpKeyWithReqPubKey = KeyHelper2.encodeB64(encryptedGroupKeyWithReqKey);

            String signedData = KeyHelper2.signData(encoEncryGrpKeyWithReqPubKey, myPriv);

            // Send data
            SendApproveGroupRequestAPI sendJoinRequestCall = new SendApproveGroupRequestAPI();
            sendJoinRequestCall.execute(
                    group_owner_id,
                    requester_id,
                    encoEncryGrpKeyWithReqPubKey,
                    group_id,
                    signedData );

//            pendingRequestsList.remove(position);
//            addGroupsRVAdapter.notifyItemChanged(position);

        } else {
            System.out.println("Group key Verification Failed !");
        }

//        PrivateKey privateKey_ = KeyHelper2.getPrivateKeyFromString(privateKeyStr);

//        String decryptedTmpGrpKey = KeyHelper2.decryptGroupKey(group_key, privateKey_);

        // encript new grp key
//        String newEncTmpGrpKey = KeyHelper2.encryptGroupKeyUsingPubKey(requestersPubKey, decryptedTmpGrpKey);



        // sign new group key
//        String NewGrpKeySignature = KeyHelper2.signData(newEncTmpGrpKey, privateKey_);



    }


    // Pending::  create API for approve pending then configure here and implement encryption/decryption
    class SendApproveGroupRequestAPI extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            Call<String> sendJoinRequestCall = Api.getClient().ApproveGroupRequest(
                    strings[0], strings[1], strings[2], strings[3], strings[4]);
            try {
                Response<String> response = sendJoinRequestCall.execute();
                if (response != null && response.isSuccessful() & response.body() != null) {
                    System.out.println(response);

                    showSnackBar(response.body().toString(), findViewById(R.id.view_requests_main));

                }

            } catch (IOException e)

            {
                e.printStackTrace();
            }
            return null;
        }

    }

    private void sendDenyRequest(int position) {

        Intent currentIntent = getIntent();


        String group_id = responseGroups.get(position).getGroup_id();
        String group_owner_id = responseGroups.get(position).getGroup_owner_id();
        String requester_id = responseGroups.get(position).getRequester_id();

        SendDenyGroupRequestAPI sendDenyRequestCall = new SendDenyGroupRequestAPI();
        sendDenyRequestCall.execute(requester_id, group_id, group_owner_id);

        pendingRequestsList.remove(position);
        addGroupsRVAdapter.notifyItemChanged(position);

    }

    class SendDenyGroupRequestAPI extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            Call<String> sendJoinRequestCall = Api.getClient().DenyAddRequest(strings[0], strings[1], strings[2]);
            try {
                Response<String> response = sendJoinRequestCall.execute();
                if (response != null && response.isSuccessful() & response.body() != null) {
                    System.out.println(response);

                    showSnackBar(response.body().toString(), findViewById(R.id.view_requests_main));

                }

            } catch (IOException e)

            {
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

    public void setToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
