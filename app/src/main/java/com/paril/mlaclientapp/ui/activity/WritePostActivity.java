package com.paril.mlaclientapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.GetGroupsModel;
import com.paril.mlaclientapp.model.NewPostModel;
import com.paril.mlaclientapp.util.SNPrefsManager;
import com.paril.mlaclientapp.webservice.Api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import retrofit2.Call;
import retrofit2.Response;

import static com.paril.mlaclientapp.ui.activity.MLASocialNetwork.showSnackBar;

public class WritePostActivity extends AppCompatActivity {

    SNPrefsManager prefsManager;
    Button writePostBtn;
    EditText writePostET;
    Spinner groupsSpinner;
    Map<Integer, String[]> groupData;
    String selectedGroupId;
    String[] selectedGroupDetails;
    String selectedGroupKey;
    Intent currentIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        setToolbarTitle("Write Post");

        currentIntent = getIntent();

        prefsManager = new SNPrefsManager(getApplicationContext(), currentIntent.getStringExtra("username"));

        writePostET = (EditText) findViewById(R.id.write_post_edittext);
        writePostBtn = (Button) findViewById(R.id.write_post_submit_btn);
        groupsSpinner = (Spinner) findViewById(R.id.spinner1);

        // Get groups this user is part of
        getGroups groupCall = new getGroups();
        groupCall.execute();

