package com.paril.mlaclientapp.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Spinner;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.GetUnjoinedGroupsModel;
import com.paril.mlaclientapp.model.ViewUnjoinedGroupsItem;
import com.paril.mlaclientapp.ui.adapter.viewGroupsAdapter;
import com.paril.mlaclientapp.util.SNPrefsManager;
import com.paril.mlaclientapp.webservice.Api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

import static com.paril.mlaclientapp.ui.activity.MLASocialNetwork.showSnackBar;

public class ViewGroupsActivity extends AppCompatActivity {

    private RecyclerView groupsRecyclerView;
    private viewGroupsAdapter groupsRVAdapter;
    private RecyclerView.LayoutManager groupsRVLayoutManager;

    private ProgressDialog progressDialog;
    Intent currentIntent;

    List<GetUnjoinedGroupsModel> responseGroups;
    SNPrefsManager prefsManager;
    ArrayList<ViewUnjoinedGroupsItem> unjoinedGroupsList;

    List<GetUnjoinedGroupsModel> joinedGroups;

    Map<Integer, String[]> groupData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_groups);
        setToolbarTitle("View Groups");

        currentIntent = getIntent();
        unjoinedGroupsList = new ArrayList<>();

        prefsManager = new SNPrefsManager(getApplicationContext(), currentIntent.getStringExtra("username"));

        getUnjoinedGroups getUnjoinedGroupsCall = new getUnjoinedGroups();
        getUnjoinedGroupsCall.execute();


        groupsRecyclerView = findViewById(R.id.view_groups_recycler_view);
        groupsRecyclerView.setHasFixedSize(true);
        groupsRVLayoutManager = new LinearLayoutManager(ViewGroupsActivity.this);
        groupsRVAdapter = new viewGroupsAdapter(unjoinedGroupsList);
        groupsRecyclerView.setLayoutManager(groupsRVLayoutManager);
        groupsRecyclerView.setAdapter(groupsRVAdapter);

        groupsRVAdapter.setOnItemClickListener(
                new viewGroupsAdapter.OnItemClickListener() {

                    @Override
                    public void onJoinBtnClick(int position) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, SignatureException, InvalidKeyException, ClassNotFoundException, InvalidKeySpecException, NoSuchProviderException {
                        sendAddRequest(position);
                    }
                });

        groupsRVAdapter.notifyDataSetChanged();
    }

    class getUnjoinedGroups extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... voids) {

            currentIntent = getIntent();

            Log.d("Is is working?", "????");

            String notJoinedMemberId = prefsManager.getStringData("user_id");
            System.out.println("Sending Request for notJoinedMemberId : " + notJoinedMemberId);
            Call<List<GetUnjoinedGroupsModel>> getUnjoinedGroupsCall = Api.getClient().GetGroupsByNotAMemberId(notJoinedMemberId);
            try {
                final Response<List<GetUnjoinedGroupsModel>> response = getUnjoinedGroupsCall.execute();
                if (response != null && response.isSuccessful() & response.body() != null && response.body().size() > 0) {

                    hideProgressDialog();

                    responseGroups = response.body();

                    for (int i = 0; i < responseGroups.size(); i++) {

                        unjoinedGroupsList.add(new ViewUnjoinedGroupsItem(
                                responseGroups.get(i).group_owner_id,
                                responseGroups.get(i).group_id,
                                responseGroups.get(i).group_name,
                                responseGroups.get(i).group_key,
                                responseGroups.get(i).signature,
                                responseGroups.get(i).public_key,
                                responseGroups.get(i).owner_fullname
                        ));
                    }

                    Log.d("Is is working?", "????");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            groupsRVAdapter.notifyDataSetChanged();
                        }
                    });

                } else {

                    if (response.errorBody().toString().contains("PRIMARY KEY VIOLATION")) {
                        showSnackBar(response.body().toString(), findViewById(R.id.activity_view_groups));
                    }


                    hideProgressDialog();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void sendAddRequest(int position) throws IOException, ClassNotFoundException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, SignatureException, InvalidKeyException, InvalidKeySpecException, NoSuchProviderException {

        Log.d("sendAddRequest ", "sent");

        Intent currentIntent = getIntent();

        String user_id = currentIntent.getStringExtra("user_id");

        String joinRequestHash = KeyHelper2.generateRandomString();

        // verify group key
        Boolean groupKeyVerification = KeyHelper2.verifyData(
                responseGroups.get(position).signature,
                responseGroups.get(position).group_key,
                responseGroups.get(position).public_key
        );

        if (groupKeyVerification) {
            Log.d("Group key", "verified");

            PrivateKey myPrivKey = (PrivateKey) KeyHelper2.deserializeKey(
                    KeyHelper2.decodeB64(
                            prefsManager.getStringData("privateKey")
                    )
            );

            String signature = KeyHelper2.signData(joinRequestHash, myPrivKey);

            SendJoinGroupRequestAPI sendJoinRequestCall = new SendJoinGroupRequestAPI();
            sendJoinRequestCall.execute(
                    user_id,
                    responseGroups.get(position).group_id,
                    responseGroups.get(position).group_owner_id,
                    joinRequestHash,
                    signature);
        } else {

            Log.d("Group key verification:", "FAILED!");

        }

    }

    class SendJoinGroupRequestAPI extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            Call<String> sendJoinRequestCall = Api.getClient().CreateNewAddRequest(strings[0], strings[1], strings[2], strings[3], strings[4]);
            try {
                Response<String> response = sendJoinRequestCall.execute();
                if (response != null && response.isSuccessful() & response.body() != null) {
                    System.out.println(response);

                    showSnackBar(response.body().toString(), findViewById(R.id.activity_view_groups));

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
