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
import com.paril.mlaclientapp.util.SNPrefsManager;
import com.paril.mlaclientapp.webservice.Api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

import static com.paril.mlaclientapp.ui.activity.MLASocialNetwork.showSnackBar;

public class AddRequestsActivity extends AppCompatActivity {

    private RecyclerView addGroupsRecyclerView;
    private viewPendingRequestsAdapter addGroupsRVAdapter;
    private RecyclerView.LayoutManager addGroupsRVLayoutManager;

    private ProgressDialog progressDialog;

    List<ViewPendingRequestsItem> responseGroups;

    Intent currentIntent;

    Map<Integer, String[]> groupData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_requests);
        setToolbarTitle("View Groups");

        getPendingAddRequests getPendingRequestsCall = new getPendingAddRequests();
        getPendingRequestsCall.execute();

        ArrayList<ViewPendingRequestsItem> groupsList = new ArrayList<>();
//        groupsList.add(new ViewPendingRequestsItem("1",
//                "1",
//                "Group1",
//                "Group 1 Name hai"));
//        groupsList.add(new ViewPendingRequestsItem("1",
//                "1",
//                "Group1",
//                "Group 1 Name hai"));
//        groupsList.add(new ViewPendingRequestsItem("1",
//                "1",
//                "Group1",
//                "Group 1 Name hai"));
//        groupsList.add(new ViewPendingRequestsItem("1",
//                "1",
//                "Group1",
//                "Group 1 Name hai"));
//        groupsList.add(new ViewPendingRequestsItem("1",
//                "1",
//                "Group1",
//                "Group 1 Name hai"));
//        groupsList.add(new ViewPendingRequestsItem("1",
//                "1",
//                "Group1",
//                "Group 1 Name hai"));

//        addGroupsRecyclerView = (RecyclerView) findViewById(R.id.view_groups_recycler_view);
//        addGroupsRecyclerView.setHasFixedSize(true);
//        groupsRVLayoutManager = new LinearLayoutManager(ViewGroupsActivity.this);
//        groupsRVAdapter = new viewGroupsAdapter(groupsList);
//        addGroupsRecyclerView.setLayoutManager(groupsRVLayoutManager);
//        addGroupsRecyclerView.setAdapter(groupsRVAdapter);
    }

    class getPendingAddRequests extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... voids) {
            final Spinner dropdown;

            currentIntent = getIntent();

            SNPrefsManager prefsManager = new SNPrefsManager(getApplicationContext(), currentIntent.getStringExtra("username"));

            List<ViewPendingRequestsItem> joinedGroups;
            String user_id = prefsManager.getStringData("user_id");

            Call<List<ViewPendingRequestsItem>> getPendingRequestsCall = Api.getClient().GetPendingAddRequests(user_id);
            try {
                final Response<List<ViewPendingRequestsItem>> response = getPendingRequestsCall.execute();
                if (response != null && response.isSuccessful() & response.body() != null && response.body().size() > 0) {

                    final ArrayList<ViewPendingRequestsItem> pendingRequestsList = new ArrayList<>();
                    hideProgressDialog();

                    List<ViewPendingRequestsItem> responseGroups = response.body();

                    for (int i = 0; i < responseGroups.size(); i++) {

                        pendingRequestsList.add(new ViewPendingRequestsItem(
                                responseGroups.get(i).getUser_id(),
                                responseGroups.get(i).getPublic_key(),
                                responseGroups.get(i).getGroup_id(),
                                responseGroups.get(i).getGroup_key(),
                                responseGroups.get(i).getGroup_name(),
                                responseGroups.get(i).getGroup_owner_id()

                        ));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addGroupsRecyclerView = (RecyclerView) findViewById(R.id.add_requests_recycler_view);
                            addGroupsRecyclerView.setHasFixedSize(true);
                            addGroupsRVLayoutManager = new LinearLayoutManager(AddRequestsActivity.this);
                            addGroupsRVAdapter = new viewPendingRequestsAdapter(pendingRequestsList);
                            addGroupsRecyclerView.setLayoutManager(addGroupsRVLayoutManager);
                            addGroupsRecyclerView.setAdapter(addGroupsRVAdapter);

                            addGroupsRVAdapter.setOnItemClickListener(
                                    new viewPendingRequestsAdapter.OnItemClickListener() {


                                        @Override
                                        public void onApproveBtnClick(int position) {
                                            sendApproveRequest(position);
                                        }

                                        @Override
                                        public void onDenyBtnClick(int position) {
                                            sendDenyRequest(position);
                                        }
                                    });
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

    private void sendApproveRequest(int position) {

        Intent currentIntent = getIntent();

        String user_id = currentIntent.getStringExtra("user_id");
        String group_id = responseGroups.get(position).getGroup_id();
        String group_owner_id = responseGroups.get(position).getGroup_owner_id();
        Log.d(" user_id frm currIntent", user_id);
        Log.d(" group_id from btnClick", group_id);


        SendApproveGroupRequestAPI sendJoinRequestCall = new SendApproveGroupRequestAPI();
        sendJoinRequestCall.execute(user_id, group_id, group_owner_id);

    }


    // Pending::  create API for approve pending then configure here and implement encryption/decryption
    class SendApproveGroupRequestAPI extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            Call<String> sendJoinRequestCall = Api.getClient().ApproveGroupRequest(strings[0], strings[1], strings[2]);
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

    private void sendDenyRequest(int position) {

        Intent currentIntent = getIntent();

        String user_id = currentIntent.getStringExtra("user_id");
        String group_id = responseGroups.get(position).getGroup_id();
        String group_owner_id = responseGroups.get(position).getGroup_owner_id();
        Log.d(" user_id frm currIntent", user_id);
        Log.d(" group_id from btnClick", group_id);


        SendApproveGroupRequestAPI sendJoinRequestCall = new SendApproveGroupRequestAPI();
        sendJoinRequestCall.execute(user_id, group_id, group_owner_id);

    }

    class SendDenyGroupRequestAPI extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            Call<String> sendJoinRequestCall = Api.getClient().CreateNewAddRequest(strings[0], strings[1], strings[2]);
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
