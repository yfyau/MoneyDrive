package com.jasonyau.moneydrive.activities;


import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.jasonyau.moneydrive.R;
import com.jasonyau.moneydrive.customClass.Record;
import com.jasonyau.moneydrive.helper.CurrencyHelper;
import com.jasonyau.moneydrive.helper.FirebaseHelper;
import com.jasonyau.moneydrive.interfaces.IFirebaseListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class SummaryFragment extends Fragment {
    private TextView tv_total;

    private TextView tvPcFieldsEmpty;
    private TextView tvBcFieldsEmpty;

    private PieChart pcFields;
    private BarChart bcFields;

    private List<Record> records;

    private Button btn_date_from;
    private Button btn_date_to;

    private Date date_from;
    private Date date_to;

    private Double total;
    private Double totalExpense;
    private List<String> accountIds;
    private List<String> fieldNames;
    private List<Double> fieldTotalExpenses;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_summary, container, false);

        accountIds = new ArrayList<>();
        if (getArguments() != null)
            accountIds = getArguments().getStringArrayList("accountIds");

        fieldNames = new ArrayList<>();
        fieldTotalExpenses = new ArrayList<>();

        tv_total = (TextView)rootView.findViewById(R.id.tv_total);
        pcFields = (PieChart)rootView.findViewById(R.id.pc_categories);
        bcFields = (BarChart)rootView.findViewById(R.id.bc_categories);
        tvPcFieldsEmpty = (TextView)rootView.findViewById(R.id.tv_bar_chart_field_empty);
        tvBcFieldsEmpty = (TextView)rootView.findViewById(R.id.tv_pie_chart_field_empty);
        btn_date_from = (Button)rootView.findViewById(R.id.btn_date_from);
        btn_date_to = (Button)rootView.findViewById(R.id.btn_date_to);

        date_from = new Date();
        date_to = new Date();

        btn_date_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog(btn_date_from.getId());
                updateDateButtonDisplay(btn_date_from.getId());
            }
        });
        updateDateButtonDisplay(btn_date_from.getId());

        btn_date_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog(btn_date_to.getId());
                updateDateButtonDisplay(btn_date_to.getId());
            }
        });
        updateDateButtonDisplay(btn_date_to.getId());

        updateData();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        records = new ArrayList<>();
        setupCharts();
    }

    private void showDateDialog(final int id) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(id == R.id.btn_date_from ? date_from : date_to);

        DatePickerDialogFragment fragment = new DatePickerDialogFragment(calendar.getTime(), new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
            {
                Calendar cal = Calendar.getInstance();

                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, monthOfYear);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                if (id == R.id.btn_date_from)  date_from = cal.getTime();
                if (id == R.id.btn_date_to)  date_to = cal.getTime();
                updateDateButtonDisplay(id);
                updateData();
            }
        });

        fragment.show(getFragmentManager(), "datePicker");

