package com.jasonyau.moneydrive.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jasonyau.moneydrive.R;
import com.jasonyau.moneydrive.customClass.Account;
import com.jasonyau.moneydrive.customClass.Record;
import com.jasonyau.moneydrive.helper.CurrencyHelper;
import com.jasonyau.moneydrive.helper.FirebaseHelper;
import com.jasonyau.moneydrive.helper.ParameterKeys;
import com.jasonyau.moneydrive.helper.Parameters;
import com.jasonyau.moneydrive.helper.UIHelper;
import com.jasonyau.moneydrive.interfaces.IFirebaseListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class EditRecordActivity extends AppCompatActivity {

    private Button      saveButtom;
    private Button      cancelButtom;

    private EditText    descriptionEditText;
    private EditText    amountEditText;
    private Button      dateButton;
    private TextView    expenseType;
    private Spinner     recurringSpinner;
    private Spinner     fieldSpinner;
    private Spinner     accountSpinner;

    private Date        chosen_date;

    private String[] fieldString;
    private String[] accountString;
    private int accountSpinnerIndex;

    private boolean isIncome = false;
    private boolean isEdit = false;

    private Record record;
    List<Account> accounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_record);

        record = getIntent().getParcelableExtra("record");
        chosen_date = new Date(getIntent().getLongExtra("chosen_date", 0));

        if ( record != null )
        {
            isIncome = record.isIncome();
            chosen_date = record.getDateDate();
            isEdit = true;
        }

        setUpButtons();
        setUpInputs();
        setUpDateButton();


//        if ( UIHelper.willAnimateActivityEnter(this) ){
//            UIHelper.animateActivityEnter(this, new AnimatorListenerAdapter()
//            {
//                @Override
//                public void onAnimationEnd(Animator animation)
//                {
//                    UIHelper.setFocus(descriptionEditText);
//                }
//            });
//        }
//        else {
//            UIHelper.setFocus(descriptionEditText);
//        }

    }

    private void setUpButtons()
    {
        expenseType = (TextView) findViewById(R.id.expense_type_tv);

        SwitchCompat expenseTypeSwitch = (SwitchCompat) findViewById(R.id.expense_type_switch);
        expenseTypeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                isIncome = isChecked;
                setExpenseTypeTextViewLayout();
            }
        });

        // Init value to checked if already a revenue (can be true if we are editing an expense)
        if( isIncome )
        {
            expenseTypeSwitch.setChecked(true);
            setExpenseTypeTextViewLayout();
        }

        saveButtom = findViewById(R.id.save_button);
        saveButtom.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if( validateInputs() )
                {
                    double amount = Double.parseDouble(amountEditText.getText().toString()) * 1.0;
                    String description = descriptionEditText.getText().toString();
                    boolean isIncome = expenseType.getText().toString().equals("Income");

                    if (isEdit){
                        if (FirebaseHelper.editRecord(record.getRecordId(), accounts.get(accountSpinner.getSelectedItemPosition()).getAcId(), fieldString[fieldSpinner.getSelectedItemPosition()],  chosen_date, amount, description, isIncome)){
                            Toast.makeText(EditRecordActivity.this, "Record Edited !", Toast.LENGTH_LONG).show();
                            finish();
                        }
                        else
                            Toast.makeText(EditRecordActivity.this, "Error Record NOT Edited !", Toast.LENGTH_LONG).show();

                    } else {
                        if (FirebaseHelper.addRecord(accounts.get(accountSpinner.getSelectedItemPosition()).getAcId(), fieldString[fieldSpinner.getSelectedItemPosition()],  chosen_date, isIncome, amount, description, "NONE")){
                            Toast.makeText(EditRecordActivity.this, "Record Added !", Toast.LENGTH_LONG).show();
                            finish();
                        }
                        else
                            Toast.makeText(EditRecordActivity.this, "Error Record NOT Added !", Toast.LENGTH_LONG).show();

                    }

                }
            }
        });

        cancelButtom = findViewById(R.id.cancel_button);
        cancelButtom.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    private void setUpInputs()
    {
        Locale defaultLocale = Locale.getDefault();
        Currency defaultCurrency = Currency.getInstance(defaultLocale);
        ((TextInputLayout) findViewById(R.id.amount_inputlayout)).setHint(getResources().getString(R.string.amount, defaultCurrency));


        descriptionEditText = (EditText) findViewById(R.id.description_edittext);

        if( record != null )
        {
            descriptionEditText.setText(record.getDescription());
            descriptionEditText.setSelection(descriptionEditText.getText().length()); // Put focus at the end of the text
        }

        amountEditText = (EditText) findViewById(R.id.amount_edittext);
        UIHelper.preventUnsupportedInputForDecimals(amountEditText);

        if( record != null )
        {
            amountEditText.setText(CurrencyHelper.getFormattedAmountValue(Math.abs(record.getAmount())));
        }



        accountSpinner = (Spinner) findViewById(R.id.account_spinner);
        FirebaseHelper.getUserAcIds(Parameters.getInstance(getApplicationContext()).getString(ParameterKeys.USER_ID), new IFirebaseListener() {
            @Override
            public void onCallback(String value) {

            }

            @Override
            public void onCallback(Double value) {

            }

            @Override
            public void onCallback(List value) {
                if (value != null) {
                    List<String> accountIds = value;
                    accounts = new ArrayList<>();
                    for (String acId : accountIds) {
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
                                    Log.d("123", "Accounts = " + accounts + " / ac = "+ ac);
                                    accounts.add(ac);
                                    accountString = new String[accounts.size()];

                                    for (int i = 0; i < accounts.size(); i++){
                                        accountString[i] = accounts.get(i).getAcName();
                                        if (record != null && accounts.get(i).getAcId().equals(record.getAcId())) {
                                            Log.d("Record", "" + accounts.get(i).getAcId() + " == " + record.getAcId());
                                            accountSpinnerIndex = i;
                                        }
                                    }

                                    final ArrayAdapter accountAdapter = new ArrayAdapter<>(getBaseContext(), R.layout.spinner_item, accountString);
                                    accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    accountSpinner.setAdapter(accountAdapter);
                                    accountSpinner.setSelection(accountSpinnerIndex, false);
                                }
                            }
                        });
                    }
                }
            }
        });

