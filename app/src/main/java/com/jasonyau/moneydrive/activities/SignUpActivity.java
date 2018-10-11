package com.jasonyau.moneydrive.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jasonyau.moneydrive.R;
import com.jasonyau.moneydrive.helper.FirebaseHelper;
import com.jasonyau.moneydrive.helper.ParameterKeys;
import com.jasonyau.moneydrive.helper.Parameters;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class SignUpActivity extends AppCompatActivity {

    EditText editTextUserId;
    EditText editTextPassword;

    Button buttonAddUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextUserId = (EditText) findViewById(R.id.editTextUserId);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        buttonAddUser = (Button) findViewById(R.id.buttonAddUser);
        buttonAddUser.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = editTextUserId.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (FirebaseHelper.addUser(userId, password)) {
                    Toast.makeText(SignUpActivity.this, "User Added !", Toast.LENGTH_LONG).show();
                    Parameters.getInstance(getApplicationContext()).putString(ParameterKeys.USER_ID, userId);
                    finish();
                }
                else
                    Toast.makeText(SignUpActivity.this, "Enter Name and Password !", Toast.LENGTH_LONG).show();
            }
        });
    }
}
