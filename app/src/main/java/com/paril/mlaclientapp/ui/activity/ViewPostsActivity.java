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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
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

//        ArrayList<ViewPostItem> postsList = new ArrayList<>();
//        postsList.add(new ViewPostItem("Author1",
//                "Lorem ipsum dolor sit amet consectetus aor enrique iglesius",
//                "Group1"));
//        postsList.add(new ViewPostItem("Author2",
//                "Lorem ipsum dolor sit amet consectetus aor enrique iglesius consectetus aor enrique iglesius consectetus aor enrique iglesius",
//                "Group2"));
//        postsList.add(new ViewPostItem("Author3",
//                "Lorem ipsum dolor sit amet consectetus aor enrique iglesius",
//                "Group3"));
//        postsList.add(new ViewPostItem("Author4",
//                "Lorem ipsum dolor sit amet consectetus aor enrique iglesius very long post consectetus aor enrique iglesius consectetus aor enrique iglesius consectetus aor enrique iglesius consectetus aor enrique iglesius consectetus aor enrique iglesius consectetus aor enrique iglesiusconsectetus aor enrique iglesius",
//                "Group4"));
//
//        postsRecyclerView = (RecyclerView) findViewById(R.id.view_posts_recycler_view);
//        postsRecyclerView.setHasFixedSize(true);
//        postsRVLayoutManager = new LinearLayoutManager(this);
//        postsRVAdapter = new viewPostsAdapter(postsList);
        showProgressDialog("Fetching Posts...");

        currentIntent = getIntent();
        viewPostsET = (TextView) findViewById(R.id.view_posts_main_ET);

        prefsManager = new SNPrefsManager(getApplicationContext(), currentIntent.getStringExtra("username"));
        String user_id = prefsManager.getStringData("user_id");

        GetPostsAPI getPostsCall = new GetPostsAPI(getApplicationContext());
        getPostsCall.execute(user_id);
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

                    String alias = prefsManager.getStringData("key_alias");

                    for (int i = 0; i < responsePosts.size(); i++) {

                        String decryptedGrpKey = KeyHelper.decryptData(responsePosts.get(i).group_key, getApplicationContext(), alias); // key is on index 1
                        Log.d("        decryptedGrpKey", decryptedGrpKey);

                        // decrypt postKey using groupKey
                        String decryptedPostKey = AESKeyHelper.decrypt(responsePosts.get(i).post_key, decryptedGrpKey);

                        // decrypt post_data using decrypted_post_key
                        String decryptedPostData = AESKeyHelper.decrypt(responsePosts.get(i).post_data, decryptedPostKey);

                        Log.d("      decryptedPostData", decryptedPostData);

                        postsList.add(new ViewPostItem(
                                responsePosts.get(i).fullname,
                                decryptedPostData,
                                responsePosts.get(i).group_name,
                                responsePosts.get(i).post_key,
                                responsePosts.get(i).group_key,
                                responsePosts.get(i).timestamp

                        ));
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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (UnrecoverableEntryException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void showProgressDialog(String message) {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = ProgressDialog.show(ViewPostsActivity.this, getString(R.string.app_name), message, true, false);
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
