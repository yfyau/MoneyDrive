package com.jasonyau.moneydrive.customClass;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class Record implements Parcelable{

    private String recordId;
    private String acId;
    private String fieldName;
    private String date;
    private String recordDollar;
    private Double amount;
    private String description;
    private String lastEdit;

    private String recurring;

    public Record() { }

    public Record(String recordId, String acId, String fieldName, Date date, String recordDollar, Double amount, String description, String recurring) {

        this.recordId = recordId;
        this.acId = acId;
        this.fieldName = fieldName;
        this.date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date);
        this.recordDollar = recordDollar;
        this.amount = amount;
        this.description = description;
        this.lastEdit = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss Z ", Locale.getDefault()).format(new Date());
        this.recurring = recurring;
    }

    public Record(String recordId, String acId, String fieldName, String date, String recordDollar, Double amount, String description, String recurring) {

        this.recordId = recordId;
        this.acId = acId;
        this.fieldName = fieldName;
        this.date = date;
        this.recordDollar = recordDollar;
        this.amount = amount;
        this.description = description;
        this.lastEdit = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss Z ", Locale.getDefault()).format(new Date());
        this.recurring = recurring;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("recordId", recordId);
        result.put("acId", acId);
        result.put("fieldName", fieldName);
        result.put("date", date);
        result.put("recordDollar", recordDollar);
        result.put("amount", amount);
        result.put("description", description);
        result.put("recurring", recurring);

        return result;
    }

    public String getRecordId() {
        return recordId;
    }
    public String getAcId() {
        return acId;
    }
    public String getFieldName() {
        return fieldName;
    }
    public String getDate() {
        return date;
    }
    @Exclude
    public Date getDateDate() {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try {
            Date dDate = format.parse(date);
            return dDate;

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    };

    public String getRecordDollar() {
        return recordDollar;
    }
    public Double getAmount() {
        return amount;
    }
    public Boolean isIncome()
    {
        return amount > 0;
    }
    public String getDescription() {
        return description;
    }
    public String getLastEdit() {
        return lastEdit;
    }
    public String getRecurring() { return recurring; }
    @Exclude
    public int getReucrringIndex() {
        switch (recurring){
            case "NONE":         return 0;
            case "WEEKLY":      return 1;
            case "BI_WEEKLY":   return 2;
            case "MONTHLY":     return 3;
            case "YEARLY":      return 4;
        }

        return 0;
    }
    @Exclude
    public Boolean isRecurring()
    {
        return !recurring.equals("NONE");
    }


    @Override
    public int describeContents()
    {
        return 0;
    }

    private Record(Parcel in)
    {
        this.recordId =  in.readString();
        this.acId = in.readString();
        this.fieldName = in.readString();
        this.amount = in.readDouble();
        this.date = in.readString();
        this.description = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(recordId);
        dest.writeString(acId);
        dest.writeString(fieldName);
        dest.writeDouble(amount);
        dest.writeString(date);
        dest.writeString(description);
    }

    public static final Creator<Record> CREATOR = new Creator<Record>()
    {
        @Override
        public Record createFromParcel(Parcel in)
        {
            return new Record(in);
        }

        @Override
        public Record[] newArray(int size)
        {
            return new Record[size];
        }
    };
}
