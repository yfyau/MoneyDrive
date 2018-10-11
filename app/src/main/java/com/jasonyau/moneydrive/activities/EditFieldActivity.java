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
import android.widget.Toast;

import com.jasonyau.moneydrive.R;
import com.jasonyau.moneydrive.adapter.FieldRecyclerViewAdapter;
import com.jasonyau.moneydrive.helper.ParameterKeys;
import com.jasonyau.moneydrive.helper.Parameters;

import java.util.List;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class EditFieldActivity  extends AppCompatActivity {

    ImageButton buttonAddField;

    List<String> fields;

    FieldRecyclerViewAdapter fieldRecyclerViewAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_field);

        fields = Parameters.getInstance(getApplicationContext()).getFields(ParameterKeys.FIELDS);

        Log.d("Field", "Fields = " + fields.isEmpty());

        recyclerView = (RecyclerView) findViewById(R.id.fieldRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fieldRecyclerViewAdapter = new FieldRecyclerViewAdapter(EditFieldActivity.this, fields);
        recyclerView.setAdapter(fieldRecyclerViewAdapter);

        buttonAddField = (ImageButton)findViewById(R.id.buttonAddField);
        buttonAddField.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("Fields", "Click!!!!");
                final EditText et = new EditText(EditFieldActivity.this);
                et.setMaxLines(1);

                new AlertDialog.Builder(EditFieldActivity.this).setTitle("Name a new record field: ")
                        .setView(et)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String input = et.getText().toString();
                                if (input.equals("")) {
                                    Toast.makeText(getApplicationContext(), "Field name cannot be empty ! " + input, Toast.LENGTH_LONG).show();
                                }
                                else {
                                    if (!fields.contains(input)){
                                        fields.add(input);
                                        Parameters.getInstance(getApplicationContext()).saveFields(ParameterKeys.FIELDS, fields);

                                        refreshRecyclerViewForField();
                                    }
                                    else
                                        Toast.makeText(getApplicationContext(), "Field name cannot be duplicated ! " + input, Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    private void refreshRecyclerViewForField()
    {
        fieldRecyclerViewAdapter.setField(fields);

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
