package com.example.catfisharea.activities.director;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityTaskManagerBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.ViewPagerAdapter;
import com.example.catfisharea.fragments.director.CompletedTaskFragment;
import com.example.catfisharea.fragments.director.FixedTaskFragment;
import com.example.catfisharea.fragments.director.OverviewTaskFragment;
import com.example.catfisharea.fragments.director.UnfinishedTaskFragment;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.github.jhonnyx2012.horizontalpicker.DatePickerListener;
import com.github.jhonnyx2012.horizontalpicker.HorizontalPicker;
import com.google.android.material.tabs.TabLayout;

import org.joda.time.DateTime;

import java.util.Calendar;

public class TaskManagerActivity extends BaseActivity implements DatePickerListener {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private PreferenceManager preferenceManager;
    private ActivityTaskManagerBinding mBinding;
    private Calendar myCal;
    private CompletedTaskFragment completedTaskFragment;
    private UnfinishedTaskFragment unfinishedTaskFragment;
    private OverviewTaskFragment overviewTaskFragment;
    private FixedTaskFragment fixedTaskFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityTaskManagerBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        init();
        Animatoo.animateSlideLeft(TaskManagerActivity.this);
        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)){
            adapter.addFragment(overviewTaskFragment, "Tổng Quan");
        } else {
            adapter.addFragment(overviewTaskFragment, "Tổng Quan Nhiệm Vụ");
        }
        adapter.addFragment(completedTaskFragment, "Đã Hoàn Thành");
        adapter.addFragment(unfinishedTaskFragment, "Chưa Hoàn Thành");
        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR) ||
            preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_WORKER)){
            adapter.addFragment(fixedTaskFragment, "Nhiệm Vụ Cố định");
        }
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    @SuppressLint("SetTextI18n")
    private void init() {

        //PreferenceManager
        preferenceManager = new PreferenceManager(getApplicationContext());

        preferenceManager.putString(Constants.KEY_DIRECTOR, Constants.KEY_MY_DIRECTOR_TASK);
        mBinding.radioMyTask.setChecked(true);

        //Calendar
        myCal = Calendar.getInstance();

        setDatePicker();

        //Fragment
        completedTaskFragment = new CompletedTaskFragment();
        unfinishedTaskFragment = new UnfinishedTaskFragment();
        overviewTaskFragment = new OverviewTaskFragment();
        fixedTaskFragment = new FixedTaskFragment();

        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR))
            mBinding.layoutRadioButton.setVisibility(View.VISIBLE);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        mBinding.yearHistory.setText(myCal.get(Calendar.YEAR) + "");
        mBinding.monthHistory.setText("Tháng " + (myCal.get(Calendar.MONTH) + 1));

        preferenceManager.putString(Constants.KEY_DAY_SELECTED, myCal.get(Calendar.DAY_OF_MONTH) + "");
        preferenceManager.putString(Constants.KEY_MONTH_SELECTED, (myCal.get(Calendar.MONTH) + 1) + "");
        preferenceManager.putString(Constants.KEY_YEAR_SELECTED, myCal.get(Calendar.YEAR) + "");
    }

    @Override
    protected void onStart() {
        super.onStart();
        setListener();
    }

    private void setListener() {
        mBinding.toolbarManageArea.setOnClickListener(view -> onBackPressed());
        mBinding.monthHistory.setOnClickListener(v ->
                openDatePicker()
        );

        mBinding.radioMyTask.setOnClickListener(view -> {
            preferenceManager.putString(Constants.KEY_DIRECTOR, Constants.KEY_MY_DIRECTOR_TASK);
            overviewTaskFragment.createPieChart();
            completedTaskFragment.getTasks();
            unfinishedTaskFragment.getTasks();
        });

        mBinding.radioAllocationTask.setOnClickListener(view -> {
            preferenceManager.putString(Constants.KEY_DIRECTOR, Constants.KEY_DIRECTOR_ALLOCATION_TASK);
            overviewTaskFragment.createPieChart();
            completedTaskFragment.getTasks();
            unfinishedTaskFragment.getTasks();
        });
    }

    @SuppressLint("NewApi")
    private void setDatePicker() {
        HorizontalPicker picker = mBinding.datePicker;

        picker
                .setDays(120)
                .setOffset(60)
                .setListener(this)
                .setDateSelectedColor(Color.parseColor("#eea39d"))
                .setDateSelectedTextColor(Color.WHITE)
                .setMonthAndYearTextColor(Color.TRANSPARENT)
                .setTodayDateBackgroundColor(Color.GRAY)
                .setUnselectedDayTextColor(Color.DKGRAY)
                .setDayOfWeekTextColor(Color.DKGRAY)
                .setTodayButtonTextColor(Color.BLACK)
                .showTodayButton(true)
                .init();
        // or on the View directly after init was completed
        picker.setBackgroundColor(getColor(R.color.white));
        picker.setDate(new DateTime());
    }

    private void openDatePicker() {
//        Calendar myCalendar = Calendar.getInstance();

        @SuppressLint("SetTextI18n") DatePickerDialog.OnDateSetListener dateListener = (view, year, month, day) -> {
            myCal.set(Calendar.YEAR, year);
            myCal.set(Calendar.MONTH, month);
            myCal.set(Calendar.DAY_OF_MONTH, day);
            DateTime date = new DateTime(year, (month + 1) , day, myCal.get(Calendar.HOUR_OF_DAY), myCal.get(Calendar.MINUTE));
            mBinding.datePicker.setDate(date);
        };

        DatePickerDialog dialog = new DatePickerDialog(
                this, dateListener,
                myCal.get(Calendar.YEAR),
                myCal.get(Calendar.MONTH),
                myCal.get(Calendar.DAY_OF_MONTH)
        );
        //String[] weekdays = new DateFormatSymbols().getWeekdays();
        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onDateSelected(@NonNull final DateTime dateSelected) {
        // log it for demo
        String month = dateSelected.getMonthOfYear() + "";
        String year = dateSelected.getYear() + "";
        String day = dateSelected.getDayOfMonth() + "";
        mBinding.monthHistory.setText("Tháng " + month);
        mBinding.yearHistory.setText(year);

        preferenceManager.putString(Constants.KEY_DAY_SELECTED, day);
        preferenceManager.putString(Constants.KEY_MONTH_SELECTED, month);
        preferenceManager.putString(Constants.KEY_YEAR_SELECTED, year);

        if (mBinding.radioMyTask.isChecked()){
            preferenceManager.putString(Constants.KEY_DIRECTOR, Constants.KEY_MY_DIRECTOR_TASK);
        } else {
            preferenceManager.putString(Constants.KEY_DIRECTOR, Constants.KEY_DIRECTOR_ALLOCATION_TASK);
        }
        completedTaskFragment.getTasks();
        unfinishedTaskFragment.getTasks();
        overviewTaskFragment.createPieChart();
    }

}