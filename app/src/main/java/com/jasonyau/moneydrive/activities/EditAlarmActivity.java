package com.jasonyau.moneydrive.activities;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.jasonyau.moneydrive.R;
import com.jasonyau.moneydrive.customClass.AlarmReceiver;
import com.jasonyau.moneydrive.helper.AlarmHelper;
import com.jasonyau.moneydrive.helper.ParameterKeys;
import com.jasonyau.moneydrive.helper.Parameters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class EditAlarmActivity extends AppCompatActivity {

    SwitchCompat alarmSwitch;
    TextView tvTime;

    LinearLayout ll_set_time;

    ImageButton buttonAddAlaram;

    Date chosenDate;
    int hour, min;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_alarm);


        chosenDate = getIntent().getParcelableExtra("chosenDate");

        alarmSwitch = findViewById(R.id.timerSwitch);
        tvTime = findViewById(R.id.tv_reminder_time_desc);
        ll_set_time = findViewById(R.id.ll_set_time);

        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        if (chosenDate != null)
            calendar.setTime(chosenDate);   // assigns calendar to given date
        else
            calendar.setTime(new Date());
        hour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
        min = calendar.get(Calendar.MINUTE);
        calendar.get(Calendar.MONTH);       // gets month number, NOTE this is zero based!

        hour = Parameters.getInstance(EditAlarmActivity.this).getInt(ParameterKeys.ALARM_HOUR, 0);
        min = Parameters.getInstance(EditAlarmActivity.this).getInt(ParameterKeys.ALARM_MIN, 0);

        Log.d("Alarm", "Create hour = " + hour + " / min = " + min);

        tvTime.setText(getFormatedTime(hour, min));
        alarmSwitch.setChecked(Parameters.getInstance(EditAlarmActivity.this).getBoolean(ParameterKeys.ALARM_STATUS, false));

        if (!Parameters.getInstance(EditAlarmActivity.this).getBoolean(ParameterKeys.ALARM_STATUS, false))
            ll_set_time.setAlpha(0.4f);

        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Parameters.getInstance(EditAlarmActivity.this).putBoolean(ParameterKeys.ALARM_STATUS, isChecked);
                if (isChecked) {
                    Log.d("Alarm", "onCheckedChanged: true");
                    int hour = Parameters.getInstance(EditAlarmActivity.this).getInt(ParameterKeys.ALARM_HOUR, 0);
                    int min = Parameters.getInstance(EditAlarmActivity.this).getInt(ParameterKeys.ALARM_MIN, 0);
                    AlarmHelper.setReminder(EditAlarmActivity.this, AlarmReceiver.class, hour, min);
                    ll_set_time.setAlpha(1f);
                } else {
                    Log.d("Alarm", "onCheckedChanged: false");
                    AlarmHelper.cancelReminder(EditAlarmActivity.this, AlarmReceiver.class);
                    ll_set_time.setAlpha(0.4f);
                }

            }
        });



        ll_set_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Parameters.getInstance(EditAlarmActivity.this).getBoolean(ParameterKeys.ALARM_STATUS, false))
                    showTimePickerDialog(Parameters.getInstance(EditAlarmActivity.this).getInt(ParameterKeys.ALARM_HOUR, hour),
                            Parameters.getInstance(EditAlarmActivity.this).getInt(ParameterKeys.ALARM_MIN, min));
            }
        });


        buttonAddAlaram = findViewById(R.id.buttonAddAlaram);
        buttonAddAlaram.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("Alarm", "Click!!!!");
            }
        });
    }

    private void showTimePickerDialog(int h, int m) {

        TimePickerDialog builder = new TimePickerDialog(this, R.style.DialogTheme,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int tsHour, int tsMin) {

                        Parameters.getInstance(EditAlarmActivity.this).putInt(ParameterKeys.ALARM_HOUR, tsHour);
                        Parameters.getInstance(EditAlarmActivity.this).putInt(ParameterKeys.ALARM_MIN, tsMin);

                        hour = tsHour;
                        min = tsMin;
                        tvTime.setText(getFormatedTime(tsHour, tsMin));
                        AlarmHelper.setReminder(EditAlarmActivity.this, AlarmReceiver.class, tsHour, tsMin);


                    }
                }, h, m, false);

        builder.show();

    }

    public String getFormatedTime(int h, int m) {
        final String OLD_FORMAT = "HH:mm";
        final String NEW_FORMAT = "hh:mm a";

        String oldDateString = h + ":" + m;
        String newDateString = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT, getResources().getConfiguration().locale);
            Date d = sdf.parse(oldDateString);
            sdf.applyPattern(NEW_FORMAT);
            newDateString = sdf.format(d);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newDateString;
    }
}
