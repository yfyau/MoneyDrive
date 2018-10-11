package com.jasonyau.moneydrive.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

// Calendar
import com.google.firebase.database.FirebaseDatabase;
import com.jasonyau.moneydrive.R;
import com.jasonyau.moneydrive.adapter.RecordRecyclerViewAdapter;
import com.jasonyau.moneydrive.calendar.CalendarFragment;


// Botton Bar
import com.jasonyau.moneydrive.customClass.Record;
import com.jasonyau.moneydrive.helper.CurrencyHelper;
import com.jasonyau.moneydrive.helper.FirebaseHelper;
import com.jasonyau.moneydrive.helper.ParameterKeys;
import com.jasonyau.moneydrive.helper.Parameters;
import com.jasonyau.moneydrive.interfaces.IFirebaseListener;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class CalendarActivity extends AppCompatActivity {

    private String TAG = "CalendarActivity";

    private View budgetLineContainer;

    private TextView budgetLine;
    private TextView budgetLineAmount;

    private RecyclerView recyclerView;

    private CalendarFragment calendarFragment;

    private RecordRecyclerViewAdapter recordsViewAdapter;
    private View recyclerViewPlaceholder;

    private FragmentManager fragmentManager;

    private Date lastStopDate;
    private Date chosenDate;

    private List<Record> records;
    private List<String> accountIds;
    private double daily_balanece;

    public static final int ADD_EXPENSE_ACTIVITY_CODE = 101;

    public final static String CENTER_X_KEY = "centerX";
    public final static String CENTER_Y_KEY = "centerY";
    public final static String ANIMATE_TRANSITION_KEY = "animate";

    private static final String RECYCLE_VIEW_SAVED_DATE = "recycleViewSavedDate";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        budgetLine = findViewById(R.id.budgetLine);
        budgetLineAmount = findViewById(R.id.budgetLineAmount);
        budgetLineContainer = findViewById(R.id.budgetLineContainer);
//        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        recyclerViewPlaceholder = findViewById(R.id.emptyRecordsRecyclerViewPlaceholder);

        // Offline mode on
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        initData(savedInstanceState);
        initCalendarFragment(savedInstanceState);
        initRecyclerView(savedInstanceState);
        initFireBase();

        // Set up Bottom Bar
        BottomBar bottomBar = findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                switch (tabId) {
                case R.id.tab_calendar:
                    fragmentManager.popBackStack();
                    initCalendarFragment(savedInstanceState);
                    initFireBase();
                    break;
                case R.id.tab_summary:
                    if(fragmentManager.getBackStackEntryCount() > 0)
                        fragmentManager.popBackStack();

                    Bundle bundle = new Bundle();
                    ArrayList<String> alaccountIds = new ArrayList<>(accountIds);
                    bundle.putStringArrayList("accountIds", alaccountIds);

                    SummaryFragment summaryFragment = new SummaryFragment();
                    summaryFragment.setArguments(bundle);

                    transaction.replace(R.id.fragment_container, summaryFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    break;
                case R.id.tab_setting:
                    if(fragmentManager.getBackStackEntryCount() > 0)
                        fragmentManager.popBackStack();
                    transaction.replace(R.id.fragment_container, new SettingFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                    break;
            }
            }
        });

        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