//        DialogManager.getInstance()
//                .showDatePicker(
//                        getContext(),
//                        new DatePickerDialog.OnDateSetListener() {
//                            @Override
//                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
//                                calendar.set(year, month, day);
//                                DateUtils.setDateStartOfDay(calendar);
//                                if (id == R.id.btn_date_from) {
//                                    DateManager.getInstance().setDateFrom(calendar.getTime());
//                                    updateDate(btnDateFrom, DateManager.getInstance().getDateFrom());
//                                } else {
//                                    DateManager.getInstance().setDateTo(calendar.getTime());
//                                    updateDate(btnDateTo, DateManager.getInstance().getDateTo());
//                                }
//                                iSelectDateFragment.updateData();
//                            }
//                        },
//                        calendar,
//                        (R.id.btn_date_from == id) ? null : DateManager.getInstance().getDateFrom(),
//                        (R.id.btn_date_from == id) ? DateManager.getInstance().getDateTo() : null);
    }

    private void updateDateButtonDisplay(int id)
    {
        SimpleDateFormat formatter = new SimpleDateFormat(getResources().getString(R.string.edit_expense_date_format), Locale.getDefault());
        if (id == R.id.btn_date_from)  btn_date_from.setText(formatter.format(date_from));
        if (id == R.id.btn_date_to)  btn_date_to.setText(formatter.format(date_to));
    }

    private void setupCharts() {

        // set up pie chart
        pcFields.setCenterText("");
        pcFields.setCenterTextSize(10f);
        pcFields.setHoleRadius(50f);
        pcFields.setTransparentCircleRadius(55f);
        pcFields.setUsePercentValues(true);
        pcFields.setDescription("");
        pcFields.setNoDataText("");

        Legend l = pcFields.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_RIGHT);
        pcFields.animateY(1500, Easing.EasingOption.EaseInOutQuad);

    }

    public void updateData() {
        // Bar Chart
        bcFields.setDescription("");
        bcFields.setNoDataText("");
        bcFields.animateY(2000);
        bcFields.setVisibleXRangeMaximum(5);
        bcFields.getAxisLeft().setDrawGridLines(false);
        bcFields.getXAxis().setDrawGridLines(false);
        bcFields.getAxisRight().setDrawGridLines(false);
        bcFields.getAxisRight().setDrawLabels(false);

        // Restarting chart views
        bcFields.notifyDataSetChanged();
        bcFields.invalidate();
        pcFields.notifyDataSetChanged();
        pcFields.invalidate();

        FirebaseHelper.getRecordByDateRange(date_from, date_to, new IFirebaseListener() {
            @Override
            public void onCallback(String value) {

            }

            @Override
            public void onCallback(Double value) {

            }

            @Override
            public void onCallback(List value) {
                List<Record> rValue = value;
                Log.d("Summary", "rValue = " + rValue);

                total = 0.0;
                totalExpense = 0.0;
                fieldNames.clear();
                fieldTotalExpenses.clear();

                for (Record record : rValue){
                    if (accountIds.contains(record.getAcId())) {
                        total += record.getAmount();

                        if (record.getAmount() < 0) {
                            if (!fieldNames.contains(record.getFieldName()))
                                fieldNames.add(record.getFieldName());

                            int index = fieldNames.indexOf(record.getFieldName());
                            if (index < fieldTotalExpenses.size())
                                fieldTotalExpenses.set(index, fieldTotalExpenses.get(index) + (-record.getAmount()));
                            else
                                fieldTotalExpenses.add(index, -record.getAmount());

                            totalExpense += (-record.getAmount());
                        }

                        Log.d("Summary", "fieldNames = " + fieldNames);
                        Log.d("Summary", "fieldTotalExpenses = " + fieldTotalExpenses);
                    }
                }
                tv_total.setText(CurrencyHelper.getFormattedCurrencyString(getContext(), totalExpense));
                setFieldsBarChart();
                setFieldsPieChart();
            }
        });

        setFieldsBarChart();
        setFieldsPieChart();
    }

    private void setFieldsBarChart() {
        List<BarEntry> entryPerCategory = new ArrayList<>();

        for (int i = 0; i < fieldTotalExpenses.size(); i++){
            entryPerCategory.add(new BarEntry(fieldTotalExpenses.get(i).floatValue(), i));
        }

        if (fieldTotalExpenses.isEmpty()) {
            tvBcFieldsEmpty.setVisibility(View.VISIBLE);
            bcFields.setVisibility(View.GONE);
        } else {
            tvBcFieldsEmpty.setVisibility(View.GONE);
            bcFields.setVisibility(View.VISIBLE);
        }
        BarDataSet dataSet = new BarDataSet(entryPerCategory, "Fields");
        dataSet.setColors(getListColors());
        BarData barData = new BarData(fieldNames, dataSet);
        bcFields.setData(barData);
        bcFields.invalidate();
    }

    private void setFieldsPieChart() {
        List<Entry> fieldPercentagesEntries = new ArrayList<>();

        for (int i = 0; i < fieldTotalExpenses.size(); i++){
            float  percentage = (float) (fieldTotalExpenses.get(i).floatValue()/totalExpense);
            Entry pieEntry = new Entry(percentage, i);
            fieldPercentagesEntries.add(pieEntry);
        }

        if (fieldTotalExpenses.isEmpty()) {
            tvPcFieldsEmpty.setVisibility(View.VISIBLE);
            bcFields.setVisibility(View.GONE);
        } else {
            tvPcFieldsEmpty.setVisibility(View.GONE);
            bcFields.setVisibility(View.VISIBLE);
        }

        PieDataSet dataSet = new PieDataSet(fieldPercentagesEntries, "Categories");
        dataSet.setSliceSpace(1f);
        dataSet.setSelectionShift(5f);

        dataSet.setColors(getListColors());

        PieData data = new PieData(fieldNames, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(getResources().getColor(R.color.primary_dark));
        pcFields.setData(data);
        pcFields.invalidate();

    }

    public static List<Integer> getListColors() {
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());
        return colors;
    }

}
