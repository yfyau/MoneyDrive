package com.jasonyau.moneydrive.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jasonyau.moneydrive.R;
import com.jasonyau.moneydrive.helper.ParameterKeys;
import com.jasonyau.moneydrive.helper.Parameters;

import java.util.Iterator;
import java.util.List;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class FieldRecyclerViewAdapter extends RecyclerView.Adapter<FieldRecyclerViewAdapter.ViewHolder>
{
    private List<String> fields;
    private Activity activity;

// ------------------------------------------->

    /**
     * Instanciate an adapter for the given date
     *
     */
    public FieldRecyclerViewAdapter(@NonNull Activity activity,  @NonNull List<String> fields)
    {
        this.activity = activity;
        this.fields = fields;
    }

    public void setField(@NonNull List<String> fields)
    {
        this.fields = fields;
        notifyDataSetChanged();
    }

    public int removeField(String field)
    {
        Iterator<String> fieldIterator = fields.iterator();
        int position = 0;
        while( fieldIterator.hasNext() )
        {
            String shownField = fieldIterator.next();
            if( shownField.equals(field) )
            {
                fieldIterator.remove();
                notifyItemRemoved(position);
                return position;
            }

            position++;
        }

        return -1;
    }

    public void addField(String field, int position)
    {
        fields.add(position, field);
        notifyItemRangeInserted(position, 1);
    }

// ------------------------------------------>

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycleview_field_cell, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i)
    {
        final String field = fields.get(i);

        viewHolder.expenseTitleTextView.setText(field);

        final View.OnClickListener onClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.dialog_edit_field_title);
            builder.setItems(R.array.dialog_edit_field_choices, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which)
                    {
                        case 0: // Edit
                        {
                            final EditText et = new EditText(activity);
                            et.setMaxLines(1);

                            new AlertDialog.Builder(activity).setTitle("Name a new record field: ")
                                    .setView(et)
                                    .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            String input = et.getText().toString();
                                            if (input.equals("")) {
                                                Toast.makeText(activity, "Field name cannot be empty ! " + input, Toast.LENGTH_LONG).show();
                                            }
                                            else {
                                                if (!fields.contains(input)){
                                                    fields.remove(i);
                                                    fields.add(input);
                                                    Parameters.getInstance(activity).saveFields(ParameterKeys.FIELDS, fields);
                                                }
                                                else
                                                    Toast.makeText(activity, "Field name cannot be duplicated ! " + input, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();

                            break;
                        }
                        case 1: // Delete
                        {
                            fields.remove(i);
                            Parameters.getInstance(activity).saveFields(ParameterKeys.FIELDS, fields);

                            Log.d("Field", "Del Fields = " + fields + " // i = " + i);

                            setField(fields);

                            break;
                        }
                    }
                }
            });
            builder.show();

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
        return fields.size();
    }

// ------------------------------------------->

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public final TextView expenseTitleTextView;
        public final ImageView positiveIndicator;
        public final View view;

        public ViewHolder(View v)
        {
            super(v);

            view = v;
            expenseTitleTextView = (TextView) v.findViewById(R.id.expense_title);
            positiveIndicator = (ImageView) v.findViewById(R.id.positive_indicator);
        }
    }
}