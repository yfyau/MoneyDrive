package com.jasonyau.moneydrive.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jasonyau.moneydrive.R;
import com.jasonyau.moneydrive.adapter.AccountRecyclerViewAdapter;
import com.jasonyau.moneydrive.customClass.Account;
import com.jasonyau.moneydrive.helper.FirebaseHelper;
import com.jasonyau.moneydrive.helper.ParameterKeys;
import com.jasonyau.moneydrive.helper.Parameters;
import com.jasonyau.moneydrive.interfaces.IFirebaseListener;

import java.util.ArrayList;
import java.util.List;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class EditAccountActivity extends AppCompatActivity {

    ImageButton buttonAddAccount;
    ImageButton buttonImportAccount;

    List<Account> accounts;

    AccountRecyclerViewAdapter accountRecyclerViewAdapter;
    private RecyclerView recyclerView;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        userId = Parameters.getInstance(getApplicationContext()).getString(ParameterKeys.USER_ID);

        accounts = new ArrayList<>();
        FirebaseHelper.getUserAcIds(userId, new IFirebaseListener() {
            @Override
            public void onCallback(String value) {

            }

            @Override
            public void onCallback(Double value) {

            }

            @Override
            public void onCallback(List value) {
                if (value != null) {
                    accounts.clear();
                    List<String> accountIds = value;
                    for (String acId : accountIds) {
                        Log.d("Account", "!!!AcId =  " + acId);
                        FirebaseHelper.getAccountsByAcId(acId, new IFirebaseListener() {
                            @Override
                            public void onCallback(String value) {

                            }

                            @Override
                            public void onCallback(Double value) {

                            }

                            @Override
                            public void onCallback(List value) {
                                if (value != null && value.size() != 0) {
                                    Account ac = (Account) value.get(0);
                                    accounts.add(ac);
                                }

                                accountRecyclerViewAdapter = new AccountRecyclerViewAdapter(EditAccountActivity.this, accounts);
                                recyclerView.setAdapter(accountRecyclerViewAdapter);
                            }
                        });
                    }
                }
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.accountRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        accountRecyclerViewAdapter = new AccountRecyclerViewAdapter(EditAccountActivity.this, accounts);
        recyclerView.setAdapter(accountRecyclerViewAdapter);


        buttonImportAccount = (ImageButton)findViewById(R.id.buttonImportAccount);
        buttonImportAccount.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {

                final EditText etAcId = new EditText(EditAccountActivity.this);
                etAcId.setMaxLines(1);

                new AlertDialog.Builder(EditAccountActivity.this).setTitle("Copy Account ID at here: ")
                        .setView(etAcId)
                        .setPositiveButton("Import", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String inputAcId = etAcId.getText().toString();
                                if (inputAcId.equals("")) {
                                    Toast.makeText(getApplicationContext(), "Account ID cannot be empty ! " + inputAcId, Toast.LENGTH_LONG).show();
                                }
                                else {
                                    boolean duplicated = false;
                                    for (Account account : accounts){
                                        if (account.getAcId().equals(inputAcId))
                                            duplicated = true;
                                    }
                                    if (!duplicated){
                                        FirebaseHelper.getAccountsByAcId(inputAcId, new IFirebaseListener() {
                                            @Override
                                            public void onCallback(String value) {

                                            }

                                            @Override
                                            public void onCallback(Double value) {

                                            }

                                            @Override
                                            public void onCallback(List value) {
                                                List<Account> aValue = value;
                                                if (value != null){
                                                    accounts.add(aValue.get(0));
                                                    updateUserAcIds();
                                                    Toast.makeText(getApplicationContext(), "Account Imported ! " + aValue.get(0).getAcName(), Toast.LENGTH_LONG).show();
                                                }
                                                else
                                                    Toast.makeText(getApplicationContext(), "Account not found ! ", Toast.LENGTH_LONG).show();

                                            }
                                        });
                                    }
                                    else
                                        Toast.makeText(getApplicationContext(), "Account cannot be duplicated ! " + inputAcId, Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });



        buttonAddAccount = (ImageButton)findViewById(R.id.buttonAddAccount);
        buttonAddAccount.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {

                final EditText etAcName = new EditText(EditAccountActivity.this);
                etAcName.setMaxLines(1);

                new AlertDialog.Builder(EditAccountActivity.this).setTitle("Name a new  Account: ")
                        .setView(etAcName)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String inputAcName = etAcName.getText().toString();
                                if (inputAcName.equals("")) {
                                    Toast.makeText(getApplicationContext(), "Account name cannot be empty ! " + inputAcName, Toast.LENGTH_LONG).show();
                                }
                                else {
                                    if (!accounts.contains(inputAcName)){
                                        Account newAccount = FirebaseHelper.addAccount(userId, inputAcName, "Saving Account", "HKD");
                                        accounts.add(newAccount);
                                        updateUserAcIds();
                                    }
                                    else
                                        Toast.makeText(getApplicationContext(), "Account cannot be duplicated ! " + inputAcName, Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    protected void updateUserAcIds() {
        List<String> accountIds = new ArrayList<>();

        for (Account account : accounts){
            accountIds.add(account.getAcId());
        }

        // Update User
        FirebaseHelper.editUserWithAccount(userId, accountIds);
    }


    private void refreshRecyclerViewForAccount()
    {
        accountRecyclerViewAdapter.setAccounts(accounts);

//        if(records.size() > 0)
//        {
//            recyclerView.setVisibility(View.VISIBLE);
//            recyclerViewPlaceholder.setVisibility(View.GONE);
//        }
//        else
//        {
//            recyclerView.setVisibility(View.GONE);
//            recyclerViewPlaceholder.setVisibility(View.VISIBLE);
//        }
    }
}
