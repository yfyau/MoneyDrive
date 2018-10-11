package com.jasonyau.moneydrive.customClass;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.jasonyau.moneydrive.activities.EditAlarmActivity;
import com.jasonyau.moneydrive.helper.AlarmHelper;
import com.jasonyau.moneydrive.helper.ParameterKeys;
import com.jasonyau.moneydrive.helper.Parameters;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class AlarmReceiver extends BroadcastReceiver {

    String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() != null && context != null) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                int hour = Parameters.getInstance(context).getInt(ParameterKeys.ALARM_HOUR, 0);
                int min = Parameters.getInstance(context).getInt(ParameterKeys.ALARM_MIN, 0);

                AlarmHelper.setReminder(context, AlarmReceiver.class, hour, min);
                return;
            }
        }

        Log.d(TAG, "onReceive: ");

        //Trigger the notification
        AlarmHelper.showNotification(context, EditAlarmActivity.class,"Money Drive", "Remember to input your records !");

    }
}