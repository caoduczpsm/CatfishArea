package com.example.catfisharea.activities.director;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityTreatmentRegimenBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.activities.alluser.ChatActivity;
import com.example.catfisharea.adapter.ReportFishAdapter;
import com.example.catfisharea.listeners.MultipleReportFishListener;
import com.example.catfisharea.models.ReportFish;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.github.jhonnyx2012.horizontalpicker.DatePickerListener;
import com.github.jhonnyx2012.horizontalpicker.HorizontalPicker;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class TreatmentRegimenActivity extends BaseActivity implements DatePickerListener, MultipleReportFishListener {

    ActivityTreatmentRegimenBinding mBinding;
    private Calendar myCal;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private ReportFishAdapter adapter;
    private List<ReportFish> reportFishList;
    String daySelected, monthSelected, yearSelected;
    String finalDateSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityTreatmentRegimenBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        init();
        setListener();
        getReportFish();
    }

    private void init(){
        //Calendar
        myCal = Calendar.getInstance();

        setDatePicker();

        preferenceManager = new PreferenceManager(getApplicationContext());

        database = FirebaseFirestore.getInstance();

        reportFishList = new ArrayList<>();
        adapter = new ReportFishAdapter(reportFishList,this);
        mBinding.treatmentRegimenRecyclerView.setAdapter(adapter);
        finalDateSelected = LocalDate.now().toString();
    }

    private void setListener(){
        mBinding.monthHistory.setOnClickListener(v ->
                openDatePicker()
        );
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

    @SuppressLint("NotifyDataSetChanged")
    private void getReportFish(){

        database.collection(Constants.KEY_COLLECTION_REPORT_FISH)
                .whereEqualTo(Constants.KEY_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID))
                .get()
                .addOnCompleteListener(task -> {
                    reportFishList.clear();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        String dateOfReport = queryDocumentSnapshot.getString(Constants.KEY_REPORT_FISH_DATE);
                        if (Objects.equals(dateOfReport, finalDateSelected)){
                            ReportFish reportFish = new ReportFish();
                            reportFish.guess = queryDocumentSnapshot.getString(Constants.KEY_REPORT_FISH_GUESS);
                            reportFish.image = queryDocumentSnapshot.getString(Constants.KEY_REPORT_FISH_IMAGE);
                            reportFish.date = queryDocumentSnapshot.getString(Constants.KEY_REPORT_FISH_DATE);
                            reportFish.reporterImage = queryDocumentSnapshot.getString(Constants.KEY_REPORTER_IMAGE);
                            reportFish.reporterName = queryDocumentSnapshot.getString(Constants.KEY_REPORTER_NAME);
                            reportFish.reporterPhone = queryDocumentSnapshot.getString(Constants.KEY_REPORTER_PHONE);
                            reportFish.reporterId = queryDocumentSnapshot.getString(Constants.KEY_REPORTER_ID);
                            reportFish.reporterPosition = queryDocumentSnapshot.getString(Constants.KEY_REPORTER_TYPE_ACCOUNT);
                            reportFish.pondId = queryDocumentSnapshot.getString(Constants.KEY_POND_ID);
                            reportFishList.add(reportFish);
                            adapter.notifyDataSetChanged();
                        }

                        if (reportFishList.size() == 0){
                            adapter.notifyDataSetChanged();
                        }
                    }

                });

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onDateSelected(DateTime dateSelected) {
        monthSelected = dateSelected.getMonthOfYear() + "";
        yearSelected = dateSelected.getYear() + "";
        daySelected = dateSelected.getDayOfMonth() + "";
        mBinding.monthHistory.setText("Tháng " + monthSelected);
        mBinding.yearHistory.setText(yearSelected);

        if (Integer.parseInt(daySelected) < 10){
            daySelected = "0" + daySelected;
        }

        if (Integer.parseInt(monthSelected) < 10){
            monthSelected = "0" + monthSelected;
        }

        finalDateSelected = yearSelected + "-" + monthSelected + "-" + daySelected;
        getReportFish();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onReportClicker(ReportFish reportFish) {
        Dialog dialog = openDialog(R.layout.layout_detail_report_fish);
        assert dialog != null;

        Button btnCreate, btnClose;
        ConstraintLayout layoutUserReport;
        CircleImageView imageProfile;
        TextView textName, textPhone, textPosition, textDateReport, textGuess, textNamePond;
        ImageView imageReason;

        btnCreate = dialog.findViewById(R.id.btnCreate);
        btnClose = dialog.findViewById(R.id.btnClose);
        layoutUserReport = dialog.findViewById(R.id.layoutUserReport);
        textName = dialog.findViewById(R.id.textName);
        textPhone = dialog.findViewById(R.id.textPhone);
        textPosition = dialog.findViewById(R.id.textPosition);
        imageProfile = dialog.findViewById(R.id.imageProfile);
        imageReason = dialog.findViewById(R.id.imageReason);
        textDateReport = dialog.findViewById(R.id.textDateReport);
        textGuess = dialog.findViewById(R.id.textGuess);
        textNamePond = dialog.findViewById(R.id.textNamePond);

        textName.setText(reportFish.reporterName);
        textPhone.setText(reportFish.reporterPhone);
        imageProfile.setImageBitmap(getUserImage(reportFish.reporterImage));

        textGuess.setText("Phỏng đoán: " + reportFish.guess);
        imageReason.setImageBitmap(getUserImage(reportFish.image));
        database.collection(Constants.KEY_COLLECTION_POND)
                .document(reportFish.pondId)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    textNamePond.setText(documentSnapshot.getString(Constants.KEY_NAME));
                });
        textDateReport.setText(reportFish.date.substring(8, 10) + "/" + reportFish.date.substring(5, 7) +
                "/" + reportFish.date.substring(0, 4));

        if (reportFish.reporterPosition.equals(Constants.KEY_DIRECTOR)){
            textPosition.setText("Trưởng khu");
        } else if (reportFish.reporterPosition.equals(Constants.KEY_WORKER)){
            textPosition.setText("Công nhân");
        }


        layoutUserReport.setOnClickListener(view ->
                database.collection(Constants.KEY_COLLECTION_USER)
                .document(reportFish.reporterId)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    User user = new User();
                    user.name = documentSnapshot.getString(Constants.KEY_NAME);
                    user.phone = documentSnapshot.getString(Constants.KEY_PHONE);
                    user.position = documentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                    user.image = documentSnapshot.getString(Constants.KEY_IMAGE);
                    user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                    user.id = documentSnapshot.getId();
                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    intent.putExtra(Constants.KEY_USER, user);
                    startActivity(intent);
                    finish();
                }));

        btnCreate.setOnClickListener(view -> {});

        btnClose.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = new byte[0];
        if (encodedImage != null){
            bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        }
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }

    private Dialog openDialog(int layout) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layout);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);

        return dialog;
    }

}