        // setup write btn
        writePostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(writePostET.getText().toString())) {
                    showSnackBar("Please enter some post data", findViewById(R.id.write_post_main_activity));
                } else {

                    hideKeyboard();
                    String user_id = prefsManager.getStringData("user_id");
                    String alias = prefsManager.getStringData("key_alias");

                    // my public key
                    try {
                        PublicKey myPubKey = (PublicKey) KeyHelper2.deserializeKey(
                                KeyHelper2.decodeB64(
                                        prefsManager.getStringData("publicKey")
                                )
                        );


//                      3) verify group key
                        Boolean groupKeyVerificationResult = KeyHelper2.verifyData(
//                                alias, without and keystore
                                selectedGroupDetails[2], // sign
                                selectedGroupDetails[1], // grp key
                                selectedGroupDetails[3] // pub key
                        );

                        // 4) check if verification succeeds
                        if (groupKeyVerificationResult) {

                            Log.d("Group key ", "VERIFIED!");

                            String grpKeyStr = selectedGroupDetails[1];

                            byte[] grpKeyBytes = KeyHelper2.decodeB64(grpKeyStr);

                            PrivateKey privKey = (PrivateKey) KeyHelper2.deserializeKey(
                                    KeyHelper2.decodeB64(
                                            prefsManager.getStringData("privateKey")
                                    )
                            );

                            byte[] decryptedGrpKeyBytes = KeyHelper2.decryptGroupKey(grpKeyBytes, privKey);

                            System.out.println("decryptedGrpKeyBytes " + KeyHelper2.encodeB64(decryptedGrpKeyBytes));
                            System.out.println("length " + decryptedGrpKeyBytes.length);
                            System.out.println("data " + Arrays.toString(decryptedGrpKeyBytes));

//                            // 1) generates 16 bytes
                            byte[] postKeyBytes = KeyHelper2.generateRandomBytes();
//
//                            System.out.println("postKeyBytes " + Arrays.toString(postKeyBytes));
//
//                            // 2) get post data in bytes
                            String postData = writePostET.getText().toString();
                            byte[] postDataBytes = postData.getBytes("UTF-8");
//
//                            // 3) encrypt postData with postKeyBytes
                            byte[] encryptedPostData = AESKeyHelper.encrypt(
                                    postDataBytes,
                                    postKeyBytes);

                            byte[] decryptedGrpKeyBytesCopy = Arrays.copyOf(decryptedGrpKeyBytes, 16);

                            System.out.println("decryptedGrpKeyBytes " + KeyHelper2.encodeB64(decryptedGrpKeyBytes));
                            System.out.println("length " + decryptedGrpKeyBytes.length);
                            System.out.println("data " + Arrays.toString(decryptedGrpKeyBytes));

//                          // encrypt postKey with groupKey
                            byte[] encryptedPostKeyBytes = AESKeyHelper.encrypt(postKeyBytes, decryptedGrpKeyBytes);

                            // test - decrypt post key
//                            String decryptedPostKey = AESKeyHelper.decrypt(encryptedPostKeyBytes, KeyHelper2.encodeB64(decryptedGrpKeyBytes));
//
//                            // decrypt post data
//                            String decryptedPostData = AESKeyHelper.decrypt(encryptedPostData, decryptedPostKey);
//                            Log.d("decryptedPostData", decryptedPostData);
                            // test decryption ENDS

//                                    // create a new post object
                            NewPostModel _newPost = new NewPostModel(
                                    KeyHelper2.encodeB64(encryptedPostKeyBytes),
                                    selectedGroupId,
                                    KeyHelper2.encodeB64(encryptedPostData),
                                    user_id
                            );
//
//                            // send data
                            writePostAPI writePostCall = new writePostAPI(getApplicationContext());
                            writePostCall.execute(
                                    _newPost.getAuthor_id(),
                                    _newPost.getGroup_id(),
                                    _newPost.getPost_key(),
                                    _newPost.getPost_data(),
                                    _newPost.getTimestamp());

                        } else {
                            Log.d("Group key ", "verification failed !");
                        }

                    } catch (IOException | ClassNotFoundException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException | CertificateException | NoSuchProviderException | UnrecoverableEntryException | NoSuchAlgorithmException | KeyStoreException | InvalidAlgorithmParameterException | InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (SignatureException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // set current group id when a group is selected
        groupsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                // your code here
                String _value = groupsSpinner.getSelectedItem().toString();

                selectedGroupId = _value.split("[.]")[0];
                Log.d("        selectedGroupId", selectedGroupId);

                selectedGroupDetails = groupData.get(Integer.parseInt(selectedGroupId));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
    }

    public void setToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    class writePostAPI extends AsyncTask<String, Void, Void> {

        Context appContext;

        public writePostAPI(Context context) {
            appContext = context;
        }

        @Override
        protected Void doInBackground(String... params) {
            Log.d("                newPost", params.toString());
            Call<String> createPostCall = Api.getClient().createNewPost(
                    params[0], params[1], params[2], params[3], params[4]
            );
            try {
                Response response = createPostCall.execute();
                if (response != null && response.isSuccessful() & response.body() != null) {
                    System.out.println(response);

                    showSnackBar(response.body().toString(), findViewById(R.id.write_post_main_activity));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            writePostET.setText("");
                        }
                    });
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
            final Spinner dropdown;
            currentIntent = getIntent();
            prefsManager = new SNPrefsManager(getApplicationContext(), currentIntent.getStringExtra("username"));

            List<GetGroupsModel> joinedGroups;
            String userId = prefsManager.getStringData("user_id");

            Call<List<GetGroupsModel>> callAuth = Api.getClient().GetGroupsByMemberId(userId);
            try {
                Response<List<GetGroupsModel>> respAuth = callAuth.execute();

                if (respAuth != null && respAuth.isSuccessful() & respAuth.body() != null && respAuth.body().size() > 0) {

                    joinedGroups = respAuth.body();
                    System.out.println("joinedGroups " + Arrays.toString(joinedGroups.toArray()));

                    groupData = new HashMap<Integer, String[]>();

                    String[] groupItems = new String[joinedGroups.size()];

                    for (int i = 0; i < joinedGroups.size(); i++) {
                        groupItems[i] = joinedGroups.get(i).group_id + ". " + joinedGroups.get(i).group_name;
                        String[] _groupDetails = {
                                joinedGroups.get(i).group_name,
                                joinedGroups.get(i).group_key,
                                joinedGroups.get(i).signature,
                                joinedGroups.get(i).grp_ownrs_pub_key,
                        };
                        groupData.put(joinedGroups.get(i).group_id, _groupDetails);
                    }

                    System.out.println(Arrays.toString(groupItems));

                    dropdown = findViewById(R.id.spinner1);

                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(WritePostActivity.this, android.R.layout.simple_spinner_dropdown_item, groupItems);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dropdown.setAdapter(adapter);
                        }
                    });

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new String[0];
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
}
