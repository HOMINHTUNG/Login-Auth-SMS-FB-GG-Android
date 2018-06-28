package com.adsmove.customerlogin.Login;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.adsmove.customerlogin.MainActivity;
import com.adsmove.customerlogin.R;
import com.adsmove.customerlogin.Register.PhoneAuthActivity;

public class LoginActivity extends AppCompatActivity {

    private TextView btnRegister;
    private TextView btnLogin;
    private EditText txtPhone;
    private TextView txtPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnRegister = (TextView)findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, PhoneAuthActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnLogin = (TextView)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
