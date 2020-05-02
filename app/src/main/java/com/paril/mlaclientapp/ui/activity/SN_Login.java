package com.paril.mlaclientapp.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.paril.mlaclientapp.R;

public class SN_Login extends AppCompatActivity {

    Button openRegisterScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sn__login);

        // open login activity
        openRegisterScreen = (Button) findViewById(R.id.login_screen_register_btn);
        openRegisterScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openRegisterIntent = new Intent(SN_Login.this, MLASocialNetwork.class);
                openRegisterIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(openRegisterIntent);
                overridePendingTransition(0, 0);
                SN_Login.this.finish();
            }
        });
    }
}
