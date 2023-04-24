package com.example.catfisharea.activities.alluser;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityTreatmentBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.ViewPagerAdapter;
import com.example.catfisharea.fragments.alluser.PendingTreatmentFragment;
import com.example.catfisharea.fragments.alluser.TreatmentAcceptedFragment;
import com.example.catfisharea.fragments.alluser.TreatmentRejectedFragment;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.github.jhonnyx2012.horizontalpicker.DatePickerListener;
import com.github.jhonnyx2012.horizontalpicker.HorizontalPicker;
import com.google.android.material.tabs.TabLayout;

import org.joda.time.DateTime;

import java.util.Calendar;

public class TreatmentActivity extends BaseActivity implements DatePickerListener {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private PreferenceManager preferenceManager;
    private ActivityTreatmentBinding mBinding;
    private Calendar myCal;
    private TreatmentAcceptedFragment treatmentAcceptedFragment;
    private PendingTreatmentFragment pendingTreatmentFragment;
    private TreatmentRejectedFragment treatmentRejectedFragment;
    //private CompletedTreatmentFragment completedTreatmentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityTreatmentBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        init();
        setListener();

        adapter.addFragment(pendingTreatmentFragment, "Chờ xử lý");
        adapter.addFragment(treatmentAcceptedFragment, "Chấp nhận");
        adapter.addFragment(treatmentRejectedFragment, "Từ chối");
        //adapter.addFragment(completedTreatmentFragment, "Hoàn thành");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    @SuppressLint("SetTextI18n")
    private void init() {

        //PreferenceManager
        preferenceManager = new PreferenceManager(getApplicationContext());

        //Calendar
        myCal = Calendar.getInstance();

        setDatePicker();

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        mBinding.yearHistory.setText(myCal.get(Calendar.YEAR) + "");
        mBinding.monthHistory.setText("Tháng " + (myCal.get(Calendar.MONTH) + 1));

        preferenceManager.putString(Constants.KEY_DAY_SELECTED, myCal.get(Calendar.DAY_OF_MONTH) + "");
        preferenceManager.putString(Constants.KEY_MONTH_SELECTED, (myCal.get(Calendar.MONTH) + 1) + "");
        preferenceManager.putString(Constants.KEY_YEAR_SELECTED, myCal.get(Calendar.YEAR) + "");

        //Fragment
        treatmentAcceptedFragment = new TreatmentAcceptedFragment();
        pendingTreatmentFragment = new PendingTreatmentFragment();
        treatmentRejectedFragment = new TreatmentRejectedFragment();
        //completedTreatmentFragment = new CompletedTreatmentFragment();
    }

    private void setListener(){
        mBinding.monthHistory.setOnClickListener(v ->
                openDatePicker()
        );
        mBinding.toolbarManageArea.setOnClickListener(view -> onBackPressed());
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

        if (Integer.parseInt(day) < 10){
            day = "0" + day;
        }

        if (Integer.parseInt(month) < 10){
            month = "0" + month;
        }

        preferenceManager.putString(Constants.KEY_DAY_SELECTED, day);
        preferenceManager.putString(Constants.KEY_MONTH_SELECTED, month);
        preferenceManager.putString(Constants.KEY_YEAR_SELECTED, year);

        treatmentAcceptedFragment.getRequest();
        pendingTreatmentFragment.getRequest();
        treatmentRejectedFragment.getRequest();
        //completedTreatmentFragment.getRequest();

    }

}