//                Toast.makeText(getApplicationContext(), TabSwitch.get(tabId, true), Toast.LENGTH_LONG).show();
            }
        });

        // Set up alarm button
        ImageButton alarmButton = findViewById(R.id.alarmButton);
        alarmButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CalendarActivity.this, EditAlarmActivity.class);
                intent.putExtra("chosen_date", calendarFragment.getSelectedDate().getTime());
                startActivity(intent);
            }
        });

        // Set up add button
        ImageButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (Parameters.getInstance(CalendarActivity.this).getString(ParameterKeys.USER_ID) != null && accountIds.size() != 0) {
                    Intent intent = new Intent(CalendarActivity.this, EditRecordActivity.class);
                    intent.putExtra("chosen_date", calendarFragment.getSelectedDate().getTime());
                    startActivity(intent);
                }
            }
        });


        recordsViewAdapter = new RecordRecyclerViewAdapter(CalendarActivity.this, chosenDate, records);
        recyclerView.setAdapter(recordsViewAdapter);

        refreshAllForDate(chosenDate);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // If the last stop happened yesterday (or another day), set and refresh to the current date
        if( lastStopDate != null )
        {
            Calendar cal = Calendar.getInstance();
            int currentDay = cal.get(Calendar.DAY_OF_YEAR);

            cal.setTime(lastStopDate);
            int lastStopDay = cal.get(Calendar.DAY_OF_YEAR);

            if( currentDay != lastStopDay )
            {
                refreshAllForDate(new Date());
            }

            lastStopDate = null;
        }
    }

    private void initData (Bundle savedInstanceState){
        long initDate = Parameters.getInstance(getApplicationContext()).getLong(ParameterKeys.INIT_DATE, 0);
        if( initDate <= 0 )
        {
            Parameters.getInstance(getApplicationContext()).putLong(ParameterKeys.INIT_DATE, new Date().getTime());
            CurrencyHelper.setUserCurrency(this, Currency.getInstance(Locale.getDefault())); // Set a default currency before onboarding

            List<String> defaultFields = new ArrayList<>();
            defaultFields.add("FOOD");
            defaultFields.add("ENTERTAINMENT");
            defaultFields.add("TRANSPORTATION");
            Parameters.getInstance(getApplicationContext()).saveFields(ParameterKeys.FIELDS, defaultFields);
        }

        records = new ArrayList<>();
        accountIds = new ArrayList<>();
    }

    private void initFireBase(){

        // Get Accounts By User for afterward usage

        Log.d("123", "USERID = " + Parameters.getInstance(getApplicationContext()).getString(ParameterKeys.USER_ID));
        FirebaseHelper.getUserAcIds(Parameters.getInstance(getApplicationContext()).getString(ParameterKeys.USER_ID), new IFirebaseListener() {
            @Override
            public void onCallback(String value) {

            }

            @Override
            public void onCallback(Double value) {

            }

            @Override
            public void onCallback(List value) {
                if (value != null)
                    accountIds = value;
            }
        });


        FirebaseHelper.getRecordByDate(chosenDate, new IFirebaseListener() {
            @Override
            public void onCallback(String value) {

            }

            @Override
            public void onCallback(Double value) {

            }

            @Override
            public void onCallback(List value) {
                records.clear();
                daily_balanece = 0;

                if (value != null && accountIds != null) {
                    List<Record> rValues = value;
                    for (final Record record : rValues) {

                        Log.d("Account", "AccountIDs = " + accountIds);

                        if (accountIds.contains(record.getAcId())) {
                            records.add(record);
                            daily_balanece += record.getAmount();
                        }
                    }
                }

                recordsViewAdapter = new RecordRecyclerViewAdapter(CalendarActivity.this, chosenDate, records);
                recyclerView.setAdapter(recordsViewAdapter);

                refreshAllForDate(chosenDate);

                Log.d("Call Back", "Call Back!!!");
            }
        });


//        FirebaseHelper.getRecordByAccount("123456", new IFirebaseListener() {
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
//                List<Record> rValues = value;
//                for (Record record : rValues){
//                    Log.d("Call Back", "Record Date = " + record.getDateDate());
//                    Log.d("Call Back", String.format("Chosen Date = " + chosenDate));
//
//                    /*--------------------- Need to Fix => records = ac1_records + ac2_records + ac3_records => filter then replace part of records ------------------*/
//                    if (record.getDateDate().compareTo(chosenDate) == 0) {
//                        Log.d("Call Back", "True");
//                        records.add(record);
//                        daily_balanece += record.getAmount();
//                    }
//                }
//
//
//                recordsViewAdapter = new RecordRecyclerViewAdapter(CalendarActivity.this, chosenDate, records);
//                recyclerView.setAdapter(recordsViewAdapter);
//
//                refreshAllForDate(chosenDate);
//
//                Log.d("Call Back", "Call Back!!!");
//            }
//        });

//
//        DatabaseReference recordRef = FirebaseDatabase.getInstance().getReference().child("record");
//        recordRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                records.clear();
//
//                if (chosenDate == null) chosenDate = new Date();
//
//                for (DataSnapshot ds: dataSnapshot.getChildren()) {
//                    String sDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(chosenDate);
//                    String fDate = (String) ds.child("date").getValue();
//                    if (sDate.equals(fDate)){
//                        records.add(FirebaseHelper.FirebaseToRecord(ds));
//                        daily_balanece += records.get(records.size()-1).getAmount();
//                    }
//                    Log.d("Get Record", "Records = " + records);
//                }
//
//                recordsViewAdapter = new RecordRecyclerViewAdapter(CalendarActivity.this, chosenDate, records);
//                recyclerView.setAdapter(recordsViewAdapter);
//
//                refreshAllForDate(chosenDate);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                Log.w(TAG, "Failed to read value.", error.toException());
//            }
//        });

    }

    private void initCalendarFragment(Bundle savedInstanceState){

        final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");

        calendarFragment = new CalendarFragment();

        // If Activity is created after rotation
        if (savedInstanceState != null) {
            calendarFragment.restoreStatesFromKey(savedInstanceState, "CALDROID_SAVED_STATE");
            Date selectedDate = (Date) savedInstanceState.getSerializable(RECYCLE_VIEW_SAVED_DATE);
            calendarFragment.setSelectedDates(selectedDate, selectedDate);
            lastStopDate = selectedDate;
        }
        // If activity is created from fresh
        else {
            Bundle args = new Bundle();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            chosenDate = cal.getTime();

            args.putInt(CalendarFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CalendarFragment.YEAR, cal.get(Calendar.YEAR));
            args.putBoolean(CalendarFragment.ENABLE_SWIPE, true);
            args.putBoolean(CalendarFragment.SIX_WEEKS_IN_CALENDAR, true);
            args.putBoolean(CalendarFragment.ENABLE_CLICK_ON_DISABLED_DATES, false);
            args.putInt(CaldroidFragment.THEME_RESOURCE, R.style.caldroid_style);

            calendarFragment.setArguments(args);
            calendarFragment.setSelectedDates(new Date(), new Date());

        }

        setCustomResourceForDates();

        // Attach to the activity
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendarView, calendarFragment);
        t.commit();

        // Setup listener
        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {

                chosenDate = date;

                FirebaseHelper.getRecordByDate(chosenDate, new IFirebaseListener() {
                    @Override
                    public void onCallback(String value) {

                    }

                    @Override
                    public void onCallback(Double value) {

                    }

                    @Override
                    public void onCallback(List value) {
                        records.clear();
                        daily_balanece = 0;
                        List<Record> rValues = value;
                        for (final Record record : rValues){
                            if (accountIds.contains(record.getAcId())) {
                                records.add(record);
                                daily_balanece += record.getAmount();
                            }
                        }

                        recordsViewAdapter = new RecordRecyclerViewAdapter(CalendarActivity.this, chosenDate, records);
                        recyclerView.setAdapter(recordsViewAdapter);

                        refreshAllForDate(chosenDate);

                        Log.d("123", "AccountIds = " + accountIds);
                        Log.d("Call Back", "Call Back!!!");
                    }
                });




//                FirebaseHelper.getRecordByAccount("123456", new IFirebaseListener() {
//                    @Override
//                    public void onCallback(String value) {
//
//                    }
//
//                    @Override
//                    public void onCallback(Double value) {
//
//                    }
//
//                    @Override
//                    public void onCallback(List value) {
//                        List<Record> rValues = value;
//                        for (Record record : rValues){
//                            if (record.getDateDate().compareTo(chosenDate) == 0) {
//                                records.add(record);
//                                daily_balanece += record.getAmount();
//                            }
//                        }
//
//                        recordsViewAdapter = new RecordRecyclerViewAdapter(CalendarActivity.this, chosenDate, records);
//                        recyclerView.setAdapter(recordsViewAdapter);
//
//                        refreshAllForDate(chosenDate);
//                    }
//                });

            }

            @Override
            public void onChangeMonth(int month, int year) {

            }

            @Override
            public void onLongClickDate(Date date, View view) {

            }

            @Override
            public void onCaldroidViewCreated() {

            }
        };
        calendarFragment.setCaldroidListener(listener);
    }

    private void setCustomResourceForDates() {
        Calendar cal = Calendar.getInstance();

        // Min date is last 7 days
        cal.add(Calendar.DATE, -7);
        Date blueDate = cal.getTime();

        // Max date is next 7 days
        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 7);
        Date greenDate = cal.getTime();

        if (calendarFragment != null) {
            ColorDrawable blue = new ColorDrawable(getResources().getColor(R.color.blue));
            ColorDrawable green = new ColorDrawable(Color.GREEN);
            calendarFragment.setBackgroundDrawableForDate(blue, blueDate);
            calendarFragment.setBackgroundDrawableForDate(green, greenDate);
            calendarFragment.setTextColorForDate(R.color.white, blueDate);
            calendarFragment.setTextColorForDate(R.color.white, greenDate);
        }
    }

    private void initRecyclerView(Bundle savedInstanceState)
    {
        recyclerView = findViewById(R.id.recordRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Date date = new Date();
        if( savedInstanceState != null && savedInstanceState.containsKey(RECYCLE_VIEW_SAVED_DATE) )
        {
            Date savedDate = (Date) savedInstanceState.getSerializable(RECYCLE_VIEW_SAVED_DATE);
            if ( savedDate != null )
            {
                date = savedDate;
            }
        }

        recordsViewAdapter = new RecordRecyclerViewAdapter(this, date, records);
        recyclerView.setAdapter(recordsViewAdapter);

        refreshRecyclerViewForDate(date);
        updateBalanceDisplayForDay(date);
    }

    private void refreshRecyclerViewForDate(@NonNull Date date)
    {
        recordsViewAdapter.setDate(date, records);

        if(records.size() > 0)
        {
            recyclerView.setVisibility(View.VISIBLE);
            recyclerViewPlaceholder.setVisibility(View.GONE);
        }
        else
        {
            recyclerView.setVisibility(View.GONE);
            recyclerViewPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private void refreshAllForDate(@NonNull Date date)
    {
        refreshRecyclerViewForDate(date);
        updateBalanceDisplayForDay(date);
        calendarFragment.setSelectedDates(date, date);
        calendarFragment.refreshView();
    }

    private void updateBalanceDisplayForDay(@NonNull Date day)
    {
        SimpleDateFormat format = new SimpleDateFormat(getResources().getString(R.string.account_balance_date_format), Locale.getDefault());

        String formatted = getResources().getString(R.string.account_balance_format, format.format(day));
        if( formatted.endsWith(".:") )
        {
            formatted = formatted.substring(0, formatted.length() - 2) + ":"; // Remove . at the end of the month (ex: nov.: -> nov:)
        }
        else if( formatted.endsWith(". :") )
        {
            formatted = formatted.substring(0, formatted.length() - 3) + " :"; // Remove . at the end of the month (ex: nov. : -> nov :)
        }

        budgetLine.setText(formatted);
        budgetLineAmount.setText(CurrencyHelper.getFormattedCurrencyString(this, daily_balanece));

        if( daily_balanece < 0 )
        {
            budgetLineContainer.setBackgroundResource(R.color.budget_red);
        }
        else if( daily_balanece < Parameters.getInstance(getApplicationContext()).getInt(ParameterKeys.LOW_MONEY_WARNING_AMOUNT, ParameterKeys.DEFAULT_LOW_MONEY_WARNING_AMOUNT) )
        {
            budgetLineContainer.setBackgroundResource(R.color.budget_orange);
        }
        else
        {
            budgetLineContainer.setBackgroundResource(R.color.budget_green);
        }
    }

    public static class RecordHolder extends RecyclerView.ViewHolder{
        private final TextView textViewDate;
        private final TextView textViewItem;

        public RecordHolder(View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(android.R.id.text1);
            textViewItem = itemView.findViewById(android.R.id.text2);
        }

        public void setValues(Record record){
            String date = new SimpleDateFormat("dd-MM-yyyy").format(record.getDate());
            textViewDate.setText(date);
            textViewItem.setText(record.getDescription());
        }
    }

    @Override
    public void onBackPressed() {

    }
}

