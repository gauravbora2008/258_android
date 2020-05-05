package com.paril.mlaclientapp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.paril.mlaclientapp.R;

public class SNHomeActivity extends AppCompatActivity implements View.OnClickListener {

    Button writePostBtn;
    Button viewPostsBtn;
    Button createGroupBtn;
    Button viewGroupsBtn;
    Button addFriendsBtn;
    Button addRequestsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snhome);

        // import main btns
        writePostBtn = (Button) findViewById(R.id.write_posts_btn);
        viewPostsBtn = (Button) findViewById(R.id.view_posts_btn);
        viewGroupsBtn = (Button) findViewById(R.id.view_groups_btn);
        createGroupBtn = (Button) findViewById(R.id.create_group_btn);
        addFriendsBtn = (Button) findViewById(R.id.add_friends_btn);
        addRequestsBtn = (Button) findViewById(R.id.add_req_btn);

        writePostBtn.setOnClickListener(this);
        viewPostsBtn.setOnClickListener(this);
        viewGroupsBtn.setOnClickListener(this);
        createGroupBtn.setOnClickListener(this);
        addFriendsBtn.setOnClickListener(this);
        addRequestsBtn.setOnClickListener(this);

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

        startActivity(intent);
    }
}
