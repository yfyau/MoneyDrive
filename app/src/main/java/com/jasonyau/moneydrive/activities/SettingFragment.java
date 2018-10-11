package com.jasonyau.moneydrive.activities;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jasonyau.moneydrive.R;
import com.jasonyau.moneydrive.helper.FirebaseHelper;
import com.jasonyau.moneydrive.helper.ParameterKeys;
import com.jasonyau.moneydrive.helper.Parameters;
import com.jasonyau.moneydrive.interfaces.IFirebaseListener;

import java.util.List;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class SettingFragment extends Fragment {

    Button buttonLogin;
    Button buttonLogout;
    Button buttonSignup;
    Button buttonEditField;
    Button buttonEditAccount;

    TextView tv_title;

    private String LOGIN_SUCCESSFUL_FLAG = "Log in Successful !";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume(){
        super.onResume();

        refreshSettingView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // Capture the layout's TextView and set the string as its text
        tv_title = view.findViewById(R.id.tv_title);
        tv_title.setText("Please Log in");

        buttonLogin = view.findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout linearLayout = new LinearLayout(getContext());
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                final EditText etUser = new EditText(getContext());
                final EditText etPw = new EditText(getContext());
                etUser.setHint("User: ");
                etUser.setMaxLines(1);
                etPw.setHint("Password: ");
                etPw.setMaxLines(1);
                linearLayout.addView(etUser);
                linearLayout.addView(etPw);

                new AlertDialog.Builder(getContext()).setTitle("Log in: ")
                        .setView(linearLayout)
                        .setPositiveButton("Log in", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final String inUser = etUser.getText().toString();
                                final String inPw = etPw.getText().toString();

                                if (inUser.equals("") || inPw.equals("")) {
                                    Toast.makeText(getContext(), "User / Pw cannot be empty ! ", Toast.LENGTH_LONG).show();
                                }
                                else {

                                    FirebaseHelper.loginUser(inUser, inPw, new IFirebaseListener() {
                                        @Override
                                        public void onCallback(String value) {
                                            Toast.makeText(getContext(), value, Toast.LENGTH_LONG).show();
                                            if (value.equals(LOGIN_SUCCESSFUL_FLAG)) {
                                                tv_title.setText(inUser);
                                                Parameters.getInstance(getContext()).putString(ParameterKeys.USER_ID, inUser);

                                                refreshSettingView();
                                            }
                                        }

                                        @Override
                                        public void onCallback(Double value) {

                                        }

                                        @Override
                                        public void onCallback(List value) {

                                        }
                                    });

                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        buttonLogout = view.findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            new AlertDialog.Builder(getContext()).setTitle("Warning! Fields Data will be Clean ! Are You sure?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Parameters.getInstance(getContext()).putString(ParameterKeys.USER_ID, null);
                            Parameters.getInstance(getContext()).putLong(ParameterKeys.INIT_DATE, 0);

                            refreshSettingView();

                            getActivity().finish();
                            System.exit(0);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
            }
        });

        buttonSignup = view.findViewById(R.id.buttonSignup);
        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(getActivity(), SignUpActivity.class);
            startActivity(intent);
            }
        });

        buttonEditField = view.findViewById(R.id.buttonEditField);
        buttonEditField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditFieldActivity.class);
                startActivity(intent);
            }
        });

        buttonEditAccount = view.findViewById(R.id.buttonEditAccount);
        buttonEditAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditAccountActivity.class);
                startActivity(intent);
            }
        });

        refreshSettingView();

        return view;
    }

    private void refreshSettingView(){
        if (Parameters.getInstance(getContext()).getString(ParameterKeys.USER_ID) != null) {
            tv_title.setText(Parameters.getInstance(getContext()).getString(ParameterKeys.USER_ID));
            buttonLogin.setVisibility(View.GONE);
            buttonLogout.setVisibility(View.VISIBLE);
            buttonSignup.setVisibility(View.GONE);
        } else {
            tv_title.setText("Please Log in");
            buttonLogin.setVisibility(View.VISIBLE);
            buttonLogout.setVisibility(View.GONE);
            buttonSignup.setVisibility(View.VISIBLE);
        }
    }

}