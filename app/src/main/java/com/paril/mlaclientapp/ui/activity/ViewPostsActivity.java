package com.paril.mlaclientapp.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.GetPostsModel;
import com.paril.mlaclientapp.model.ViewPostItem;
import com.paril.mlaclientapp.ui.adapter.viewPostsAdapter;
import com.paril.mlaclientapp.util.SNPrefsManager;
import com.paril.mlaclientapp.webservice.Api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import retrofit2.Call;
import retrofit2.Response;

import static com.paril.mlaclientapp.ui.activity.MLASocialNetwork.showSnackBar;

public class ViewPostsActivity extends AppCompatActivity {

    SNPrefsManager prefsManager;
    TextView viewPostsET;
    private RecyclerView postsRecyclerView;
    private RecyclerView.Adapter postsRVAdapter;
    private RecyclerView.LayoutManager postsRVLayoutManager;
    Intent currentIntent;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);
        setToolbarTitle("View Posts");

        showProgressDialog("Fetching Posts...");

        currentIntent = getIntent();
        viewPostsET = (TextView) findViewById(R.id.view_posts_main_ET);

        prefsManager = new SNPrefsManager(getApplicationContext(), currentIntent.getStringExtra("username"));
        String user_id = prefsManager.getStringData("user_id");

        GetPostsAPI getPostsCall = new GetPostsAPI(getApplicationContext());
        getPostsCall.execute(user_id);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    class GetPostsAPI extends AsyncTask<String, Void, GetPostsModel> {

        Context appContext;

        public GetPostsAPI(Context context) {
            appContext = context;
        }

        @Override
        protected GetPostsModel doInBackground(String... _user_id) {

            Call<List<GetPostsModel>> getPostsCall = Api.getClient().getPosts(_user_id[0]);
            try {
                final Response<List<GetPostsModel>> response = getPostsCall.execute();
                if (response != null && response.isSuccessful() & response.body() != null && response.body().size() > 0) {

                    final ArrayList<ViewPostItem> postsList = new ArrayList<>();

                    List<GetPostsModel> responsePosts = response.body();

                    Boolean grpKeyVerify;

                    // verify group key
                    for (int i = 0; i < responsePosts.size(); i++) {

                        PublicKey sendersPubKey = (PublicKey) KeyHelper2.deserializeKey(
                                KeyHelper2.decodeB64(
                                        responsePosts.get(i).ownr_public_key
                                )
                        );

                        if (KeyHelper2.verifyData(
                                responsePosts.get(i).signature,
                                responsePosts.get(i).group_key,
                                KeyHelper2.encodeB64(
                                        KeyHelper2.serializeKey(sendersPubKey)
                                )
                        )) grpKeyVerify = true;
                        else grpKeyVerify = false;

                        if (!grpKeyVerify) {
                            Log.d("Group Key Verify failed", grpKeyVerify.toString());
                        } else {

                            Log.d("Grp Key", "VERIFIED!");

                            String privateKeyStr = prefsManager.getStringData("privateKey");

                            PrivateKey privateKey_ = (PrivateKey) KeyHelper2.deserializeKey(
                                    KeyHelper2.decodeB64(privateKeyStr)
                            );

                            System.out.println("Decrypting Grp Key");
                            byte[] decryptedGrpKey = KeyHelper2.decryptGroupKey(
                                    KeyHelper2.decodeB64(responsePosts.get(i).group_key),
                                    privateKey_);

                            System.out.println("Tag 1");

                            // 1) decrypt postKey using groupKey
                            byte[] decryptedPostKey = AESKeyHelper.decrypt(
                                    KeyHelper2.decodeB64(responsePosts.get(i).post_key),
                                    decryptedGrpKey);

                            System.out.println("Tag 2");

                            // 2) decrypt post_data using decrypted_post_key
                            byte[] decryptedPostData = AESKeyHelper.decrypt(
                                    KeyHelper2.decodeB64(responsePosts.get(i).post_data),
                                    decryptedPostKey);

                            postsList.add(new ViewPostItem(

                                    responsePosts.get(i).fullname,
                                    new String(decryptedPostData, StandardCharsets.UTF_8),
                                    responsePosts.get(i).group_name,
                                    responsePosts.get(i).post_key,
                                    responsePosts.get(i).group_key,
                                    responsePosts.get(i).timestamp

                            ));
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            postsRecyclerView = (RecyclerView) findViewById(R.id.view_posts_recycler_view);
                            postsRecyclerView.setHasFixedSize(true);
                            postsRVLayoutManager = new LinearLayoutManager(ViewPostsActivity.this);
                            postsRVAdapter = new viewPostsAdapter(postsList);
                            postsRecyclerView.setLayoutManager(postsRVLayoutManager);
                            postsRecyclerView.setAdapter(postsRVAdapter);

                            hideProgressDialog();
                        }
                    });

                } else {
                    hideProgressDialog();
                    showSnackBar("Some error occurred!", findViewById(R.id.view_posts_main_activity));
                }
            } catch (IOException | SignatureException | InvalidKeySpecException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | CertificateException | NoSuchProviderException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableEntryException | InvalidKeyException e) {
                System.out.println(e.getCause());
                System.out.println(e.getLocalizedMessage());
                System.out.println(e.getMessage());
                System.out.println(e.toString());
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void showProgressDialog(String message) {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = ProgressDialog.show(ViewPostsActivity.this, getString(R.string.app_name), message, true, false);
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void setToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
