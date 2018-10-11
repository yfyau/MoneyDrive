package com.jasonyau.moneydrive.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import com.jasonyau.moneydrive.customClass.Account;
import com.jasonyau.moneydrive.helper.FirebaseHelper;
import com.jasonyau.moneydrive.helper.ParameterKeys;
import com.jasonyau.moneydrive.helper.Parameters;

import java.util.Iterator;
import java.util.List;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class AccountRecyclerViewAdapter extends RecyclerView.Adapter<AccountRecyclerViewAdapter.ViewHolder>
{
    private List<Account> accounts;
    private Activity activity;

// ------------------------------------------->

    /**
     * Instanciate an adapter for the given date
     *
     */
    public AccountRecyclerViewAdapter(@NonNull Activity activity, @NonNull List<Account> accounts)
    {
        this.activity = activity;
        this.accounts = accounts;
    }

    public void setAccounts(@NonNull List<Account> accounts)
    {
        this.accounts = accounts;
        notifyDataSetChanged();
    }

    public int removeAccount(Account account)
    {
        Iterator<Account> accountIterator = accounts.iterator();
        int position = 0;
        while( accountIterator.hasNext() )
        {
            Account shownAccount = accountIterator.next();
            if( shownAccount.equals(account) )
            {
                accountIterator.remove();
                notifyItemRemoved(position);
                return position;
            }

            position++;
        }

        return -1;
    }

    public void addAccount(Account account, int position)
    {
        accounts.add(position, account);
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
        final Account account = accounts.get(i);

        viewHolder.expenseTitleTextView.setText(account.getAcName());

        Log.d("Account", "AcName = " + account.getAcName());

        final View.OnClickListener onClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.dialog_edit_account_title);
            builder.setItems(R.array.dialog_edit_account_choices, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which)
                    {
                        case 0: // Edit
                        {
                            final EditText etAcName = new EditText(activity);
                            etAcName.setMaxLines(1);

                            new AlertDialog.Builder(activity).setTitle("Rename the Account: ")
                                    .setView(etAcName)
                                    .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            String inputAcName = etAcName.getText().toString();
                                            if (inputAcName.equals("")) {
                                                Toast.makeText(activity, "Account name cannot be empty ! " + inputAcName, Toast.LENGTH_LONG).show();
                                            }
                                            else {
                                                if (!accounts.contains(inputAcName)){
                                                    FirebaseHelper.editAccount(accounts.get(i).getAcId(), Parameters.getInstance(activity).getString(ParameterKeys.USER_ID), inputAcName, "Saving Account", "HKD");
                                                }
                                                else
                                                    Toast.makeText(activity, "Account name cannot be duplicated ! " + inputAcName, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();

                            break;
                        }
                        case 1: // Delete
                        {
                            FirebaseHelper.delAccount(accounts.get(i).getAcId());
                            accounts.remove(i);

                            break;
                        }
                        case 2: // Share
                        {
                            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Share AcId", accounts.get(i).getAcId());
                            clipboard.setPrimaryClip(clip);

                            Toast.makeText(activity, "Share AcId: " + accounts.get(i).getAcName() + " Copied !", Toast.LENGTH_LONG).show();

                            break;
                        }
                    }

                    setAccounts(accounts);
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
        return accounts.size();
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