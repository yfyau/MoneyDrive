package com.jasonyau.moneydrive.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jasonyau.moneydrive.customClass.Field;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class Parameters
{

    private final static String SHARED_PREFERENCES_FILE_NAME = "moneydrive_sp";
    private final SharedPreferences preferences;


    private Parameters(@NonNull Context context)
    {
        preferences = context.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    }


    public void putInt(@NonNull String key, int value)
    {
        preferences.edit().putInt(key, value).apply();
    }


    public void putLong(@NonNull String key, long value)
    {
        preferences.edit().putLong(key, value).apply();
    }


    public void putString(@NonNull String key, @NonNull String value)
    {
        preferences.edit().putString(key, value).apply();
    }


    public void putBoolean(String key, boolean value)
    {
        preferences.edit().putBoolean(key, value).apply();
    }


    public int getInt(@NonNull String key, int defaultValue)
    {
        return preferences.getInt(key, defaultValue);
    }


    public long getLong(@NonNull String key, long defaultValue)
    {
        return preferences.getLong(key, defaultValue);
    }


    public boolean getBoolean(@NonNull String key, boolean defaultValue)
    {
        return preferences.getBoolean(key, defaultValue);
    }


    @Nullable
    public String getString(String key)
    {
        return preferences.getString(key, null);
    }


    public void saveFields(@NonNull String key, List<String> fields) {
        Set<String> fieldsSet = new HashSet<String>();
        fieldsSet.addAll(fields);
        preferences.edit().putStringSet(key, fieldsSet).apply();

        Log.d("Parameters", "Save Fields Finished");
    }

    @Nullable
    public List<String> getFields(@NonNull String key) {
        Set<String> fieldsSet = preferences.getStringSet(key, null);

        if (fieldsSet == null)
            return new ArrayList<>();

        List<String> fieldsList = new ArrayList<String>(fieldsSet);
        Log.d("Parameters", "Get Fields = " + fieldsList);

        return fieldsList;
    }


    private static Parameters ourInstance;

    public static synchronized Parameters getInstance(Context context)
    {
        if (ourInstance == null)
        {
            ourInstance = new Parameters(context);
        }

        return ourInstance;
    }
}
