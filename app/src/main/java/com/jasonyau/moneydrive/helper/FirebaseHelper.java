package com.jasonyau.moneydrive.helper;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jasonyau.moneydrive.activities.CalendarActivity;
import com.jasonyau.moneydrive.adapter.RecordRecyclerViewAdapter;
import com.jasonyau.moneydrive.customClass.Account;
import com.jasonyau.moneydrive.customClass.Record;
import com.jasonyau.moneydrive.customClass.User;
import com.jasonyau.moneydrive.interfaces.IFirebaseListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class FirebaseHelper {

    public static boolean addUser(String userId, String password){
       if(userId != null && !userId.isEmpty() && password != null && !password.isEmpty()) {

            DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference("user");
            User user = new User(userId, password);
            firebaseRef.child(userId).setValue(user);

            return true;
        }

        return false;
    }

    public static boolean editUserWithAccount(String userId, List<String> accountIds) {

        if(userId != null && !userId.isEmpty()) {

            DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference("user");
            firebaseRef.child(userId).child("accountIds").setValue(accountIds);

            return true;
        }

        return false;
    }

    public static void getUserAcIds (final String userId, final IFirebaseListener iFirebaseListener){
        if(userId != null && !userId.isEmpty()) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user");

            userRef.orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<String> accountIds = new ArrayList<>();
                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        Log.d("Account", "AccountId DS = " + ds.child("accountIds").getValue());
                        accountIds = (List<String>) ds.child("accountIds").getValue();
                    }
                    iFirebaseListener.onCallback(accountIds);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("Total", "Failed to read value.", error.toException());
                }
            });
        }
    }

    public static void loginUser (final String userId, final String password, final IFirebaseListener iFirebaseListener){
        if(userId != null && !userId.isEmpty() && password != null && !password.isEmpty()) {

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user");

            Log.d("User", "User Checking1");

            userRef.orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean found = false;
                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        found = true;
                        if (password.equals(ds.child("password").getValue()))
                            iFirebaseListener.onCallback("Log in Successful !");
                        else
                            iFirebaseListener.onCallback("Incorrect Password !");
                    }

                    if (!found)
                        iFirebaseListener.onCallback("User Unfounded");
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("Total", "Failed to read value.", error.toException());
                }
            });
        }
    }

    public static Account addAccount(String userId, String acName, String acType, String acDollar) {

        if(userId != null && !userId.isEmpty()) {
            DatabaseReference accountRef = FirebaseDatabase.getInstance().getReference("account");
            String acId = accountRef.push().getKey();

            Account account = new Account(acId, userId, acName, acType, "HKD");
            accountRef.child(acId).setValue(account);

            return account;
        }

        return null;
    }

    public static boolean editAccount(String acId, String userId, String acName, String acType, String acDollar) {

        if(acId != null && !acId.isEmpty()) {
            DatabaseReference accountRef = FirebaseDatabase.getInstance().getReference("account");

            Account account = new Account(acId, userId, acName, acType, "HKD");
            accountRef.child(acId).setValue(account);

            return true;
        }

        return false;
    }

    public static boolean delAccount(String acId) {

        if(acId != null && !acId.isEmpty()) {
            DatabaseReference accountRef = FirebaseDatabase.getInstance().getReference("account");

            accountRef.child(acId).setValue(null);


            return true;
        }

        return false;
    }

    public static void getAccountsByUserId(final String userId, final IFirebaseListener iFirebaseListener){
        if(userId != null && !userId.isEmpty()) {

            Log.d("Account", "Get Account / UserId = " + userId);

            DatabaseReference accountRef = FirebaseDatabase.getInstance().getReference("account");

            accountRef.orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Account> accounts = new ArrayList<>();
                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        accounts.add(FirebaseHelper.FirebaseToAccount(ds));
                    }
                    iFirebaseListener.onCallback(accounts);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("Total", "Failed to read value.", error.toException());
                }
            });
        }
    }

    public static void getAccountsByAcId(final String acId, final IFirebaseListener iFirebaseListener){
        if(acId != null && !acId.isEmpty()) {

            Log.d("Account", "Get Account / AcId = " + acId);

            DatabaseReference accountRef = FirebaseDatabase.getInstance().getReference("account");

            accountRef.orderByChild("acId").equalTo(acId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Account> accounts = new ArrayList<>();
                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        accounts.add(FirebaseHelper.FirebaseToAccount(ds));
                    }
                    iFirebaseListener.onCallback(accounts);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("Total", "Failed to read value.", error.toException());
                }
            });
        }
    }

    public static boolean addRecord(String acId, String fieldName, Date date, boolean isIncome, double amount, String description, String recurring) {

        if(amount >= 0) {
            DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference("record");
            String recordId = firebaseRef.push().getKey();

            if (!isIncome)
                amount = -amount;

            Record record = new Record(recordId, acId, fieldName, date, "HKD", amount, description, "NONE");
            firebaseRef.child(recordId).setValue(record);

            return true;
        }

        return false;
    }

    public static boolean editRecord(String recordId, String acId, String fieldName, Date date, double amount, String description, boolean isIncome) {

        if(amount >= 0) {
            DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference("record");

            if (!isIncome)
                amount = -amount;

            Record record = new Record(recordId, acId, fieldName, date, "HKD", amount, description, "NONE");
            firebaseRef.child(recordId).setValue(record);

            return true;
        }

        return false;
    }

    public static boolean delRecord(String recordId) {

        DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference("record");

        firebaseRef.child(recordId).setValue(null);

        return true;
    }

    public static void getRecordByAccount(String acId, final IFirebaseListener iFirebaseListener){
        DatabaseReference recordRef = FirebaseDatabase.getInstance().getReference().child("record");

        recordRef.orderByChild("acId").equalTo(acId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Record> records = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    records.add(FirebaseHelper.FirebaseToRecord(ds));
                }
                iFirebaseListener.onCallback(records);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("Total", "Failed to read value.", error.toException());
            }
        });
    }

    public static void getRecordByDate(Date date, final IFirebaseListener iFirebaseListener){
        DatabaseReference recordRef = FirebaseDatabase.getInstance().getReference().child("record");

        String sdate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date);

        recordRef.orderByChild("date").equalTo(sdate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Record> records = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    records.add(FirebaseHelper.FirebaseToRecord(ds));
                }
                iFirebaseListener.onCallback(records);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("Total", "Failed to read value.", error.toException());
            }
        });
    }

    public static void getRecordByDateRange(Date date_from, Date date_to, final IFirebaseListener iFirebaseListener){
        DatabaseReference recordRef = FirebaseDatabase.getInstance().getReference().child("record");

        String sdate_from = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date_from);
        String sdate_to = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date_to);

        recordRef.orderByChild("date").startAt(sdate_from).endAt(sdate_to).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Record> records = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    records.add(FirebaseHelper.FirebaseToRecord(ds));
                }
                iFirebaseListener.onCallback(records);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("Total", "Failed to read value.", error.toException());
            }
        });
    }

    protected Double getFieldTotalByDate (final Date fromDate, final Date toDate, final String field){

        final DateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        String sfromDate = df.format(fromDate);
        String stoDate = df.format(toDate);

        DatabaseReference recordRef = FirebaseDatabase.getInstance().getReference().child("record");
        recordRef.orderByChild("date").startAt(sfromDate).endAt(stoDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                double total = 0;

                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    String fDate = (String) ds.child("date").getValue();
                    try {
                        Date dfDate = df.parse(fDate);

                        if (dfDate.compareTo(fromDate) < 0 || toDate.compareTo(dfDate) < 0){
                            // Out of range Data -- Do nothing
                        }
                        else{
                            // In range=
                            if (field == null || field == FirebaseHelper.FirebaseToRecord(ds).getFieldName())
                                total += FirebaseHelper.FirebaseToRecord(ds).getAmount();
                        }

                        Log.d("Total", "Total = " + total);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("Total", "Failed to read value.", error.toException());
            }
        });

        return 0.0;
    }

    public static Account FirebaseToAccount(DataSnapshot dataSnapshot){

        String acId = (String) dataSnapshot.child("acId").getValue();
        String userId = (String) dataSnapshot.child("userId").getValue();
        String acName = (String) dataSnapshot.child("acName").getValue();
        String acType = (String) dataSnapshot.child("acType").getValue();
        String acDollar = (String) dataSnapshot.child("acDollar").getValue();
        Long acAssetl = (Long) dataSnapshot.child("acAsset").getValue();
        Double acAsset = acAssetl.doubleValue();

        Account account = new Account(acId, userId, acName, acType,  acDollar, acAsset);

        Log.d("Account", "Account1 = " + account);

        return account;
    }

    public static Record FirebaseToRecord(DataSnapshot dataSnapshot){

        String recordId = (String) dataSnapshot.child("recordId").getValue();
        String acId = (String) dataSnapshot.child("acId").getValue();
        String fieldName = (String) dataSnapshot.child("fieldName").getValue();
        String date = (String) dataSnapshot.child("date").getValue();
        String recordDollar = (String) dataSnapshot.child("recordDollar").getValue();
        Long amountl = (Long) dataSnapshot.child("amount").getValue();
        Double amount = amountl.doubleValue();
        String description = (String) dataSnapshot.child("description").getValue();
        String recurring = (String) dataSnapshot.child("recurring").getValue();

        Record record = new Record( recordId, acId, fieldName, date, recordDollar, amount, description, recurring);

        return record;
    }
}
