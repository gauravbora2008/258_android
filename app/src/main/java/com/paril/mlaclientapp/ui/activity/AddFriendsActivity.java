package com.paril.mlaclientapp.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.paril.mlaclientapp.R;

public class AddFriendsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        setToolbarTitle("Add Friends");

    }

    public void setToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
