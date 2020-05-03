package com.paril.mlaclientapp.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.paril.mlaclientapp.R;

public class ViewGroupsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_groups);
        setToolbarTitle("View Groups");
    }

    public void setToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
