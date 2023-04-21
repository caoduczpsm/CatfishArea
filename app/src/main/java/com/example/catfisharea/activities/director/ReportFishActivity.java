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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityTreatmentRegimenBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.activities.alluser.ChatActivity;
import com.example.catfisharea.adapter.MedicineAdapter;
import com.example.catfisharea.adapter.MedicineAutoCompleteAdapter;
import com.example.catfisharea.adapter.ReportFishAdapter;
import com.example.catfisharea.listeners.MultipleReportFishListener;
import com.example.catfisharea.models.Medicine;
import com.example.catfisharea.models.ReportFish;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.github.jhonnyx2012.horizontalpicker.DatePickerListener;
import com.github.jhonnyx2012.horizontalpicker.HorizontalPicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReportFishActivity extends BaseActivity implements DatePickerListener, MultipleReportFishListener {

    ActivityTreatmentRegimenBinding mBinding;
    private Calendar myCal;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private ReportFishAdapter adapter;
    private List<ReportFish> reportFishList;
    String daySelected, monthSelected, yearSelected;
    String finalDateSelected;
    private ReportFish finalReportFish;

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
        adapter = new ReportFishAdapter(reportFishList,this, this);
        mBinding.treatmentRegimenRecyclerView.setAdapter(adapter);
        finalDateSelected = LocalDate.now().toString();
    }

    private void setListener(){
        mBinding.monthHistory.setOnClickListener(v ->
                openDatePicker()
        );
        mBinding.toolbarReqest.setOnClickListener(view -> onBackPressed());
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
                            reportFish.id = queryDocumentSnapshot.getId();
                            reportFish.guess = queryDocumentSnapshot.getString(Constants.KEY_REPORT_FISH_GUESS);
                            reportFish.image = queryDocumentSnapshot.getString(Constants.KEY_REPORT_FISH_IMAGE);
                            reportFish.date = queryDocumentSnapshot.getString(Constants.KEY_REPORT_FISH_DATE);
                            reportFish.reporterImage = queryDocumentSnapshot.getString(Constants.KEY_REPORTER_IMAGE);
                            reportFish.reporterName = queryDocumentSnapshot.getString(Constants.KEY_REPORTER_NAME);
                            reportFish.reporterPhone = queryDocumentSnapshot.getString(Constants.KEY_REPORTER_PHONE);
                            reportFish.reporterId = queryDocumentSnapshot.getString(Constants.KEY_REPORTER_ID);
                            reportFish.reporterPosition = queryDocumentSnapshot.getString(Constants.KEY_REPORTER_TYPE_ACCOUNT);
                            reportFish.pondId = queryDocumentSnapshot.getString(Constants.KEY_POND_ID);
                            reportFish.status = queryDocumentSnapshot.getString(Constants.KEY_REPORT_STATUS);
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

        finalReportFish = new ReportFish(reportFish);

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

        btnCreate.setOnClickListener(view -> {
            if (finalReportFish.status.equals(Constants.KEY_REPORT_PENDING)) {
                openTreatmentProtocolDialog();
                dialog.dismiss();
            } else {
                showToast("Bạn đã tạo phát đồ điều trị cho báo cáo này!");
            }
        });

        btnClose.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void openTreatmentProtocolDialog(){
        final Dialog dialog = openDialog(R.layout.layout_create_treatment_protocol_dialog);
        assert dialog != null;

        AutoCompleteTextView nameItem = dialog.findViewById(R.id.nameItem);
        Button btnCreate, btnClose;
        TextView textDateReport, textNamePond;
        TextInputEditText edtNote;
        CheckBox cbWater, cbFood, cbMud;
        MultiAutoCompleteTextView edtMedicine;
        RecyclerView medicineRecyclerView;
        ConstraintLayout layoutQuantity, layoutDropdown;
        ImageView imageDropdown;

        btnClose = dialog.findViewById(R.id.btnClose);
        btnCreate = dialog.findViewById(R.id.btnCreate);
        textDateReport = dialog.findViewById(R.id.textDateReport);
        textNamePond = dialog.findViewById(R.id.textNamePond);
        edtMedicine = dialog.findViewById(R.id.edtMedicine);
        edtNote = dialog.findViewById(R.id.edtNote);
        cbWater = dialog.findViewById(R.id.cbWater);
        cbFood = dialog.findViewById(R.id.cbFood);
        cbMud = dialog.findViewById(R.id.cbMud);
        medicineRecyclerView = dialog.findViewById(R.id.medicineRecyclerView);
        layoutQuantity = dialog.findViewById(R.id.layoutQuantityMedicine);
        layoutDropdown = dialog.findViewById(R.id.layoutDropdown);
        imageDropdown = dialog.findViewById(R.id.imageDropdown);

        database.collection(Constants.KEY_COLLECTION_POND)
                .document(finalReportFish.pondId)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    textNamePond.setText(documentSnapshot.getString(Constants.KEY_NAME));
                });

        textDateReport.setText(finalReportFish.date.substring(8, 10) + "/" + finalReportFish.date.substring(5, 7) +
                "/" + finalReportFish.date.substring(0, 4));

        ArrayList<String> arrayItem = new ArrayList<>();
        arrayItem.add("Xuất huyết, phù đầu");
        arrayItem.add("Gan thận mũ");
        arrayItem.add("Trắng gan, trắng man");
        arrayItem.add("Vàng da");
        arrayItem.add("Bống hơi");
        arrayItem.add("Nội ngoại kí sinh trùng");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, arrayItem);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        nameItem.setAdapter(adapter);
        nameItem.showDropDown();

        ArrayList<Medicine> medicineItemList = new ArrayList<>();

        List<Medicine> medicinesSelected = new ArrayList<>();
        MedicineAdapter medicineAdapter = new MedicineAdapter(this, medicinesSelected);
        medicineRecyclerView.setAdapter(medicineAdapter);

        database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                .whereEqualTo(Constants.KEY_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID))
                .get()
                .addOnCompleteListener(task -> {
                   for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                       database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                               .document(queryDocumentSnapshot.getId())
                               .collection(Constants.KEY_COLLECTION_CATEGORY)
                               .whereEqualTo(Constants.KEY_CATEGORY_TYPE, Constants.KEY_CATEGORY_TYPE_MEDICINE)
                               .get()
                               .addOnCompleteListener(task1 -> {
                                   for (QueryDocumentSnapshot queryDocumentSnapshot1 : task1.getResult()){
                                       Medicine medicine = new Medicine();
                                       medicine.id = queryDocumentSnapshot1.getId();
                                       medicine.amount = queryDocumentSnapshot1.getString(Constants.KEY_AMOUNT_OF_ROOM);
                                       medicine.effect = queryDocumentSnapshot1.getString(Constants.KEY_EFFECT);
                                       medicine.producer = queryDocumentSnapshot1.getString(Constants.KEY_PRODUCER);
                                       medicine.type = queryDocumentSnapshot1.getString(Constants.KEY_CATEGORY_TYPE);
                                       medicine.name = queryDocumentSnapshot1.getString(Constants.KEY_NAME);
                                       medicine.unit = queryDocumentSnapshot1.getString(Constants.KEY_UNIT);
                                       medicineItemList.add(medicine);
                                       medicineAdapter.notifyDataSetChanged();
                                   }
                               });
                   }
                });

        edtMedicine.setOnItemClickListener((adapterView, view, i, l) -> {
            if (!medicinesSelected.contains(medicineItemList.get(i))){
                medicinesSelected.add(medicineItemList.get(i));
            }
            medicineAdapter.notifyDataSetChanged();
            layoutDropdown.setVisibility(View.VISIBLE);
        });
        
        layoutDropdown.setOnClickListener(view -> {
            if (medicineRecyclerView.getVisibility() == View.GONE){
                medicineRecyclerView.setVisibility(View.VISIBLE);
                layoutQuantity.setVisibility(View.VISIBLE);
                imageDropdown.setImageResource(R.drawable.ic_down);
            } else {
                medicineRecyclerView.setVisibility(View.GONE);
                layoutQuantity.setVisibility(View.GONE);
                imageDropdown.setImageResource(R.drawable.ic_up);
            }
        });

        MedicineAutoCompleteAdapter medicineAutoCompleteAdapter = new MedicineAutoCompleteAdapter(this,
                R.layout.item_container_medicine_autocomplete, medicineItemList);
        edtMedicine.setAdapter(medicineAutoCompleteAdapter);
        edtMedicine.showDropDown();
        edtMedicine.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        btnCreate.setOnClickListener(view -> {

            if (nameItem.getText().toString().equals("") ||
                    edtMedicine.getText().toString().equals("")) {
                showToast("Vui lòng nhập đầy đủ thông tin!");
            } else {
                boolean isValidationData = true;
                for (int i=0;i<medicineAdapter.getItemCount();i++) {
                    MedicineAdapter.MedicineViewHolder viewHolder= (MedicineAdapter.MedicineViewHolder) medicineRecyclerView.findViewHolderForAdapterPosition(i);
                    assert viewHolder != null;
                    EditText edtQuantity = viewHolder.itemView.findViewById(R.id.edtQuantity);
                    if (edtQuantity.getText().toString().equals("")){
                        isValidationData = false;
                        break;
                    }
                }

                if (!isValidationData){
                    showToast("Vui lòng nhập số lượng thuốc cần dùng!");
                } else {
                    HashMap<String, Object> treatment = new HashMap<>();
                    treatment.put(Constants.KEY_TREATMENT_DATE, LocalDate.now().toString());
                    treatment.put(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID));
                    treatment.put(Constants.KEY_TREATMENT_POND_ID, finalReportFish.pondId);
                    treatment.put(Constants.KEY_TREATMENT_SICK_NAME, nameItem.getText().toString());
                    treatment.put(Constants.KEY_TREATMENT_CREATOR_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                    treatment.put(Constants.KEY_TREATMENT_CREATOR_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                    treatment.put(Constants.KEY_CREATOR_NAME, preferenceManager.getString(Constants.KEY_NAME));
                    treatment.put(Constants.KEY_CREATOR_PHONE, preferenceManager.getString(Constants.KEY_PHONE));
                    treatment.put(Constants.KEY_TREATMENT_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID));
                    treatment.put(Constants.KEY_TREATMENT_NOTE, Objects.requireNonNull(edtNote.getText()).toString());
                    treatment.put(Constants.KEY_TREATMENT_STATUS, Constants.KEY_TREATMENT_PENDING);

                    HashMap<String, Object> medicineUsed = new HashMap<>();
                    for (int i=0;i<medicineAdapter.getItemCount();i++) {
                        MedicineAdapter.MedicineViewHolder viewHolder= (MedicineAdapter.MedicineViewHolder) medicineRecyclerView.findViewHolderForAdapterPosition(i);
                        assert viewHolder != null;
                        EditText edtQuantity = viewHolder.itemView.findViewById(R.id.edtQuantity);
                        medicineUsed.put(medicinesSelected.get(i).id, edtQuantity.getText().toString());
                    }
                    treatment.put(Constants.KEY_TREATMENT_MEDICINE, medicineUsed);

                    if (cbFood.isChecked()){
                        treatment.put(Constants.KEY_TREATMENT_NO_FOOD, Constants.KEY_TREATMENT_NO_FOOD);
                    }

                    if (cbMud.isChecked()){
                        treatment.put(Constants.KEY_TREATMENT_SUCK_MUD, Constants.KEY_TREATMENT_SUCK_MUD);
                    }

                    if (cbWater.isChecked()){
                        treatment.put(Constants.KEY_TREATMENT_REPLACE_WATER, Constants.KEY_TREATMENT_REPLACE_WATER);
                    }

                    database.collection(Constants.KEY_COLLECTION_TREATMENT)
                            .add(treatment)
                            .addOnSuccessListener(runnable -> {
                                showToast("Tạo phát đồ thành công");
                                dialog.dismiss();
                                HashMap<String, Object> updateReport = new HashMap<>();
                                updateReport.put(Constants.KEY_REPORT_STATUS, Constants.KEY_REPORT_COMPLETED);
                                database.collection(Constants.KEY_COLLECTION_REPORT_FISH)
                                        .document(finalReportFish.id)
                                        .update(updateReport);
                                getReportFish();
                                finalReportFish.status = Constants.KEY_REPORT_COMPLETED;
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(runnable -> dialog.dismiss());
                }

            }


        });

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

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
