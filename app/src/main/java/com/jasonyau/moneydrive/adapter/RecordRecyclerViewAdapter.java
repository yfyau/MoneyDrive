package com.jasonyau.moneydrive.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jasonyau.moneydrive.R;
import com.jasonyau.moneydrive.activities.CalendarActivity;
import com.jasonyau.moneydrive.activities.EditRecordActivity;
import com.jasonyau.moneydrive.customClass.Record;
import com.jasonyau.moneydrive.helper.CurrencyHelper;
import com.jasonyau.moneydrive.helper.FirebaseHelper;

import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class RecordRecyclerViewAdapter extends RecyclerView.Adapter<RecordRecyclerViewAdapter.ViewHolder>
{
    private List<Record> records;
    private Date date;
    private Activity activity;


    public RecordRecyclerViewAdapter(@NonNull Activity activity,  @NonNull Date date, @NonNull List<Record> records)
    {
        this.activity = activity;
        this.date = date;
        this.records = records;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(@NonNull Date date, @NonNull List<Record> records)
    {
        this.date = date;
        this.records = records;

        Log.d("Get Record", "Record Size = " + records.size());

        notifyDataSetChanged();
    }

    public int removeRecord(Record expense)
    {
        Iterator<Record> recordIterator = records.iterator();
        int position = 0;
        while( recordIterator.hasNext() )
        {
            Record shownExpense = recordIterator.next();
            if( shownExpense.getRecordId().equals(expense.getRecordId()) )
            {
                recordIterator.remove();
                notifyItemRemoved(position);
                return position;
            }

            position++;
        }

        return -1;
    }

    public void addRecord(Record record, int position)
    {
        records.add(position, record);
        notifyItemRangeInserted(position, 1);
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycleview_record_cell, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i)
    {
        final Record record = records.get(i);

        viewHolder.expenseTitleTextView.setText(record.getFieldName());
        viewHolder.expenseAmountTextView.setText(CurrencyHelper.getFormattedCurrencyString(viewHolder.view.getContext(), record.getAmount()));
        viewHolder.expenseAmountTextView.setTextColor(ContextCompat.getColor(viewHolder.view.getContext(), record.isIncome() ? R.color.budget_green : R.color.budget_red));
        viewHolder.recurringIndicator.setVisibility(record.isRecurring() ? View.VISIBLE : View.GONE);
        viewHolder.positiveIndicator.setImageResource(record.isIncome() ? R.drawable.ic_label_green : R.drawable.ic_label_red);

//        if( record.isRecurring() )
//        {
//            assert record.getAssociatedRecurringExpense() != null;
//            switch (record.getAssociatedRecurringExpense().getType())
//            {
//                case WEEKLY:
//                    viewHolder.recurringIndicatorTextview.setText(viewHolder.view.getContext().getString(R.string.weekly));
//                    break;
//                case BI_WEEKLY:
//                    viewHolder.recurringIndicatorTextview.setText(viewHolder.view.getContext().getString(R.string.bi_weekly));
//                    break;
//                case MONTHLY:
//                    viewHolder.recurringIndicatorTextview.setText(viewHolder.view.getContext().getString(R.string.monthly));
//                    break;
//                case YEARLY:
//                    viewHolder.recurringIndicatorTextview.setText(viewHolder.view.getContext().getString(R.string.yearly));
//                    break;
//            }
//        }

        final View.OnClickListener onClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (record.isRecurring())
                {

                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(record.isIncome() ? R.string.dialog_edit_income_title : R.string.dialog_edit_expense_title);
                    builder.setItems(record.isIncome() ? R.array.dialog_edit_income_choices : R.array.dialog_edit_expense_choices, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            switch (which)
                            {
                                case 0: // Edit expense
                                {
                                    Intent startIntent = new Intent(viewHolder.view.getContext(), EditRecordActivity.class);
                                    startIntent.putExtra("chosen_date", record.getDateDate().getTime());
                                    startIntent.putExtra("record", record);

//                                    Intent intent = new Intent(CalendarActivity.this, EditRecordActivity.class);
//                                    intent.putExtra("chosen_date", calendarFragment.getSelectedDate().getTime());
//                                    startActivity(intent);

                                    ActivityCompat.startActivityForResult(activity, startIntent, CalendarActivity.ADD_EXPENSE_ACTIVITY_CODE, null);

                                    break;
                                }
                                case 1: // Delete
                                {
                                    // Send notification to inform views that this expense has been deleted
                                    FirebaseHelper.delRecord(record.getRecordId());

                                    break;
                                }
                            }
                        }
                    });
                    builder.show();
                }

            }
        };

        viewHolder.view.setOnClickListener(onClickListener);

        viewHolder.view.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                onClickListener.onClick(v);
                return true;
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return records.size();
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public final TextView expenseTitleTextView;
        public final TextView expenseAmountTextView;
        public final ViewGroup recurringIndicator;
        public final TextView recurringIndicatorTextview;
        public final ImageView positiveIndicator;
        public final View view;

        public ViewHolder(View v)
        {
            super(v);

            view = v;
            expenseTitleTextView = (TextView) v.findViewById(R.id.expense_title);
            expenseAmountTextView = (TextView) v.findViewById(R.id.expense_amount);
            recurringIndicator = (ViewGroup) v.findViewById(R.id.recurring_indicator);
            recurringIndicatorTextview = (TextView) v.findViewById(R.id.recurring_indicator_textview);
            positiveIndicator = (ImageView) v.findViewById(R.id.positive_indicator);
        }
    }
}