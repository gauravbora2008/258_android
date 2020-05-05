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

    Map<Integer, String[]> groupData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_groups);
        setToolbarTitle("View Groups");

        getUnjoinedGroups getUnjoinedGroupsCall = new getUnjoinedGroups();
        getUnjoinedGroupsCall.execute();

    }

    class getUnjoinedGroups extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... voids) {
            final Spinner dropdown;

            currentIntent = getIntent();
            SNPrefsManager prefsManager = new SNPrefsManager(getApplicationContext(), currentIntent.getStringExtra("username"));

            List<GetUnjoinedGroupsModel> joinedGroups;
            String notJoinedMemberId = prefsManager.getStringData("user_id");
            System.out.println("Sending Request for notJoinedMemberId : " + notJoinedMemberId);
            Call<List<GetUnjoinedGroupsModel>> getUnjoinedGroupsCall = Api.getClient().GetGroupsByNotAMemberId(notJoinedMemberId);
            try {
                final Response<List<GetUnjoinedGroupsModel>> response = getUnjoinedGroupsCall.execute();
                if (response != null && response.isSuccessful() & response.body() != null && response.body().size() > 0) {

                    final ArrayList<ViewUnjoinedGroupsItem> unjoinedGroupsList = new ArrayList<>();
                    hideProgressDialog();

                    responseGroups = response.body();

                    for (int i = 0; i < responseGroups.size(); i++) {

                        unjoinedGroupsList.add(new ViewUnjoinedGroupsItem(
                                responseGroups.get(i).group_id,
                                responseGroups.get(i).group_owner_id,
                                responseGroups.get(i).owner_fullname,
                                responseGroups.get(i).group_name,
                                responseGroups.get(i).group_key
                        ));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            groupsRecyclerView = (RecyclerView) findViewById(R.id.view_groups_recycler_view);
                            groupsRecyclerView.setHasFixedSize(true);
                            groupsRVLayoutManager = new LinearLayoutManager(ViewGroupsActivity.this);
                            groupsRVAdapter = new viewGroupsAdapter(unjoinedGroupsList);
                            groupsRecyclerView.setLayoutManager(groupsRVLayoutManager);
                            groupsRecyclerView.setAdapter(groupsRVAdapter);

                            groupsRVAdapter.setOnItemClickListener(new viewGroupsAdapter.OnItemClickListener() {

                                @Override
                                public void onJoinBtnClick(int position) {
                                    sendAddRequest(position);
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

    private void sendAddRequest(int position) {

        Intent currentIntent = getIntent();

        String user_id = currentIntent.getStringExtra("user_id");
        String group_id = responseGroups.get(position).group_id;
        String group_owner_id = responseGroups.get(position).group_owner_id;
        Log.d(" user_id frm currIntent", user_id);
        Log.d(" group_id from btnClick", group_id);


        SendJoinGroupRequestAPI sendJoinRequestCall = new SendJoinGroupRequestAPI();
        sendJoinRequestCall.execute(user_id, group_id, group_owner_id);

    }

    class SendJoinGroupRequestAPI extends AsyncTask<String, Void, Void> {

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