//        FirebaseHelper.getAccountsByUserId(Parameters.getInstance(getApplicationContext()).getString(ParameterKeys.USER_ID), new IFirebaseListener() {
//            @Override
//            public void onCallback(String value) {
//
//            }
//
//            @Override
//            public void onCallback(Double value) {
//
//            }
//
//            @Override
//            public void onCallback(List value) {
//                accounts = value;
//                accountString = new String[accounts.size()];
//                for (int i = 0; i < accounts.size(); i++){
//                    accountString[i] = accounts.get(i).getAcName();
//                    if (record != null && accounts.get(i).getAcId().equals(record.getAcId())) {
//                        Log.d("Record", "" + accounts.get(i).getAcId() + " == " + record.getAcId());
//                        accountSpinnerIndex = i;
//                    }
//                }
//
//                final ArrayAdapter accountAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, accountString);
//                accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                accountSpinner.setAdapter(accountAdapter);
//                accountSpinner.setSelection(accountSpinnerIndex, false);
//            }
//        });



        fieldSpinner = (Spinner) findViewById(R.id.field_spinner);
        fieldString = Parameters.getInstance(getApplicationContext()).getFields(ParameterKeys.FIELDS).toArray(new String[0]);

        final ArrayAdapter fieldAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, fieldString);
        fieldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fieldSpinner.setAdapter(fieldAdapter);

        if( record != null )
        {
            Log.d("Record", "accountSpinnerIndex = " + accountSpinnerIndex);
            accountSpinner.setSelection(accountSpinnerIndex, false);
            fieldSpinner.setSelection(Arrays.asList(fieldString).indexOf(record.getFieldName()), false);

        }
        else
        {
            accountSpinner.setSelection(0, false);
            fieldSpinner.setSelection(0, false);
        }


        recurringSpinner = (Spinner) findViewById(R.id.expense_type_spinner);

        String[] recurringTypesString = new String[5];
        recurringTypesString[0] = getString(R.string.recurring_interval_none);
        recurringTypesString[1] = getString(R.string.recurring_interval_weekly);
        recurringTypesString[2] = getString(R.string.recurring_interval_bi_weekly);
        recurringTypesString[3] = getString(R.string.recurring_interval_monthly);
        recurringTypesString[4] = getString(R.string.recurring_interval_yearly);

        final ArrayAdapter recurringAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, recurringTypesString);
        recurringAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recurringSpinner.setAdapter(recurringAdapter);

        if( record != null )
        {
            recurringSpinner.setSelection(Arrays.asList(recurringTypesString).indexOf(record.getRecurring()), false);
        }
        else
        {
            recurringSpinner.setSelection(0, false);
        }


    }

    private String getRecurringTypeFromSpinnerSelection(int spinnerSelectedItem)
    {
        switch (spinnerSelectedItem)
        {
            case 0:
                return "NONE";
            case 1:
                return "WEEKLY";
            case 2:
                return "BI_WEEKLY";
            case 3:
                return "MONTHLY";
            case 4:
                return "YEARLY";
        }

        throw new IllegalStateException("getRecurringTypeFromSpinnerSelection unable to get value for "+spinnerSelectedItem);
    }

    private void setUpDateButton()
    {
        dateButton = (Button) findViewById(R.id.date_button);

        updateDateButtonDisplay();

        dateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatePickerDialogFragment fragment = new DatePickerDialogFragment(chosen_date, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                    {
                        Calendar cal = Calendar.getInstance();

                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, monthOfYear);
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        chosen_date = cal.getTime();
                        updateDateButtonDisplay();
                    }
                });

                fragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
    }

    private void updateDateButtonDisplay()
    {
        SimpleDateFormat formatter = new SimpleDateFormat(getResources().getString(R.string.edit_expense_date_format), Locale.getDefault());
        dateButton.setText(formatter.format(chosen_date));
    }

    private void setExpenseTypeTextViewLayout()
    {
        if( isIncome )
        {
            expenseType.setText("Income");
            expenseType.setTextColor(ContextCompat.getColor(this, R.color.budget_green));
        }
        else
        {
            expenseType.setText("Expense");
            expenseType.setTextColor(ContextCompat.getColor(this, R.color.budget_red));
        }
    }

    private boolean validateInputs()
    {
        boolean ok = true;

//        String description = descriptionEditText.getText().toString();
//        if( description.trim().isEmpty() )
//        {
//            descriptionEditText.setError(getResources().getString(R.string.no_description_error));
//            ok = false;
//        }

        String amount = amountEditText.getText().toString();
        if( amount.trim().isEmpty() )
        {
            amountEditText.setError(getResources().getString(R.string.no_amount_error));
            ok = false;
        }
        else
        {
            try
            {
                double value = Double.parseDouble(amount);
                if( value < 0 )
                {
                    amountEditText.setError(getResources().getString(R.string.negative_amount_error));
                    ok = false;
                }
            }
            catch(Exception e)
            {
                amountEditText.setError(getResources().getString(R.string.invalid_amount));
                ok = false;
            }
        }

        return ok;
    }

    // Set up Date Picker
    public void showDatePickerDialog(View view){
        DialogFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
        }
    }
}
