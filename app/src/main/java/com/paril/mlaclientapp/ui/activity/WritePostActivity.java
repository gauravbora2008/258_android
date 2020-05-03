package com.paril.mlaclientapp.ui.activity;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.paril.mlaclientapp.R;
import com.paril.mlaclientapp.model.GetGroupsModel;
import com.paril.mlaclientapp.util.PrefsManager;
import com.paril.mlaclientapp.webservice.Api;

import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static com.paril.mlaclientapp.ui.activity.MLASocialNetwork.showSnackBar;

public class WritePostActivity extends AppCompatActivity {

    PrefsManager prefsManager;
    Button writePostBtn;
    EditText writePostET;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        setToolbarTitle("Write Post");

        writePostET = (EditText) findViewById(R.id.write_post_edittext);
        writePostBtn = (Button) findViewById(R.id.write_post_submit_btn);
        writePostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(writePostET.getText().toString())) {
                    showSnackBar("Please enter some post data", findViewById(R.id.write_post_main_activity));
                } else {
                    String postKey = KeyHelper.generateRandomString();
                    System.out.println("postKey" + postKey);
                }

            }
        });

        // Get groups this user is part of
        getGroups groupCall = new getGroups();
        groupCall.execute();


    }

    public void setToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    class getGroups extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... voids) {
            final Spinner dropdown;
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

                    String[] groupItems = new String[joinedGroups.size()];

                    for (int i = 0; i < joinedGroups.size(); i++) {
                        groupItems[i] = joinedGroups.get(i).group_name;
                    }

                    System.out.println(groupItems.toString());

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
}
