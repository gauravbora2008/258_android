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
import java.lang.reflect.Array;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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

        writePostET = (EditText) findViewById(R.id.write_post_edittext);
        writePostBtn = (Button) findViewById(R.id.write_post_submit_btn);
        groupsSpinner = (Spinner) findViewById(R.id.spinner1);

        writePostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(writePostET.getText().toString())) {
                    showSnackBar("Please enter some post data", findViewById(R.id.write_post_main_activity));
                } else {

                    hideKeyboard();

                    currentIntent = getIntent();

                    // 1) Generate a post key, get user_id and get post data from ET
                    String postKey = KeyHelper.generateRandomString();
                    String postData = writePostET.getText().toString();
                    prefsManager = new SNPrefsManager(getApplicationContext(), currentIntent.getStringExtra("username"));
                    String user_id = prefsManager.getStringData("user_id");
                    String alias = prefsManager.getStringData("key_alias");;

                    Log.d("                user_id", user_id);
                    Log.d("                  alias", alias);

                    // 2) decrypt group key using private key
                    try {
                        Log.d("selectedGroupDetails[1]", selectedGroupDetails[1]);
                        String decryptedGrpKey = KeyHelper.decryptData(selectedGroupDetails[1], getApplicationContext(), alias); // key is on index 1
                        Log.d("   decrGrpKey writePost", decryptedGrpKey);

                        // encrypt postData with postkey
                        String postDataEncryptedWithPostKey = AESKeyHelper.encrypt(postData, postKey);

                        // encrypt postKey with groupKey
                        String encryptedPostKey = AESKeyHelper.encrypt(postKey, decryptedGrpKey);

                        String decryptedPostKey = AESKeyHelper.decrypt(encryptedPostKey, decryptedGrpKey);
                        String decryptedPostData = AESKeyHelper.decrypt(postDataEncryptedWithPostKey, decryptedPostKey);
                        Log.d("      decryptedPostData", ""+decryptedPostData);


                        NewPostModel _newPost = new NewPostModel(
                                encryptedPostKey,
                                selectedGroupId,
                                postDataEncryptedWithPostKey,
                                user_id
                        );

                        writePostAPI writePostCall = new writePostAPI(getApplicationContext());
                        writePostCall.execute(
                                _newPost.getAuthor_id(),
                                _newPost.getGroup_id(),
                                _newPost.getPost_key(),
                                _newPost.getPost_data(),
                                _newPost.getTimestamp());

                    } catch (UnrecoverableEntryException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (KeyStoreException e) {
                        e.printStackTrace();
                    } catch (NoSuchProviderException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (InvalidAlgorithmParameterException e) {
                        e.printStackTrace();
                    } catch (CertificateException e) {
                        e.printStackTrace();
                    }

                }

            }
        });

        // Get groups this user is part of
        getGroups groupCall = new getGroups();
        groupCall.execute();

        // set current groud id when a group is selected
        groupsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                String _value = groupsSpinner.getSelectedItem().toString();
//                String _grpid = groupData.get(_value);


                selectedGroupId = _value.split("[.]")[0];
                Log.d("        selectedGroupId", selectedGroupId);

                selectedGroupDetails = groupData.get(Integer.parseInt(selectedGroupId));
//                String _selectedGroupDetails0 = groupData.get(selectedGroupId).toString();
//                String _selectedGroupDetails1 = groupData.get(selectedGroupId);
//                groupData.

//                Log.v("                  value", "" + _value);
                Log.v("                     id", "this is split" + _value.split("[.]")[0]);
                Log.v("   selectedGroupDetails", "this is map value:" + Arrays.toString(selectedGroupDetails));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
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

//        @Override
//        protected SNUserLogin doInBackground(String... params) {
//            SNUserLogin login = new SNUserLogin();
//            System.out.println("params============== "+ params[0] + " " + params[1]);
//            Call<List<SNUserLogin>> loginCallAuth = Api.getClient().loginAuth(params[0], params[1]);
//            try {
//                Response<List<SNUserLogin>> respAuth = loginCallAuth.execute();
//                if (respAuth != null && respAuth.isSuccessful() & respAuth.body() != null && respAuth.body().size() > 0) {
//                    login = respAuth.body().get(0);
//                    System.out.println(login.user_id + " ...... " + login.fullname);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return login;
//        }

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
            System.out.println("Sending Request for userId : " + userId);
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
                        String[] _groupDetails = {joinedGroups.get(i).group_name, joinedGroups.get(i).group_key};
                        groupData.put(joinedGroups.get(i).group_id, _groupDetails);
                    }
                    System.out.println(Arrays.toString(groupItems));

                    //get the spinner from the xml.
                    dropdown = (Spinner) findViewById(R.id.spinner1);

                    //create an adapter to describe how the items are displayed, adapters are used in several places in android.
                    //There are multiple variations of this, but this is the basic variant.
                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(WritePostActivity.this, android.R.layout.simple_spinner_dropdown_item, groupItems);
                    //set the spinners adapter to the previously created one.

                    // setAdapter needs to run on main ui thread
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
