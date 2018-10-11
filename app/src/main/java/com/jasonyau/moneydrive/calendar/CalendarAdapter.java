package com.jasonyau.moneydrive.calendar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jasonyau.moneydrive.R;
import com.jasonyau.moneydrive.customClass.Record;
import com.jasonyau.moneydrive.helper.FirebaseHelper;
import com.jasonyau.moneydrive.helper.ParameterKeys;
import com.jasonyau.moneydrive.helper.Parameters;
import com.jasonyau.moneydrive.interfaces.IFirebaseListener;
import com.roomorama.caldroid.CaldroidGridAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class CalendarAdapter extends CaldroidGridAdapter
{

    private List<String> accountIds;

// ----------------------------------->

    public CalendarAdapter(@NonNull Context context, int month, int year, Map<String, Object> caldroidData, Map<String, Object> extraData)
    {
        super(context, month, year, caldroidData, extraData);

        accountIds = new ArrayList<>();

        FirebaseHelper.getUserAcIds(Parameters.getInstance(context).getString(ParameterKeys.USER_ID), new IFirebaseListener() {
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

//        FirebaseHelper.getAccountsByUserId(Parameters.getInstance(context).getString(ParameterKeys.USER_ID), new IFirebaseListener() {
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
//                List<Account> accounts = value;
//                Log.d("Calendar", "accounts = "+ accounts);
//                for (Account account : accounts){
//                    Log.d("Calendar", "AccountID = " + account.getAcId());
//                    accountIds.add(account.getAcId());
//                }
//            }
//        });
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
    }

// ----------------------------------->

    @Override
    public View getView(int position, View convertView, final ViewGroup parent)
    {
        final View cellView = convertView == null ? createView(parent) : convertView;

        ViewData viewData = (ViewData) cellView.getTag();

        // Get dateTime of this cell
        DateTime dateTime = this.datetimeList.get(position);
        boolean isToday = dateTime.equals(getToday());
        boolean isDisabled = (minDateTime != null && dateTime.lt(minDateTime)) || (maxDateTime != null && dateTime.gt(maxDateTime)) || (disableDates != null && disableDatesMap.containsKey(dateTime));
        final boolean isOutOfMonth = dateTime.getMonth() != month;

        final TextView tv1 = viewData.dayTextView;

        // Set today's date
        tv1.setText("" + dateTime.getDay());

        // Customize for disabled dates and date outside min/max dates
        if ( isDisabled )
        {
            if( !viewData.isDisabled )
            {
                tv1.setTextColor(ContextCompat.getColor(context, R.color.calendar_cell_disabled_text_color));
                cellView.setBackgroundResource(android.R.color.white);

                viewData.isDisabled = true;
            }
        }
        else if( viewData.isDisabled ) // Reset all view params
        {
            tv1.setTextColor(ContextCompat.getColor(context, R.color.primary_text));
            cellView.setBackgroundResource(R.drawable.custom_grid_cell_drawable);

            viewData.isDisabled = false;
            viewData.isSelected = false;
            viewData.isToday = false;
            viewData.containsExpenses = false;
            viewData.colorIndicatorMarginForToday = false;
            viewData.isOutOfMonth = false;
        }

        if( !isDisabled )
        {
            if( isOutOfMonth )
            {
                if( !viewData.isOutOfMonth )
                {
                    tv1.setTextColor(ContextCompat.getColor(context, R.color.divider));

                    viewData.isOutOfMonth = true;
                }
            }
            else if( viewData.isOutOfMonth )
            {
                tv1.setTextColor(ContextCompat.getColor(context, R.color.primary_text));

                viewData.isOutOfMonth = false;
            }

            // Today's cell
            if( isToday )
            {
                // Customize for selected dates
                if (selectedDates != null && selectedDatesMap.containsKey(dateTime))
                {
                    if( !viewData.isToday || !viewData.isSelected )
                    {
                        cellView.setBackgroundResource(R.drawable.custom_grid_today_cell_selected_drawable);

                        viewData.isToday = true;
                        viewData.isSelected = true;
                    }
                }
                else if( !viewData.isToday || viewData.isSelected )
                {
                    cellView.setBackgroundResource(R.drawable.custom_grid_today_cell_drawable);

                    viewData.isToday = true;
                    viewData.isSelected = false;
                }
            }
            else
            {
                // Customize for selected dates
                if (selectedDates != null && selectedDatesMap.containsKey(dateTime))
                {
                    if( viewData.isToday || !viewData.isSelected )
                    {
                        cellView.setBackgroundResource(R.drawable.custom_grid_cell_selected_drawable);

                        viewData.isToday = false;
                        viewData.isSelected = true;
                    }
                }
                else if( viewData.isToday || viewData.isSelected )
                {
                    cellView.setBackgroundResource(R.drawable.custom_grid_cell_drawable);

                    viewData.isToday = false;
                    viewData.isSelected = false;
                }
            }

            final Date date = new Date(dateTime.getMilliseconds(TimeZone.getDefault()));

            FirebaseHelper.getRecordByDate(date, new IFirebaseListener() {
                @Override
                public void onCallback(String value) {

                }

                @Override
                public void onCallback(Double value) {

                }

                @Override
                public void onCallback(List value) {
                    List<Record> rValues = value;
                    double daily_balance = 0;
                    for (final Record record : rValues){
                        if (accountIds.contains(record.getAcId())) {
                            daily_balance += record.getAmount();
                        }
                    }

                    if ( daily_balance < 0 )
                    {
                        tv1.setTextColor(ContextCompat.getColor(context, isOutOfMonth ? R.color.budget_red_out : R.color.budget_red));
                    }
                    else if ( daily_balance > 0 )
                    {
                        tv1.setTextColor(ContextCompat.getColor(context, isOutOfMonth ? R.color.budget_green_out : R.color.budget_green));
                    }

                    else if ( daily_balance == 0 )
                        tv1.setTextColor(ContextCompat.getColor(context, isOutOfMonth ? R.color.grey_transparent : R.color.primary_text));
                }
            });
        }

        cellView.setTag(viewData);
        return cellView;
    }

    /**
     * Inflate a new cell view and attach ViewData as tag
     *
     * @param parent
     * @return
     */


    private View createView(ViewGroup parent)
    {
        View v = LayoutInflater.from(context).inflate(R.layout.custom_grid_cell, parent, false);
        ViewData viewData = new ViewData();

        viewData.dayTextView = (TextView) v.findViewById(R.id.grid_cell_tv1);

        v.setTag(viewData);

        return v;
    }

// --------------------------------------->

    /**
     * Object that represent data of a cell for optimization purpose
     */
    public static class ViewData
    {
        /**
         * TextView that contains the day
         */
        public TextView dayTextView;

        /**
         * Is this cell a disabled date
         */
        public boolean isDisabled                   = false;
        /**
         * Is this cell out of the current month
         */
        public boolean isOutOfMonth                 = false;
        /**
         * Is this cell today's cell
         */
        public boolean isToday                      = false;
        /**
         * Is this cell selected
         */
        public boolean isSelected                   = false;
        /**
         * Does this cell contain expenses
         */
        public boolean containsExpenses             = false;
        /**
         * Are color indicator margin set for today
         */
        public boolean colorIndicatorMarginForToday = false;
    }
}
