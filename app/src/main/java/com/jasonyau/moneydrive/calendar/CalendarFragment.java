package com.jasonyau.moneydrive.calendar;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;

import java.util.Date;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class CalendarFragment extends CaldroidFragment
{
    private Date selectedDate;

// --------------------------------------->

    @Override
    public CaldroidGridAdapter getNewDatesGridAdapter(int month, int year)
    {
        return new CalendarAdapter(getActivity(), month, year, getCaldroidData(), extraData);
    }

    @Override
    public void setSelectedDates(Date fromDate, Date toDate)
    {
        this.selectedDate = fromDate;
        super.setSelectedDates(fromDate, toDate);
        try
        {
            super.moveToDate(fromDate);
        }
        catch (Exception ignored){} // Exception that occurs if we call this code before the calendar being initialized
    }

    public Date getSelectedDate()
    {
        return selectedDate;
    }
}
